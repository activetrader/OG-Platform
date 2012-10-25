/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;

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

import com.opengamma.batch.BatchMaster;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;

@BeanDefinition
public class MarketDataValue extends DirectBean implements ObjectIdentifiable {
  
  @PropertyDefinition
  private long _id;
  
  @PropertyDefinition
  private long _marketDataId;
  
  /*
    This value is not stored in db, but it is rather paired with _computationTargetId
   */
  @PropertyDefinition
  private ComputationTargetSpecification _computationTargetSpecification;
  
  @PropertyDefinition
  private Long _computationTargetSpecificationId;
  
  @PropertyDefinition
  private String _name;
  
  @PropertyDefinition
  private double _value;

  public MarketDataValue() {
  }

  public MarketDataValue(ComputationTargetSpecification computationTargetSpecification, double value, String name) {
    _computationTargetSpecification = computationTargetSpecification;
    _name = name;
    _value = value;
  }
  
  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(BatchMaster.BATCH_IDENTIFIER_SCHEME, Long.toString(getId()));
  }

  /*private Map<ComputationTargetSpecification, Map<String, MarketDataValue>> _ct2FieldName2Entry; 
  
  private void buildIndex(MarketDataValue entry) {
    if (_ct2FieldName2Entry == null) {
      _ct2FieldName2Entry = new HashMap<ComputationTargetSpecification, Map<String, MarketDataValue>>();
    }

    Map<String, MarketDataValue> fieldName2Entry = _ct2FieldName2Entry.get(entry.getComputationTarget().toComputationTargetSpec());
    if (fieldName2Entry == null) {
      fieldName2Entry = new HashMap<String, MarketDataValue>();
      _ct2FieldName2Entry.put(entry.getComputationTarget().toComputationTargetSpec(), fieldName2Entry);
    }

    if (fieldName2Entry.get(entry.getField().getName()) != null) {
      throw new IllegalArgumentException("Already has entry for " + 
          entry.getComputationTarget().toComputationTargetSpec() + "/" + entry.getField().getName());
    }
    fieldName2Entry.put(entry.getField().getName(), entry);
  }
  
  public MarketDataValue getEntry(ComputationTargetSpecification computationTargetSpec, String fieldName) {
    if (_ct2FieldName2Entry == null) {
      _ct2FieldName2Entry = new HashMap<ComputationTargetSpecification, Map<String, MarketDataValue>>();
      for (MarketDataValue entry : getSnapshotEntries()) {
        buildIndex(entry);
      }
    }

    Map<String, MarketDataValue> fieldName2Entry = _ct2FieldName2Entry.get(computationTargetSpec);
    if (fieldName2Entry == null) {
      return null;
    }
    return fieldName2Entry.get(fieldName);
  }*/
  

  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MarketDataValue}.
   * @return the meta-bean, not null
   */
  public static MarketDataValue.Meta meta() {
    return MarketDataValue.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(MarketDataValue.Meta.INSTANCE);
  }

  @Override
  public MarketDataValue.Meta metaBean() {
    return MarketDataValue.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        return getId();
      case -530966079:  // marketDataId
        return getMarketDataId();
      case -1157884501:  // computationTargetSpecification
        return getComputationTargetSpecification();
      case -330473434:  // computationTargetSpecificationId
        return getComputationTargetSpecificationId();
      case 3373707:  // name
        return getName();
      case 111972721:  // value
        return getValue();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 3355:  // id
        setId((Long) newValue);
        return;
      case -530966079:  // marketDataId
        setMarketDataId((Long) newValue);
        return;
      case -1157884501:  // computationTargetSpecification
        setComputationTargetSpecification((ComputationTargetSpecification) newValue);
        return;
      case -330473434:  // computationTargetSpecificationId
        setComputationTargetSpecificationId((Long) newValue);
        return;
      case 3373707:  // name
        setName((String) newValue);
        return;
      case 111972721:  // value
        setValue((Double) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MarketDataValue other = (MarketDataValue) obj;
      return JodaBeanUtils.equal(getId(), other.getId()) &&
          JodaBeanUtils.equal(getMarketDataId(), other.getMarketDataId()) &&
          JodaBeanUtils.equal(getComputationTargetSpecification(), other.getComputationTargetSpecification()) &&
          JodaBeanUtils.equal(getComputationTargetSpecificationId(), other.getComputationTargetSpecificationId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getValue(), other.getValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getComputationTargetSpecification());
    hash += hash * 31 + JodaBeanUtils.hashCode(getComputationTargetSpecificationId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValue());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the value of the property
   */
  public long getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the new value of the property
   */
  public void setId(long id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<Long> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the marketDataId.
   * @return the value of the property
   */
  public long getMarketDataId() {
    return _marketDataId;
  }

  /**
   * Sets the marketDataId.
   * @param marketDataId  the new value of the property
   */
  public void setMarketDataId(long marketDataId) {
    this._marketDataId = marketDataId;
  }

  /**
   * Gets the the {@code marketDataId} property.
   * @return the property, not null
   */
  public final Property<Long> marketDataId() {
    return metaBean().marketDataId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
   * 
   * Please see distribution for license.
   * /
   * package com.opengamma.batch.domain;
   * 
   * import java.util.Map;
   * 
   * import org.joda.beans.BeanBuilder;
   * import org.joda.beans.BeanDefinition;
   * import org.joda.beans.JodaBeanUtils;
   * import org.joda.beans.MetaProperty;
   * import org.joda.beans.Property;
   * import org.joda.beans.PropertyDefinition;
   * import org.joda.beans.impl.direct.DirectBean;
   * import org.joda.beans.impl.direct.DirectBeanBuilder;
   * import org.joda.beans.impl.direct.DirectMetaBean;
   * import org.joda.beans.impl.direct.DirectMetaProperty;
   * import org.joda.beans.impl.direct.DirectMetaPropertyMap;
   * 
   * import com.opengamma.batch.BatchMaster;
   * import com.opengamma.engine.ComputationTargetSpecification;
   * import com.opengamma.id.ObjectId;
   * import com.opengamma.id.ObjectIdentifiable;
   * 
   * @BeanDefinition
   * public class MarketDataValue extends DirectBean implements ObjectIdentifiable {
   * 
   * @PropertyDefinition
   * private long _id;
   * 
   * @PropertyDefinition
   * private long _marketDataId;
   * 
   * /*
   * This value is not stored in db, but it is rather paired with _computationTargetId
   * @return the value of the property
   */
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _computationTargetSpecification;
  }

  /**
   * Sets copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
   * 
   * Please see distribution for license.
   * /
   * package com.opengamma.batch.domain;
   * 
   * import java.util.Map;
   * 
   * import org.joda.beans.BeanBuilder;
   * import org.joda.beans.BeanDefinition;
   * import org.joda.beans.JodaBeanUtils;
   * import org.joda.beans.MetaProperty;
   * import org.joda.beans.Property;
   * import org.joda.beans.PropertyDefinition;
   * import org.joda.beans.impl.direct.DirectBean;
   * import org.joda.beans.impl.direct.DirectBeanBuilder;
   * import org.joda.beans.impl.direct.DirectMetaBean;
   * import org.joda.beans.impl.direct.DirectMetaProperty;
   * import org.joda.beans.impl.direct.DirectMetaPropertyMap;
   * 
   * import com.opengamma.batch.BatchMaster;
   * import com.opengamma.engine.ComputationTargetSpecification;
   * import com.opengamma.id.ObjectId;
   * import com.opengamma.id.ObjectIdentifiable;
   * 
   * @BeanDefinition
   * public class MarketDataValue extends DirectBean implements ObjectIdentifiable {
   * 
   * @PropertyDefinition
   * private long _id;
   * 
   * @PropertyDefinition
   * private long _marketDataId;
   * 
   * /*
   * This value is not stored in db, but it is rather paired with _computationTargetId
   * @param computationTargetSpecification  the new value of the property
   */
  public void setComputationTargetSpecification(ComputationTargetSpecification computationTargetSpecification) {
    this._computationTargetSpecification = computationTargetSpecification;
  }

  /**
   * Gets the the {@code computationTargetSpecification} property.
   * 
   * Please see distribution for license.
   * /
   * package com.opengamma.batch.domain;
   * 
   * import java.util.Map;
   * 
   * import org.joda.beans.BeanBuilder;
   * import org.joda.beans.BeanDefinition;
   * import org.joda.beans.JodaBeanUtils;
   * import org.joda.beans.MetaProperty;
   * import org.joda.beans.Property;
   * import org.joda.beans.PropertyDefinition;
   * import org.joda.beans.impl.direct.DirectBean;
   * import org.joda.beans.impl.direct.DirectBeanBuilder;
   * import org.joda.beans.impl.direct.DirectMetaBean;
   * import org.joda.beans.impl.direct.DirectMetaProperty;
   * import org.joda.beans.impl.direct.DirectMetaPropertyMap;
   * 
   * import com.opengamma.batch.BatchMaster;
   * import com.opengamma.engine.ComputationTargetSpecification;
   * import com.opengamma.id.ObjectId;
   * import com.opengamma.id.ObjectIdentifiable;
   * 
   * @BeanDefinition
   * public class MarketDataValue extends DirectBean implements ObjectIdentifiable {
   * 
   * @PropertyDefinition
   * private long _id;
   * 
   * @PropertyDefinition
   * private long _marketDataId;
   * 
   * /*
   * This value is not stored in db, but it is rather paired with _computationTargetId
   * @return the property, not null
   */
  public final Property<ComputationTargetSpecification> computationTargetSpecification() {
    return metaBean().computationTargetSpecification().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the computationTargetSpecificationId.
   * @return the value of the property
   */
  public Long getComputationTargetSpecificationId() {
    return _computationTargetSpecificationId;
  }

  /**
   * Sets the computationTargetSpecificationId.
   * @param computationTargetSpecificationId  the new value of the property
   */
  public void setComputationTargetSpecificationId(Long computationTargetSpecificationId) {
    this._computationTargetSpecificationId = computationTargetSpecificationId;
  }

  /**
   * Gets the the {@code computationTargetSpecificationId} property.
   * @return the property, not null
   */
  public final Property<Long> computationTargetSpecificationId() {
    return metaBean().computationTargetSpecificationId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the value.
   * @return the value of the property
   */
  public double getValue() {
    return _value;
  }

  /**
   * Sets the value.
   * @param value  the new value of the property
   */
  public void setValue(double value) {
    this._value = value;
  }

  /**
   * Gets the the {@code value} property.
   * @return the property, not null
   */
  public final Property<Double> value() {
    return metaBean().value().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MarketDataValue}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<Long> _id = DirectMetaProperty.ofReadWrite(
        this, "id", MarketDataValue.class, Long.TYPE);
    /**
     * The meta-property for the {@code marketDataId} property.
     */
    private final MetaProperty<Long> _marketDataId = DirectMetaProperty.ofReadWrite(
        this, "marketDataId", MarketDataValue.class, Long.TYPE);
    /**
     * The meta-property for the {@code computationTargetSpecification} property.
     */
    private final MetaProperty<ComputationTargetSpecification> _computationTargetSpecification = DirectMetaProperty.ofReadWrite(
        this, "computationTargetSpecification", MarketDataValue.class, ComputationTargetSpecification.class);
    /**
     * The meta-property for the {@code computationTargetSpecificationId} property.
     */
    private final MetaProperty<Long> _computationTargetSpecificationId = DirectMetaProperty.ofReadWrite(
        this, "computationTargetSpecificationId", MarketDataValue.class, Long.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", MarketDataValue.class, String.class);
    /**
     * The meta-property for the {@code value} property.
     */
    private final MetaProperty<Double> _value = DirectMetaProperty.ofReadWrite(
        this, "value", MarketDataValue.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "marketDataId",
        "computationTargetSpecification",
        "computationTargetSpecificationId",
        "name",
        "value");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case -530966079:  // marketDataId
          return _marketDataId;
        case -1157884501:  // computationTargetSpecification
          return _computationTargetSpecification;
        case -330473434:  // computationTargetSpecificationId
          return _computationTargetSpecificationId;
        case 3373707:  // name
          return _name;
        case 111972721:  // value
          return _value;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MarketDataValue> builder() {
      return new DirectBeanBuilder<MarketDataValue>(new MarketDataValue());
    }

    @Override
    public Class<? extends MarketDataValue> beanType() {
      return MarketDataValue.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code marketDataId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> marketDataId() {
      return _marketDataId;
    }

    /**
     * The meta-property for the {@code computationTargetSpecification} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComputationTargetSpecification> computationTargetSpecification() {
      return _computationTargetSpecification;
    }

    /**
     * The meta-property for the {@code computationTargetSpecificationId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> computationTargetSpecificationId() {
      return _computationTargetSpecificationId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code value} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> value() {
      return _value;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
