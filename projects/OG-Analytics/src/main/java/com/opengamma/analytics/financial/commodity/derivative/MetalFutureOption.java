/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.MetalFutureDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * energy future option commodity derivative
 */
public class MetalFutureOption extends CommodityFutureOption<MetalFutureDefinition> {

  /**
   * Constructor for future options
   *
   * @param expiry Time (in years as a double) until the date-time at which the future expires
   * @param underlying Underlying future
   * @param strike Strike price
   * @param exerciseType Exercise type - European or American
   * @param isCall Call if true, Put if false
   */
  public MetalFutureOption(final double expiry, final MetalFutureDefinition underlying, final double strike, final ExerciseDecisionType exerciseType, final boolean isCall) {
    super(expiry, underlying, strike, exerciseType, isCall);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitMetalFutureOption(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitMetalFutureOption(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MetalFutureOption)) {
      return false;
    }
    return super.equals(obj);
  }
}
