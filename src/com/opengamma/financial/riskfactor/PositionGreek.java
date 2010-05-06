/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import com.opengamma.financial.greeks.Greek;

/**
 *
 */
public class PositionGreek {
  private final Greek _underlyingGreek;

  public PositionGreek(final Greek underlyingGreek) {
    _underlyingGreek = underlyingGreek;
  }

  public Greek getUnderlyingGreek() {
    return _underlyingGreek;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_underlyingGreek == null) ? 0 : _underlyingGreek.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final PositionGreek other = (PositionGreek) obj;
    if (_underlyingGreek == null) {
      if (other._underlyingGreek != null)
        return false;
    } else if (!_underlyingGreek.equals(other._underlyingGreek))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Position[" + _underlyingGreek + "]";
  }

}
