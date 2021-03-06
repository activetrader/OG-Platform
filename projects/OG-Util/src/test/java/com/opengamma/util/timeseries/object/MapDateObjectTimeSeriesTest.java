/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.date.DateObjectTimeSeries;
import com.opengamma.util.timeseries.date.MapDateObjectTimeSeries;

@Test
public class MapDateObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  public ObjectTimeSeries<Date, BigDecimal> createEmptyTimeSeries() {
    return new MapDateObjectTimeSeries<BigDecimal>();
  }

  @Override
  public DateObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new MapDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public DateObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new MapDateObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public DateObjectTimeSeries<BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new MapDateObjectTimeSeries<BigDecimal>(dts);
  }
}
