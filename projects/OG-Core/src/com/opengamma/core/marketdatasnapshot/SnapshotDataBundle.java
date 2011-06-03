/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.Identifier;

/**
 * A bundle of data to use in some structured context
 */
@BeanDefinition
public class SnapshotDataBundle extends DirectBean {

  /**
   * The market values in the bundle
   */
  @PropertyDefinition
  private Map<Identifier, Double> _dataPoints;
  
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SnapshotDataBundle}.
   * @return the meta-bean, not null
   */
  public static SnapshotDataBundle.Meta meta() {
    return SnapshotDataBundle.Meta.INSTANCE;
  }

  @Override
  public SnapshotDataBundle.Meta metaBean() {
    return SnapshotDataBundle.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 1186222381:  // dataPoints
        return getDataPoints();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 1186222381:  // dataPoints
        setDataPoints((Map<Identifier, Double>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SnapshotDataBundle other = (SnapshotDataBundle) obj;
      return JodaBeanUtils.equal(getDataPoints(), other.getDataPoints());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataPoints());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market values in the bundle
   * @return the value of the property
   */
  public Map<Identifier, Double> getDataPoints() {
    return _dataPoints;
  }

  /**
   * Sets the market values in the bundle
   * @param dataPoints  the new value of the property
   */
  public void setDataPoints(Map<Identifier, Double> dataPoints) {
    this._dataPoints = dataPoints;
  }

  /**
   * Gets the the {@code dataPoints} property.
   * @return the property, not null
   */
  public final Property<Map<Identifier, Double>> dataPoints() {
    return metaBean().dataPoints().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SnapshotDataBundle}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code dataPoints} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Identifier, Double>> _dataPoints = DirectMetaProperty.ofReadWrite(
        this, "dataPoints", SnapshotDataBundle.class, (Class) Map.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "dataPoints");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1186222381:  // dataPoints
          return _dataPoints;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SnapshotDataBundle> builder() {
      return new DirectBeanBuilder<SnapshotDataBundle>(new SnapshotDataBundle());
    }

    @Override
    public Class<? extends SnapshotDataBundle> beanType() {
      return SnapshotDataBundle.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code dataPoints} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Identifier, Double>> dataPoints() {
      return _dataPoints;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
