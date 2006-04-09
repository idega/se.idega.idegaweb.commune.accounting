package se.idega.idegaweb.commune.accounting.business;

import java.text.Collator;
import java.util.Comparator;

import se.idega.idegaweb.commune.accounting.invoice.data.PaymentHeader;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecord;

import com.idega.util.LocaleUtil;

/**
 * A class to compare a collection of PaymentRecords or PaymentHeaders objects.
 * @author Sigtryggur
 */
public class PaymentComparator implements Comparator {

	private Collator collator;
	private String compareString1;
	private String compareString2;
	/** 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	
	public int compare(Object o1, Object o2) {		
		this.collator = Collator.getInstance(LocaleUtil.getSwedishLocale());

		if (o1 instanceof PaymentHeader) {
			this.compareString1 = ((PaymentHeader) o1).getSchool().getName();
			this.compareString2 = ((PaymentHeader) o2).getSchool().getName();
		}
		else if (o1 instanceof PaymentRecord) {
			int int1 = ((PaymentRecord) o1).getOrderId();
			int int2 = ((PaymentRecord) o2).getOrderId();
			
			if (int1 == int2) {
				return 0;
			}
			else if (int1 < int2) {
				return -1;
			}
			else {
				return 1;
			}
		}
		return this.collator.compare(this.compareString1, this.compareString2);
	}
}
