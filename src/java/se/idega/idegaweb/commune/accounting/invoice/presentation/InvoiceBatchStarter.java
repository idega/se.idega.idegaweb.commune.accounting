package se.idega.idegaweb.commune.accounting.invoice.presentation;

import java.rmi.RemoteException;

import se.idega.idegaweb.commune.accounting.invoice.business.InvoiceBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingBusinessHome;
import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.presentation.OperationalFieldsMenu;

import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.InputContainer;
import com.idega.util.IWTimestamp;

/**
 * @author Joakim
 *
 */
public class InvoiceBatchStarter extends AccountingBlock{
	
	private static String PREFIX="cacc_invbs_";
	private static String PARAM_MONTH=PREFIX+"month";
	private static String PARAM_READ_DATE=PREFIX+"read_date";
	DateInput monthInput;
	DateInput readDateInput;	

	public void init(IWContext iwc){
		String schoolCategory=null;
		OperationalFieldsMenu opFields = new OperationalFieldsMenu();
		try {
			schoolCategory = getSession().getOperationalField();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		handleAction(iwc,schoolCategory);
		
		Form form = new Form();
		add(form);
		
		add(opFields);

		monthInput = new DateInput(PARAM_MONTH,true);
		monthInput.setToCurrentDate();
		monthInput.setToShowDay(false);
		
		InputContainer month = getInputContainer(PARAM_MONTH,"Month", monthInput);
		form.add(month);

		readDateInput = new DateInput(PARAM_READ_DATE,true);	

		InputContainer readDate = getInputContainer(PARAM_READ_DATE,"Read date", readDateInput);
		form.add(readDate);
		
		GenericButton saveButton = this.getSaveButton();
		GenericButton cancelButton = this.getCancelButton();
		form.add(saveButton);
		form.add(cancelButton);
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
	private void handleSave(IWContext iwc, String schoolCategory) {
		try {
			InvoiceBusiness invoiceBusiness = (InvoiceBusiness)IBOLookup.getServiceInstance(iwc, InvoiceBusiness.class);
			invoiceBusiness.startPostingBatch(new IWTimestamp(iwc.getParameter(PARAM_MONTH)).getDate(), schoolCategory, iwc);
		} catch (Exception e) {
			add(new ExceptionWrapper(e));
		}
	}

	public PostingBusinessHome getPostingBusinessHome() throws RemoteException {
		return (PostingBusinessHome) IDOLookup.getHome(PostingBusiness.class);
	}
}
