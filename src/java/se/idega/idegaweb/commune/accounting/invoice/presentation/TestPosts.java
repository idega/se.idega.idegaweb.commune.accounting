package se.idega.idegaweb.commune.accounting.invoice.presentation;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.invoice.business.BatchRunQueue;
import se.idega.idegaweb.commune.accounting.invoice.business.SchoolCategoryNotFoundException;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolCategory;
import com.idega.block.school.data.SchoolHome;
import com.idega.business.IBOLookup;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.ui.DropdownMenu;


/**
 * Starts the batch run that will create billing and invoicing information
 * according to the parameters set in the UI.
 * 
 * @author Roar
 * 
 * @see se.idega.idegaweb.commune.accounting.invoice.business.InvoiceBusiness
 * @see se.idega.idegaweb.commune.accounting.invoice.business.BillingThread
 */
public class TestPosts extends InvoiceBatchStarter{
	private static final String KEY_PROVIDER = "provider";	
	private static String PAR_PROVIDER = KEY_PROVIDER; 	
	private School _currentSchool = null;	
	private SchoolCategory _currentSchoolCategory = null;	
	

	
	protected PresentationObject getShoolDropDown(){
		DropdownMenu dropDown = getDropdownMenu(PAR_PROVIDER, getSchools(getIWContext()), "getSchoolName");
		dropDown.setToSubmit(false);
		if (_currentSchool != null){
			dropDown.setSelectedElement((String) _currentSchool.getPrimaryKey());
		}

		return getInputContainer("cacc_testposts_school", "School", dropDown);
	}
	
	protected void handleSave(IWContext iwc, String schoolCategory) {
		//Getting selected school
		try{
			SchoolHome schoolHome = (SchoolHome) IDOLookup.getHome(School.class);			
			int currentSchoolId = new Integer(iwc.getParameter(PAR_PROVIDER)).intValue();
			_currentSchool = schoolHome.findByPrimaryKey("" + currentSchoolId);	
		}catch(RemoteException ex){
			ex.printStackTrace();
		}catch(FinderException ex){ 
			ex.printStackTrace();
		}		
		
		super.handleSave(iwc, schoolCategory);
	}
		
	protected void addBatchRunToQueue(Date month, Date readDate, String schoolCategory, IWContext iwc) throws SchoolCategoryNotFoundException{
		BatchRunQueue.addBatchRunToQueue(month, readDate, schoolCategory, _currentSchool, iwc, true);		
	}	
	
		
	
	private Collection getSchools(IWContext iwc){
		Collection providers = new ArrayList();		
		try{
			SchoolHome home = (SchoolHome) IDOLookup.getHome(School.class);	
			SchoolCategory category = getCurrentSchoolCategory(iwc);
			if (category != null){			
				providers = home.findAllByCategory(category);
			}
		}catch(RemoteException ex){
			ex.printStackTrace();
		}catch(FinderException ex){ 
			ex.printStackTrace();
		}	
		return providers;	
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
	

		

}