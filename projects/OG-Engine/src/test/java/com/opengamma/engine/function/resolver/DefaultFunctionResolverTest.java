/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.test.PrimitiveTestFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Triple;

/**
 * 
 */
@Test
public class DefaultFunctionResolverTest {

  private ComputationTarget _target;
  private PrimitiveTestFunction _f1;
  private PrimitiveTestFunction _f2;
  private ParameterizedFunction _parameterizedF1;
  private ParameterizedFunction _parameterizedF2;
  private DefaultCompiledFunctionResolver _resolver;

  @BeforeMethod
  public void setUp() {
    _target = new ComputationTarget(UniqueId.of("scheme", "test_target"));
    _f1 = new PrimitiveTestFunction("req1");
    _f1.setUniqueId("1");
    _f2 = new PrimitiveTestFunction("req1");
    _f2.setUniqueId("2");
    _parameterizedF1 = new ParameterizedFunction(_f1, _f1.getDefaultParameters());
    _parameterizedF2 = new ParameterizedFunction(_f2, _f2.getDefaultParameters());
    _resolver = new DefaultCompiledFunctionResolver(new FunctionCompilationContext());
  }

  public void globalRuleSelection() {
    _resolver.addRule(new ResolutionRule(_parameterizedF1, ApplyToAllTargets.INSTANCE, 100));
    _resolver.addRule(new ResolutionRule(_parameterizedF2, ApplyToAllTargets.INSTANCE, 200));
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = _resolver.resolveFunction(new ValueRequirement("req1", _target.toSpecification()), _target).next();
    assertEquals(_parameterizedF2, result.getFirst());
  }

  private static class Filter extends ComputationTargetFilter {

    private final ComputationTarget _match;

    public Filter(final ComputationTarget match) {
      super(null);
      _match = match;
    }

    @Override
    public boolean accept(ComputationTarget target) {
      return target == _match;
    }

  }

  public void nonGlobalRuleSelection() {
    _resolver.addRule(new ResolutionRule(_parameterizedF1, ApplyToAllTargets.INSTANCE, 100));
    _resolver.addRule(new ResolutionRule(_parameterizedF2, new Filter(_target), 200));
    Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> result = _resolver.resolveFunction(new ValueRequirement("req1", _target.toSpecification()), _target).next();
    assertEquals(_parameterizedF2, result.getFirst());
    ComputationTarget anotherTarget = new ComputationTarget(UniqueId.of("scheme", "target2"));
    result = _resolver.resolveFunction(new ValueRequirement("req1", anotherTarget.toSpecification()), anotherTarget).next();
    assertEquals(_parameterizedF1, result.getFirst());
  }

}
