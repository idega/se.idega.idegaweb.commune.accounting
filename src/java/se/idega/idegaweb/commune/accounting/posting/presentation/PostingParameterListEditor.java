/*
 * $Id: PostingParameterListEditor.java,v 1.30 2003/10/21 23:22:50 kjell Exp $
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
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.core.builder.data.ICPage;
import com.idega.user.data.User;
import com.idega.builder.business.BuilderLogic;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ExceptionWrapper;
import com.idega.util.IWTimestamp;
import com.idega.block.school.business.SchoolBusiness;

import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.presentation.ListTable;
import se.idega.idegaweb.commune.accounting.presentation.ApplicationForm;
import se.idega.idegaweb.commune.accounting.presentation.ButtonPanel;
import se.idega.idegaweb.commune.accounting.posting.data.PostingParameters;
import se.idega.idegaweb.commune.accounting.posting.data.PostingField;
import se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingBusiness;
import se.idega.idegaweb.commune.accounting.posting.business.PostingParametersException;


/**
 * PostingParameterListEdit is an idegaWeb block that handles maintenance of some 
 * default data thatis used in a "posting". The block shows/edits Period, Activity, Regulation specs, 
 * Company types and Commune belonging. 
 * It handles posting variables for both own and double entry accounting
 *  
 * <p>
 * $Id: PostingParameterListEditor.java,v 1.30 2003/10/21 23:22:50 kjell Exp $
 *
 * @author <a href="http://www.lindman.se">Kjell Lindman</a>
 * @version $Revision: 1.30 $
 */
public class PostingParameterListEditor extends AccountingBlock {

	private final static int ACTION_DEFAULT = 0;
	private final static int ACTION_SAVE = 2;
	private final static int ACTION_CANCEL = 3;

	private final static String KEY_SAVE = "posting_parm_edit.save";
	private final static String KEY_CANCEL = "posting_parm_edit.cancel";

	private final static String KEY_ACTIVITY_HEADER_ONE = "posting_parm_edit.activity_headerone";
	private final static String KEY_ACTIVITY_HEADER_TWO = "posting_parm_edit.activity_headertwo";
	private final static String KEY_REGSPEC_HEADER_ONE = "regulation_specification_headerone";
	private final static String KEY_REGSPEC_HEADER_TWO = "regulation_specification_headertwo";
	private final static String KEY_COMPANY_TYPE_HEADER_ONE = "posting_parm_edit.company_type_headerone";
	private final static String KEY_COMPANY_TYPE_HEADER_TWO = "posting_parm_edit.company_type_headertwo";
	private final static String KEY_COM_BEL_HEADER_ONE = "posting_parm_edit.com_bel_headerone";
	private final static String KEY_COM_BEL_HEADER_TWO = "posting_parm_edit.com_bel_headertwo";

	private final static String KEY_HEADER = "posting_parm_edit.header";
	private final static String KEY_HEADER_OWN_ENTRY = "posting_parm_edit.header_own_entry";
	private final static String KEY_HEADER_DOUBLE_ENTRY = "posting_parm_edit.header_double_entry";
	private final static String KEY_FROM_DATE = "posting_parm_edit.from_date";
	private final static String KEY_TO_DATE = "posting_parm_edit.to_date";
	private final static String KEY_CHANGE_DATE = "posting_parm_edit.change_date";
	private final static String KEY_CHANGE_SIGN = "posting_parm_edit.change_sign";
	private final static String KEY_CONDITIONS = "posting_def_edit.conditions";
	private final static String KEY_ACTIVITY = "posting_parm_edit.activity";
	private final static String KEY_REG_SPEC = "posting_parm_edit.reg_spec";
	private final static String KEY_COMPANY_TYPE = "posting_parm_edit.company_type";
	private final static String KEY_COMMUNE_BELONGING = "posting_parm_edit.commune_belonging";
	private final static String KEY_SCHOOL_YEAR = "posting_parm_edit.school_yearfrom";
	private final static String KEY_SCHOOL_YEAR_TO = "posting_parm_edit.school_year_to";
	private final static String KEY_SCHOOL_YEAR_SELECTOR_BLANK = "posting_parm_edit.school_yer_selector_blank";
	private final static String KEY_ERROR_DATE_NULL	= "posting_parm_edit.error_date";
	private final static String KEY_NUMERIC = "posting_parm_edit.numeric_only";
	private final static String KEY_ALPHA = "posting_parm_edit.alpha_only";
	
	private final static String PARAM_BUTTON_SAVE = "button_save";
	private final static String PARAM_BUTTON_CANCEL = "button_cancel";
	
	private final static String PARAM_EDIT_ID = "param_edit_id";
	private final static String PARAM_PERIOD_FROM = "pp_edit_periode_from";
	private final static String PARAM_PERIOD_TO = "pp_edit_period_to";
	private final static String PARAM_SIGNED = "pp_edit_signed";
	private final static String PARAM_MODE_COPY = "mode_copy";

	private final static String PARAM_OWN_STRING = "own_string";
	private final static String PARAM_DOUBLE_STRING = "double_string";

	private final static String PARAM_SELECTOR_ACTIVITY = "selector_activity";
	private final static String PARAM_SELECTOR_REGSPEC = "selector_regspec";
	private final static String PARAM_SELECTOR_COMPANY_TYPE = "selector_company_type";
	private final static String PARAM_SELECTOR_COM_BELONGING = "selector_com_belonging";
	private final static String PARAM_SELECTOR_SCHOOL_YEAR1 = "selector_school_year1";
	private final static String PARAM_SELECTOR_SCHOOL_YEAR2 = "selector_school_year2";
	
	
	private ICPage _responsePage;
	private String _errorText = "";
	private String _theOwnString = "";
	private String _theDoubleString = "";
	private Map _pMap;
	
	public void setResponsePage(ICPage page) {
		_responsePage = page;
	}

	public ICPage getResponsePage() {
		return _responsePage;
	}
	
	/**
	 * Handles all of the blocks presentation.
	 * @param iwc user/session context 
	 */
	public void init(final IWContext iwc) {
		try {
			int action = parseAction(iwc);
			setDefaultParameters();
			switch (action) {
				case ACTION_DEFAULT :
					viewMainForm(iwc, "");
					break;
				case ACTION_SAVE :
					if (!saveData(iwc)) {
						viewMainForm(iwc, _errorText);
					}
					break;
				case ACTION_CANCEL :
					closeMe(iwc);
					break;
			}
		}
		catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}
	}
		 
	private boolean saveData(IWContext iwc) {

		_pMap.clear();
		_pMap.put(PARAM_PERIOD_FROM, iwc.getParameter(PARAM_PERIOD_FROM));
		_pMap.put(PARAM_PERIOD_TO, iwc.getParameter(PARAM_PERIOD_TO));
		_pMap.put(PARAM_SIGNED, iwc.getParameter(PARAM_SIGNED));
		_pMap.put(PARAM_SELECTOR_ACTIVITY, iwc.getParameter(PARAM_SELECTOR_ACTIVITY));				
		_pMap.put(PARAM_SELECTOR_REGSPEC, iwc.getParameter(PARAM_SELECTOR_REGSPEC));		
		_pMap.put(PARAM_SELECTOR_COMPANY_TYPE, iwc.getParameter(PARAM_SELECTOR_COMPANY_TYPE));					
		_pMap.put(PARAM_SELECTOR_COM_BELONGING, iwc.getParameter(PARAM_SELECTOR_COM_BELONGING));
		_pMap.put(PARAM_SELECTOR_SCHOOL_YEAR1, iwc.getParameter(PARAM_SELECTOR_SCHOOL_YEAR1));
		_pMap.put(PARAM_SELECTOR_SCHOOL_YEAR2, iwc.getParameter(PARAM_SELECTOR_SCHOOL_YEAR2));
		
		addTempFieldParameters(iwc, parseDate(iwc.getParameter(PARAM_PERIOD_FROM)));
		
		try {
			generateStrings(iwc);
			String id = null;
			if (iwc.isParameterSet(PARAM_EDIT_ID)) {
				id = iwc.getParameter(PARAM_EDIT_ID);
			}
			if (iwc.isParameterSet(PARAM_MODE_COPY)) {
				id = null;
			}
			getPostingBusiness(iwc).savePostingParameter(id,
			parseDate(iwc.getParameter(PARAM_PERIOD_FROM)),
			parseDate(iwc.getParameter(PARAM_PERIOD_TO)),
			iwc.getParameter(PARAM_SIGNED),
			iwc.getParameter(PARAM_SELECTOR_ACTIVITY),				
			iwc.getParameter(PARAM_SELECTOR_REGSPEC),					
			iwc.getParameter(PARAM_SELECTOR_COMPANY_TYPE),					
			iwc.getParameter(PARAM_SELECTOR_COM_BELONGING),
			iwc.getParameter(PARAM_SELECTOR_SCHOOL_YEAR1),
			iwc.getParameter(PARAM_SELECTOR_SCHOOL_YEAR2),
			_theOwnString,
			_theDoubleString
			);
		} catch (PostingParametersException e) {
			_errorText = localize(e.getTextKey(), e.getDefaultText());
			return false;
		} catch (RemoteException e) {
			super.add(new ExceptionWrapper(e, this));
			return false;
		}
		
		closeMe(iwc);
		return true;
	}

	public void generateStrings(IWContext iwc) throws PostingParametersException {

		_theOwnString = "";
		_theDoubleString = "";
		
		try {
			int index = 1;
			PostingBusiness pBiz = getPostingBusiness(iwc);
			Date date = null;
			String dateString = iwc.getParameter(PARAM_PERIOD_FROM);
			if (dateString == null) {
				date = new Date(System.currentTimeMillis()); 
			} else {
				date = parseDate(dateString);
			}
			if (date == null) {
				throw new PostingParametersException(KEY_ERROR_DATE_NULL, "Datum saknas");			
			}
			Collection fields = pBiz.getAllPostingFieldsByDate(date);
			Iterator iter = fields.iterator();
			while (iter.hasNext()) {
				PostingField field = (PostingField) iter.next();
				_theOwnString += pBiz.pad(iwc.getParameter(PARAM_OWN_STRING + "_" + index), field);
				_theDoubleString += pBiz.pad(iwc.getParameter(PARAM_DOUBLE_STRING + "_" + index), field);
				index++;
			}
			
		} catch (RemoteException e) {
		}
	}

	/**
	 * Returns the own posting string.
	 */
	public String getOwnPosting() {
		return _theOwnString;
	}

	/**
	 * Returns the double posting string.
	 */
	public String getDoublePosting() {
		return _theDoubleString;
	}

	private void closeMe(IWContext iwc) {
		String backUrl = BuilderLogic.getInstance().getIBPageURL(iwc, ((Integer)_responsePage.getPrimaryKey()).intValue());
		backUrl += 	"&"	+ PostingParameterList.PARAM_RETURN_FROM_DATE + "=" + 
						iwc.getParameter(PostingParameterList.PARAM_RETURN_FROM_DATE)+
					"&"	+ PostingParameterList.PARAM_RETURN_TO_DATE + "=" + 
						iwc.getParameter(PostingParameterList.PARAM_RETURN_TO_DATE);
		getParentPage().setToRedirect(backUrl);
	}
	 
	/*
	 * Returns the action constant for the action to perform based 
	 * on the POST parameters in the specified context.
	 */
	private int parseAction(IWContext iwc) {
		int action = ACTION_DEFAULT;
		if (iwc.isParameterSet(PARAM_BUTTON_SAVE)) {
		  action = ACTION_SAVE;
		}
		if (iwc.isParameterSet(PARAM_BUTTON_CANCEL)) {
		  action = ACTION_CANCEL;
		}
		return action;
	}
	/*
	 * Adds the default form to the block.
    */	
	private void viewMainForm(IWContext iwc, String error) {
		ApplicationForm app = new ApplicationForm(this);
		PostingParameters pp = getThisPostingParameter(iwc);
		
		Table topPanel = getTopPanel(iwc, pp);		
		Table postingForm = getPostingForm(iwc, pp);
					
		ButtonPanel buttonPanel = new ButtonPanel(this);
		buttonPanel.addLocalizedButton(PARAM_BUTTON_SAVE, KEY_SAVE, "Spara");
		buttonPanel.addLocalizedButton(PARAM_BUTTON_CANCEL, KEY_CANCEL, "Avbryt");
		
		app.setLocalizedTitle(KEY_HEADER, "Skapa/�ndra konteringlista");
		app.setSearchPanel(topPanel);
		app.setMainPanel(postingForm);
		app.setButtonPanel(buttonPanel);
		if(iwc.isParameterSet(PostingParameterList.PARAM_RETURN_FROM_DATE)) {
			app.addHiddenInput(PostingParameterList.PARAM_RETURN_FROM_DATE, iwc.getParameter(PostingParameterList.PARAM_RETURN_FROM_DATE));
		}
		if(iwc.isParameterSet(PostingParameterList.PARAM_RETURN_TO_DATE)) {
			app.addHiddenInput(PostingParameterList.PARAM_RETURN_TO_DATE, iwc.getParameter(PostingParameterList.PARAM_RETURN_TO_DATE));
		}
		add(app);		
	}

	/*
	 * Returns a top panel consisting of from, to and changing dates + signature 
	 * @param iwc user/session context 
	 * @param pp PostingParameter 
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters
	 * @return populated table
	 */
	private Table getTopPanel(IWContext iwc, PostingParameters pp) {

		Table table = new Table();
		table.setWidth("75%");
		int row = 1;
		String userName = "";
		User user = iwc.getCurrentUser();
		
		if (user != null) {
			userName = user.getFirstName();
		}

		Timestamp rightNow = IWTimestamp.getTimestampRightNow();
		Date dd = new Date(System.currentTimeMillis());

		String from = formatDate(pp != null ? pp.getPeriodeFrom() : dd, 4);
		String to = formatDate(pp != null ? pp.getPeriodeTo() : dd, 4);
		if(hasError()) {
			if (_errorText.length() != 0) {
				table.add(getErrorText(_errorText), 1, row);
				table.mergeCells(1, row, 4, row);
				row++;
			}
		}

		table.add(getLocalizedLabel(KEY_FROM_DATE, "Fr�n datum"),1 ,row);
		table.add(getTextInput(PARAM_PERIOD_FROM, 
				(pp != null ? from : 
				(String) _pMap.get(PARAM_PERIOD_FROM)), 40, 4), 2, row);
	
		table.add(getLocalizedLabel(KEY_TO_DATE, "Tom datum"),3 ,row);
		table.add(getTextInput(PARAM_PERIOD_TO, 
				(pp != null ? to : 
				(String) _pMap.get(PARAM_PERIOD_TO)), 40, 4), 4, row);
		
		row++;

		table.add(getLocalizedLabel(KEY_CHANGE_DATE, "�ndringsdatum"),1 ,row);
		String dt = formatDate(pp != null ? pp.getChangedDate(): rightNow, 6);
		table.add(getText(pp != null ? dt : ""), 2, row);

		table.add(getLocalizedLabel(KEY_CHANGE_SIGN, "�ndringssignatur"),3 ,row);
		table.add(getText(pp != null ? pp.getChangedSign() : ""), 4, row);
		table.add(new HiddenInput(PARAM_SIGNED, ""+userName));
		if (iwc.isParameterSet(PARAM_MODE_COPY)) {
			table.add(new HiddenInput(PARAM_MODE_COPY, ""+iwc.getParameter(PARAM_MODE_COPY)));
		}
		if (iwc.isParameterSet(PARAM_EDIT_ID)) {
			table.add(new HiddenInput(PARAM_EDIT_ID, ""+iwc.getParameter(PARAM_EDIT_ID)));
		}
		return table;	
	}
	
	/*
	 * Generates the main posting form with selectors for Activity, Regulation specs, Company types
	 * and Commune belonging type.
	 * It contains edit fields for "own entry" account as well as "double entry" accounts
	 *    
	 * @param dt user/session context 
	 * @param pp PostingParameter 
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters
	 * @return main posting table
	 */
	private Table getPostingForm(IWContext iwc, PostingParameters pp) {

		Table table = new Table();
		Table selectors = new Table();

		try {
			int actPK = Integer.parseInt((String) _pMap.get(PARAM_SELECTOR_ACTIVITY));
			int regPK = Integer.parseInt((String) _pMap.get(PARAM_SELECTOR_REGSPEC));
			String comPK = (String) _pMap.get(PARAM_SELECTOR_COMPANY_TYPE);
			int comBelPK = Integer.parseInt((String) _pMap.get(PARAM_SELECTOR_REGSPEC));
			int schoolYearPK1 = Integer.parseInt((String) _pMap.get(PARAM_SELECTOR_SCHOOL_YEAR1));
			int schoolYearPK2 = Integer.parseInt((String) _pMap.get(PARAM_SELECTOR_SCHOOL_YEAR2));
			
			if (pp != null) {
				actPK = Integer.parseInt(pp.getActivity() != null ? 
						pp.getActivity().getPrimaryKey().toString() : "0");	
				regPK = Integer.parseInt(pp.getRegSpecType() != null ? 
						pp.getRegSpecType().getPrimaryKey().toString() : "0");
				comPK = pp.getCompanyType() != null ? 
						pp.getCompanyType().getPrimaryKey().toString() : "0";
				comBelPK = Integer.parseInt(pp.getCommuneBelonging() != null ? 
						pp.getCommuneBelonging().getPrimaryKey().toString() : "0");
				schoolYearPK1 = Integer.parseInt(pp.getSchoolYear1() != null ? 
						pp.getSchoolYear1().getPrimaryKey().toString() : "0");
				schoolYearPK2 = Integer.parseInt(pp.getSchoolYear2() != null ? 
						pp.getSchoolYear2().getPrimaryKey().toString() : "0");				
			}
			selectors.add(getLocalizedLabel(KEY_ACTIVITY, "Verksamhet"), 1, 1);
			selectors.add(activitySelector(iwc, PARAM_SELECTOR_ACTIVITY, actPK), 2, 1);
						
			selectors.add(getLocalizedLabel(KEY_REG_SPEC, "Regelspec.typ"), 1, 2);
			selectors.add(regSpecSelector(iwc, PARAM_SELECTOR_REGSPEC, regPK), 2, 2);
	
			selectors.add(getLocalizedLabel(KEY_COMPANY_TYPE, "Bolagstyp"), 1, 3);
			selectors.add(companyTypeSelector(iwc, PARAM_SELECTOR_COMPANY_TYPE, comPK), 2, 3);
	
			selectors.add(getLocalizedLabel(KEY_COMMUNE_BELONGING, "Kommuntillh�righet:"), 1, 4);
			selectors.add(communeBelongingSelector(iwc, PARAM_SELECTOR_COM_BELONGING, comBelPK), 2, 4);

			selectors.add(getLocalizedLabel(KEY_SCHOOL_YEAR, "Skol�r fr om"), 1, 5);
			selectors.add(schoolYearSelector(iwc, PARAM_SELECTOR_SCHOOL_YEAR1, schoolYearPK1), 2, 5);
			selectors.add(getLocalizedLabel(KEY_SCHOOL_YEAR_TO, "t o m"), 3, 5);
			selectors.add(schoolYearSelector(iwc, PARAM_SELECTOR_SCHOOL_YEAR2, schoolYearPK2), 4, 5);

		} catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}	

		
		table.add(getLocalizedLabel(KEY_CONDITIONS, "Villkor"), 1, 1);
		table.add(selectors, 1, 2);
		String postingString = null;
		String doublePostingString = null;
		if (pp != null) {
			postingString = pp.getPostingString();
			doublePostingString = pp.getDoublePostingString();
		}
		table.add(getPostingParameterForm(iwc, pp, postingString, doublePostingString), 1, 3);
		
		return table;
	}

	
	/*
	 * Returns the posting form with selectors edit fields 
	 * for "own entry" account as well as "double entry" accounts
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters
	 */
	protected Table getPostingParameterForm(IWContext iwc, PostingParameters pp, String postingString, String doublePostingString) {
		Table accounts = new Table();
		ListTable list1 = null;
		ListTable list2 = null;
		
		try {
			int index = 1;
			PostingBusiness pBiz = getPostingBusiness(iwc);
			Date defaultDate;
			if (pp == null) {
				defaultDate = new Date(System.currentTimeMillis());
			} else  {
				defaultDate = pp.getPeriodeFrom();
			}
			Collection fields = pBiz.getAllPostingFieldsByDate(defaultDate);
			if (fields == null) {
				return accounts;
			}
			int size = fields.size();
			list1 = new ListTable(this, size);
			list2 = new ListTable(this, size);
			Iterator iter = fields.iterator();
			int readPointer = 0;
			while (iter.hasNext()) {
				PostingField field = (PostingField) iter.next();
				list1.setHeader(field.getFieldTitle(), index);
				list2.setHeader(field.getFieldTitle(), index);

				int fieldLength = field.getLen();
				String theData1 = "";
				String theData2 = "";
				if(iwc.isParameterSet(PARAM_OWN_STRING+"_"+index)) {
					theData1 = (String) _pMap.get(PARAM_OWN_STRING+"_"+index);
				}
				if(iwc.isParameterSet(PARAM_DOUBLE_STRING+"_"+index)) {
					theData2 = (String) _pMap.get(PARAM_DOUBLE_STRING+"_"+index);
				}
				if (postingString != null) {
					theData1 = pBiz.extractField(postingString,readPointer, fieldLength, field);
					theData2 = pBiz.extractField(doublePostingString,readPointer, fieldLength, field);
				}
				readPointer += fieldLength;
				list1.add(inputTextFieldValidation(
						getTextInput(PARAM_OWN_STRING+"_"+index, theData1, 80, field.getLen()),
						field
				));
				list2.add(inputTextFieldValidation(
						getTextInput(PARAM_DOUBLE_STRING+"_"+index, theData2, 80, field.getLen()),
						field
						));
				index++;
			}
			
		} catch (RemoteException e) {
		}
		accounts.add(getLocalizedText(KEY_HEADER_OWN_ENTRY, "Egen kontering"), 1, 1);
		accounts.add(list1, 1, 2);
		accounts.add(getLocalizedText(KEY_HEADER_DOUBLE_ENTRY, "Mot kontering"), 1, 3);
		accounts.add(list2, 1, 4);
				
		return accounts;
	}


	private void addTempFieldParameters(IWContext iwc, Date defaultDate) {
		
		try {
			int index = 1;
			PostingBusiness pBiz = getPostingBusiness(iwc);
			Collection fields = pBiz.getAllPostingFieldsByDate(defaultDate);
			if (fields == null) {
				return;
			}
			Iterator iter = fields.iterator();
			while (iter.hasNext()) {
				iter.next();
				_pMap.put(PARAM_OWN_STRING+"_"+index, iwc.getParameter(PARAM_OWN_STRING+"_"+index)); 
				_pMap.put(PARAM_DOUBLE_STRING+"_"+index, iwc.getParameter(PARAM_DOUBLE_STRING+"_"+index)); 
				index++;
			}
		} catch (RemoteException e) {
		}
		
	}

	private void setDefaultParameters() {
		//String ds = formatDate(new Date(System.currentTimeMillis()), 4);
		if(_pMap == null) {
			_pMap = new HashMap();
		}
		if(!_pMap.containsKey(PARAM_PERIOD_FROM)) {
			_pMap.put(PARAM_PERIOD_FROM, "" );
			_pMap.put(PARAM_PERIOD_TO, "");
			_pMap.put(PARAM_SELECTOR_ACTIVITY, "0");
			_pMap.put(PARAM_SELECTOR_REGSPEC, "0");
			_pMap.put(PARAM_SELECTOR_COMPANY_TYPE, "0");
			_pMap.put(PARAM_SELECTOR_SCHOOL_YEAR1, "0");
			_pMap.put(PARAM_SELECTOR_SCHOOL_YEAR2, "0");
		}	
	}

	/*
	 * formats the textinput and sets restrictions on inputs depending on field type
	 */
	private TextInput inputTextFieldValidation(TextInput ti, PostingField field) {
		
		if (field.isAlpha()) {
			ti.setAsAlphabeticText(localize(KEY_ALPHA, "Endast bokst�ver till�tna"));
		}
		if (field.isNumeric()) {
			ti.setAsFloat(localize(KEY_NUMERIC, "Endast siffror till�tna"));
		}
		return ti;
	}
	
	/*
	 * Retrives from business the current posting data that is pointed out by PARAM_EDIT_ID.
	 * Remeber that this app only can edit one record at a time.
	 *    
	 * @param iwc Idega Web Context 
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters
	 * @return PostingParameter loaded with data
	 */
	protected PostingParameters getThisPostingParameter(IWContext iwc) {
		PostingBusiness pBiz;
		PostingParameters pp = null;
		try {
			int postingID = 0;
			
			if (iwc.isParameterSet(PARAM_EDIT_ID)) {
				postingID = Integer.parseInt(iwc.getParameter(PARAM_EDIT_ID));
			}
			
			pBiz = getPostingBusiness(iwc);
			pp = (PostingParameters) pBiz.findPostingParameter(postingID);
			
		} catch (Exception e) {
			super.add(new ExceptionWrapper(e, this));
		}	
		return pp;
	}

	/*
	 * Generates a DropDownSelector for school years that is collected from the school business 
	 *    
	 * @param iwc Idega Web Context 
	 * @param name HTML Parameter ID for this selector
	 * @param refIndex The initial position to set the selector to 
	 * @see com.idega.block.school.data.SchoolType#
	 * @return the drop down menu
	 */
	private DropdownMenu schoolYearSelector(IWContext iwc, String name, int refIndex) throws Exception {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(
					getDropdownMenuLocalized(name, getSchoolBusiness(iwc).findAllSchoolYears(), 
					"getLocalizationKey"));
		menu.addMenuElementFirst("0", localize(KEY_SCHOOL_YEAR_SELECTOR_BLANK, "Inget"));
		menu.setSelectedElement(refIndex);
		return menu;
	}

	
	/*
	 * Generates a DropDownSelector for activites that is collected from the regulation framework. 
	 *    
	 * @param iwc Idega Web Context 
	 * @param name HTML Parameter ID for this selector
	 * @param refIndex The initial position to set the selector to 
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.ActivityType
	 * @return the drop down menu
	 */
	private DropdownMenu activitySelector(IWContext iwc, String name, int refIndex) throws Exception {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(
					getDropdownMenuLocalized(name, getRegulationsBusiness(iwc).findAllActivityTypes(), 
					"getLocalizationKey"));
		menu.addMenuElementFirst("0", localize(KEY_ACTIVITY_HEADER_TWO, "Ingen"));
		menu.addMenuElementFirst("0", localize(KEY_ACTIVITY_HEADER_ONE, "V�lj Verksamhet"));
		menu.setSelectedElement(refIndex);
		return menu;
	}
	
	/*
	 * Generates a DropDownSelector for Regulation specifications that is collected 
	 * from the regulation framework. 
	 *    
	 * @param iwc Idega Web Context 
	 * @param name HTML Parameter ID for this selector
	 * @param refIndex The initial position to set the selector to 
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType
	 * @return the drop down menu
	 */
	private DropdownMenu regSpecSelector(IWContext iwc, String name, int refIndex) throws Exception {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(
					getDropdownMenuLocalized(name, getRegulationsBusiness(iwc).findAllRegulationSpecTypes(), 
					"getLocalizationKey"));
		menu.addMenuElementFirst("0", localize(KEY_REGSPEC_HEADER_TWO, "Ingen"));
		menu.addMenuElementFirst("0", localize(KEY_REGSPEC_HEADER_ONE, "V�lj Regelspec. typ"));
		menu.setSelectedElement(refIndex);
		return menu;
	}

	/*
	 * Generates a DropDownSelector for Company types that is collected 
	 * from the regulation framework. 
	 *    
	 * @param iwc Idega Web Context 
	 * @param name HTML Parameter ID for this selector
	 * @param refIndex The initial position to set the selector to 
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.CompanyType
	 * @return the drop down menu
	 */
	private DropdownMenu companyTypeSelector(IWContext iwc, String name, String refIndex) throws Exception {
			DropdownMenu menu = (DropdownMenu) getStyledInterface(
					getDropdownMenuLocalized(name, getRegulationsBusiness(iwc).findAllCompanyTypes(), 
					"getLocalizedKey"));
		menu.addMenuElementFirst("0", localize(KEY_COMPANY_TYPE_HEADER_TWO, "Ingen"));
		menu.addMenuElementFirst("0", localize(KEY_COMPANY_TYPE_HEADER_ONE, "V�lj Bolagstyp"));
		menu.setSelectedElement(refIndex);
		return menu;
	}

	/*
	 * Generates a DropDownSelector for Commune belongings that is collected 
	 * from the regulation framework. 
	 *    
	 * @param iwc Idega Web Context 
	 * @param name HTML Parameter ID for this selector
	 * @param refIndex The initial position to set the selector to 
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.CommuneBelongingType
	 * @return the drop down menu
	 */
	private DropdownMenu communeBelongingSelector(IWContext iwc, String name, int refIndex) throws Exception {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(
				getDropdownMenuLocalized(name, getRegulationsBusiness(iwc).findAllCommuneBelongingTypes(), 
				"getLocalizationKey"));
		menu.addMenuElementFirst("0", localize(KEY_COM_BEL_HEADER_TWO, "Ingen"));
		menu.addMenuElementFirst("0", localize(KEY_COM_BEL_HEADER_ONE, "V�lj Kommuntillh�righet"));
		menu.setSelectedElement(refIndex);
		return menu;
	}

	private boolean hasError() {
		return _errorText.length() == 0 ? false : true;
	}

	private SchoolBusiness getSchoolBusiness(IWContext iwc) throws RemoteException {
		return (SchoolBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
	}
	private RegulationsBusiness getRegulationsBusiness(IWContext iwc) throws RemoteException {
		return (RegulationsBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, RegulationsBusiness.class);
	}

	private PostingBusiness getPostingBusiness(IWContext iwc) throws RemoteException {
		return (PostingBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, PostingBusiness.class);
	}
}


