/*
 * Created on 5.11.2003
 *
 */
package se.idega.idegaweb.commune.accounting.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection; 
import java.sql.Date;
import java.util.Iterator;
import java.util.List;


import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.posting.business.PostingBusiness;
import se.idega.idegaweb.commune.accounting.posting.data.PostingParameters;
import se.idega.idegaweb.commune.accounting.regulations.data.Regulation;
import se.idega.idegaweb.commune.accounting.regulations.data.RegulationHome;
import se.idega.idegaweb.commune.accounting.school.data.Provider;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolCategory;
import com.idega.block.school.data.SchoolHome;
import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Parameter;

/**
 * @author Roar
 * 
 * Search panel for searchin in regulations and postings. 
 * The panel shows a dropdown with providers, a field for regulation name and a field
 * for date.
 */
public class RegulationSearchPanel extends AccountingBlock {
	private static final String KEY_PROVIDER = "provider";
	private static final String KEY_PLACING = "placing";	
	private static final String KEY_VALID_DATE = "valid_date";	
	private static final String KEY_SEARCH = "search";	
	
	private static final String PAR_PROVIDER = KEY_PROVIDER; 
	private static final String PAR_PLACING = KEY_PLACING;	
	private static final String PAR_VALID_DATE = KEY_VALID_DATE; 
	private static final String PAR_ENTRY_PK = "PAR_ENTRY_PK";
	
	//Force the request to be processed at once.
	private RegulationSearchPanel(){
		super();
	}
	public RegulationSearchPanel(IWContext iwc){
		super(); 
		process(iwc);
	}
		
	
	private static final String ACTION_SEARCH_REGULATION = "ACTION_SEARCH_REGULATION";
	
	private Regulation _currentRegulation = null;
	private Collection _searchResult = null;
	private SchoolCategory _currentSchoolCategory = null;
	private String[] _currentPosting = null;
	private int _currentSchoolId = 0;
	private Date _validDate = null;
	private String _currentPlacing = null;
	private String _errorMessage = null;


	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.presentation.AccountingBlock#init(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		process(iwc); //This should not be necessary, as the request will always be processed by now...

		maintainParameter(PAR_PLACING);
		maintainParameter(PAR_PROVIDER);
		maintainParameter(PAR_VALID_DATE);
				
		add(getSearchForm(iwc));
		
		if (_searchResult != null){
			add(getResultList(iwc, _searchResult)); 
		} 
	} 
	
	private boolean processed = false;
	/**
	 * Processes the request: does the actual search or lookup so that the
	 * enclosing block can use the result to populate fields.
	 * @param iwc
	 */
	public void process(IWContext iwc){
		if (! processed){
			boolean searchAction = iwc.getParameter(ACTION_SEARCH_REGULATION) != null;
			
			//Find selected category, date and provider
			String vDate = iwc.getParameter(PAR_VALID_DATE);
			_validDate = parseDate(vDate);		
			if (vDate != null && vDate.length() > 0 && _validDate == null){
				_errorMessage = localize("regulation_search_panel.date_format_error", "Error i dateformat");
			} else {
				School currentSchool = null;
				try{

					SchoolHome schoolHome = (SchoolHome) IDOLookup.getHome(School.class);	
					if (iwc.getParameter(PAR_PROVIDER) != null){
						_currentSchoolId = new Integer(iwc.getParameter(PAR_PROVIDER)).intValue();
					}
					currentSchool = schoolHome.findByPrimaryKey("" + _currentSchoolId);
				}catch(RemoteException ex){
					ex.printStackTrace();
				}catch(FinderException ex){ 
					ex.printStackTrace();
				}
				
					
				//Search regulations
				if (searchAction){
					_searchResult = doSearch(iwc);
				} 
						
				//Lookup regulation and postings
				String regId = iwc.getParameter(PAR_ENTRY_PK);
				if (regId != null){
					_currentRegulation = getRegulation(regId);
					_currentPosting = getPosting(iwc, getCurrentSchoolCategory(iwc), _currentRegulation, new Provider(currentSchool), _validDate);				
				}
				if (_currentRegulation!= null){
					_currentPlacing = _currentRegulation.getName();
				} else if (iwc.getParameter(PAR_PLACING) != null){
					_currentPlacing = iwc.getParameter(PAR_PLACING);
				}
								
			}
			processed = true;
		}
	}
	
	public SchoolCategory getCurrentSchoolCategory(IWContext iwc){
		if (_currentSchoolCategory == null){
		
			try {
				SchoolBusiness schoolBusiness = (SchoolBusiness) IBOLookup.getServiceInstance(iwc.getApplicationContext(),	SchoolBusiness.class);
				String opField = getSession().getOperationalField();
				_currentSchoolCategory = schoolBusiness.getSchoolCategoryHome().findByPrimaryKey(opField);					
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (FinderException e) {
				e.printStackTrace();
			}	
		}
		
		return _currentSchoolCategory;	
	}
		
	
	private List _maintainParameters = new ArrayList();
	public void maintainParameter(String par){
		_maintainParameters.add(par);
	}
	public void maintainParameter(String[] parameters){
		for(int i = 0; i < parameters.length; i++){
			_maintainParameters.add(parameters[i]);			
		}
	}	
	
	private boolean _maintainAllParameters = false;
	public void maintainAllParameters(){
		_maintainAllParameters = true;
	}	
	
	private List _setParameters = new ArrayList();	
	public void setParameter(String par, String value){
		_setParameters.add(new Parameter(par, value));
	}	
	
	
	private void maintainParameters(IWContext iwc, Link link){
		Iterator i = _maintainParameters.iterator();
		while(i.hasNext()){
		String par = (String) i.next();
			link.maintainParameter(par, iwc);
		}
	}
	
	private void setParameters(Link link){
		Iterator i = _setParameters.iterator();
		while(i.hasNext()){
			link.addParameter((Parameter) i.next());
		}
	}		

	/**
	 * Formats the search results and returns a Table that displays it
	 * as links, or an approperiate text if none where found
	 * @param iwc
	 * @param results
	 * @return
	 */
	private Table getResultList(IWContext iwc, Collection results){
		Table table = new Table(); 
		table.setCellspacing(10);
		int row = 1, col = 1;
		
		if (results.size() == 0){
			table.add(getErrorText(localize("regulation_search_panel.no_regulations_found", "No regulations found")));
			
		} else {
			Iterator i = results.iterator();
	
			HiddenInput h = new HiddenInput(PAR_ENTRY_PK, "");
			table.add(h); 
							
			while(i.hasNext()){
				Regulation reg = (Regulation) i.next();
				Link link = new Link(reg.getName() + " ("+formatDate(reg.getPeriodFrom(), 4) + "-" + formatDate(reg.getPeriodTo(), 4)+")");
				link.addParameter(new Parameter(PAR_ENTRY_PK, reg.getPrimaryKey().toString()));
				maintainParameters(iwc, link);
				setParameters(link);
				
	//THIS doean't work for opera...				
	//			link.setOnClick("getElementById('"+ pkId +"').value='"+ reg.getPrimaryKey() +"'");			
	//			link.setToFormSubmit(form);
						
				if (col > 3){
					col = 1;
					row++;
				} else{
					col++;
				}
				
				table.add(link, col, row);
			}
		}
		return table;
	}

	/**
	 *	Does the search in the regulations and return them as a Collection
	 */
	private Collection doSearch(IWContext iwc){
		Collection matches = new ArrayList();
		String wcName = "%"+iwc.getParameter(PAR_PLACING)+"%";
		try{
			RegulationHome regHome = (RegulationHome) IDOLookup.getHome(Regulation.class);
			
			matches = _validDate != null ? 
				regHome.findRegulationsByNameNoCaseAndDate(wcName, _validDate)
				: regHome.findRegulationsByNameNoCase(wcName);

		}catch(RemoteException ex){
			ex.printStackTrace();
			
		}catch(FinderException ex){
			ex.printStackTrace();			
		}
		
		return matches;
	}	
	
	/**
	 * Does a lookup to find a regulation.
	 * @param regId
	 * @return the Regulation
	 */
	private Regulation getRegulation(String regId){
		Regulation reg = null;
		try{
			RegulationHome regHome = (RegulationHome) IDOLookup.getHome(Regulation.class);
			reg = regHome.findByPrimaryKey(regId);


		}catch(RemoteException ex){
			ex.printStackTrace();
			
		}catch(FinderException ex){
			ex.printStackTrace();			
		}		
		return reg;
	}
	
	/**
	 * 
	 * @return the currently chosen Regulation
	 */
	public Regulation getRegulation(){
		return _currentRegulation;
	}
	
	/**
	 * Does the search in the postings
	 * @param iwc
	 * @param TODO_USE_THIS_PARAMETER
	 * @param reg
	 * @param provider
	 * @param date
	 * @return the Posting strings as String[]
	 */
	private String[] getPosting(IWContext iwc, SchoolCategory TODO_USE_THIS_PARAMETER, Regulation reg, Provider provider, Date date) {
		
		String ownPosting = null, doublePosting = null;
		if (reg != null){
			int regSpecType = new Integer("" + reg.getRegSpecType().getPrimaryKey()).intValue();
		
			int catId =  1; //new Integer("" + category.getPrimaryKey()).intValue();
	
			try{
				//Set the posting strings
				PostingBusiness postingBusiness = (PostingBusiness) IBOLookup.getServiceInstance(iwc.getApplicationContext(), PostingBusiness.class);
		
				PostingParameters parameters;
				parameters = postingBusiness.getPostingParameter(date, catId, regSpecType, 0, 0);
		
				ownPosting = parameters.getPostingString();
				ownPosting = postingBusiness.generateString(ownPosting, provider.getOwnPosting(), date);
		//			ownPosting = postingBusiness.generateString(ownPosting, categoryPosting.getAccount(), date);
				postingBusiness.validateString(ownPosting,date);
		
				doublePosting = parameters.getDoublePostingString();
				doublePosting = postingBusiness.generateString(doublePosting, provider.getDoublePosting(), date);
		//			doublePosting = postingBusiness.generateString(doublePosting, categoryPosting.getCounterAccount(), date);
				postingBusiness.validateString(doublePosting,date);
			}catch(Exception ex){
				ex.printStackTrace();
	//		}catch(IDOLookupException ex){
	//		}catch(CreateException ex){
	//		}catch(RemoteException ex){
	//		}catch(PostingException ex){
	//		}catch(MissingMandatoryFieldException ex){
	//		}catch(PostingParametersException ex){
			}		
		}
		return new String[] {ownPosting, doublePosting};
	}
			
	/**
	 * 
	 * @return the currently chosen posting strings as String[]
	 */
	public String[] getPosting(){
		return _currentPosting;
	}	

	/**
	 * Returns the search form as an Table
	 * @param iwc
	 * @return
	 */
	private Table getSearchForm(IWContext iwc){
		Collection providers = new ArrayList();		
		try{
			SchoolHome home = (SchoolHome) IDOLookup.getHome(School.class);				
			providers = home.findAllByCategory(getCurrentSchoolCategory(iwc));
		}catch(RemoteException ex){
			ex.printStackTrace();
		}catch(FinderException ex){ 
			ex.printStackTrace();
		}

		Table table = new Table();
		int row = 1;
		
		addDropDown(table, PAR_PROVIDER, KEY_PROVIDER, providers, "" + _currentSchoolId, "getSchoolName", 1, row++);
		if (_errorMessage != null){
			table.add(getErrorText(_errorMessage), 4, row++);
		}		
		addField(table, PAR_PLACING, KEY_PLACING, _currentPlacing, 1, row);		
		addField(table, PAR_VALID_DATE, KEY_VALID_DATE, iwc.getParameter(PAR_VALID_DATE), 3, row);	
		table.add(getLocalizedButton(ACTION_SEARCH_REGULATION, KEY_SEARCH, "Search"), 5, row++);


		return table;
	
	}

	
	
	private Table addDropDown(Table table, String parameter, String key, Collection options, String selected, String method, int col, int row) {
		DropdownMenu dropDown = getDropdownMenu(parameter, options, method);
		dropDown.setSelectedElement(selected);
		return addWidget(table, key, dropDown, col, row);		
	}
	
	private Table addField(Table table, String parameter, String key, String value, int col, int row){
		return addWidget(table, key, getTextInput(parameter, value), col, row);
	}
	
	private Table addWidget(Table table, String key, PresentationObject widget, int col, int row){
		table.add(getLocalizedLabel(key, key), col, row);
		table.add(widget, col + 1, row);
		return table;
	
	}
	

	public void setPlacingIfNull(String placing) {
		if (_currentPlacing == null){
			_currentPlacing = placing;
		}
	}		
	
	public void setSchoolIdIfNull(int schoolId) {
		if (_currentSchoolId == 0){
			_currentSchoolId = schoolId;
		}
	}		
		

		
		
	

}