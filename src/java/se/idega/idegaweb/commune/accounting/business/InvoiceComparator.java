package se.idega.idegaweb.commune.accounting.business;

import java.text.Collator;
import java.util.Comparator;

import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceRecord;

import com.idega.util.LocaleUtil;

/**
 * A class to compare a collection of InvoiceRecords or InvoiceHeaders objects.
 * @author Sigtryggur
 */
public class InvoiceComparator implements Comparator {

	private Collator collator;
	private String compareString1;
	private String compareString2;
	/** 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	
	public int compare(Object o1, Object o2) {		
		this.collator = Collator.getInstance(LocaleUtil.getSwedishLocale());


		if (o1 instanceof InvoiceRecord){
			this.compareString1 = String.valueOf(((InvoiceRecord) o1).getOrderId());
			this.compareString2 = String.valueOf(((InvoiceRecord) o2).getOrderId());
		}
		return this.collator.compare(this.compareString1, this.compareString2);
	}
}
