/*
 * Copyright (C) 2003 Idega software. All Rights Reserved.
 *
 * This software is the proprietary information of Idega software.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.export.ifs.presentation;

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.export.business.ExportBusiness;
import se.idega.idegaweb.commune.accounting.export.ifs.business.IFSBusiness;
import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.presentation.ApplicationForm;
import se.idega.idegaweb.commune.accounting.presentation.ButtonPanel;
import se.idega.idegaweb.commune.accounting.presentation.OperationalFieldsMenu;

import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.TextInput;

/**
 * @author palli
 */
public class CreateIFSFiles extends AccountingBlock {
	protected final static String KEY_HEADER = "cacc_create_files_header";

	protected final static String KEY_HEADER_OPERATION = "cacc_create_files_operation";
	protected final static String KEY_PAYMENT_DATE = "cacc_create_files_payment_date";
	protected final static String KEY_PERIOD_TEXT = "cacc_create_files_period_text";
	protected final static String KEY_CREATE = "cacc_create_files_save";

	protected final static String PARAM_CREATE_FILE = "ifs_file_create";
	protected final static String PARAM_CANCEL = "ifs_file_cancel";
	protected final static String PARAM_PAYMENT_DATE = "ifs_payment_date";
	protected final static String PARAM_PERIOD_TEXT = "ifs_period_text";

	protected final static int ACTION_VIEW = 0;
	protected final static int ACTION_CREATE = 1;
	protected final static int ACTION_CANCEL = 2; //and does what??

	protected String _currentOperation = null;

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.presentation.AccountingBlock#init(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		try {
			int action = parseAction(iwc);
			switch (action) {
				case ACTION_VIEW :
					viewForm(iwc);
					break;
				case ACTION_CREATE :
					createFiles(iwc);
					viewForm(iwc);
					break;
			}
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
	}

	private int parseAction(IWContext iwc) {
		try {
			_currentOperation = getSession().getOperationalField();
			if (_currentOperation == null)
				_currentOperation = "";
		}
		catch (RemoteException e) {
		}

		if (iwc.isParameterSet(PARAM_CREATE_FILE)) {
			return ACTION_CREATE;
		}
		return ACTION_VIEW;
	}

	private void createFiles(IWContext iwc) {
		String date = iwc.getParameter(PARAM_PAYMENT_DATE);
		String period = iwc.getParameter(PARAM_PERIOD_TEXT);
		
		try {
			getIFSBusiness(iwc).createFiles(_currentOperation,date,period,iwc.getCurrentUser());
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
			
			//Throw new Exception?
		}
	}

	private void viewForm(IWContext iwc) {
		ApplicationForm form = new ApplicationForm(this);
		form.setLocalizedTitle(KEY_HEADER, "Create files");
		form.setSearchPanel(getTopPanel(iwc));
		form.setButtonPanel(getButtonPanel());
		add(form);
	}

	private Table getTopPanel(IWContext iwc) {
		Table table = new Table();
		table.setColumnAlignment(1, Table.HORIZONTAL_ALIGN_LEFT);
		table.setColumnAlignment(2, Table.HORIZONTAL_ALIGN_LEFT);
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());

		table.add(getLocalizedLabel(KEY_HEADER_OPERATION, "School category"), 1, 1);
		table.add(new OperationalFieldsMenu(), 2, 1);

		int paymentDate = -1;
		try {
			paymentDate = getExportBusiness(iwc).getExportDataMapping(_currentOperation).getStandardPaymentDay();
		}
		catch (RemoteException e) {
			paymentDate = -1;		
		}
		catch (FinderException e) {
			paymentDate = -1;		
		}

		DateInput date = new DateInput();
		if (paymentDate != -1)
			date.setDay(paymentDate-1);
		TextInput period = getTextInput(PARAM_PERIOD_TEXT,"");
		period.setMaxlength(20);
		period.setSize(20);

		table.add(getLocalizedLabel(KEY_PAYMENT_DATE, "Payment date"), 1, 2);
		table.add(date, 2, 2);
		table.add(getLocalizedLabel(KEY_PERIOD_TEXT, "Period text for bill"), 1, 3);
		table.add(period, 2, 3);
		

		return table;
	}
	
	private ButtonPanel getButtonPanel() {
		ButtonPanel buttonPanel = new ButtonPanel(this);
		buttonPanel.addLocalizedButton(PARAM_CREATE_FILE, "true", KEY_CREATE, "Save");
		return buttonPanel;
	}
		
	private IFSBusiness getIFSBusiness(IWContext iwc) throws RemoteException {
		return (IFSBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, IFSBusiness.class);
	}
	
	private ExportBusiness getExportBusiness(IWContext iwc) throws RemoteException {
		return (ExportBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, ExportBusiness.class);
	}			
}