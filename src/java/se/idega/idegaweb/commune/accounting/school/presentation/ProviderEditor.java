/*
 * $Id: ProviderEditor.java,v 1.18 2003/10/08 08:53:52 anders Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.school.presentation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.rmi.RemoteException;
import java.sql.Date;

import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.CountryDropdownMenu;

import com.idega.core.builder.data.ICPage;

import com.idega.core.location.data.Commune;
import com.idega.core.location.data.CommuneHome;
import com.idega.core.location.data.Country;
import com.idega.core.location.data.CountryHome;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolArea;
import com.idega.block.school.data.SchoolType;
import com.idega.block.school.data.SchoolYear;
import com.idega.block.school.data.SchoolManagementType;

import se.idega.idegaweb.commune.accounting.school.data.Provider;
import se.idega.idegaweb.commune.accounting.school.data.ProviderStatisticsType;
import se.idega.idegaweb.commune.accounting.school.data.ProviderStatisticsTypeHome;
import se.idega.idegaweb.commune.accounting.school.business.ProviderBusiness;
import se.idega.idegaweb.commune.accounting.school.business.ProviderException;

import se.idega.idegaweb.commune.accounting.regulations.data.ProviderType;
import se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness;

import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.presentation.ApplicationForm;
import se.idega.idegaweb.commune.accounting.presentation.ListTable;
import se.idega.idegaweb.commune.accounting.presentation.ButtonPanel;

/** 
 * AgeEditor is an idegaWeb block that handles age values and
 * age regulations for children in childcare.
 * <p>
 * Last modified: $Date: 2003/10/08 08:53:52 $ by $Author: anders $
 *
 * @author Anders Lindman
 * @version $Revision: 1.18 $
 */
public class ProviderEditor extends AccountingBlock {

	private final static int ACTION_DEFAULT = 0;
	private final static int ACTION_CANCEL = 1;
	private final static int ACTION_NEW = 3;
	private final static int ACTION_OPEN = 4;
	private final static int ACTION_SAVE = 5;
	private final static int ACTION_DELETE = 6;
	
	private final static String PP = "cacc_provider_editor_"; // Parameter prefix 

	private final static String PARAMETER_SCHOOL_ID = PP + "school_id";
	private final static String PARAMETER_NAME = PP + "name";
	private final static String PARAMETER_ADDRESS = PP + "address";
	private final static String PARAMETER_ZIP_CODE = PP + "zip_code";
	private final static String PARAMETER_ZIP_AREA = PP + "zip_area";
	private final static String PARAMETER_PHONE = PP + "phone";
	private final static String PARAMETER_INFO = PP + "info";
	private final static String PARAMETER_KEY_CODE = PP + "key_code";
	private final static String PARAMETER_LONGITUDE = PP + "longitude";
	private final static String PARAMETER_LATITUDE = PP + "latitude";	
	private final static String PARAMETER_SCHOOL_AREA_ID = PP + "school_area_id";	
	private final static String PARAMETER_SCHOOL_TYPE_ID = PP + "school_type_id";	
	private final static String PARAMETER_SCHOOL_YEAR_ID = PP + "school_year_id";	
	private final static String PARAMETER_ORGANIZATION_NUMBER = PP + "organization_number";
	private final static String PARAMETER_EXTRA_PROVIDER_ID = PP + "extra_provider_id";
	private final static String PARAMETER_PROVIDER_TYPE_ID = PP + "provider_type_id";
	private final static String PARAMETER_SCHOOL_MANAGEMENT_TYPE_ID = PP + "school_management_type_id";
	private final static String PARAMETER_TERMINATION_DATE = PP + "termination_date";
	private final static String PARAMETER_COMMUNE_ID = PP + "commune_id";
	private final static String PARAMETER_COUNTRY_ID = PP + "country_id";
	private final static String PARAMETER_CENTRALIZED_ADMINISTRATION = PP + "centralized_administration";
	private final static String PARAMETER_PAYMENT_BY_INVOICE = PP + "payment_by_invoice";
	private final static String PARAMETER_POSTGIRO = PP + "postgiro";
	private final static String PARAMETER_BANKGIRO = PP + "bankgiro";
	private final static String PARAMETER_STATISTICS_TYPE = PP + "statistics_type";
	private final static String PARAMETER_DELETE_ID = PP + "delete_id";
	private final static String PARAMETER_NEW = PP + "new";
	private final static String PARAMETER_SAVE = PP + "save";
	private final static String PARAMETER_CANCEL = PP + "cancel";
	private final static String PARAMETER_EDIT = PP + "edit";
	
	private final static String KP = "provider_editor."; // key prefix 
	
	private final static String KEY_TITLE = KP + "title";
	private final static String KEY_TITLE_ADD = KP + "title_add";
	private final static String KEY_TITLE_EDIT = KP + "title_edit";
	private final static String KEY_TITLE_DELETE = KP + "title_delete";
	private final static String KEY_NAME = KP + "name";
	private final static String KEY_ADDRESS = KP + "address";
	private final static String KEY_ZIP_CODE = KP+ "zip_code";
	private final static String KEY_ZIP_AREA = KP + "zip_area";
	private final static String KEY_PHONE = KP + "phone";
	private final static String KEY_INFO = KP + "info";
	private final static String KEY_OPERATIONS = KP + "operations";
	
	private final static String KEY_SCHOOL_AREA = KP + "school_area";
	private final static String KEY_ORGANIZATION_NUMBER = KP + "organization_number";
	private final static String KEY_EXTRA_PROVIDER_ID = KP + "extra_provider_id";
	private final static String KEY_PROVIDER_TYPE = KP + "provider_type";
	private final static String KEY_SCHOOL_MANAGEMENT_TYPE = KP + "school_management_type";
	private final static String KEY_TERMINATION_DATE = KP + "termination_date";
	private final static String KEY_COMMUNE = KP + "commune";
	private final static String KEY_COUNTRY = KP + "country";
	private final static String KEY_CENTRALIZED_ADMINISTRATION = KP + "key_centralized_administration";
	private final static String KEY_PAYMENT_BY_INVOICE = KP + "payment_by_invoice";
	private final static String KEY_POSTGIRO = KP + "postgiro";
	private final static String KEY_BANKGIRO = KP + "bankgiro";
	private final static String KEY_STATISTICS_TYPE = KP + "statistics_type";
	private final static String KEY_SCHOOL_AREA_SELECTOR_HEADER = KP + "school_area_selector_header";
	private final static String KEY_PROVIDER_TYPE_SELECTOR_HEADER = KP + "school_provider_type_selector_header";
	private final static String KEY_SCHOOL_MANAGEMENT_TYPE_SELECTOR_HEADER = KP + "school_management_type_selector_header";
	private final static String KEY_STATISTICS_TYPE_SELECTOR_HEADER = KP + "statistics_type_selector_header";
	private final static String KEY_COMMUNE_SELECTOR_HEADER = KP + "commune_selector_header";
	private final static String KEY_NEW = KP + "new";
	private final static String KEY_SAVE = KP + "save";
	private final static String KEY_CANCEL = KP + "cancel";
	private final static String KEY_EDIT = KP + "edit";
	private final static String KEY_DELETE = KP + "delete";
	private final static String KEY_DELETE_CONFIRM = KP + "delete_confirm_message";
	private final static String KEY_BUTTON_EDIT = KP + "button_edit";
	private final static String KEY_BUTTON_DELETE = KP + "button_delete";	

	/**
	 * @see com.idega.presentation.Block#main()
	 */
	public void init(final IWContext iwc) {
		try {
			int action = parseAction(iwc);
			switch (action) {
				case ACTION_DEFAULT:
					handleDefaultAction(iwc);
					break;
				case ACTION_CANCEL:
					handleDefaultAction(iwc);
					break;
				case ACTION_NEW:
					handleNewAction(iwc);
					break;
				case ACTION_OPEN:
					handleOpenAction(iwc);
					break;
				case ACTION_SAVE:
					handleSaveAction(iwc);
					break;
				case ACTION_DELETE:
					handleDeleteAction(iwc);
					break;
			}
		}
		catch (Exception e) {
			add(new ExceptionWrapper(e, this));
		}
	}

	/*
	 * Returns the action constant for the action to perform based 
	 * on the POST parameters in the specified context.
	 */
	private int parseAction(IWContext iwc) {
		int action = ACTION_DEFAULT;
		
		if (iwc.isParameterSet(PARAMETER_CANCEL)) {
			action = ACTION_CANCEL;
		} else if (iwc.isParameterSet(PARAMETER_NEW)) {
			action = ACTION_NEW;
		} else if (iwc.isParameterSet(PARAMETER_SAVE)) {
			action = ACTION_SAVE;
		} else if (iwc.isParameterSet(PARAMETER_DELETE_ID)) {
			action = ACTION_DELETE;
		} else if (iwc.isParameterSet(PARAMETER_SCHOOL_ID)) {
			action = ACTION_OPEN;
		}

		return action;
	}

	/*
	 * Handles the default action for this block.
	 */	
	private void handleDefaultAction(IWContext iwc) {
		ApplicationForm app = new ApplicationForm(this);
		app.setLocalizedTitle(KEY_TITLE, "Providers");
		app.setSearchPanel(getButtonPanel(iwc));
		app.setMainPanel(getProviderList(iwc));
		add(app);
	}

	/*
	 * Handles the new action for this block.
	 */	
	private void handleNewAction(IWContext iwc) {
		add(getProviderForm(iwc, "-1", "", "", "", "", "", "", "", "", "", "-1", 
				new TreeMap(), "", "", "", "", "", "", "", "", "", "", "", "", null, null, null, true));
	}

	/*
	 * Handles the open action (edit icon clicked in the list) for this block.
	 */	
	private void handleOpenAction(IWContext iwc) {
		try {
			Provider provider = new Provider(getIntParameter(iwc, PARAMETER_SCHOOL_ID));
			School school = provider.getSchool();
			SchoolBusiness sb = getSchoolBusiness(iwc);
			Map schoolTypeMap = new TreeMap();
			Map sts = sb.getSchoolRelatedSchoolTypes(school);
			Map sys = sb.getSchoolRelatedSchoolYears(school);
			if (sts != null) {
				Iterator iter = sts.values().iterator();
				while(iter.hasNext()) {
					SchoolType st = (SchoolType) iter.next();
					int stId = ((Integer) st.getPrimaryKey()).intValue();
					String stIdString = st.getPrimaryKey().toString();
					Map schoolYearMap = new TreeMap();
					if (sys != null) {
						Iterator iter2 = sys.values().iterator();
						while(iter2.hasNext()) {
							SchoolYear sy = (SchoolYear) iter2.next();
							if (sy.getSchoolTypeId() == stId) {
								String syId = sy.getPrimaryKey().toString();
								schoolYearMap.put(syId, syId);
							}
						}
					}
					schoolTypeMap.put(stIdString, schoolYearMap);
				}			
			}
			add(getProviderForm(
					iwc,
					school.getPrimaryKey().toString(),
					school.getSchoolName(),
					school.getSchoolInfo(),
					school.getSchoolAddress(),
					school.getSchoolZipCode(),
					school.getSchoolZipArea(),
					school.getSchoolPhone(),
					school.getSchoolKeyCode(),
					school.getSchoolLatitude(),
					school.getSchoolLongitude(),
					"" + school.getSchoolAreaId(),
					schoolTypeMap,
					school.getOrganizationNumber(),
					school.getExtraProviderId(),
					"" + provider.getProviderTypeId(),
					"" + school.getManagementTypeId(),
					"" + school.getTerminationDate(),
					"" + school.getCommunePK(),
					"" + school.getCountryId(),
					school.getCentralizedAdministration() ? "true" : "",
					provider.getPaymentByInvoice() ? "true" : "",
					provider.getPostgiro(),
					provider.getBankgiro(),
					provider.getStatisticsType(),
					provider.getOwnPosting(),
					provider.getDoublePosting(),
					null,
					false));
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		}
	}

	/*
	 * Handles the save action for this block.
	 */
	private void handleSaveAction(IWContext iwc) {
		String errorMessage = null;
		PostingBlock p = new PostingBlock(null, null);
		p.generateStrings(iwc);
		String ownPosting = p.getOwnPosting();
		String doublePosting = p.getDoublePosting();

		try {			
			ProviderBusiness pb = getProviderBusiness(iwc);
			pb.saveProvider(
					getParameter(iwc, PARAMETER_SCHOOL_ID),
					getParameter(iwc, PARAMETER_NAME),
					getParameter(iwc, PARAMETER_INFO),
					getParameter(iwc, PARAMETER_ADDRESS),
					getParameter(iwc, PARAMETER_ZIP_CODE),
					getParameter(iwc, PARAMETER_ZIP_AREA),
					getParameter(iwc, PARAMETER_PHONE),
					getParameter(iwc, PARAMETER_KEY_CODE),
					getParameter(iwc, PARAMETER_LATITUDE),
					getParameter(iwc, PARAMETER_LONGITUDE),
					getParameter(iwc, PARAMETER_SCHOOL_AREA_ID),
					getSchoolTypeMap(iwc),
					getParameter(iwc, PARAMETER_ORGANIZATION_NUMBER),
					getParameter(iwc, PARAMETER_EXTRA_PROVIDER_ID),
					getParameter(iwc, PARAMETER_PROVIDER_TYPE_ID),
					getParameter(iwc, PARAMETER_SCHOOL_MANAGEMENT_TYPE_ID),
					parseDate(getParameter(iwc, PARAMETER_TERMINATION_DATE)),
					getParameter(iwc, PARAMETER_COMMUNE_ID),
					getParameter(iwc, PARAMETER_COUNTRY_ID),
					getParameter(iwc, PARAMETER_CENTRALIZED_ADMINISTRATION),
					getParameter(iwc, PARAMETER_PAYMENT_BY_INVOICE),
					getParameter(iwc, PARAMETER_POSTGIRO),
					getParameter(iwc, PARAMETER_BANKGIRO),
					getParameter(iwc, PARAMETER_STATISTICS_TYPE),
					ownPosting, 
					doublePosting); 
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
			return;
		} catch (ProviderException e) {
			errorMessage = localize(e.getTextKey(), e.getDefaultText());
		}
		
		if (errorMessage != null) {
			add(getProviderForm(
					iwc,
					getParameter(iwc, PARAMETER_SCHOOL_ID),
					getParameter(iwc, PARAMETER_NAME),
					getParameter(iwc, PARAMETER_INFO),
					getParameter(iwc, PARAMETER_ADDRESS),
					getParameter(iwc, PARAMETER_ZIP_CODE),
					getParameter(iwc, PARAMETER_ZIP_AREA),
					getParameter(iwc, PARAMETER_PHONE),
					getParameter(iwc, PARAMETER_KEY_CODE),
					getParameter(iwc, PARAMETER_LATITUDE),
					getParameter(iwc, PARAMETER_LONGITUDE),
					getParameter(iwc, PARAMETER_SCHOOL_AREA_ID),
					getSchoolTypeMap(iwc),
					getParameter(iwc, PARAMETER_ORGANIZATION_NUMBER),
					getParameter(iwc, PARAMETER_EXTRA_PROVIDER_ID),
					getParameter(iwc, PARAMETER_PROVIDER_TYPE_ID),
					getParameter(iwc, PARAMETER_SCHOOL_MANAGEMENT_TYPE_ID),
					getParameter(iwc, PARAMETER_TERMINATION_DATE),
					getParameter(iwc, PARAMETER_COMMUNE_ID),
					getParameter(iwc, PARAMETER_COUNTRY_ID),
					getParameter(iwc, PARAMETER_CENTRALIZED_ADMINISTRATION),
					getParameter(iwc, PARAMETER_PAYMENT_BY_INVOICE),
					getParameter(iwc, PARAMETER_POSTGIRO),
					getParameter(iwc, PARAMETER_BANKGIRO),
					getParameter(iwc, PARAMETER_STATISTICS_TYPE),
					ownPosting,
					doublePosting,
					errorMessage,
					!iwc.isParameterSet(PARAMETER_EDIT)));
		} else {
			handleDefaultAction(iwc);
		}
	}
	
	/*
	 * Handles the delete action for this block.
	 */	
	private void handleDeleteAction(IWContext iwc) {
		String errorMessage = null;
		try {
			ProviderBusiness pb = getProviderBusiness(iwc);
			pb.deleteProvider(getParameter(iwc, PARAMETER_DELETE_ID));
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		} catch (ProviderException e) {
			errorMessage = localize(e.getTextKey(), e.getDefaultText());
		}

		if (errorMessage != null) {
			ApplicationForm app = new ApplicationForm(this);
			app.setLocalizedTitle(KEY_TITLE_DELETE, "Delete provider");
			Table table = new Table();
			table.setCellpadding(getCellpadding());
			table.setCellspacing(getCellspacing());
			table.add(getErrorText(errorMessage), 1, 1);
			app.setMainPanel(table);
			ButtonPanel bp = new ButtonPanel(this);
			bp.addLocalizedButton(PARAMETER_CANCEL, KEY_CANCEL, "Avbryt");
			app.setButtonPanel(bp);
			add(app);		
		} else {
			handleDefaultAction(iwc);
		}
	}
	 
	/*
	 * Returns the search panel for this block.
	 */
//	private Table getSearchPanel() {
//		Table table = new Table();
//		table.add(getLocalizedLabel(KEY_MAIN_ACTIVITY, "Huvudverksamhet"), 1, 1);
//		table.add(getLocalizedText(KEY_UPPER_SECONDARY_SCHOOL, "Gymnasieskola"), 2, 1);
//		return table;
//	}
	
	/*
	 * Returns the list of providers.
	 */
	private Table getProviderList(IWContext iwc) {
		Collection providers = null;

		try {
			ProviderBusiness pb = getProviderBusiness(iwc);			
			providers = pb.findAllSchools();
		} catch (RemoteException e) {
			Table t = new Table();
			t.add(new ExceptionWrapper(e), 1, 1);
			return t;
		}

		ListTable list = new ListTable(this, 7);
		
		list.setLocalizedHeader(KEY_NAME, "Name", 1);
		list.setLocalizedHeader(KEY_ADDRESS, "Address", 2);
		list.setLocalizedHeader(KEY_ZIP_CODE, "Zip code", 3);
		list.setLocalizedHeader(KEY_ZIP_AREA, "Zip area", 4);
		list.setLocalizedHeader(KEY_PHONE, "Phone", 5);
		list.setLocalizedHeader(KEY_EDIT, "Edit", 6);
		list.setLocalizedHeader(KEY_DELETE, "Delete", 7);

//		list.setColumnWidth(2, "66%");
//		list.setColumnWidth(3, "60");
//		list.setColumnWidth(4, "60");

		if (providers != null) {
			Iterator iter = providers.iterator();
			while (iter.hasNext()) {
				School p = (School) iter.next();
				Link l = getSmallLink(p.getSchoolName());
				l.addParameter(PARAMETER_SCHOOL_ID, p.getPrimaryKey().toString());
				list.add(l);
				list.add(p.getSchoolAddress());
				list.add(p.getSchoolZipCode());
				list.add(p.getSchoolZipArea());
				list.add(p.getSchoolPhone());

				Link edit = new Link(getEditIcon(localize(KEY_BUTTON_EDIT, "Edit this provider")));
				edit.addParameter(PARAMETER_SCHOOL_ID, p.getPrimaryKey().toString());
				list.add(edit);

				SubmitButton delete = new SubmitButton(getDeleteIcon(localize(KEY_DELETE, "Delete")));
				delete.setDescription(localize(KEY_BUTTON_DELETE, "Click here to remove this provider"));
				delete.setValueOnClick(PARAMETER_DELETE_ID, p.getPrimaryKey().toString());
				delete.setSubmitConfirm(localize(KEY_DELETE_CONFIRM, "Do you really want to delete this provider?"));
				list.add(delete);
			}
		}

		Table mainPanel = new Table();
		mainPanel.setCellpadding(0);
		mainPanel.setCellspacing(0);
		mainPanel.add(new HiddenInput(PARAMETER_DELETE_ID, "-1"), 1, 1);
	
		mainPanel.add(list, 1, 1);
		
		return mainPanel;
	}

	/*
	 * Returns the default button panel for this block.
	 */
	private ButtonPanel getButtonPanel(IWContext iwc) {
		ButtonPanel bp = new ButtonPanel(this);
		bp.addLocalizedButton(PARAMETER_NEW, KEY_NEW, "New");
		try {
			ICPage homePage = iwc.getCurrentUser().getHomePage();
			if (homePage == null) {
				homePage = iwc.getCurrentUser().getPrimaryGroup().getHomePage();
			}
			bp.addLocalizedButton(PARAM_CANCEL, KEY_CANCEL, "Cancel", homePage);
		} catch (Exception e) {}
		return bp;
	}
	
	/*
	 * Returns the application form for creating or editing a provider.
	 */
	private ApplicationForm getProviderForm(
			IWContext iwc,
			String schoolId,
			String name,
			String info,
			String address,
			String zipCode,
			String zipArea,
			String phone,
			String keyCode,
			String latitude,
			String longitude,
			String schoolAreaId,
			Map schoolTypeMap,
			String organizationNumber,
			String extraProviderId,
			String providerTypeId,
			String schoolManagementTypeId,
			String terminationDate,
			String communeId,
			String countryId,
			String centralizedAdministration,
			String paymentByInvoice,
			String postgiro,
			String bankgiro,
			String statisticsType,
			String ownPosting,
			String doublePosting,
			String errorMessage,
			boolean isNew) {

		name = name == null ? "" : name;
		address = address == null ? "" : address;
		zipCode = zipCode == null ? "" : zipCode;
		zipArea = zipArea == null ? "" : zipArea;
		phone = phone == null ? "" : phone;
		info = info == null ? "" : info;
		if (info.equals("null")) {
			info = "";
		}		
		keyCode = keyCode == null ? "" : keyCode;
		longitude = longitude == null ? "" : longitude;
		latitude = latitude == null ? "" : latitude;
		organizationNumber = organizationNumber == null ? "" : organizationNumber;
		extraProviderId = extraProviderId == null ? "" : extraProviderId;
		postgiro = postgiro == null ? "" : postgiro;
		bankgiro = bankgiro == null ? "" : bankgiro;
				
		ApplicationForm app = new ApplicationForm(this);
		if (isNew) {
			app.setLocalizedTitle(KEY_TITLE_ADD, "Create new provider");
		} else {
			app.setLocalizedTitle(KEY_TITLE_EDIT, "Edit provider");
		}
		
		Table table = new Table();
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		int row = 1;
		table.add(getSmallHeader(localize(KEY_NAME, "Name")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getTextInput(PARAMETER_NAME, name, 140), 2, row++);
		table.add(getSmallHeader(localize(KEY_ADDRESS, "Address")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getTextInput(PARAMETER_ADDRESS, address, 200), 2, row++);
		table.add(getSmallHeader(localize(KEY_ZIP_CODE, "Zip code")), 1, row);
		table.add(getTextInput(PARAMETER_ZIP_CODE, zipCode, 50), 2, row);
		table.add(getSmallHeader(localize(KEY_ZIP_AREA, "Zip area")), 3, row);
		table.add(getText("&nbsp;&nbsp;&nbsp;&nbsp;"), 3, row);
		table.add(getTextInput(PARAMETER_ZIP_AREA, zipArea, 120), 3, row);
		table.setColumnWidth(4, "200");
		row++;
		table.add(getSmallHeader(localize(KEY_COMMUNE, "Commune")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getCommuneDropdownMenu(PARAMETER_COMMUNE_ID, communeId), 2, row++);
		table.add(getSmallHeader(localize(KEY_COUNTRY, "Country")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getCountryDropdownMenu(PARAMETER_COUNTRY_ID, countryId), 2, row++);
		table.add(getSmallHeader(localize(KEY_PHONE, "Phone")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getTextInput(PARAMETER_PHONE, phone, 100), 2, row++);
		
		table.add(getSmallHeader(localize(KEY_INFO, "Information")), 1, row);
		table.setVerticalAlignment(1, row, Table. VERTICAL_ALIGN_TOP);
		table.mergeCells(2, row, 4, row);
		TextArea infoTextArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_INFO));
		infoTextArea.setColumns(32);
		infoTextArea.setRows(3);
		infoTextArea.setValue(info);
		table.add(infoTextArea, 2, row++);		
		table.add(getSmallHeader(localize(KEY_SCHOOL_AREA, "School area")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getSchoolAreaDropdownMenu(iwc, PARAMETER_SCHOOL_AREA_ID, schoolAreaId), 2, row++);

		row++;
		Table schoolTypeTable = new Table();
		schoolTypeTable.setCellpadding(getCellpadding());
		schoolTypeTable.setCellspacing(getCellspacing());
		Collection schoolTypes = getSchoolTypes(iwc);				
		Iterator iter = schoolTypes.iterator();
		int stRow = 1;
		while (iter.hasNext()) {
			SchoolType st = (SchoolType) iter.next();
			String stId = st.getPrimaryKey().toString();
			CheckBox stCheckBox = new CheckBox(PARAMETER_SCHOOL_TYPE_ID + stId, stId);
			if (schoolTypeMap.get(stId) != null) {
				stCheckBox.setChecked(true);
			}
			schoolTypeTable.add(stCheckBox, 1, stRow);
			schoolTypeTable.add(getSmallHeader(st.getName()), 2, stRow);
			stRow++;
			Table t = getSchoolYearCheckBoxes(iwc, ((Integer) st.getPrimaryKey()).intValue(), (Map) schoolTypeMap.get(stId));
			if (t != null) {
				schoolTypeTable.add(t, 2, stRow);
				stRow++; 
			}
		}
		table.add(getSmallHeader(localize(KEY_OPERATIONS, "Operations")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.setVerticalAlignment(1, row, Table. VERTICAL_ALIGN_TOP);
		table.add(schoolTypeTable, 2, row++);

		table.add(getSmallHeader(localize(KEY_EXTRA_PROVIDER_ID, "Provider id")), 1, row);
		table.add(getTextInput(PARAMETER_EXTRA_PROVIDER_ID, extraProviderId, 100), 2, row);
		table.add(getSmallHeader(localize(KEY_ORGANIZATION_NUMBER, "Organization number")), 3, row);
		table.add(getText("&nbsp;&nbsp;&nbsp;&nbsp;"), 3, row);
		table.add(getTextInput(PARAMETER_ORGANIZATION_NUMBER, organizationNumber, 100), 3, row++);
		table.add(getSmallHeader(localize(KEY_PROVIDER_TYPE, "Provider type")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getProviderTypeDropdownMenu(iwc, PARAMETER_PROVIDER_TYPE_ID, providerTypeId), 2, row++);
		table.add(getSmallHeader(localize(KEY_SCHOOL_MANAGEMENT_TYPE, "School management type")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getSchoolManagementTypeDropdownMenu(iwc, PARAMETER_SCHOOL_MANAGEMENT_TYPE_ID, schoolManagementTypeId), 2, row++);
		table.add(getSmallHeader(localize(KEY_STATISTICS_TYPE, "Statistics type")), 1, row);
		table.mergeCells(2, row, 4, row);
		table.add(getStatisticsTypeDropdownMenu(PARAMETER_STATISTICS_TYPE, statisticsType), 2, row++);
		table.add(getSmallHeader(localize(KEY_TERMINATION_DATE, "Termination date")), 1, row);
		table.mergeCells(2, row, 4, row);
		DateInput di = new DateInput(PARAMETER_TERMINATION_DATE);
		di.setToDisplayDayLast(true);
		Date d = parseDate(terminationDate);
		if (d != null) {
			di.setDate(parseDate(terminationDate));
		}
		table.add(di, 2, row++);

		row++;
		table.mergeCells(2, row, 4, row);
		table.add(getCheckBoxTable(PARAMETER_PAYMENT_BY_INVOICE, paymentByInvoice, KEY_PAYMENT_BY_INVOICE, "Payment by invoice"), 2, row++);
		table.mergeCells(2, row, 4, row);
		table.add(getCheckBoxTable(PARAMETER_CENTRALIZED_ADMINISTRATION, centralizedAdministration, KEY_CENTRALIZED_ADMINISTRATION, "Centralized administration"), 2, row++);

		row++;
		table.mergeCells(2, row, 4, row);
		table.add(getText("&nbsp;"), 2, row);
		table.add(getSmallHeader(localize(KEY_POSTGIRO, "Postgiro")), 2, row);
		table.add(getText("&nbsp;&nbsp;&nbsp;&nbsp;"), 2, row);
		table.add(getTextInput(PARAMETER_POSTGIRO, postgiro, 100), 2, row);
		table.add(getText("&nbsp;&nbsp;&nbsp;&nbsp;"), 2, row);
		table.add(getSmallHeader(localize(KEY_BANKGIRO, "Bankgiro")), 2, row);
		table.add(getText("&nbsp;&nbsp;&nbsp;&nbsp;"), 2, row);
		table.add(getTextInput(PARAMETER_BANKGIRO, bankgiro, 100), 2, row++);

		row++;
		table.mergeCells(2, row, 4, row);
		table.add(new PostingBlock(ownPosting, doublePosting), 2, row);
		
		Table mainPanel = new Table();
		mainPanel.setCellpadding(0);
		mainPanel.setCellspacing(0);
		
		if (errorMessage != null) {
			Table t = new Table();
			t.setCellpadding(getCellpadding());
			t.setCellspacing(getCellspacing());
			t.add(getErrorText(errorMessage), 1, 1);
			mainPanel.add(t, 1, 1);
			mainPanel.add(table, 1, 2);
		} else {
			mainPanel.add(table, 1, 1);
		}
		app.addHiddenInput(PARAMETER_SCHOOL_ID, schoolId);
		if (!isNew) {
			app.addHiddenInput(PARAMETER_EDIT, "true");
		}
		app.setMainPanel(mainPanel);	
		
		ButtonPanel bp = new ButtonPanel(this);
		bp.addLocalizedButton(PARAMETER_SAVE, KEY_SAVE, "Spara");
		bp.addLocalizedButton(PARAMETER_CANCEL, KEY_CANCEL, "Avbryt");
		app.setButtonPanel(bp);
		
		return app;		
	}

	private Map getSchoolTypeMap(IWContext iwc) {
		Iterator iter = getSchoolTypes(iwc).iterator();
		Map schoolTypeMap = new TreeMap();
		while(iter.hasNext()) {
			SchoolType st = (SchoolType) iter.next();
			String stId = st.getPrimaryKey().toString();			
			if (iwc.isParameterSet(PARAMETER_SCHOOL_TYPE_ID + stId)) {		
				Map schoolYearMap = new TreeMap();
				String[] schoolYearIds = iwc.getParameterValues(PARAMETER_SCHOOL_YEAR_ID + stId);
				if (schoolYearIds != null) {
					for (int i = 0; i < schoolYearIds.length; i++) {
						schoolYearMap.put(schoolYearIds[i], schoolYearIds[i]);
					}
				}
				schoolTypeMap.put(stId, schoolYearMap);
			}
		}
		return schoolTypeMap;
	}

	/*
	 * Returns a checkbox table.
	 */	
	private Table getCheckBoxTable(String parameter, String value, String textKey, String defaultText) {
		Table table = new Table();
		table.add(getSmallHeader(localize(textKey, defaultText)), 2, 1);
		CheckBox cb = new CheckBox(parameter);
		if (!value.equals("")) {
			cb.setChecked(true);
		}
		table.add(cb, 1, 1);
		return table;
	}
	
	/*
	 * Returns a DropdownMenu for school areas. 
	 */
	private DropdownMenu getSchoolAreaDropdownMenu(IWContext iwc, String parameter, String schoolAreaId) {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(parameter));
		menu.addMenuElement(0, localize(KEY_SCHOOL_AREA_SELECTOR_HEADER, "Choose school area"));
		int selectedId = (new Integer(schoolAreaId)).intValue();
		Collection c = getSchoolAreas(iwc);
		if (c != null) {
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				SchoolArea sa = (SchoolArea) iter.next();
				int saId = ((Integer) sa.getPrimaryKey()).intValue();
				menu.addMenuElement("" + saId, sa.getName());
			}
			if (selectedId > 0) {
				menu.setSelectedElement(selectedId);
			}
		}		
		return menu;	
	}
	
	/*
	 * Returns a DropdownMenu for provider types. 
	 */
	private DropdownMenu getProviderTypeDropdownMenu(IWContext iwc, String name, String selectedIndex) {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(name));
		menu.addMenuElement(0, localize(KEY_PROVIDER_TYPE_SELECTOR_HEADER, "Choose provider type"));
		try {
			Collection c = getRegulationsBusiness(iwc).findAllProviderTypes();
			if (c != null) {
				Iterator iter = c.iterator();
				while (iter.hasNext()) {
					ProviderType pt = (ProviderType) iter.next();
					menu.addMenuElement("" + (((Integer) pt.getPrimaryKey()).intValue()), 
							localize(pt.getLocalizationKey(), pt.getLocalizationKey()));
				}
				if (selectedIndex != null) {
					menu.setSelectedElement(selectedIndex);
				}
			}
		} catch (RemoteException e) {}

		return menu;	
	}
	
	/*
	 * Returns a DropdownMenu for school management types. 
	 */
	private DropdownMenu getSchoolManagementTypeDropdownMenu(IWContext iwc, String parameter, String companyTypeId) {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(parameter));
		menu.addMenuElement(0, localize(KEY_SCHOOL_MANAGEMENT_TYPE_SELECTOR_HEADER, "Choose school management type"));
		Collection c = getSchoolManagementTypes(iwc);
		if (c != null) {
			Iterator iter = c.iterator();
			while (iter.hasNext()) {
				SchoolManagementType smt = (SchoolManagementType) iter.next();
				String id = smt.getPrimaryKey().toString();
				String key = smt.getLocalizedKey();
				menu.addMenuElement(id, localize(key, key));
			}
			if (companyTypeId != null) {
				menu.setSelectedElement(companyTypeId);
			}
		}		
		return menu;	
	}
	
	/*
	 * Returns a DropdownMenu for communes. 
	 */
	private DropdownMenu getCommuneDropdownMenu(String parameter, String communeId) {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(parameter));
		menu.addMenuElement(0, localize(KEY_COMMUNE_SELECTOR_HEADER, "Choose commune"));
		int selectedId = communeId.equals("") ? -1 : (new Integer(communeId)).intValue();
		try {
			CommuneHome home = (CommuneHome) com.idega.data.IDOLookup.getHome(Commune.class);
			home.toString();			
			Collection c = home.findAllCommunes();
			if (c != null) {
				Iterator iter = c.iterator();
				while (iter.hasNext()) {
					Commune commune = (Commune) iter.next();
					int id = ((Integer) commune.getPrimaryKey()).intValue();
					menu.addMenuElement("" + id, commune.getCommuneName());
				}
				if (selectedId > 0) {
					menu.setSelectedElement(selectedId);
				}
			}		
		} catch (Exception e) {
			add(new ExceptionWrapper(e));
		}
		return menu;	
	}
	
	/*
	 * Returns a DropdownMenu for countries. 
	 */
	private CountryDropdownMenu getCountryDropdownMenu(String parameter, String countryId) {
		CountryDropdownMenu menu = new CountryDropdownMenu(parameter);
		try {
			CountryHome home = (CountryHome) com.idega.data.IDOLookup.getHome(Country.class);
			Country country = home.findByPrimaryKey(new Integer(countryId));
			menu.setSelectedCountry(country);						
		} catch (Exception e) {}
		return menu;
	}
	
	/*
	 * Returns a DropdownMenu for provider statistics types. 
	 */
	private DropdownMenu getStatisticsTypeDropdownMenu(String parameter, String statisticsType) {
		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(parameter));
		menu.addMenuElement("", localize(KEY_STATISTICS_TYPE_SELECTOR_HEADER, "Choose statistics type"));
		try {
			ProviderStatisticsTypeHome home = (ProviderStatisticsTypeHome) com.idega.data.IDOLookup.getHome(ProviderStatisticsType.class);			
			Collection c = home.findAll();
			if (c != null) {
				Iterator iter = c.iterator();
				while (iter.hasNext()) {
					ProviderStatisticsType pst = (ProviderStatisticsType) iter.next();
					String id = pst.getPrimaryKey().toString();
					String key = pst.getLocalizationKey();
					menu.addMenuElement(id, localize(key, key));
				}
				if (!statisticsType.equals("")) {
					menu.setSelectedElement(statisticsType);
				}
			}		
		} catch (Exception e) {
			add(new ExceptionWrapper(e));
		}
		return menu;	
	}

	/*
	 * Returns a table with checkboxes for school years for the specified school type id. 
	 */
	Table getSchoolYearCheckBoxes(IWContext iwc, int schoolTypeId, Map schoolYearMap) {
		Table table = new Table();
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		
		Collection c = getSchoolYears(iwc, schoolTypeId);
		Iterator iter = c.iterator();
		int col = 1;
		boolean hasSchoolYears = false;
		while(iter.hasNext()) {
			hasSchoolYears = true;
			SchoolYear sy = (SchoolYear) iter.next();
			String syId = sy.getPrimaryKey().toString();
			CheckBox syCheckBox = new CheckBox(PARAMETER_SCHOOL_YEAR_ID + schoolTypeId, syId);
			if (schoolYearMap != null && schoolYearMap.get(syId) != null) {
				syCheckBox.setChecked(true);
			}
			table.add(syCheckBox, col, 1);
			table.add(getText(sy.getName()), col, 2);
			table.setAlignment(col, 2, Table.HORIZONTAL_ALIGN_CENTER);
			col++;								
		}

		if (hasSchoolYears) {
			return table;
		} else {
			return null;
		}
	}
	 
	/*
	 * Returns all school areas.
	 */
	private Collection getSchoolAreas(IWContext iwc) {
		Collection c = null;
		try {
			c = getSchoolBusiness(iwc).findAllSchoolAreas();
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		}
		return c;
	}

	/*
	 * Returns all school types.
	 */
	private Collection getSchoolTypes(IWContext iwc) {
		Collection c = null;
		try {
			c = getSchoolBusiness(iwc).findAllSchoolTypes();
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		}
		return c;
	}

	/*
	 * Returns all school years for the specified school type id.
	 */
	private Collection getSchoolYears(IWContext iwc, int schoolTypeId) {
		Collection c = null;
		try {
			c = getSchoolBusiness(iwc).findAllSchoolYearsBySchoolType(schoolTypeId);
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		}
		return c;
	}

	/*
	 * Returns all school management types.
	 */
	private Collection getSchoolManagementTypes(IWContext iwc) {
		Collection c = null;
		try {
			c = getSchoolBusiness(iwc).getSchoolManagementTypes();
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		}
		return c;
	}
	
	/*
	 * Returns a school business object
	 */
	private SchoolBusiness getSchoolBusiness(IWContext iwc) {
		SchoolBusiness sb = null;
		try {
			sb = (SchoolBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		}
		return sb;
	}	
	
	/*
	 * Returns a provider business object
	 */
	private ProviderBusiness getProviderBusiness(IWContext iwc) {
		ProviderBusiness pb = null;
		try {
			pb = (ProviderBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, ProviderBusiness.class);
		} catch (RemoteException e) {
			add(new ExceptionWrapper(e));
		}
		return pb;
	}	

	/*
	 * Returns a regulations business object
	 */
	private RegulationsBusiness getRegulationsBusiness(IWContext iwc) throws RemoteException {
		return (RegulationsBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, RegulationsBusiness.class);
	}
}
