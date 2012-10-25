/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.AbstractSearchResult;
import com.opengamma.util.PublicSPI;

/**
 * Result from searching for historical time-series information.
 * <p>
 * The returned documents will match the search criteria.
 * See {@link HistoricalTimeSeriesInfoSearchRequest} for more details.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesInfoSearchResult extends AbstractSearchResult<HistoricalTimeSeriesInfoDocument> {

  /**
   * Creates an instance.
   */
  public HistoricalTimeSeriesInfoSearchResult() {
  }

  /**
   * Creates an instance from a collection of documents.
   * 
   * @param coll  the collection of documents to add, not null
   */
  public HistoricalTimeSeriesInfoSearchResult(Collection<HistoricalTimeSeriesInfoDocument> coll) {
    super(coll);
  }

  /**
   * Creates an instance specifying the version-correction searched for.
   * 
   * @param versionCorrection  the version-correction of the data, not null
   */
  public HistoricalTimeSeriesInfoSearchResult(VersionCorrection versionCorrection) {
    setVersionCorrection(versionCorrection);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the returned series information from within the documents.
   * 
   * @return the series, not null
   */
  public List<ManageableHistoricalTimeSeriesInfo> getInfoList() {
    List<ManageableHistoricalTimeSeriesInfo> result = new ArrayList<ManageableHistoricalTimeSeriesInfo>();
    if (getDocuments() != null) {
      for (HistoricalTimeSeriesInfoDocument doc : getDocuments()) {
        result.add(doc.getInfo());
      }
    }
    return result;
  }

  /**
   * Gets the first series information, or null if no documents.
   * 
   * @return the first series information, null if none
   */
  public ManageableHistoricalTimeSeriesInfo getFirstInfo() {
    return getDocuments().size() > 0 ? getDocuments().get(0).getInfo() : null;
  }

  /**
   * Gets the single result expected from a query.
   * <p>
   * This throws an exception if more than 1 result is actually available.
   * Thus, this method implies an assumption about uniqueness of the queried series.
   * 
   * @return the matching exchange, not null
   * @throws IllegalStateException if no series was found
   */
  public ManageableHistoricalTimeSeriesInfo getSingleInfo() {
    if (getDocuments().size() != 1) {
      throw new OpenGammaRuntimeException("Expecting zero or single resulting match, and was " + getDocuments().size());
    } else {
      return getDocuments().get(0).getInfo();
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HistoricalTimeSeriesInfoSearchResult}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static HistoricalTimeSeriesInfoSearchResult.Meta meta() {
    return HistoricalTimeSeriesInfoSearchResult.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(HistoricalTimeSeriesInfoSearchResult.Meta.INSTANCE);
  }

  @Override
  public HistoricalTimeSeriesInfoSearchResult.Meta metaBean() {
    return HistoricalTimeSeriesInfoSearchResult.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HistoricalTimeSeriesInfoSearchResult}.
   */
  public static class Meta extends AbstractSearchResult.Meta<HistoricalTimeSeriesInfoDocument> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesInfoSearchResult> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesInfoSearchResult>(new HistoricalTimeSeriesInfoSearchResult());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesInfoSearchResult> beanType() {
      return HistoricalTimeSeriesInfoSearchResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
