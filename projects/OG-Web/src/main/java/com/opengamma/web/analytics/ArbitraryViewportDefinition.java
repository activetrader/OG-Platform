/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.analytics.formatting.TypeFormatter;

/**
 * Viewport containing an arbitrary collection of cells.
 */
public class ArbitraryViewportDefinition extends ViewportDefinition {

  /** Cells in the viewport, not empty, sorted by column then row (i.e. along rows) */
  private final List<GridCell> _cells;

  /**
   * @param version
   * @param cells Cells in the viewport, not empty
   * @param format
   */
  /* package */ ArbitraryViewportDefinition(int version, List<GridCell> cells, TypeFormatter.Format format) {
    super(version, format);
    ArgumentChecker.notEmpty(cells, "cells");
    _cells = new ArrayList<GridCell>(cells);
    Collections.sort(_cells);
  }

  @Override
  public Iterator<GridCell> iterator() {
    return _cells.iterator();
  }

  @Override
  public boolean isValidFor(GridStructure gridStructure) {
    for (GridCell cell : _cells) {
      if (cell.getRow() >= gridStructure.getRowCount() ||
          cell.getColumn() >= gridStructure.getColumnCount()) {
        return false;
      }
    }
    return true;
  }
}
