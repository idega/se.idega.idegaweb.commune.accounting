/*
 * Created on Aug 13, 2003
 *
 */
package se.idega.idegaweb.commune.accounting.userinfo.presentation;
import is.idega.idegaweb.member.business.NoChildrenFound;
import is.idega.idegaweb.member.business.NoCohabitantFound;
import is.idega.idegaweb.member.business.NoCustodianFound;
import is.idega.idegaweb.member.business.NoSpouseFound;
import is.idega.idegaweb.member.presentation.UserSearcher;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import se.idega.idegaweb.commune.accounting.presentation.AccountingBlock;
import se.idega.idegaweb.commune.accounting.presentation.ApplicationForm;
import se.idega.idegaweb.commune.accounting.presentation.ButtonPanel;
import se.idega.idegaweb.commune.accounting.presentation.ListTable;
import se.idega.idegaweb.commune.accounting.regulations.business.AgeBusiness;
import se.idega.idegaweb.commune.accounting.userinfo.data.BruttoIncome;
import se.idega.idegaweb.commune.accounting.userinfo.data.BruttoIncomeHome;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.childcare.presentation.ChildContractsWindow;
import se.idega.idegaweb.commune.user.presentation.CitizenEditorWindow;
import com.idega.business.IBOLookup;
import com.idega.core.location.data.Address;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.Window;
import com.idega.user.data.User;
import com.idega.util.Age;
import com.idega.util.IWTimestamp;
/**
 * HouseHoldViewer
 * @author aron 
 * @version 1.0
 */
public class HouseHoldViewer extends AccountingBlock {
	private User firstUser = null;
	private User secondUser = null;
	private boolean hasUser = false;
	private List children = null;
	private Map childrenMap = null;
	private NumberFormat nf = null;
	private Integer userEditorPageID = null;
	private Integer userBruttoIncomePageID = null;
	private Integer userLowIncomePageID = null;
	private Class userEditorWindowClass = CitizenEditorWindow.class;
	private Class userBruttoIncomeWindowClass = BruttoIncomeWindow.class;
	private Class userLowIncomeWindowClass = null;
	private Class childContractHistoryWindowClass = ChildContractsWindow.class;
	private String childContractHistoryChildParameterName = ChildContractsWindow.PARAMETER_CHILD_ID;
	private String userEditorUserParameterName = CitizenEditorWindow.getUserIDParameterName();
	private String userBruttoIncomeUserParameterName = BruttoIncomeWindow.getUserIDParameterName();
	private String userLowIncomeUserParameterName = null;
	private ApplicationForm appForm = null;
	private int nameInputLength = 25;
	private int personalIdInputLength = 15;
	private boolean constrainSearchToUniqueIdentifier = false;
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		nf = NumberFormat.getNumberInstance(iwc.getCurrentLocale());
		
		process(iwc);
		presentate(iwc);
	}
	public void process(IWContext iwc) {
		String prm = UserSearcher.getUniqueUserParameterName("one");
		if (iwc.isParameterSet(prm)) {
			Integer firstUserID = Integer.valueOf(iwc.getParameter(prm));
			if(firstUserID.intValue()>0){
			try {
				firstUser = getUserService(iwc).getUser(firstUserID);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			}
			//add(firstUserID.toString());
		}
		prm = UserSearcher.getUniqueUserParameterName("two");
		if (iwc.isParameterSet(prm)) {
			Integer secondUserID = Integer.valueOf(iwc.getParameter(prm));
			if(secondUserID.intValue()>0){
			try {
				secondUser = getUserService(iwc).getUser(secondUserID);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			}
			//add(secondUserID.toString());
		}
		lookupChildren(iwc);
	}
	private void lookupChildren(IWContext iwc) {
		try {
			CommuneUserBusiness userService = getUserService(iwc);
			childrenMap = new HashMap();
			children = new Vector();
			List parents = new Vector();
			if (firstUser != null) {
				parents.add(firstUser);
			}
			if (secondUser != null) {
				parents.add(secondUser);
			}
			for (Iterator iter = parents.iterator(); iter.hasNext();) {
				User parent = (User) iter.next();
				Vector childs = new Vector();
				Collection parentialChildren = null;
				try {
					parentialChildren = userService.getMemberFamilyLogic().getChildrenFor(parent);
					//System.out.println("parential children "+parentialChildren.size());
				}
				catch (NoChildrenFound e2) {
					
				}
				if (parentialChildren != null)
					childs.addAll(parentialChildren);
				Collection custodianChildren = null;
				try {
					custodianChildren = userService.getMemberFamilyLogic().getChildrenInCustodyOf(parent);
					//System.out.println("custodian children "+custodianChildren.size());
				}
				catch (NoChildrenFound e1) {
				}
				
				
				if (custodianChildren != null)
					childs.addAll(custodianChildren);
				if (childs != null && !childs.isEmpty()) {
					for (Iterator iter2 = childs.iterator(); iter2.hasNext();) {
						User child = (User) iter2.next();
						if (!childrenMap.containsKey(child.getPrimaryKey())) {
							children.add(child);
							childrenMap.put(child.getPrimaryKey(), child);
						}
					}
				}
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (EJBException e) {
			e.printStackTrace();
		}
	}
	public void presentate(IWContext iwc) {
		appForm = new ApplicationForm(this);
		appForm.setLocalizedTitle("household.title", "Household info");
		presentateSearch(iwc);
		presentateUsersFound(iwc);
		presentateChildren(iwc);
		presentateButtons(iwc);
		add(appForm);
	}
	public void presentateSearch(IWContext iwc) {
		Table table = new Table();
		UserSearcher searcherOne = new UserSearcher();
		searcherOne.setShowMiddleNameInSearch(false);
		searcherOne.setOwnFormContainer(false);
		searcherOne.setUniqueIdentifier("one");
		searcherOne.setSkipResultsForOneFound(false);
		searcherOne.setHeaderFontStyleName(getStyleName(STYLENAME_HEADER));
		searcherOne.setButtonStyleName(getStyleName(STYLENAME_INTERFACE_BUTTON));
		searcherOne.setPersonalIDLength(personalIdInputLength);
		searcherOne.setFirstNameLength(nameInputLength);
		searcherOne.setLastNameLength(nameInputLength);
		searcherOne.setConstrainToUniqueSearch(constrainSearchToUniqueIdentifier);
		searcherOne.addMonitoredSearchIdentifier("two");
		searcherOne.setShowResetButton(false);
		searcherOne.setShowMultipleResetButton(true);
		UserSearcher searcherTwo = new UserSearcher();
		searcherTwo.setShowMiddleNameInSearch(false);
		searcherTwo.setOwnFormContainer(false);
		searcherTwo.setUniqueIdentifier("two");
		searcherTwo.setSkipResultsForOneFound(false);
		searcherTwo.setHeaderFontStyleName(getStyleName(STYLENAME_HEADER));
		searcherTwo.setButtonStyleName(getStyleName(STYLENAME_INTERFACE_BUTTON));
		searcherTwo.setPersonalIDLength(personalIdInputLength);
		searcherTwo.setFirstNameLength(nameInputLength);
		searcherTwo.setLastNameLength(nameInputLength);
		searcherTwo.setConstrainToUniqueSearch(constrainSearchToUniqueIdentifier);
		searcherTwo.addClearButtonIdentifiers("one");
		searcherTwo.addMonitoredSearchIdentifier("one");
		String prmTwo = UserSearcher.getUniqueUserParameterName("two");
		String prmOne = UserSearcher.getUniqueUserParameterName("one");
		if (iwc.isParameterSet(prmTwo)) {
			searcherOne.maintainParameter(new Parameter(prmTwo, iwc.getParameter(prmTwo)));
		}
		if (iwc.isParameterSet(prmOne)) {
			searcherTwo.maintainParameter(new Parameter(prmOne, iwc.getParameter(prmOne)));
		}
		table.add(searcherOne, 1, 1);
		table.add(searcherTwo, 1, 2);
		//add(table);
		//Form form = new Form();
		appForm.maintainParameter(prmOne);
		appForm.maintainParameter(prmTwo);
		//form.add(table);
		//add(form);
		//add(Text.getBreak());
		appForm.setSearchPanel(table);
	}
	public void presentateUsersFound(IWContext iwc) {
		Text tAdults = getHeader(localize("household.adults", "Adults"));
		//add(tAdults);
		Table T = new Table();
		T.add(tAdults, 1, 1);
		ListTable table = new ListTable(this, 6);
		T.add(table, 1, 2);
		Text tIndividual = getHeader(localize("household.individual", "Individual"));
		Text tPersonalID = getHeader(localize("household.personal_id", "Personal ID"));
		Text tStreetAddress = getHeader(localize("household.streetaddress", "Street address"));
		Text tSpouse = getHeader(localize("household.spouse", "Spouse"));
		Text tPartner = getHeader(localize("household.partner", "Partner"));
		Text tBruttoIncome = getHeader(localize("household.brutto_income", "Brutto income"));
		int col = 1;
		int row = 1;
		table.setHeader(tIndividual, col++);
		table.setHeader(tPersonalID, col++);
		table.setHeader(tStreetAddress, col++);
		table.setHeader(tSpouse, col++);
		table.setHeader(tPartner, col++);
		table.setHeader(tBruttoIncome, col++);
		row++;
		Vector users = new Vector(2);
		if (firstUser != null) {
			users.add(firstUser);
		}
		if (secondUser != null) {
			users.add(secondUser);
		}
		for (Iterator iter = users.iterator(); iter.hasNext();) {
			User user = (User) iter.next();
			col = 1;
			table.add(getText(user.getNameLastFirst()));
			table.add(getText(user.getPersonalID()));
			Address address = getUserAddress(iwc, user);
			if (address != null) {
				table.add(getText(address.getStreetAddress()));
			}
			else {
				table.skip();
			}
			// Spouse 
			User spouse = getSpouse(iwc, user);
			if (spouse != null) {
				table.add(getText(spouse.getPersonalID()));
			}
			else {
				table.skip();
			}
			// Cohabitant
			User cohabitant = getCohabitant(iwc, user);
			if(cohabitant!=null){
				table.add(getText(cohabitant.getPersonalID()));
			}
			else{
				table.skip();
			}
			
			//table.skip();
			
			
			BruttoIncome income = getBruttoIncome(user);
			if (income != null) {
				table.add(getText(nf.format(income.getIncome().doubleValue())));
			}
			else {
				table.skip();
			}
			row++;
		}
		//add(table);
		//add(Text.getBreak());
		T.add(Text.getBreak(), 1, 3);
		appForm.setMainPanel(T);
	}
	public void presentateChildren(IWContext iwc) {
		Table T = new Table();
		Text tChildren = getHeader(localize("household.children", "Children"));
		//add(tChildren);
		T.add(tChildren, 1, 1);
		ListTable table = new ListTable(this, 8);
		T.add(table, 1, 2);
		Text tIndividual = getHeader(localize("household.individual", "Individual"));
		Text tPersonalID = getHeader(localize("household.personal_id", "Personal ID"));
		Text tStreetAddress = getHeader(localize("household.streetaddress", "Street address"));
		Text tSiblingOrder = getHeader(localize("household.sibling_order", "Sibling order"));
		Text tCalculatedAge = getHeader(localize("household.calculated_age", "Calculated age"));
		Text tLowIncome = getHeader(localize("household.low_income", "Low income"));
		Text tFirstCustodian = getHeader(localize("household.first_custodian", "First custodian"));
		Text tSecondCustodian = getHeader(localize("household.second_custodian", "Second custodian"));
		int row = 1;
		int col = 1;
		table.setHeader(tIndividual, col++);
		table.setHeader(tPersonalID, col++);
		table.setHeader(tStreetAddress, col++);
		table.setHeader(tSiblingOrder, col++);
		table.setHeader(tCalculatedAge, col++);
		table.setHeader(tLowIncome, col++);
		table.setHeader(tFirstCustodian, col++);
		table.setHeader(tSecondCustodian, col++);
		row++;
		if (children != null) {
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				User child = (User) iter.next();
				col = 1;
				
				table.add(getChildHistoryLink(child));
				table.add(getText(child.getPersonalID()));
				Address address = getUserAddress(iwc, child);
				if (address != null) {
					table.add(getText(address.getStreetAddress()));
				}
				else {
					table.skip();
				}
				Integer siblingOrder = getSiblingOrder(child,children);
				if (siblingOrder != null) {
					table.add(getText(siblingOrder.toString()));
				}
				else {
					table.skip();
				}
				int age = getCalculatedAge(iwc, child);
				if (age >= 0) {
					table.add(getText(String.valueOf(age)));
				}
				else {
					table.add(getText(String.valueOf(0)));
					//table.skip();
				}
				// TODO get lowIncome properly
				Object lowIncome = getLowIncome(child);
				if (lowIncome != null) {
					table.add(getText(nf.format(lowIncome.toString())));
				}
				else {
					table.skip();
				}
				Collection custodians = getCustodians(iwc, child);
				if (custodians != null && !custodians.isEmpty()) {
					Iterator iterator = custodians.iterator();
					// first custodian
					User custodian_1 = null;
					User custodian_2 = null;
					if (iterator.hasNext()) {
						custodian_1 = (User) iterator.next();
					}
					// second custodian
					if (iterator.hasNext()) {
						custodian_2 = (User) iterator.next();
					}
					int skip = 0;
					if (custodian_1 != null) {
						table.add(getText(custodian_1.getPersonalID()));
					}
					else
						skip++;
					if (custodian_2 != null)
						table.add(getText(custodian_2.getPersonalID()));
					else
						skip++;
					if (skip > 0)
						table.skip(skip);
				}
				else {
					table.skip(2);
				}
			}
		}
		//add(table);
		//add(Text.getBreak());
		appForm.setMainPanel(T);
	}
	public void presentateButtons(IWContext iwc) {
		DropdownMenu drp = new DropdownMenu("usr_drp");
		if (firstUser != null) {
			drp.addMenuElement(firstUser.getPrimaryKey().toString(), firstUser.getName());
			hasUser = true;
		}
		if (secondUser != null) {
			drp.addMenuElement(secondUser.getPrimaryKey().toString(), secondUser.getName());
			hasUser = true;
		}
		/*
		Table table = new Table();
		table.add(drp, 1, 1);
		table.add(getUserEditorButton(iwc),2,1);
		table.add(getBruttoIncomeEditorButton(iwc),3,1);
		table.add(getLowIncomeEditorButton(iwc),4,1);
		
		Form form = new Form();
		form.add(table);
		add(form);
		*/
		ButtonPanel bPanel = new ButtonPanel(this);
		bPanel.add(drp);
		bPanel.add(getUserEditorButton(iwc));
		bPanel.add(getBruttoIncomeEditorButton(iwc));
		bPanel.add(getLowIncomeEditorButton(iwc));
		appForm.setButtonPanel(bPanel);
	}
	private PresentationObject getUserEditorButton(IWContext iwc) {
		GenericButton button = new SubmitButton(localize("household.edit_user", "Edit user"));
		button = getButton(button);
		if (hasUser && userEditorPageID != null) {
			button.setPageToOpen(userEditorPageID.intValue());
		}
		else if (hasUser && userEditorWindowClass != null) {
			button.setOnClick(getButtonOnClickForWindow(iwc, userEditorWindowClass, userEditorUserParameterName));
		}
		else {
			button.setDisabled(true);
		}
		return button;
	}
	private PresentationObject getBruttoIncomeEditorButton(IWContext iwc) {
		GenericButton button = new SubmitButton(localize("household.edit_brutto_income", "Edit brutto income"));
		button = getButton(button);
		if (hasUser && userBruttoIncomePageID != null) {
			button.setPageToOpen(userBruttoIncomePageID.intValue());
		}
		else if (hasUser && userBruttoIncomeWindowClass != null) {
			button.setOnClick(
				getButtonOnClickForWindow(iwc, userBruttoIncomeWindowClass, userBruttoIncomeUserParameterName));
		}
		else {
			button.setDisabled(true);
		}
		return button;
	}
	private PresentationObject getLowIncomeEditorButton(IWContext iwc) {
		GenericButton button = new SubmitButton(localize("household.edit_low_income", "Edit low income"));
		button = getButton(button);
		if (hasUser && userLowIncomePageID != null) {
			button.setPageToOpen(userLowIncomePageID.intValue());
		}
		else if (hasUser && userLowIncomeWindowClass != null) {
			button.setOnClick(getButtonOnClickForWindow(iwc, userLowIncomeWindowClass, userLowIncomeUserParameterName));
		}
		else {
			button.setDisabled(true);
		}
		return button;
	}
	private String getButtonOnClickForWindow(IWContext iwc, Class windowClass, String userParameterName) {
		String prm = "";
		if (userParameterName != null)
			prm = "&" + userParameterName + "=" + "'+this.form.usr_drp.value+' ";
		String URL = Window.getWindowURL(windowClass, iwc) + prm;
		return "javascript:" + Window.getCallingScriptString(windowClass, URL, true, iwc) + ";return false;";
	}
	private Collection getCustodians(IWContext iwc, User user) {
		try {
			return getUserService(iwc).getMemberFamilyLogic().getCustodiansFor(user);
		}
		catch (NoCustodianFound e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	private User getSpouse(IWContext iwc, User user) {
		try {
			return getUserService(iwc).getMemberFamilyLogic().getSpouseFor(user);
		}
		catch (NoSpouseFound e) {
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	private User getCohabitant(IWContext iwc, User user) {
		try {
			return getUserService(iwc).getMemberFamilyLogic().getCohabitantFor(user);
		}
		catch (NoCohabitantFound e) {
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
	private Address getUserAddress(IWContext iwc, User user) {
		try {
			return getUserService(iwc).getUserAddress1(((Integer) user.getPrimaryKey()).intValue());
		}
		catch (EJBException e) {
			e.printStackTrace();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
	//  some clever calculation
	private int getCalculatedAge(IWContext iwc, User user) {
		try {
			return getAgeService(iwc).getChildAge(user.getPersonalID());
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		if (user.getDateOfBirth() != null)
			return new Age(user.getDateOfBirth()).getYears();
		else
			return 0;
	}
	// TODO get sibling order from database somehow
	private Integer getSiblingOrder(User child,Collection children) {
		if(children!=null && !children.isEmpty()){
			IWTimestamp birthdate = child.getDateOfBirth()!=null?new IWTimestamp(child.getDateOfBirth()):getBirthDateFromPin(child.getPersonalID());
			// setting the order as of the oldest child
			int order = children.size();
			// lets find if anybody is older
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				User sibling = (User) iter.next();
				// only test the other siblings
				if(!sibling.getPrimaryKey().toString().equals(child.getPrimaryKey().toString())){
					IWTimestamp birth = sibling.getDateOfBirth()!=null?new IWTimestamp(sibling.getDateOfBirth()):getBirthDateFromPin(sibling.getPersonalID());
					//  sibling is older than current child lets lower the order
					if(birth.isEarlierThan(birthdate))
						order--;
				}
				
			}
			return new Integer(order);
		}
		return new Integer(1);
	}
	// TODO fetch low income invoice record
	private Object getLowIncome(User user) {
		System.out.println("Unimplemented: getting lowincome for userid"+user.getPrimaryKey());
		return null;
	}
	private CommuneUserBusiness getUserService(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
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
		return nameInputLength;
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
		return personalIdInputLength;
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
		return userBruttoIncomePageID;
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
		return userBruttoIncomeUserParameterName;
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
		return userBruttoIncomeWindowClass;
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
		return userEditorPageID;
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
		return userEditorUserParameterName;
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
		return userEditorWindowClass;
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
		return userLowIncomePageID;
	}
	/**
	 * @param userLowIncomePageID
	 */
	public void setUserLowIncomePageID(Integer userLowIncomePageID) {
		this.userLowIncomePageID = userLowIncomePageID;
	}
	/**
	 * @return
	 */
	public String getUserLowIncomeUserParameterName() {
		return userLowIncomeUserParameterName;
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
		return userLowIncomeWindowClass;
	}
	/**
	 * @param userLowIncomeWindowClass
	 */
	public void setUserLowIncomeWindowClass(Class userLowIncomeWindowClass) {
		this.userLowIncomeWindowClass = userLowIncomeWindowClass;
	}
	
	
	// TODO taken from commune userbusiness, maybe this should be a public method there
	private IWTimestamp getBirthDateFromPin(String pin){
			int dd = Integer.parseInt(pin.substring(6,8));
			int mm = Integer.parseInt(pin.substring(4,6));
			int yyyy = Integer.parseInt(pin.substring(0,4));
			IWTimestamp dob = new IWTimestamp(dd,mm,yyyy);
			return dob;
		}
	/**
	 * @return
	 */
	public String getChildContractHistoryChildParameterName() {
		return childContractHistoryChildParameterName;
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
		return childContractHistoryWindowClass;
	}

	/**
	 * @param childContractHistoryWindowClass
	 */
	public void setChildContractHistoryWindowClass(Class childContractHistoryWindowClass) {
		this.childContractHistoryWindowClass = childContractHistoryWindowClass;
	}
	
	private PresentationObject getChildHistoryLink(User child){
		if(childContractHistoryWindowClass!=null && childContractHistoryChildParameterName!=null){
			Link l = new Link(child.getFirstName());
			l.setWindowToOpen(childContractHistoryWindowClass);
			l.addParameter(childContractHistoryChildParameterName,child.getPrimaryKey().toString());
			return l;
		}
		return getText(child.getFirstName());
	}

}
