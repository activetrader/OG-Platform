/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;

/**
 * 
 */
public class EmpiricalPositionHistoricalVaRDefaultPropertiesFunction extends EmpiricalHistoricalVaRDefaultPropertiesFunction {

  public EmpiricalPositionHistoricalVaRDefaultPropertiesFunction(final String samplingPeriod, final String scheduleCalculator, final String samplingCalculator, 
      final String confidenceLevel, final String horizon) {
    super(samplingPeriod, scheduleCalculator, samplingCalculator, confidenceLevel, horizon, ComputationTargetType.POSITION);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION;
  }
}
