/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class VolatilityCubeDataFormatter extends AbstractFormatter<VolatilityCubeData> {

  /* package */ VolatilityCubeDataFormatter() {
    super(VolatilityCubeData.class);
  }

  @Override
  public String formatCell(VolatilityCubeData value, ValueSpecification valueSpec) {
    return "Volatility Cube data (" + value.getDataPoints().size() + " volatility points, " + value.getATMStrikes().size()
        + " strikes, " + value.getOtherData().getDataPoints().size() + " other data points " + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.PRIMITIVE;
  }
}
