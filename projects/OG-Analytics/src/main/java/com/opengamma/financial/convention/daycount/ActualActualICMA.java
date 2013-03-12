/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.JulianFields;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.convention.StubType;

/**
 * The 'Actual/Actual ICMA' day count.
 */
public class ActualActualICMA extends ActualTypeDayCount {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    throw new NotImplementedException("Cannot get daycount fraction; need information about the coupon and payment frequency");
  }

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getAccruedInterest(previousCouponDate, date, nextCouponDate, coupon, paymentsPerYear, StubType.NONE);
  }

  public double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final double paymentsPerYear,
      final StubType stubType) {
    return getAccruedInterest(previousCouponDate.toLocalDate(), date.toLocalDate(), nextCouponDate.toLocalDate(), coupon, paymentsPerYear, stubType);
  }

  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear,
      final StubType stubType) {
    testDates(previousCouponDate, date, nextCouponDate);
    Validate.notNull(stubType, "stub type");

    long daysBetween, daysBetweenCoupons;
    final long previousCouponDateJulian = previousCouponDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long nextCouponDateJulian = nextCouponDate.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final long dateJulian = date.getLong(JulianFields.MODIFIED_JULIAN_DAY);
    final int months = (int) (12 / paymentsPerYear);
    switch (stubType) {
      case NONE: {
        daysBetween = dateJulian - previousCouponDateJulian;
        daysBetweenCoupons = nextCouponDate.getLong(JulianFields.MODIFIED_JULIAN_DAY) - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case SHORT_START: {
        final LocalDate notionalStart = getEOMAdjustedDate(nextCouponDate, nextCouponDate.minusMonths(months));
        daysBetweenCoupons = nextCouponDateJulian - notionalStart.getLong(JulianFields.MODIFIED_JULIAN_DAY);
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_START: {
        final long firstNotionalJulian = getEOMAdjustedDate(nextCouponDate, nextCouponDate.minusMonths(months * 2)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long secondNotionalJulian = getEOMAdjustedDate(nextCouponDate, nextCouponDate.minusMonths(months)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long daysBetweenStub = secondNotionalJulian - previousCouponDateJulian;
        final double daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        if (dateJulian > secondNotionalJulian) {
          daysBetween = dateJulian - secondNotionalJulian;
          return coupon * (daysBetweenStub / daysBetweenTwoNotionalCoupons + 1) / paymentsPerYear;
        }
        daysBetween = dateJulian - firstNotionalJulian;
        return coupon * (daysBetween / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      }
      case SHORT_END: {
        final LocalDate notionalEnd = getEOMAdjustedDate(previousCouponDate, previousCouponDate.plusMonths(months));
        daysBetweenCoupons = notionalEnd.getLong(JulianFields.MODIFIED_JULIAN_DAY) - previousCouponDateJulian;
        daysBetween = dateJulian - previousCouponDateJulian;
        return coupon * daysBetween / daysBetweenCoupons / paymentsPerYear;
      }
      case LONG_END: {
        final long firstNotionalJulian = getEOMAdjustedDate(previousCouponDate, previousCouponDate.plusMonths(months)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long secondNotionalJulian = getEOMAdjustedDate(previousCouponDate, previousCouponDate.plusMonths(2 * months)).getLong(JulianFields.MODIFIED_JULIAN_DAY);
        final long daysBetweenPreviousAndFirstNotional = firstNotionalJulian - previousCouponDateJulian;
        if (dateJulian < firstNotionalJulian) {
          daysBetween = dateJulian - previousCouponDateJulian;
          return coupon * daysBetween / daysBetweenPreviousAndFirstNotional / paymentsPerYear;
        }
        final long daysBetweenStub = dateJulian - firstNotionalJulian;
        final double daysBetweenTwoNotionalCoupons = secondNotionalJulian - firstNotionalJulian;
        return coupon * (1 + daysBetweenStub / daysBetweenTwoNotionalCoupons) / paymentsPerYear;
      }
      default:
        throw new IllegalArgumentException("Cannot handle stub type " + stubType);
    }
  }

  @Override
  public String getConventionName() {
    return "Actual/Actual ICMA";
  }

  // -------------------------------------------------------------------------
  /**
   * Adjusts the date to the last day of month if necessary.
   * 
   * @param comparison  the date to check as to being the last day of month, not null
   * @param date  the date to adjust, not null
   * @return the adjusted date, not null
   */
  private static LocalDate getEOMAdjustedDate(final LocalDate comparison, final LocalDate date) {
    if (comparison.getDayOfMonth() == comparison.lengthOfMonth()) {
      return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    return date;
  }

}
