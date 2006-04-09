package se.idega.idegaweb.commune.accounting.invoice.business;

import com.idega.data.IDOException;
import com.idega.data.IDOLookup;
import java.rmi.RemoteException;
import java.util.Map;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceRecord;
import se.idega.idegaweb.commune.accounting.invoice.data.InvoiceRecordHome;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecord;
import se.idega.idegaweb.commune.accounting.regulations.business.RegSpecConstant;
import se.idega.idegaweb.commune.accounting.regulations.data.MainRule;
import se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType;
import se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecTypeHome;

/**
 * Last modified: $Date: 2006/04/09 11:53:32 $ by $Author: laddi $
 *
 * @author <a href="http://www.staffannoteberg.com">Staffan N�teberg</a>
 * @version $Revision: 1.7 $
 */
public class PaymentSummary {
	private int placementCount = 0;
	private int individualCount = 0;
	private long totalAmountVatExcluded = 0;
	private long totalAmountVat = 0;

	public PaymentSummary (final PaymentRecord [] records) throws RemoteException {
		this(records, null);
		//Optimization by Sigtryggur New constructor with HashMap added to cache MainRule strings
	}

	public PaymentSummary (final PaymentRecord [] records, Map mainRuleStringValues) throws RemoteException {
		// get home objects
		final InvoiceRecordHome invoiceRecordHome
				= (InvoiceRecordHome) IDOLookup.getHome (InvoiceRecord.class);
		final RegulationSpecTypeHome regSpecTypeHome
				= (RegulationSpecTypeHome) IDOLookup.getHome (RegulationSpecType.class);

		// count values
		for (int i = 0; i < records.length; i++) {
			final PaymentRecord paymentRecord = records [i];
			this.placementCount += paymentRecord.getPlacements ();
			final String regSpecTypeName = paymentRecord.getRuleSpecType ();
			String mainRuleName = "";
			if (mainRuleStringValues != null && mainRuleStringValues.containsKey(regSpecTypeName)) {
				mainRuleName = (String)mainRuleStringValues.get(regSpecTypeName);
			}
			else if (regSpecTypeName != null){
				try {
					final	RegulationSpecType regSpecType
							= regSpecTypeHome.findByRegulationSpecType (regSpecTypeName);
					final MainRule mainRule = regSpecType.getMainRule ();
					mainRuleName = mainRule.getMainRule ();
					if (mainRuleStringValues != null) {
						mainRuleStringValues.put(regSpecTypeName, mainRuleName);
					}
				} catch (FinderException e) {
					System.err.println ("PaymentRecord id with incorrect reg spec type = "
															+ paymentRecord.getPrimaryKey ());
					e.printStackTrace ();
				}
			}
			final long amountExcl = se.idega.idegaweb.commune.accounting.business.AccountingUtil.roundAmount (paymentRecord.getTotalAmount ());
			if (mainRuleName.equals (RegSpecConstant.MAIN_RULE_VAT)) {
				this.totalAmountVat += amountExcl;
			} else {
				this.totalAmountVatExcluded += amountExcl;
			}
		}		

		try {
			this.individualCount
					= invoiceRecordHome.getIndividualCountByPaymentRecords (records);
		} catch (IDOException e) {
			e.printStackTrace ();
		}
	}

	/*	
	private static int getIndividualCount(final PaymentRecord[] records, final SchoolClassMemberHome placementHome, final InvoiceRecordHome invoiceRecordHome) {
		final Set result = new HashSet ();
		try {
			final Collection invoiceRecords
					= invoiceRecordHome.findByPaymentRecords (records);
			for (Iterator j = invoiceRecords.iterator (); j.hasNext ();) {
				final InvoiceRecord invoiceRecord = (InvoiceRecord) j.next ();
				try {
					final Integer placementId = new Integer
							(invoiceRecord.getSchoolClassMemberId ());
					final SchoolClassMember placement
							= placementHome.findByPrimaryKey (placementId);
					final User user = placement.getStudent ();
					result.add (user.getPrimaryKey ());
				} catch (Exception e) {
					e.printStackTrace ();
				}
			}
		} catch (FinderException e) {
			// no problem, no invoice records found
		}
		return result.size ();
	}
	*/

	public long getPlacementCount () {
		return this.placementCount;
	}

	public long getIndividualsCount () {
		return this.individualCount; 
	}

	public long getTotalAmountVatExcluded () {
		return this.totalAmountVatExcluded;
	}
	
	public long getTotalAmountVat () {
		 return this.totalAmountVat;
	}
}
