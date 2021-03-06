/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/* package */class LazyTargetResolverTrade extends LazyTargetResolverPositionOrTrade implements Trade {

  public LazyTargetResolverTrade(final ComputationTargetResolver resolver, final ComputationTargetSpecification specification) {
    super(resolver, specification);
  }

  protected Trade getResolved() {
    return getResolvedTarget().getTrade();
  }

  @Override
  public Map<String, String> getAttributes() {
    return getResolved().getAttributes();
  }

  @Override
  public void setAttributes(Map<String, String> attributes) {
    getResolved().setAttributes(attributes);
  }

  @Override
  public void addAttribute(String key, String value) {
    getResolved().addAttribute(key, value);
  }

  @Override
  public UniqueId getParentPositionId() {
    return getResolved().getParentPositionId();
  }

  @Override
  public Counterparty getCounterparty() {
    return getResolved().getCounterparty();
  }

  @Override
  public LocalDate getTradeDate() {
    return getResolved().getTradeDate();
  }

  @Override
  public OffsetTime getTradeTime() {
    return getResolved().getTradeTime();
  }

  @Override
  public Double getPremium() {
    return getResolved().getPremium();
  }

  @Override
  public Currency getPremiumCurrency() {
    return getResolved().getPremiumCurrency();
  }

  @Override
  public LocalDate getPremiumDate() {
    return getResolved().getPremiumDate();
  }

  @Override
  public OffsetTime getPremiumTime() {
    return getResolved().getPremiumTime();
  }

}
