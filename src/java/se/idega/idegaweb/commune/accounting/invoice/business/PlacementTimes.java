package se.idega.idegaweb.commune.accounting.invoice.business;

import se.idega.idegaweb.commune.accounting.business.AccountingUtil;

import com.idega.util.IWTimestamp;

/**
 * Holder for times that are calculated for placement times
 * 
 * @author Joakim
 * 
 */
public class PlacementTimes {

	private final IWTimestamp firstCheckDay;

	private final IWTimestamp lastCheckDay;

	private final int specificNumberOfDaysPrMonth;

	public PlacementTimes(final IWTimestamp firstCheckDay, final IWTimestamp lastCheckDay, int specificNumberOfDays) {
		this.firstCheckDay = firstCheckDay;
		this.lastCheckDay = lastCheckDay;
		this.specificNumberOfDaysPrMonth = specificNumberOfDays;
	}

	public int getDays() {
		return 1 + AccountingUtil.getDayDiff(this.firstCheckDay, this.lastCheckDay);
	}

	public float getMonths() {
		if (isWholeNumberOfMonths()) {
			return getNumberOfMonths(daysInMonth(this.firstCheckDay), daysInMonth(this.lastCheckDay));
		}

		int daysInMonthFirstCheckDay = (this.specificNumberOfDaysPrMonth > 0) ? this.specificNumberOfDaysPrMonth : daysInMonth(this.firstCheckDay);
		int daysInMonthLastCheckDay = (this.specificNumberOfDaysPrMonth > 0) ? this.specificNumberOfDaysPrMonth : daysInMonth(this.lastCheckDay);
		
		return getNumberOfMonths(daysInMonthFirstCheckDay, daysInMonthLastCheckDay);
	}

	public IWTimestamp getLastCheckDay() {
		return this.lastCheckDay;
	}

	public IWTimestamp getFirstCheckDay() {
		return this.firstCheckDay;
	}

	private float getNumberOfMonths(int daysInMonthFirstCheckDay, int daysInMonthLastCheckDay) {
		float months = 1.0f + (this.lastCheckDay.getYear() * 12 + this.lastCheckDay.getMonth())
				- (this.firstCheckDay.getYear() * 12 + this.firstCheckDay.getMonth());

		// decrease with days before start date
		months -= (float) (this.firstCheckDay.getDay() - 1) / (float) daysInMonthFirstCheckDay;
		// decrease with days after end date
		months -= 1.0f - (float) this.lastCheckDay.getDay() / (float) daysInMonthLastCheckDay;

		return months;
	}

	/*
	 * A method that checks if the number of days between the two check dates is
	 * a whole number of months. Will return true if the difference between the
	 * dates + 1 is 0 or is equal to the number of days in the first month (this
	 * is true if we are handling only one month or multiple months, but the
	 * start date is not the 1st and the end date is not the last day of that
	 * month). Will alse return true if the first date is the 1st of some month
	 * and the last date is the last day of some other month.
	 * 
	 */
	private boolean isWholeNumberOfMonths() {
		int days = this.lastCheckDay.getDay() - this.firstCheckDay.getDay() + 1;
		if (days == 0 || days == daysInMonth(this.firstCheckDay)) {
			return true;
		}

		if (this.firstCheckDay.getDay() == 1 && (this.lastCheckDay.getDay() == daysInMonth(this.lastCheckDay))) {
			return true;
		}

		return false;
	}

	private int daysInMonth(final IWTimestamp date) {
		final IWTimestamp firstDay = new IWTimestamp(date);
		firstDay.setDay(1);
		final IWTimestamp lastDay = new IWTimestamp(firstDay);
		lastDay.addMonths(1);
		final int daysInMonth = AccountingUtil.getDayDiff(firstDay, lastDay);
		return daysInMonth;
	}
}