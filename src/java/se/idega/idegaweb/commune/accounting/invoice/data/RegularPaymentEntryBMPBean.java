/*
 * Created on 28.10.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package se.idega.idegaweb.commune.accounting.invoice.data;

import java.sql.Date;
import java.util.Collection;
import javax.ejb.FinderException;


import se.idega.idegaweb.commune.accounting.regulations.data.VATRegulation;

import com.idega.block.school.data.School;
import com.idega.data.GenericEntity;
import com.idega.user.data.User;

/**
 * @author Roar
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class RegularPaymentEntryBMPBean extends GenericEntity implements RegularPaymentEntry {

	private static final String COLUMN_PRIMARY_KEY = "cacc_reg_pay_entry_id";

	private static final String COLUMN_OWN_POSTING = "own_posting";
	private static final String COLUMN_DOUBLE_POSTING = "double_posting";
	private static final String COLUMN_NOTE = "note";
	private static final String COLUMN_VAT = "vat";
	private static final String COLUMN_VAT_REG_ID = "vat_reg_id";
	private static final String COLUMN_AMOUNT = "amount";
	private static final String COLUMN_SCHOOL_ID = "school_id";
	private static final String COLUMN_USER_ID = "user_id";
	private static final String COLUMN_PLACING = "placing";
	private static final String COLUMN_TO = "periode_to";
	private static final String COLUMN_FROM = "periode_from";
	
	private static final String ENTITY_NAME = "cacc_regular_payment_entry";
	
	
	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecord#initializeAttributes()
	 */
	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addAttribute(COLUMN_OWN_POSTING, "", true, true, java.lang.String.class);
		addAttribute(COLUMN_DOUBLE_POSTING, "", true, true, java.lang.String.class);
		addAttribute(COLUMN_NOTE, "", true, true, java.lang.String.class);
		addAttribute(COLUMN_VAT, "", true, true, java.lang.Float.class);
		addAttribute(COLUMN_VAT_REG_ID, "", true, true, java.lang.Integer.class, 1);
		addAttribute(COLUMN_AMOUNT, "", true, true, java.lang.Float.class);
		addAttribute(COLUMN_SCHOOL_ID, "", true, true, java.lang.Integer.class);
		addAttribute(COLUMN_USER_ID, "", true, true, java.lang.Integer.class);
		addAttribute(COLUMN_PLACING, "", true, true, java.lang.String.class);
		addAttribute(COLUMN_TO, "", true, true, java.sql.Date.class);
		addAttribute(COLUMN_FROM, "", true, true, java.sql.Date.class);

		addManyToOneRelationship(COLUMN_USER_ID, User.class);
		addManyToOneRelationship(COLUMN_SCHOOL_ID, School.class);
	}
	

	/* (non-Javadoc)
	 * @see com.idega.data.GenericEntity#getEntityName()
	 */
	public String getEntityName() {
		return ENTITY_NAME;
	}
			
	public String getIDColumnName(){
		return COLUMN_PRIMARY_KEY;
	}

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getFrom()
	 */
	public Date getFrom() {
		return getDateColumnValue(COLUMN_FROM);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getTo()
	 */
	public Date getTo() {
		return getDateColumnValue(COLUMN_TO);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getPlacing()
	 */
	public String getPlacing() {
		return getStringColumnValue(COLUMN_PLACING);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getUser()
	 */
	public User getUser() {
		return (User) getColumnValue(COLUMN_USER_ID);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getSchool()
	 */
	public School getSchool() {
		return (School) getColumnValue(COLUMN_SCHOOL_ID);
	}
	
	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getSchoolId()
	 */
	public int getSchoolId() {
		return getIntColumnValue(COLUMN_SCHOOL_ID);
	}	


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getDoublePostin()
	 */
	public String getDoublePosting() {
		return getStringColumnValue(COLUMN_DOUBLE_POSTING);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getAmount()
	 */
	public float getAmount() {
		return getFloatColumnValue(COLUMN_AMOUNT);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getVAT()
	 */
	public float getVAT() {
		return getFloatColumnValue(COLUMN_VAT);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getVatRegulation()
	 */
	public VATRegulation getVatRegulation() {
		return (VATRegulation) getColumnValue(COLUMN_VAT_REG_ID);
	}
	

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getVatRegulationID()
	 */
	public int getVatRegulationId() {
		return getIntColumnValue(COLUMN_VAT_REG_ID);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getNote()
	 */
	public String getNote() {
		return getStringColumnValue(COLUMN_NOTE);
	}

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#getOwnPosting()
	 */
	public String getOwnPosting() {
		return getStringColumnValue(COLUMN_OWN_POSTING);
	}
	

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setFrom(java.util.Date)
	 */
	public void setFrom(Date from) {
		setColumn(COLUMN_FROM, from);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setTo(java.util.Date)
	 */
	public void setTo(Date to) {
		setColumn(COLUMN_TO, to);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setPlacing(java.lang.String)
	 */
	public void setPlacing(String plascing) {
		setColumn(COLUMN_PLACING, plascing);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setAmount(float)
	 */
	public void setAmount(float amount) {
		setColumn(COLUMN_AMOUNT, amount);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setNote(java.lang.String)
	 */
	public void setNote(String note) {
		setColumn(COLUMN_NOTE, note);
	}
	
	public Collection ejbFindByPeriodeAndUser(Date from, Date to, int userId) throws FinderException {
		return idoFindPKsByQuery(idoQuery() 
		.appendSelectAllFrom(this)
		.appendWhereEquals(COLUMN_USER_ID, userId)
		.appendAnd()
		.append(COLUMN_FROM)
		.appendLessThanOrEqualsSign()
		.append(to)
		.appendAnd()
		.appendLeftParenthesis()
		.append(COLUMN_TO)
		.appendGreaterThanOrEqualsSign()
		.append(from)
//		.appendOr()
//		.append(COLUMN_TO)
//		.appendIsNull()
		.appendRightParenthesis());
	}

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setUser(com.idega.user.data.User)
	 */
	public void setUser(User user) {
		setColumn(COLUMN_USER_ID, user.getPrimaryKey());
	}

	
	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setProvider(com.idega.block.school.data.School)
	 */
	public void setSchoolId(int schoolId) {
		setColumn(COLUMN_SCHOOL_ID, schoolId);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setVAT(float)
	 */
	public void setVAT(float vat) {
		setColumn(COLUMN_VAT, vat);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setVatRegulation(se.idega.idegaweb.commune.accounting.regulations.data.VATRegulation)
	 */
	public void setVatRegulation(VATRegulation vatRegulation) {
		setColumn(COLUMN_VAT_REG_ID, vatRegulation);
	}

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setVatRegulationId(int)
	 */
	public void setVatRegulationId(int vatRegId){
		setColumn(COLUMN_VAT_REG_ID, vatRegId);		
	}
	
	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setOwnPosting(java.lang.String)
	 */
	public void setOwnPosting(String ownPosting) {
		setColumn(COLUMN_OWN_POSTING, ownPosting);
	}


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry#setDoublePosting(java.lang.String)
	 */
	public void setDoublePosting(String doublePosting) {
		setColumn(COLUMN_DOUBLE_POSTING, doublePosting);
	}
}