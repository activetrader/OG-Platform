/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class DoubleObjectArrayFormatter extends NoHistoryFormatter<Double[][]> {

  @Override
  public Object formatForDisplay(Double[][] value, ValueSpecification valueSpec) {
    int rowCount;
    int colCount;
    rowCount = value.length;
    if (rowCount == 0) {
      colCount = 0;
    } else {
      colCount = value[0].length;
    }
    return "Matrix (" + rowCount + " x " + colCount + ")";
  }

  @Override
  public Double[][] formatForExpandedDisplay(Double[][] value, ValueSpecification valueSpec) {
    return value;
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.LABELLED_MATRIX_2D;
  }
}
