/*
 * Created on Aug 13, 2003
 */
package se.idega.idegaweb.commune.accounting.userinfo.presentation;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.invoice.presentation.RegularInvoiceEntriesList;
import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.presentation.ApplicationForm;
import se.idega.idegaweb.commune.accounting.presentation.ButtonPanel;
import se.idega.idegaweb.commune.accounting.presentation.ListTable;
import se.idega.idegaweb.commune.accounting.regulations.business.AgeBusiness;
import se.idega.idegaweb.commune.accounting.userinfo.business.UserInfoService;
import se.idega.idegaweb.commune.accounting.userinfo.data.BruttoIncome;
import se.idega.idegaweb.commune.accounting.userinfo.data.BruttoIncomeHome;
import se.idega.idegaweb.commune.accounting.userinfo.data.HouseHoldFamily;
import se.idega.idegaweb.commune.care.presentation.ChildContracts;
import se.idega.idegaweb.commune.user.presentation.CitizenEditorWindow;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
import com.idega.repository.data.ImplementorRepository;
import com.idega.user.business.UserSession;
import com.idega.user.data.User;
import com.idega.util.Age;
import com.idega.util.CalendarMonth;
import com.idega.util.IWTimestamp;
import com.idega.util.URLUtil;
import com.idega.util.text.Name;

/*
 * HouseHoldViewer contains a search used to find citizens and their family
 * living at the same address. Tvo user families can be searched an compared.
 * @author aron
 * 
 * @version 1.0
 */
public class HouseHoldViewer extends AccountingBlock {

	private User firstUser = null;

	private User secondUser = null;

	private HouseHoldFamily firstFamily = null, secondFamily = null;

	private boolean hasUser = false;

	private NumberFormat nf = null;

	private Integer userEditorPageID = null;

	private Integer userBruttoIncomePageID = null;

	private Integer userLowIncomePageID = null;

	private Class userEditorWindowClass = CitizenEditorWindow.class;

	private Class userBruttoIncomeWindowClass = BruttoIncomeWindow.class;

	private Class userLowIncomeWindowClass = null;

	private Class childContractHistoryWindowClass = null;

	private String childContractHistoryChildParameterName = null;

	private String userEditorUserParameterName = CitizenEditorWindow.getUserIDParameterName();

	private String userBruttoIncomeUserParameterName = BruttoIncomeWindow.getUserIDParameterName();

	private String userLowIncomeUserParameterName = RegularInvoiceEntriesList.getUserIDParameterName();

	private ApplicationForm appForm = null;

	private int nameInputLength = 25;

	private int personalIdInputLength = 15;

	private boolean showCohabitant = true;

	public HouseHoldViewer() {
		ImplementorRepository repository = ImplementorRepository.getInstance();
		ChildContracts childContracts = (ChildContracts) repository.newInstanceOrNull(ChildContracts.class, this.getClass());
		if (childContracts != null) {
			this.childContractHistoryWindowClass = childContracts.getWindowClass();
			this.childContractHistoryChildParameterName = childContracts.getParameterChildID();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		this.nf = NumberFormat.getNumberInstance(iwc.getCurrentLocale());
		process(iwc);
		present(iwc);
	}

	public void process(IWContext iwc) {
		try {
			this.firstUser = getUserSession(iwc).getUser();
		}
		catch (RemoteException re) {
			log(re);
		}
		lookupFamilies(iwc);
	}

	private void lookupFamilies(IWContext iwc) {
		try {
			this.firstFamily = getUserInfoService(iwc).getHouseHoldFamily(this.firstUser);
			this.secondFamily = getUserInfoService(iwc).getHouseHoldFamily(this.secondUser);
		}
		catch (RemoteException e) {
			log(e);
		}
	}

	protected UserSession getUserSession(IWUserContext iwuc) {
		try {
			return (UserSession) IBOLookup.getSessionInstance(iwuc, UserSession.class);
		}
		catch (IBOLookupException e) {
			throw new IBORuntimeException(e);
		}
	}

	public void present(IWContext iwc) {
		this.appForm = new ApplicationForm(this);
		presentUsersFound(iwc);
		presentChildren(iwc);
		add(this.appForm);
	}

	public void presentUsersFound(IWContext iwc) {
		Text tAdults = getHeader(localize("household.adults", "Adults"));
		// add(tAdults);
		Table T = new Table();
		T.add(tAdults, 1, 1);
		ListTable table = new ListTable(this, 4);
		T.add(table, 1, 2);
		Text tIndividual = getHeader(localize("household.individual", "Individual"));
		Text tPersonalID = getHeader(localize("household.personal_id", "Personal ID"));
		Text tBruttoIncome = getHeader(localize("household.brutto_income", "Brutto income"));
		int col = 1;
		int row = 1;
		table.setHeader(tIndividual, col++);
		table.setHeader(tPersonalID, col++);
		table.setHeader(tBruttoIncome, col++);
		table.setHeader(Text.getNonBrakingSpace(), col++);
		row++;
		Vector users = new Vector();
		if (this.firstFamily != null) {
			users.add(this.firstFamily.getHeadOfFamily());
			if (this.firstFamily.hasSpouse()) {
				users.add((this.firstFamily.getSpouse()));
			}
			if (this.showCohabitant && this.firstFamily.hasCohabitant()) {
				users.add(this.firstFamily.getCohabitant());
			}
		}
		if (this.secondFamily != null) {
			users.add(this.secondFamily.getHeadOfFamily());
			if (this.secondFamily.hasSpouse()) {
				users.add((this.secondFamily.getSpouse()));
			}
			if (this.showCohabitant && this.secondFamily.hasCohabitant()) {
				users.add(this.secondFamily.getCohabitant());
			}
		}
		for (Iterator iter = users.iterator(); iter.hasNext();) {
			User user = (User) iter.next();
			col = 1;
			Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
			table.add(getText(name.getName(iwc.getApplicationSettings().getDefaultLocale())));
			table.add(getText(user.getPersonalID()));
			// table.skip();
			BruttoIncome income = getBruttoIncome(user);
			if (income != null) {
				table.add(getText(this.nf.format(income.getIncome().doubleValue())));
			}
			else {
				table.skip();
			}
			Link edit = new Link(getEditIcon(localize("household.edit_brutto_income", "Edit brutto income")));
			edit.setWindowToOpen(getUserBruttoIncomeWindowClass());
			edit.addParameter(getUserBruttoIncomeUserParameterName(), user.getPrimaryKey().toString());
			table.add(edit);

			row++;
		}
		// add(table);
		// add(Text.getBreak());
		T.add(Text.getBreak(), 1, 3);
		this.appForm.setMainPanel(T);
	}

	public void presentChildren(IWContext iwc) {
		Table T = new Table();
		Text tChildren = getHeader(localize("household.children", "Children"));
		// add(tChildren);
		T.add(tChildren, 1, 1);
		ListTable table = new ListTable(this, 4);
		T.add(table, 1, 2);
		Text tIndividual = getHeader(localize("household.individual", "Individual"));
		Text tPersonalID = getHeader(localize("household.personal_id", "Personal ID"));
		Text tSiblingOrder = getHeader(localize("household.sibling_order", "Sibling order"));
		Text tCalculatedAge = getHeader(localize("household.calculated_age", "Calculated age"));
		int row = 1;
		int col = 1;
		table.setHeader(tIndividual, col++);
		table.setHeader(tPersonalID, col++);
		table.setHeader(tSiblingOrder, col++);
		table.setHeader(tCalculatedAge, col++);
		row++;

		Collection[] familyChildren = new Collection[2];
		if (this.firstFamily != null && this.firstFamily.hasChildren()) {
			familyChildren[0] = this.firstFamily.getChildren();
		}
		if (this.secondFamily != null && this.secondFamily.hasChildren()) {
			familyChildren[1] = this.secondFamily.getChildren();
		}
		for (int i = 0; i < familyChildren.length; i++) {
			Collection children = familyChildren[i];
			if (children != null) {
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					User child = (User) iter.next();
					col = 1;
					table.add(getChildHistoryLink(child));
					table.add(getText(child.getPersonalID()));
					try {
						int siblingOrder = getUserInfoService(iwc).getSiblingOrder(child, new CalendarMonth(new IWTimestamp(System.currentTimeMillis())));
						table.add(getText(siblingOrder + ""));
					}
					catch (Exception e) {
						table.skip();
					}
					int age = getCalculatedAge(iwc, child);
					if (age >= 0) {
						table.add(getText(String.valueOf(age)));
					}
					else {
						table.add(getText(String.valueOf(0)));
						// table.skip();
					}
				}
			}
		}
		// add(table);
		// add(Text.getBreak());
		this.appForm.setMainPanel(T);
	}

	public void presentButtons(IWContext iwc) {
		DropdownMenu drp = new DropdownMenu("usr_drp");
		HouseHoldFamily[] families = { this.firstFamily, this.secondFamily };
		for (int i = 0; i < families.length; i++) {
			HouseHoldFamily family = families[i];
			if (family != null) {
				drp.addMenuElement(family.getHeadOfFamily().getPrimaryKey().toString(), family.getHeadOfFamily().getName());
				if (this.firstFamily.hasSpouse()) {
					drp.addMenuElement(family.getSpouse().getPrimaryKey().toString(), family.getSpouse().getName());
				}
				if (this.firstFamily.hasCohabitant()) {
					drp.addMenuElement(family.getCohabitant().getPrimaryKey().toString(), family.getCohabitant().getName());
				}
				if (this.firstFamily.hasChildren()) {
					Collection children = family.getChildren();
					if (children != null) {
						for (Iterator iter = children.iterator(); iter.hasNext();) {
							User child = (User) iter.next();
							drp.addMenuElement(child.getPrimaryKey().toString(), child.getName());
						}
					}
				}
				this.hasUser = true;
			}
		}
		/*
		 * Table table = new Table(); table.add(drp, 1, 1);
		 * table.add(getUserEditorButton(iwc),2,1);
		 * table.add(getBruttoIncomeEditorButton(iwc),3,1);
		 * table.add(getLowIncomeEditorButton(iwc),4,1); Form form = new Form();
		 * form.add(table); add(form);
		 */
		ButtonPanel bPanel = new ButtonPanel(this);
		bPanel.add(drp);
		bPanel.add(getUserEditorButton(iwc));
		bPanel.add(getBruttoIncomeEditorButton(iwc));
		bPanel.add(getLowIncomeEditorButton(iwc));
		this.appForm.setButtonPanel(bPanel);
	}

	private PresentationObject getUserEditorButton(IWContext iwc) {
		GenericButton button = new SubmitButton(localize("household.edit_user", "Edit user"));
		button = getButton(button);
		if (this.hasUser && this.userEditorPageID != null) {
			button.setPageToOpen(this.userEditorPageID.intValue());
		}
		else if (this.hasUser && this.userEditorWindowClass != null) {
			button.setOnClick(getButtonOnClickForWindow(iwc, this.userEditorWindowClass, this.userEditorUserParameterName));
		}
		else {
			button.setDisabled(true);
		}
		return button;
	}

	private PresentationObject getBruttoIncomeEditorButton(IWContext iwc) {
		GenericButton button = new SubmitButton(localize("household.edit_brutto_income", "Edit brutto income"));
		button = getButton(button);
		if (this.hasUser && this.userBruttoIncomePageID != null) {
			button.setPageToOpen(this.userBruttoIncomePageID.intValue());
		}
		else if (this.hasUser && this.userBruttoIncomeWindowClass != null) {
			button.setOnClick(getButtonOnClickForWindow(iwc, this.userBruttoIncomeWindowClass, this.userBruttoIncomeUserParameterName));
		}
		else {
			button.setDisabled(true);
		}
		return button;
	}

	private PresentationObject getLowIncomeEditorButton(IWContext iwc) {
		GenericButton button = new SubmitButton(localize("household.edit_low_income", "Edit low income"));
		button = getButton(button);
		if (this.hasUser && this.userLowIncomePageID != null) {
			String onclickString = getButtonOnClickForPage(iwc, this.userLowIncomePageID.intValue(), this.userLowIncomeUserParameterName);
			if (onclickString != null) {
				button.setOnClick(onclickString);
			}
			else {
				button.setPageToOpen(this.userLowIncomePageID.intValue());
			}
		}
		else if (this.hasUser && this.userLowIncomeWindowClass != null) {
			button.setOnClick(getButtonOnClickForWindow(iwc, this.userLowIncomeWindowClass, this.userLowIncomeUserParameterName));
		}
		else {
			button.setDisabled(true);
		}
		return button;
	}

	private String getButtonOnClickForWindow(IWContext iwc, Class windowClass, String userParameterName) {
		String URL = null;
		if (userParameterName != null) {
			URL = Window.getWindowURLWithParameter(windowClass, iwc, userParameterName, "'+this.form.usr_drp.value+' ");
		}
		else {
			URL = Window.getWindowURL(windowClass, iwc);
		}
		return "javascript:" + Window.getCallingScriptString(windowClass, URL, true, iwc) + ";return false;";
	}

	private String getButtonOnClickForPage(IWContext iwc, int pageID, String userParameterName) {
		try {
			URLUtil url = new URLUtil(getBuilderService(iwc).getPageURI(pageID), false);
			if (userParameterName != null) {
				url.addParameter(userParameterName, "'+this.form.usr_drp.value");
			}
			return "javascript:window.location='" + url.toString() + ";return false;";
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	private BruttoIncome getBruttoIncome(User user) {
		try {
			return getBruttoIncomeHome().findLatestByUser((Integer) user.getPrimaryKey());
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (EJBException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
		}
		return null;
	}

	// some clever calculation
	private int getCalculatedAge(IWContext iwc, User user) {
		try {
			return getAgeService(iwc).getChildAge(user.getPersonalID());
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		if (user.getDateOfBirth() != null) {
			return new Age(user.getDateOfBirth()).getYears();
		}
		else {
			return 0;
		}
	}

	private UserInfoService getUserInfoService(IWApplicationContext iwac) throws RemoteException {
		return (UserInfoService) IBOLookup.getServiceInstance(iwac, UserInfoService.class);
	}

	private AgeBusiness getAgeService(IWContext iwc) throws RemoteException {
		return (AgeBusiness) IBOLookup.getServiceInstance(iwc, AgeBusiness.class);
	}

	private BruttoIncomeHome getBruttoIncomeHome() throws RemoteException {
		return (BruttoIncomeHome) IDOLookup.getHome(BruttoIncome.class);
	}

	/**
	 * @return
	 */
	public int getNameInputLength() {
		return this.nameInputLength;
	}

	/**
	 * @param nameInputLength
	 */
	public void setNameInputLength(int nameInputLength) {
		this.nameInputLength = nameInputLength;
	}

	/**
	 * @return
	 */
	public int getPersonalIdInputLength() {
		return this.personalIdInputLength;
	}

	/**
	 * @param personalIdInputLength
	 */
	public void setPersonalIdInputLength(int personalIdInputLength) {
		this.personalIdInputLength = personalIdInputLength;
	}

	/**
	 * @return
	 */
	public Integer getUserBruttoIncomePageID() {
		return this.userBruttoIncomePageID;
	}

	/**
	 * @param userBruttoIncomePageID
	 */
	public void setUserBruttoIncomePageID(Integer userBruttoIncomePageID) {
		this.userBruttoIncomePageID = userBruttoIncomePageID;
	}

	/**
	 * @return
	 */
	public String getUserBruttoIncomeUserParameterName() {
		return this.userBruttoIncomeUserParameterName;
	}

	/**
	 * @param userBruttoIncomeUserParameterName
	 */
	public void setUserBruttoIncomeUserParameterName(String userBruttoIncomeUserParameterName) {
		this.userBruttoIncomeUserParameterName = userBruttoIncomeUserParameterName;
	}

	/**
	 * @return
	 */
	public Class getUserBruttoIncomeWindowClass() {
		return this.userBruttoIncomeWindowClass;
	}

	/**
	 * @param userBruttoIncomeWindowClass
	 */
	public void setUserBruttoIncomeWindowClass(Class userBruttoIncomeWindowClass) {
		this.userBruttoIncomeWindowClass = userBruttoIncomeWindowClass;
	}

	/**
	 * @return
	 */
	public Integer getUserEditorPageID() {
		return this.userEditorPageID;
	}

	/**
	 * @param userEditorPageID
	 */
	public void setUserEditorPageID(Integer userEditorPageID) {
		this.userEditorPageID = userEditorPageID;
	}

	/**
	 * @return
	 */
	public String getUserEditorUserParameterName() {
		return this.userEditorUserParameterName;
	}

	/**
	 * @param userEditorUserParameterName
	 */
	public void setUserEditorUserParameterName(String userEditorUserParameterName) {
		this.userEditorUserParameterName = userEditorUserParameterName;
	}

	/**
	 * @return
	 */
	public Class getUserEditorWindowClass() {
		return this.userEditorWindowClass;
	}

	/**
	 * @param userEditorWindowClass
	 */
	public void setUserEditorWindowClass(Class userEditorWindowClass) {
		this.userEditorWindowClass = userEditorWindowClass;
	}

	/**
	 * @return
	 */
	public Integer getUserLowIncomePageID() {
		return this.userLowIncomePageID;
	}

	/**
	 * @param userLowIncomePageID
	 */
	public void setUserLowIncomePageID(Integer userLowIncomePageID) {
		this.userLowIncomePageID = userLowIncomePageID;
	}

	public void setUserLowIncomePageID(int userLowIncomePageID) {
		this.userLowIncomePageID = new Integer(userLowIncomePageID);
	}

	/**
	 * @return
	 */
	public String getUserLowIncomeUserParameterName() {
		return this.userLowIncomeUserParameterName;
	}

	/**
	 * @param userLowIncomeUserParameterName
	 */
	public void setUserLowIncomeUserParameterName(String userLowIncomeUserParameterName) {
		this.userLowIncomeUserParameterName = userLowIncomeUserParameterName;
	}

	/**
	 * @return
	 */
	public Class getUserLowIncomeWindowClass() {
		return this.userLowIncomeWindowClass;
	}

	/**
	 * @param userLowIncomeWindowClass
	 */
	public void setUserLowIncomeWindowClass(Class userLowIncomeWindowClass) {
		this.userLowIncomeWindowClass = userLowIncomeWindowClass;
	}

	/**
	 * @return
	 */
	public String getChildContractHistoryChildParameterName() {
		return this.childContractHistoryChildParameterName;
	}

	/**
	 * @param childContractHistoryChildParameterName
	 */
	public void setChildContractHistoryChildParameterName(String childContractHistoryChildParameterName) {
		this.childContractHistoryChildParameterName = childContractHistoryChildParameterName;
	}

	/**
	 * @return
	 */
	public Class getChildContractHistoryWindowClass() {
		return this.childContractHistoryWindowClass;
	}

	/**
	 * @param childContractHistoryWindowClass
	 */
	public void setChildContractHistoryWindowClass(Class childContractHistoryWindowClass) {
		this.childContractHistoryWindowClass = childContractHistoryWindowClass;
	}

	private PresentationObject getChildHistoryLink(User child) {
		if (this.childContractHistoryWindowClass != null && this.childContractHistoryChildParameterName != null) {
			Link l = new Link(child.getFirstName());
			l.setWindowToOpen(this.childContractHistoryWindowClass);
			l.addParameter(this.childContractHistoryChildParameterName, child.getPrimaryKey().toString());
			return l;
		}
		return getText(child.getFirstName());
	}

	public void setShowCohabitant(boolean flag) {
		this.showCohabitant = flag;
	}
}