package se.idega.idegaweb.commune.accounting.invoice.presentation;

import java.rmi.RemoteException;
import java.sql.Date;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.export.business.ExportBusiness;
import se.idega.idegaweb.commune.accounting.export.data.ExportDataMapping;
import se.idega.idegaweb.commune.accounting.invoice.business.BatchRunQueue;
import se.idega.idegaweb.commune.accounting.invoice.business.SchoolCategoryNotFoundException;
import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.presentation.OperationalFieldsMenu;

import com.idega.data.IDOLookupException;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.InputContainer;
import com.idega.util.IWTimestamp;

/**
 * Starts the batch run that will create billing and invoicing information
 * according to the parameters set in the UI.
 * 
 * @author Joakim
 * 
 * @see se.idega.idegaweb.commune.accounting.invoice.business.InvoiceBusiness
 * @see se.idega.idegaweb.commune.accounting.invoice.business.BillingThread
 */
public class InvoiceBatchStarter extends AccountingBlock{
	
	private static String PREFIX="cacc_invbs_";
	private static String PARAM_MONTH=PREFIX+"month";
	private static String PARAM_READ_DATE=PREFIX+"read_date";
	DateInput monthInput;
	DateInput dateInput;
	DateInput readDateInput;
	private String link=null;
	private IWContext _iwc;

	public void init(IWContext iwc){
		this._iwc = iwc;
		String schoolCategory=null;
		OperationalFieldsMenu opFields = new OperationalFieldsMenu();
		try {
			schoolCategory = getSession().getOperationalField();
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e, this));
			e.printStackTrace();
		}

		handleAction(iwc,schoolCategory);
		
		add(opFields);
		
		Form form = new Form();
		add(form);

		form.add(getShoolDropDown());

		if(null!=schoolCategory){
			try {
				ExportBusiness exportBusiness = getBusiness().getExportBusiness();
				ExportDataMapping exportDataMapping = exportBusiness.getExportDataMapping(schoolCategory);
				if(exportDataMapping.getAccountSettlementType() ==
					exportBusiness.getAccountSettlementTypeSpecificDate())
				{
					this.readDateInput = (DateInput) iwc.getApplicationAttribute(PARAM_READ_DATE+iwc.getCurrentUserId());
					if (this.readDateInput == null){
						this.readDateInput = new DateInput(PARAM_READ_DATE,true);
						this.readDateInput.setToCurrentDate();	
						this.readDateInput.setToDisplayDayLast(true);
						int currentYear = java.util.Calendar.getInstance ().get (java.util.Calendar.YEAR);
						this.readDateInput.setYearRange(currentYear - 1, currentYear + 1);
						iwc.setApplicationAttribute(PARAM_READ_DATE+iwc.getCurrentUserId(), this.readDateInput);						
					}
					String date = iwc.getParameter(PARAM_READ_DATE);
					if(date!=null){
						this.readDateInput.setDate(new IWTimestamp(date).getDate());
					}
					
					InputContainer readDate = getInputContainer(PARAM_READ_DATE,"Read date", this.readDateInput);
					form.add(readDate);
				}else{
					this.monthInput = (DateInput) iwc.getApplicationAttribute(PARAM_MONTH+iwc.getCurrentUserId());
					if (this.monthInput == null) {
						this.monthInput = new DateInput(PARAM_MONTH,true);
						this.monthInput.setToCurrentDate();	
						this.monthInput.setToShowDay(false);
						this.monthInput.setToDisplayDayLast(true);						
						int currentYear = java.util.Calendar.getInstance ().get (java.util.Calendar.YEAR);
						this.monthInput.setYearRange(currentYear - 1, currentYear + 1);							
						iwc.setApplicationAttribute(PARAM_MONTH+iwc.getCurrentUserId(), this.monthInput);						
					}
					String date = iwc.getParameter(PARAM_MONTH);
					if(date!=null){
						this.monthInput.setDate(new IWTimestamp(date).getDate());
					}

					InputContainer month = getInputContainer(PARAM_MONTH,"Month", this.monthInput);
					form.add(month);
				}
			} catch (IDOLookupException e) {
				add(new ExceptionWrapper(e, this));
				e.printStackTrace();
			} catch (RemoteException e) {
				add(new ExceptionWrapper(e, this));
				e.printStackTrace();
			} catch (FinderException e) {
				add(new ExceptionWrapper(e, this));
				e.printStackTrace();
			}
			
			GenericButton saveButton = this.getSaveButton();
			form.add(saveButton);
		}
	}
	
	protected Date getParamMonth(){
		Date month = null;
		if(getIWContext().getParameter(PARAM_MONTH)!=null){
			month = new IWTimestamp(getIWContext().getParameter(PARAM_MONTH)).getDate();
		}		
		return month;
	}
	/**
	 * @param iwc
	 */
	private void handleAction(IWContext iwc, String schoolCategory) {
		if(iwc.isParameterSet(PARAM_SAVE)){
			handleSave(iwc, schoolCategory);
		}
	}
	
	/**
	 * @param iwc
	 */
	protected void handleSave(IWContext iwc, String schoolCategory) {
		try {
//			InvoiceBusiness invoiceBusiness = (InvoiceBusiness)IBOLookup.getServiceInstance(iwc, InvoiceBusiness.class);
			Date month = null;
			Date readDate = null;
			if(iwc.getParameter(PARAM_MONTH)!=null){
				month = new IWTimestamp(iwc.getParameter(PARAM_MONTH)).getDate();
			}
			if(iwc.getParameter(PARAM_READ_DATE)!=null){
				try{
					readDate = new IWTimestamp(iwc.getParameter(PARAM_READ_DATE)).getDate();
				}catch(IllegalArgumentException e){
					add(getErrorText(getLocalizedString("invbr.Please_provide_a_valid_date","Please provide a proper date.",iwc)));
					return;
				}
			}
			addBatchRunToQueue(month, readDate, schoolCategory, iwc);

//			invoiceBusiness.startPostingBatch(month, readDate, schoolCategory, iwc);
			add(getLocalizedText("invbr.batchrun_started","Batchrun started"));
			add(new Break());
/*
			if(link!=null)
			{
				Link uiLink = new Link();
				uiLink.setText(getLocalizedLabel("invbr.progress","Progress"));
				uiLink.setTarget(link);
				add(uiLink);
			} else {
				System.out.println("WARNING need to set the Link property for invoice batch start block!");
			}
*/
			} catch (SchoolCategoryNotFoundException e) {
			add(getErrorText(getLocalizedString("invbr.please_select_valid_school_category","Please select valid school category.",iwc)));
			e.printStackTrace();
//		} catch (BatchAlreadyRunningException e) {
//			add(getErrorText(getLocalizedString("invbr.batchrun_already_started","Batchrun already started",iwc)));
		} catch (Exception e) {
			add(new ExceptionWrapper(e));
		}
	}
	
	protected void addBatchRunToQueue(Date month, Date readDate, String schoolCategory, IWContext iwc) throws SchoolCategoryNotFoundException{
		BatchRunQueue.addBatchRunToQueue(month, readDate, schoolCategory, iwc);		
	}
	
	/**
	 * @return
	 */
	public String getLink() {
		return this.link;
	}

	/**
	 * @param string
	 */
	public void setLink(String page) {
		this.link = page;
	}


	protected PresentationObject getShoolDropDown(){
		return new Text("");
	}
	
	protected IWContext getIWContext() {
		return this._iwc;
	}

}
