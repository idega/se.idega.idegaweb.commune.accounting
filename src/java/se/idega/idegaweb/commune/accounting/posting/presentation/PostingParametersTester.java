/*
 * $Id: PostingParametersTester.java,v 1.1 2003/08/27 14:04:19 kjell Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.posting.presentation;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;

import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ExceptionWrapper;
import com.idega.util.IWTimestamp;
import com.idega.presentation.ui.Form;

import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.posting.data.PostingParameters;
import se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingParametersException;


/**
 * PostingParametersTest is an idegaWeb block that is used to test the Posting parameters retrieval 
 *  
 * <p>
 * $Id: PostingParametersTester.java,v 1.1 2003/08/27 14:04:19 kjell Exp $
 *
 * @author <a href="http://www.lindman.se">Kjell Lindman</a>
 * @version $Version$
 */
public class PostingParametersTester extends AccountingBlock {

	private final static int ACTION_DEFAULT = 0;
	private final static int ACTION_SEARCH = 1;
	private Form mainForm = null;

	private final static String PARAM_BUTTON_SEARCH = "button_search";

	private final static String PARAM_FIELD_ACTIVITY = "field_activity";
	private final static String PARAM_FIELD_REGSPEC = "field_regspec";
	private final static String PARAM_FIELD_COMPANY_TYPE = "field_company_type";
	private final static String PARAM_FIELD_COM_BELONGING = "field_com_belonging";
	private final static String PARAM_FIELD_DATE = "field_date";

	private String _errorMessage = "";

	public void main(final IWContext iwc) {
		setResourceBundle(getResourceBundle(iwc));

		try {
			int action = parseAction(iwc);
			prepareMainTable(iwc);
			switch (action) {
				case ACTION_DEFAULT :
					viewMainForm(iwc);
					break;
				case ACTION_SEARCH :
					_errorMessage = "";
					viewMainForm(iwc);
					viewSearchedResults(iwc);
					break;
			}
			add(mainForm);
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
	}
		 
	private int parseAction(IWContext iwc) {
		int action = ACTION_DEFAULT;
		if (iwc.isParameterSet(PARAM_BUTTON_SEARCH)) {
		  action = ACTION_SEARCH;
		}
		return action;
	}

	private void viewSearchedResults(IWContext iwc) {
		Table table = new Table();
		int row = 1;

		PostingParameters pp = searchPostingParameter(iwc);

		if (pp == null) {
			_errorMessage = "Hittades ej";		
		}
		if(_errorMessage.length() !=0) {
			table.add(getSmallErrorText(_errorMessage),1 ,row);
			mainForm.add(table);
			return;
		}
		
		table.add(getSmallText(""),1 ,row);
		table.add(getSmallText("Egen kontering"),2 ,row);
		table.add(getSmallText("Mot kontering"),3 ,row++);
		
		table.add(getSmallText("Konto"),1 ,row);
		table.add(getSmallText(pp.getPostingAccount()),2 ,row);
		table.add(getSmallText(pp.getDoublePostingAccount()),3 ,row++);

		table.add(getSmallText("Ansvar"),1 ,row);
		table.add(getSmallText(pp.getPostingLiability()),2 ,row);
		table.add(getSmallText(pp.getDoublePostingLiability()),3 ,row++);
		
		table.add(getSmallText("Resurs"),1 ,row);
		table.add(getSmallText(pp.getPostingResource()),2 ,row);
		table.add(getSmallText(pp.getDoublePostingResource()),3 ,row++);

		table.add(getSmallText("Verksamhet"),1 ,row);
		table.add(getSmallText(pp.getPostingActivityCode()),2 ,row);
		table.add(getSmallText(pp.getPostingActivityCode()),3 ,row++);
		
		table.add(getSmallText("Motpart"),1 ,row);
		table.add(getSmallText(pp.getPostingDoubleEntry()),2 ,row);
		table.add(getSmallText(pp.getDoublePostingDoubleEntry()),3 ,row++);
		
		table.add(getSmallText("Aktivitet"),1 ,row);
		table.add(getSmallText(pp.getPostingActivity()),2 ,row);
		table.add(getSmallText(pp.getDoublePostingActivity()),3 ,row++);

		table.add(getSmallText("Projekt"),1 ,row);
		table.add(getSmallText(pp.getPostingProject()),2 ,row);
		table.add(getSmallText(pp.getDoublePostingProject()),3 ,row++);

		table.add(getSmallText("Objekt"),1 ,row);
		table.add(getSmallText(pp.getPostingObject()),2 ,row);
		table.add(getSmallText(pp.getPostingObject()),3 ,row++);
		mainForm.add(table);
	}
	
	private void viewMainForm(IWContext iwc) {
		Table table = new Table();
		Timestamp rightNow = IWTimestamp.getTimestampRightNow();
		Date dd = new Date(System.currentTimeMillis());
		
		table.add(getLocalizedLabel("posting_test_date", "Datum"),1 ,1);
		table.add(getTextInput(PARAM_FIELD_DATE, formatDate(dd, 10)), 2, 1);
		
		table.add(getLocalizedLabel("posting_test_activity", "Verksamhet"),1 ,2);
		table.add(getTextInput(
				PARAM_FIELD_ACTIVITY, 
				iwc.isParameterSet(PARAM_FIELD_ACTIVITY) ?
				iwc.getParameter(PARAM_FIELD_ACTIVITY) : ""), 2, 2);
		table.add(getLocalizedText("posting_test_demo1", "keys: skola, forskola"), 3, 2);



		table.add(getLocalizedLabel("posting_test_regspec", "Regelspec. typ"),1 ,3);
		table.add(getTextInput(
				PARAM_FIELD_REGSPEC, 
				iwc.isParameterSet(PARAM_FIELD_REGSPEC) ?
				iwc.getParameter(PARAM_FIELD_REGSPEC) : ""), 2, 3);
		table.add(getLocalizedText("posting_test_demo2", "keys: check, modersmal"), 3, 3);


					 
		table.add(getLocalizedLabel("posting_test_company_type", "Bolagstyp"),1 ,4);
		table.add(getTextInput(
				PARAM_FIELD_COMPANY_TYPE, 
				iwc.isParameterSet(PARAM_FIELD_COMPANY_TYPE) ?
				iwc.getParameter(PARAM_FIELD_COMPANY_TYPE) : ""), 2, 4);
		table.add(getLocalizedText("posting_test_demo3", "keys: kommun, stiftelse, ab, ovr_foretag"), 3, 4);


		table.add(getLocalizedLabel("posting_test_com_bel_type", "Kommuntillh�righet"),1 ,5);
		table.add(getTextInput(
				PARAM_FIELD_COM_BELONGING, 
				iwc.isParameterSet(PARAM_FIELD_COM_BELONGING) ?
				iwc.getParameter(PARAM_FIELD_COM_BELONGING) : ""), 2, 5);
		table.add(getLocalizedText("posting_test_demo4", "keys: nacka, ej_nacka"), 3, 5);

		table.add(getLocalizedButton(PARAM_BUTTON_SEARCH, "posting_test_search", "S�k"), 2, 6);

		mainForm.add(table);		
	}

	private PostingParameters searchPostingParameter(IWContext iwc) {
		PostingBusiness pBiz;
		PostingParameters pp = null;
		try {
			int postingID = 0;
			pBiz = getPostingBusiness(iwc);
			
			pp = (PostingParameters) 
				pBiz.getPostingParameter(
						parseDate(iwc.getParameter(PARAM_FIELD_DATE)),
						iwc.getParameter(PARAM_FIELD_ACTIVITY),
						iwc.getParameter(PARAM_FIELD_REGSPEC),
						iwc.getParameter(PARAM_FIELD_COMPANY_TYPE),
						iwc.getParameter(PARAM_FIELD_COM_BELONGING)
				);
			
		} catch (PostingParametersException e) {
			_errorMessage = localize(e.getTextKey(), e.getDefaultText());
		} catch (RemoteException e) {
		}
		return pp;
	}

	private void prepareMainTable(IWContext iwc) {
		mainForm = new Form();
	}
	
	private RegulationsBusiness getRegulationsBusiness(IWContext iwc) throws RemoteException {
		return (RegulationsBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, RegulationsBusiness.class);
	}
	private PostingBusiness getPostingBusiness(IWContext iwc) throws RemoteException {
		return (PostingBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, PostingBusiness.class);
	}
}