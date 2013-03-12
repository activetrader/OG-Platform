/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

/**
 * Handles callback notifications of terminal values to populate a graph set.
 */
/* package */class GetTerminalValuesCallback implements ResolvedValueCallback {

  private static final Logger s_logger = LoggerFactory.getLogger(GetTerminalValuesCallback.class);

  /**
   * Buffer of resolved value specifications. For any entries in here, all input values have been previously resolved and are in this buffer or the partially constructed graph. Information here gets
   * used to construct dependency graph fragments whenever a terminal item can be resolved.
   */
  private final ConcurrentMap<ValueSpecification, ResolvedValue> _resolvedBuffer = new ConcurrentHashMap<ValueSpecification, ResolvedValue>();

  /**
   * Index into the dependency graph nodes, keyed by their output specifications.
   */
  private final Map<ValueSpecification, DependencyNode> _spec2Node = new ConcurrentHashMap<ValueSpecification, DependencyNode>();

  /**
   * Index into the dependency graph nodes, keyed by their targets and functions.
   */
  private final Map<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNode>>> _func2target2nodes =
      new HashMap<ParameterizedFunction, Map<ComputationTargetSpecification, Set<DependencyNode>>>();

  /**
   * All dependency graph nodes.
   */
  private final Set<DependencyNode> _graphNodes = new HashSet<DependencyNode>();

  /**
   * All leaf graph nodes.
   */
  private final Set<DependencyNode> _leafNodes = new HashSet<DependencyNode>();

  /**
   * Terminal value resolutions, mapping the resolved value specifications back to the originally requested requested value requirements.
   */
  private final Map<ValueSpecification, Collection<ValueRequirement>> _resolvedValues = new HashMap<ValueSpecification, Collection<ValueRequirement>>();

  /**
   * Queue of completed resolutions that have not been processed into the partial graph. Graph construction is single threaded, with this queue holding work items if other thread produce results while
   * one is busy building the graph.
   */
  private final BlockingQueue<Pair<ValueRequirement, ResolvedValue>> _resolvedQueue = new LinkedBlockingQueue<Pair<ValueRequirement, ResolvedValue>>();

  /**
   * Mutex for working on the resolved queue. Any thread can add to the queue, only the one that has claimed this mutex can process the elements from it and be sure of exclusive access to the other
   * data structures.
   */
  private final AtomicReference<Thread> _singleton = new AtomicReference<Thread>();

  /**
   * Optional visitor to process failures from the graph build. This can be specified to provide additional error reporting to the user.
   */
  private ResolutionFailureVisitor<?> _failureVisitor;

  /**
   * Optional logic to collapse nodes on mutually compatible targets into a single node.
   */
  private ComputationTargetCollapser _computationTargetCollapser;

  public GetTerminalValuesCallback(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  public void setResolutionFailureVisitor(final ResolutionFailureVisitor<?> failureVisitor) {
    _failureVisitor = failureVisitor;
  }

  public void setComputationTargetCollapser(final ComputationTargetCollapser collapser) {
    _computationTargetCollapser = collapser;
  }

  public ResolvedValue getProduction(final ValueSpecification specification) {
    final ResolvedValue value = _resolvedBuffer.get(specification);
    if (value != null) {
      return value;
    }
    final DependencyNode node = _spec2Node.get(specification);
    if (node == null) {
      return null;
    }
    // Can only use the specification if it is consumed by another node; i.e. it has been fully resolved
    // and is not just an advisory used to merge tentative results to give a single node producing multiple
    // outputs.
    synchronized (this) {
      for (final DependencyNode dependent : node.getDependentNodes()) {
        if (dependent.hasInputValue(specification)) {
          return new ResolvedValue(specification, node.getFunction(), node.getInputValuesCopy(), node.getOutputValuesCopy());
        }
      }
    }
    return null;
  }

  public void declareProduction(final ResolvedValue resolvedValue) {
    _resolvedBuffer.put(resolvedValue.getValueSpecification(), resolvedValue);
  }

  @Override
  public void failed(final GraphBuildingContext context, final ValueRequirement value, final ResolutionFailure failure) {
    s_logger.error("Couldn't resolve {}", value);
    if (failure != null) {
      final ResolutionFailure failureImpl = failure.checkFailure(value);
      if (_failureVisitor != null) {
        failureImpl.accept(_failureVisitor);
      }
      context.exception(new UnsatisfiableDependencyGraphException(failureImpl));
    } else {
      s_logger.warn("No failure state for {}", value);
      context.exception(new UnsatisfiableDependencyGraphException(value));
    }
  }

  private static boolean mismatchUnionImpl(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    nextA: for (final ValueSpecification a : as) { //CSIGNORE
      if (bs.contains(a)) {
        // Exact match
        continue;
      }
      final String aName = a.getValueName();
      final ValueProperties aProperties = a.getProperties();
      boolean mismatch = false;
      for (final ValueSpecification b : bs) {
        if (aName == b.getValueName()) {
          // Match the name; check the constraints
          if (aProperties.isSatisfiedBy(b.getProperties())) {
            continue nextA;
          } else {
            // Mismatch found
            mismatch = true;
          }
        }
      }
      if (mismatch) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether the union of value specifications would be mismatched; that is the two sets can't be composed. Given the intersection of common value names, the properties must be mutually
   * compatible.
   * 
   * @param as the first set of values, not null
   * @param bs the second set of values, not null
   * @return true if the values can't be composed, false if they can
   */
  private static boolean mismatchUnion(final Set<ValueSpecification> as, final Set<ValueSpecification> bs) {
    return mismatchUnionImpl(as, bs) || mismatchUnionImpl(bs, as);
  }

  public synchronized void populateState(final DependencyGraph graph) {
    final Set<DependencyNode> remove = new HashSet<DependencyNode>();
    for (final DependencyNode node : graph.getDependencyNodes()) {
      for (final DependencyNode dependent : node.getDependentNodes()) {
        if (!graph.containsNode(dependent)) {
          // Need to remove "dependent" from the node. We can leave the output values there; they might be used, or will get discarded by discardUnusedOutputs afterwards
          remove.add(dependent);
        }
      }
      _graphNodes.add(node);
      for (final ValueSpecification output : node.getOutputValues()) {
        _spec2Node.put(output, node);
      }
      getOrCreateNodes(node.getFunction(), node.getComputationTarget()).add(node);
    }
    for (final DependencyNode node : remove) {
      node.clearInputs();
    }
    for (final Map.Entry<ValueSpecification, Set<ValueRequirement>> terminal : graph.getTerminalOutputs().entrySet()) {
      _resolvedValues.put(terminal.getKey(), new ArrayList<ValueRequirement>(terminal.getValue()));
    }
  }

  private DependencyNode collapseNode(final DependencyNode node, final DependencyNode peer) {
    final ComputationTargetSpecification collapsed = _computationTargetCollapser.collapse(node.getFunction().getFunction(), peer.getComputationTarget(), node.getComputationTarget());
    if (collapsed == null) {
      return null;
    }
    if (collapsed.equals(peer.getComputationTarget())) {
      s_logger.debug("Collapsing new node {} into existing node {}", node, peer);
      node.replaceWith(peer);
      return peer;
    } else if (collapsed.equals(node.getComputationTarget())) {
      s_logger.debug("Collapsing existing node {} into new node {}", peer, node);
      peer.replaceWith(node);
      // Replace references to the original peer with the new node
      for (final ValueSpecification output : peer.getOutputValues()) {
        _spec2Node.put(output, node);
      }
      _graphNodes.remove(peer);
      _leafNodes.remove(peer);
      return node;
    } else {
      s_logger.debug("Collapsing {} and {} into new target {}", new Object[] {node, peer, collapsed });
      final DependencyNode collapsedNode = new DependencyNode(collapsed);
      collapsedNode.setFunction(node.getFunction());
      peer.replaceWith(collapsedNode);
      node.replaceWith(collapsedNode);
      // Replace the references to the original peer to the collapsed node. The new node is not referenced other than by graph edges.
      for (final ValueSpecification output : peer.getOutputValues()) {
        _spec2Node.put(output, collapsedNode);
        final Collection<ValueRequirement> requirements = _resolvedValues.remove(output);
        if (requirements != null) {
          _resolvedValues.put(MemoryUtils.instance(new ValueSpecification(output.getValueName(), collapsed, output.getProperties())), requirements);
        }
      }
      _graphNodes.remove(peer);
      _graphNodes.add(collapsedNode);
      if (_leafNodes.remove(peer)) {
        _leafNodes.add(collapsedNode);
      }
      return collapsedNode;
    }
  }

  private static boolean equals(final ParameterizedFunction a, final ParameterizedFunction b) {
    if (a == b) {
      return true;
    }
    if (a.getFunction() != b.getFunction()) {
      if (!a.getFunction().getFunctionDefinition().getUniqueId().equals(b.getFunction().getFunctionDefinition().getUniqueId())) {
        return false;
      }
    }
    return a.getParameters().equals(b.getParameters());
  }

  private DependencyNode collapseNode(final DependencyNode node) {
    final Set<DependencyNode> inputs = node.getInputNodes();
    final ParameterizedFunction function = node.getFunction();
    if (inputs.isEmpty()) {
      for (final DependencyNode peer : _leafNodes) {
        if (peer == node) {
          // Can't merge with ourselves
          continue;
        }
        if (!equals(peer.getFunction(), function)) {
          // Can't merge with different function
          continue;
        }
        if (!peer.getComputationTarget().getType().equals(node.getComputationTarget().getType())) {
          // Can't merge with different types
          continue;
        }
        final DependencyNode collapsed = collapseNode(node, peer);
        if (collapsed == null) {
          // Collapser couldn't handle the nodes
          continue;
        }
        return collapsed;
      }
    } else {
      for (final DependencyNode inputNode : inputs) {
        for (final DependencyNode peer : inputNode.getDependentNodes()) {
          if (peer == node) {
            // Can't merge with ourselves
            continue;
          }
          if (!equals(peer.getFunction(), function)) {
            // Can't merge with different function
            continue;
          }
          if (!peer.getComputationTarget().getType().equals(node.getComputationTarget().getType())) {
            // Can't merge with different types
            continue;
          }
          if (!inputs.equals(peer.getInputNodes())) {
            // Can't merge with different inputs
            continue;
          }
          final DependencyNode collapsed = collapseNode(node, peer);
          if (collapsed == null) {
            // Collapser couldn't handle the nodes
            continue;
          }
          return collapsed;
        }
      }
    }
    return node;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, DependencyNode node,
      final boolean newNode) {
    Set<ValueSpecification> downstreamCopy = null;
    for (final ValueSpecification input : resolvedValue.getFunctionInputs()) {
      DependencyNode inputNode;
      inputNode = _spec2Node.get(input);
      if (inputNode != null) {
        s_logger.debug("Found node {} for input {}", inputNode, input);
        node.addInputValue(input);
        node.addInputNode(inputNode);
      } else {
        s_logger.debug("Finding node production for {}", input);
        final ResolvedValue inputValue = _resolvedBuffer.get(input);
        if (inputValue != null) {
          if (downstreamCopy == null) {
            downstreamCopy = new HashSet<ValueSpecification>(downstream);
            downstreamCopy.add(resolvedValue.getValueSpecification());
            s_logger.debug("Downstream = {}", downstreamCopy);
          }
          inputNode = getOrCreateNode(inputValue, downstreamCopy);
          if (inputNode != null) {
            node.addInputNode(inputNode);
            if (input.getTargetSpecification().equals(inputNode.getComputationTarget())) {
              node.addInputValue(input);
            } else {
              // The node we connected to is a substitute following a target collapse; the original input value is now incorrect
              final ValueSpecification substituteInput = new ValueSpecification(input.getValueName(), inputNode.getComputationTarget(), input.getProperties());
              assert inputNode.getOutputValues().contains(substituteInput);
              node.addInputValue(substituteInput);
            }
          } else {
            s_logger.warn("No node production for {}", inputValue);
            return null;
          }
        } else {
          s_logger.warn("No registered production for {}", input);
          return null;
        }
      }
    }
    if (newNode) {
      s_logger.debug("Adding {} to graph set", node);
      // [PLAT-346] Here is a good spot to tackle PLAT-346; which node's outputs do we discard if there are multiple
      // productions for a given value specification?
      for (final ValueSpecification valueSpecification : resolvedValue.getFunctionOutputs()) {
        final DependencyNode existing = _spec2Node.get(valueSpecification);
        if (existing == null) {
          _spec2Node.put(valueSpecification, node);
        } else {
          // Simplest to keep the existing one (otherwise have to reconnect dependent nodes in the graph)
          node.removeOutputValue(valueSpecification);
        }
      }
      if ((_computationTargetCollapser != null) && _computationTargetCollapser.canApplyTo(node.getComputationTarget().getSpecification())) {
        // Test if the targets can be collapsed and another node used here
        final DependencyNode collapsedNode = collapseNode(node);
        if (collapsedNode != node) {
          s_logger.debug("Rewrite after collapse to {}", collapsedNode);
          node = collapsedNode;
        } else {
          _graphNodes.add(node);
          if (resolvedValue.getFunctionInputs().isEmpty()) {
            _leafNodes.add(node);
          }
        }
      } else {
        _graphNodes.add(node);
        if (resolvedValue.getFunctionInputs().isEmpty()) {
          _leafNodes.add(node);
        }
      }
      _resolvedBuffer.remove(resolvedValue.getValueSpecification());
    }
    return node;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream, final DependencyNode existingNode,
      final Set<DependencyNode> nodes) {
    if (existingNode != null) {
      return getOrCreateNode(resolvedValue, downstream, existingNode, false);
    } else {
      DependencyNode newNode = new DependencyNode(resolvedValue.getValueSpecification().getTargetSpecification());
      newNode.setFunction(resolvedValue.getFunction());
      newNode.addOutputValues(resolvedValue.getFunctionOutputs());
      newNode = getOrCreateNode(resolvedValue, downstream, newNode, true);
      if (newNode != null) {
        nodes.add(newNode);
      }
      return newNode;
    }
  }

  private Set<DependencyNode> getOrCreateNodes(final ParameterizedFunction function, final ComputationTargetSpecification targetSpecification) {
    Map<ComputationTargetSpecification, Set<DependencyNode>> target2nodes = _func2target2nodes.get(function);
    if (target2nodes == null) {
      target2nodes = new HashMap<ComputationTargetSpecification, Set<DependencyNode>>();
      _func2target2nodes.put(function, target2nodes);
    }
    Set<DependencyNode> nodes = target2nodes.get(targetSpecification);
    if (nodes == null) {
      nodes = new HashSet<DependencyNode>();
      target2nodes.put(targetSpecification, nodes);
    }
    return nodes;
  }

  private DependencyNode findExistingNode(final Set<DependencyNode> nodes, final ResolvedValue resolvedValue) {
    for (final DependencyNode node : nodes) {
      final Set<ValueSpecification> outputValues = node.getOutputValues();
      if (mismatchUnion(outputValues, resolvedValue.getFunctionOutputs())) {
        s_logger.debug("Can't reuse {} for {}", node, resolvedValue);
      } else {
        s_logger.debug("Considering {} for {}", node, resolvedValue);
        // Update the output values for the node with the union. The input values will be dealt with by the caller.
        List<ValueSpecification> replacements = null;
        boolean matched = false;
        for (final ValueSpecification output : resolvedValue.getFunctionOutputs()) {
          if (outputValues.contains(output)) {
            // Exact match found
            matched = true;
            continue;
          }
          final String outputName = output.getValueName();
          final ValueProperties outputProperties = output.getProperties();
          for (final ValueSpecification outputValue : outputValues) {
            if (outputName == outputValue.getValueName()) {
              if (outputValue.getProperties().isSatisfiedBy(outputProperties)) {
                // Found match
                matched = true;
                final ValueProperties composedProperties = outputValue.getProperties().compose(outputProperties);
                if (!composedProperties.equals(outputValue.getProperties())) {
                  final ValueSpecification newOutputValue = MemoryUtils
                      .instance(new ValueSpecification(outputValue.getValueName(), outputValue.getTargetSpecification(), composedProperties));
                  s_logger.debug("Replacing {} with {} in reused node", outputValue, newOutputValue);
                  if (replacements == null) {
                    replacements = new ArrayList<ValueSpecification>(outputValues.size() * 2);
                  }
                  replacements.add(outputValue);
                  replacements.add(newOutputValue);
                }
              }
            }
          }
        }
        if (!matched) {
          continue;
        }
        if (replacements != null) {
          final Iterator<ValueSpecification> replacement = replacements.iterator();
          while (replacement.hasNext()) {
            final ValueSpecification oldValue = replacement.next();
            final ValueSpecification newValue = replacement.next();
            final int newConsumers = node.replaceOutputValue(oldValue, newValue);
            DependencyNode n = _spec2Node.remove(oldValue);
            assert n == node;
            n = _spec2Node.get(newValue);
            if (n != null) {
              // Reducing the value has created a collision ...
              if (newConsumers == 0) {
                // Keep the existing one (it's being used, or just an arbitrary choice if neither are used)
                node.removeOutputValue(newValue);
              } else {
                int existingConsumers = 0;
                for (final DependencyNode child : n.getDependentNodes()) {
                  if (child.hasInputValue(newValue)) {
                    existingConsumers++;
                  }
                }
                if (existingConsumers == 0) {
                  // Lose the existing (not being used), keep the new one
                  n.removeOutputValue(newValue);
                  _spec2Node.put(newValue, node);
                } else {
                  if (newConsumers <= existingConsumers) {
                    // Adjust the consumers of the reduced value to use the existing one
                    for (final DependencyNode child : node.getDependentNodes()) {
                      child.replaceInput(newValue, node, n);
                    }
                    node.removeOutputValue(newValue);
                  } else {
                    // Adjust the consumers of the existing value to use the new one
                    for (final DependencyNode child : n.getDependentNodes()) {
                      child.replaceInput(newValue, n, node);
                    }
                    n.removeOutputValue(newValue);
                    _spec2Node.put(newValue, node);
                  }
                }
              }
            } else {
              _spec2Node.put(newValue, node);
            }
          }
        }
        return node;
      }
    }
    return null;
  }

  private DependencyNode getOrCreateNode(final ResolvedValue resolvedValue, final Set<ValueSpecification> downstream) {
    s_logger.debug("Resolved {}", resolvedValue.getValueSpecification());
    if (downstream.contains(resolvedValue.getValueSpecification())) {
      s_logger.debug("Already have downstream production of {} in {}", resolvedValue.getValueSpecification(), downstream);
      return null;
    }
    final DependencyNode existingNode = _spec2Node.get(resolvedValue.getValueSpecification());
    if (existingNode != null) {
      s_logger.debug("Existing production of {} found in graph set", resolvedValue);
      return existingNode;
    }
    final Set<DependencyNode> nodes = getOrCreateNodes(resolvedValue.getFunction(), resolvedValue.getValueSpecification().getTargetSpecification());
    return getOrCreateNode(resolvedValue, downstream, findExistingNode(nodes, resolvedValue), nodes);
  }

  private void completeGraphBuild() {
    while (!_resolvedQueue.isEmpty() && _singleton.compareAndSet(null, Thread.currentThread())) {
      synchronized (this) {
        Pair<ValueRequirement, ResolvedValue> resolved = _resolvedQueue.poll();
        while (resolved != null) {
          final DependencyNode node = getOrCreateNode(resolved.getSecond(), Collections.<ValueSpecification>emptySet());
          if (node != null) {
            ValueSpecification outputValue = resolved.getSecond().getValueSpecification();
            if (!outputValue.getTargetSpecification().equals(node.getComputationTarget())) {
              outputValue = MemoryUtils.instance(new ValueSpecification(outputValue.getValueName(), node.getComputationTarget(), outputValue.getProperties()));
            }
            assert node.getOutputValues().contains(outputValue);
            Collection<ValueRequirement> requirements = _resolvedValues.get(outputValue);
            if (requirements == null) {
              requirements = new ArrayList<ValueRequirement>();
              _resolvedValues.put(outputValue, requirements);
            }
            requirements.add(resolved.getFirst());
          } else {
            s_logger.error("Resolved {} to {} but couldn't create one or more dependency node", resolved.getFirst(), resolved.getSecond().getValueSpecification());
          }
          resolved = _resolvedQueue.poll();
        }
      }
      _singleton.set(null);
    }
  }

  /**
   * Reports a successful resolution of a top level requirement. The production of linked {@link DependencyNode} instances to form the final graph is single threaded. The resolution is added to a
   * queue of successful resolutions. If this is the only (or first) thread to report resolutions then this will work to drain the queue and produce nodes for the graph based on the resolved value
   * cache in the building context. If other threads report resolutions while this is happening they are added to the queue and those threads return immediately.
   */
  @Override
  public void resolved(final GraphBuildingContext context, final ValueRequirement valueRequirement, final ResolvedValue resolvedValue, final ResolutionPump pump) {
    s_logger.info("Resolved {} to {}", valueRequirement, resolvedValue.getValueSpecification());
    if (pump != null) {
      context.close(pump);
    }
    _resolvedQueue.add(Pair.of(valueRequirement, resolvedValue));
    completeGraphBuild();
  }

  @Override
  public String toString() {
    return "TerminalValueCallback";
  }

  /**
   * Returns the dependency graph nodes built by calls to {@link #resolved}. It is only valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned.
   * A copy of the internal structure is used so that it may be modified by the caller and this callback instance be used to process subsequent resolutions.
   * 
   * @return the dependency graph nodes, not null
   */
  public synchronized Collection<DependencyNode> getGraphNodes() {
    return new ArrayList<DependencyNode>(_graphNodes);
  }

  /**
   * Returns the map of top level requirements requested of the graph builder to the specifications it produced that are in the dependency graph. Failed resolutions are not reported here. It is only
   * valid to call this when there are no pending resolutions - that is all calls to {@link #resolved} have returned. A copy of the internal structure is used so that it may be modified by the caller
   * and this callback instance be used to process subsequent resolutions.
   * 
   * @return the map of resolutions, not null
   */
  public synchronized Map<ValueRequirement, ValueSpecification> getTerminalValues() {
    final Map<ValueRequirement, ValueSpecification> result = new HashMap<ValueRequirement, ValueSpecification>(_resolvedValues.size());
    for (final Map.Entry<ValueSpecification, Collection<ValueRequirement>> resolvedValues : _resolvedValues.entrySet()) {
      for (final ValueRequirement requirement : resolvedValues.getValue()) {
        result.put(requirement, resolvedValues.getKey());
      }
    }
    return result;
  }

  public void reportStateSize() {
    if (!s_logger.isInfoEnabled()) {
      return;
    }
    s_logger.info("Graph = {} nodes for {} terminal outputs", _graphNodes.size(), _resolvedValues.size());
    s_logger.info("Resolved buffer = {}, resolved queue = {}", _resolvedBuffer.size(), _resolvedQueue.size());
  }

}
