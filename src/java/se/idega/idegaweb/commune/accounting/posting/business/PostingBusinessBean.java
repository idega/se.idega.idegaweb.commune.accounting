/*
 * $Id: PostingBusinessBean.java,v 1.35 2003/11/17 12:49:53 roar Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.posting.business;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.export.data.ExportDataMapping;
import se.idega.idegaweb.commune.accounting.posting.data.PostingField;
import se.idega.idegaweb.commune.accounting.posting.data.PostingFieldHome;
import se.idega.idegaweb.commune.accounting.posting.data.PostingParameters;
import se.idega.idegaweb.commune.accounting.posting.data.PostingParametersHome;
import se.idega.idegaweb.commune.accounting.posting.data.PostingString;
import se.idega.idegaweb.commune.accounting.posting.data.PostingStringHome;
import se.idega.idegaweb.commune.accounting.regulations.data.ActivityType;
import se.idega.idegaweb.commune.accounting.regulations.data.ActivityTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.CommuneBelongingType;
import se.idega.idegaweb.commune.accounting.regulations.data.CommuneBelongingTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.Regulation;
import se.idega.idegaweb.commune.accounting.school.data.Provider;

import com.idega.block.school.data.SchoolCategory;
import com.idega.block.school.data.SchoolType;
import com.idega.business.IBOLookup;
import com.idega.core.location.data.Commune;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.util.IWTimestamp;

/**
 * Business logic for creating assembling, and disassembling the posting strings, and for setting up 
 * the rules and structures of the posting strings (C&P req. spec. konteringsstrangar och 
 * konteringsfalt).
 * 
 * @author Joakim
 * @author Kjell Lindman
 * 
 * 
 */
public class PostingBusinessBean extends com.idega.business.IBOServiceBean implements PostingBusiness  {
	
	public static final int JUSTIFY_LEFT = 0;
	public static final int JUSTIFY_RIGHT = 1;

	private final static String KEY_ERROR_POST_PARAM_CREATE = "posting_param_err.create";
	private final static String KEY_ERROR_POST_NOT_FOUND = "posting_parm_edit.post_not_found";
	private final static String KEY_ERROR_POST_PARAM_DATE_ORDER = "posting_parm_edit.post_dateorder";
	private final static String KEY_ERROR_POST_PARAM_SAME_ENTRY = "posting_parm_edit.post_sameentry";
	private final static String KEY_ERROR_POST_PARAM_SCHOOL_YEAR_ORDER = "posting_parm_edit.post_school_year_order";
	private final static String KEY_ERROR_POST_PARAM_DATE_MISSING = "posting_parm_edit.post_date_empty";
	private final static String KEY_ERROR_POST_PARAM_DATE_NULL = "posting_parm_edit.post_date_null";
	private final static String KEY_ERROR_POST_PARAM_SCHOOL_YEARS = "posting_parm_edit.post_school_years";
	
	/**
	 * Merges two posting strings according to 15.2 and 15.3 in the Kravspecification Check & Peng
	 * @param first posting string
	 * @param second posting string
	 * @param date for valid time of posting rules
	 * @return the merged posting strings
	 * @throws RemoteException
	 * @author Joakim
	 */
	public String generateString(String first, String second, Date date) throws RemoteException, PostingException {
		StringBuffer ret = new StringBuffer();		//used to build together returnstring
		String temp;
		int readPointer = 0;						//Pointer to place where next field is
		int fieldLength=0;							//Length of next field. Fetched from the definition 

		PostingStringHome ksHome = getPostingStringHome();
		PostingFieldHome kfHome = getPostingFieldHome();
		try {
			PostingString posting = ksHome.findPostingStringByDate(date);
			Collection list = kfHome.findAllFieldsByPostingString(Integer.parseInt(posting.getPrimaryKey().toString()));
			Iterator iter = list.iterator();
			while (iter.hasNext()) 
			{
				PostingField field = (PostingField)iter.next();
				fieldLength = field.getLen();
				temp = trim(first.substring(readPointer,readPointer+fieldLength),field);
				if (temp.length()==0)
				{
					temp = trim(second.substring(readPointer,readPointer+fieldLength),field);
				}
				temp = pad(temp,field);
				ret.append(temp);
				readPointer += fieldLength;
			}
			if (readPointer != first.length()){
				System.out.println("Error: Wrong length of the string used for posting. Expected: "+readPointer+
				"  Actual: "+first.length());
			}
		} catch (Exception e) {
			System.out.println("Error: The postingt definition and the posting strings did not match.");
			System.out.println("First posting string: '"+first+"'");
			System.out.println("Second posting string:'"+second+"'");
			System.out.println("Date for posting rule: "+date.toString());
			e.printStackTrace();
			throw new PostingException("posting.exception","malformated posting field encountered");
		}
		return ret.toString();
	}
	
	/**
	 * Validates that all the required fields have been set. If they are not set, a
	 * MissingMandatoryFieldException will be thrown.
	 * 
	 * @param postingString
	 * @param date
	 * @throws MissingMandatoryFieldException
	 * @throws PostingException
	 * 
	 * @author Joakim
	 */
	public void validateString(String postingString, Date date) throws MissingMandatoryFieldException, PostingException{
		int fieldLength, readPointer = 0;
		try {
			PostingStringHome ksHome = getPostingStringHome();
			PostingFieldHome kfHome = getPostingFieldHome();
			PostingString posting = ksHome.findPostingStringByDate(date);
			Collection list = kfHome.findAllFieldsByPostingString(Integer.parseInt(posting.getPrimaryKey().toString()));
			Iterator iter = list.iterator();
			//Go through all fields
			while (iter.hasNext()) 
			{
				PostingField field = (PostingField)iter.next();
				fieldLength = field.getLen();
				if(field.getIsMandatory()){
					//Check if mandatory field is empty
					if (trim(postingString.substring(readPointer,readPointer+fieldLength),field).length()==0)
					{
						throw new MissingMandatoryFieldException(field.getFieldTitle());
					}
				}
				readPointer += fieldLength;
			}
		} catch (Exception e) {
			System.out.println("Error: The postingt definition and the posting strings did not match.");
			System.out.println("First posting string: '"+postingString+"'");
			System.out.println("Date for posting rule: "+date.toString());
			e.printStackTrace();
			throw new PostingException("posting.exception","malformated posting field encountered");
		}
	}
	
	
	/**
	 * extractField
	 * Retrieves portion of a PostingString 
	 * @param ps posting string
	 * @param readPointer index in string
	 * @param postingField The field itself
	 * @return an extracted part of the ps string
	*/
	public String extractField(String ps, int readPointer, int fieldLength, PostingField field) {
		if (ps == null) {
			return "";
		}
		try {
			return trim(ps.substring(readPointer, readPointer + fieldLength), field);
		} catch (StringIndexOutOfBoundsException e) {
			return "";
		}

	}

	/**
	 * H�mta konteringsinformation
	 * Retrieves accounting default information. Matching on the main rules like 
	 * Verksamhet, Regspectyp, Bolagstyp, kommuntill.h�righet is done via primary keys
	 *  
	 * @param date Date when the transaction took place
	 * 
	 * @param act_id Verksamhet. Related to:
	 * @see com.idega.block.school.data.SchoolType#
	 * 
	 * @param reg_id Reg.Spec type. Related to:
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType#
	 * 
	 * @param com_id Company type
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.CompanyType#
	 * This bean will be moved later to the Idega school:com.idega.block.school.data
	 * 
	 * @param com_bel_id Commmune belonging type
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.CommuneBelongingType#
	 * This will probably be moved into the accounting.data
	 * 
	 * make parameter 0 for macthing of all
	 * 
	 * @return PostingParameters @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters#
	 * @throws PostingParametersException
	 * 
	 * @author Kjell
	 */
	public PostingParameters getPostingParameter(Date date, int act_id, 
			int reg_id, String com_id, int com_bel_id) throws PostingParametersException {
			return getPostingParameter(date, act_id, reg_id, com_id, com_bel_id, 0, 0);
	}
	
	public PostingParameters getPostingParameter(Date date, int act_id, 
			int reg_id, String com_id, int com_bel_id, int schoolYear1_id, int schoolYear2_id) throws PostingParametersException {
		
		try {
			int match;
			PostingParametersHome home = getPostingParametersHome();
			
			Collection ppCol = home.findPostingParametersByDate(date);
			Iterator iter = ppCol.iterator();

			while (iter.hasNext())  {
				PostingParameters pp = (PostingParameters) iter.next();
				String the_act_id = pp.getActivity() != null ? 
						pp.getActivity().getPrimaryKey().toString() : "0";
				String the_reg_id = pp.getRegSpecType() != null ? 
						pp.getRegSpecType().getPrimaryKey().toString() : "0";
				String the_com_id = pp.getCompanyType() != null ? 
						pp.getCompanyType().getPrimaryKey().toString() : "0";
				String the_com_bel_id = pp.getCommuneBelonging() != null ? 
						pp.getCommuneBelonging().getPrimaryKey().toString() : "0";

				String the_school_year1_id = pp.getSchoolYear1() != null ? 
						pp.getSchoolYear1().getPrimaryKey().toString() : "0";

				String the_school_year2_id = pp.getSchoolYear2() != null ? 
						pp.getSchoolYear2().getPrimaryKey().toString() : "0";
				
				match = 0;
				
				if (act_id == 0) { 
					match++;
				} else if (Integer.parseInt(the_act_id) == act_id) { 
					match++; 
				}

				if (reg_id == 0) { 
					match++;
				} else if (Integer.parseInt(the_reg_id) == reg_id) { 
					match++; 
				}

				if (com_id == null) { 
					match++;
				} else if (the_com_id.equals(com_id)) { 
					match++; 
				}

				if (com_bel_id == 0) { 
					match++;
				} else if (Integer.parseInt(the_com_bel_id) == com_bel_id) { 
					match++; 
				}

				if (schoolYear1_id == 0) { 
					match++;
				} else if (Integer.parseInt(the_school_year1_id) == schoolYear1_id) { 
					match++; 
				}

				if (schoolYear2_id == 0) { 
					match++;
				} else if (Integer.parseInt(the_school_year2_id) == schoolYear2_id) { 
					match++; 
				}

				if (match == 6) {
					return pp;
				}
			}
			throw new PostingParametersException(KEY_ERROR_POST_NOT_FOUND, "S�kt parameter hittades ej");
		} catch (RemoteException e) {
			return null;
		} catch (FinderException e) {
			return null;
		}
	}	

	/**
	 * Save posting parameter
	 *  
	 * @param sppID PostingParameter ID
	 * @param periodFrom from date
	 * @param periodTo to date
	 * @param changedSign changed by user xxx
	 * @param activityID id for the activity in this parameter 
	 * @param regSpecTypeID id for the regulation specification in this parameter 
	 * @param companyTypeID id for the Company type that this parameter has
	 * @param communeBelonging id to what commune this parameter belongs to
	 * @param ownPostingString  the own posting string
	 * @param doublePostingString  the double accounting posting string
	 * @see se.idega.idegaweb.commune.accounting.posting.-data.PostingParameter#
	 * @author kelly
     */
	public void savePostingParameter(String sppID,
				Date periodeFrom, 
				Date periodeTo,
				String changedSign,
				String activityID,
				String regSpecTypeID,
				String companyTypeID,
				String communeBelongingID,
				String schoolYear1ID,
				String schoolYear2ID,
				String studyPathID,
				String ownPostingString,
				String doublePostingString
			) throws PostingParametersException, RemoteException {

			PostingParametersHome home = null;
			PostingParameters pp = null;
			int parm1 = 0;
			int parm2 = 0;
			String parm3 = null;
			int parm4 = 0;
			int parm5 = 0;
			int parm6 = 0;
			int parm7 = 0;

			
			if (	((schoolYear1ID.compareTo("0") != 0) && 
					( schoolYear2ID.compareTo("0") == 0)) ||
					((schoolYear1ID.compareTo("0") == 0) && 
					( schoolYear2ID.compareTo("0") != 0))) {
				throw new PostingParametersException(KEY_ERROR_POST_PARAM_SCHOOL_YEARS, "Ange skol�rsintervall!");			
			}
			
			if (periodeFrom == null || periodeTo == null) {
				throw new PostingParametersException(KEY_ERROR_POST_PARAM_DATE_NULL, "Datum m�ste fyllas i!");			
			}

			if (schoolYear1ID != null && schoolYear1ID != null) {
				if (Integer.parseInt(schoolYear1ID) > Integer.parseInt(schoolYear2ID)) {
					throw new PostingParametersException(KEY_ERROR_POST_PARAM_SCHOOL_YEAR_ORDER, "Fel ordning bland skol�ren!");			
				}
			}
			if(periodeFrom == null || periodeTo == null) {
				throw new PostingParametersException(KEY_ERROR_POST_PARAM_DATE_MISSING, "Datum saknas!");			
			}
			if (periodeFrom.after(periodeTo)) {
				throw new PostingParametersException(KEY_ERROR_POST_PARAM_DATE_ORDER, "Fr�n datum kan ej vara senare �n tom datum!");			
			}
		
			try {
				home = (PostingParametersHome) IDOLookup.getHome(PostingParameters.class);
	
				if (activityID == null) activityID = "0"; 
				if (regSpecTypeID == null) regSpecTypeID = "0"; 
				if (companyTypeID == null) companyTypeID = "0"; 
				if (communeBelongingID == null) communeBelongingID = "0"; 
				if (schoolYear1ID == null) schoolYear1ID = "0";
				if (schoolYear2ID == null) schoolYear2ID = "0";
				if (studyPathID == null) studyPathID = "0";
				
				parm1 = Integer.parseInt(activityID);
				parm2 = Integer.parseInt(regSpecTypeID);
				parm3 = companyTypeID;
				parm4 = Integer.parseInt(communeBelongingID);
				parm5 = Integer.parseInt(schoolYear1ID);
				parm6 = Integer.parseInt(schoolYear2ID);
				parm7 = Integer.parseInt(studyPathID);

				if (searchPP(
						periodeFrom, 
						periodeTo, 
						ownPostingString, 
						doublePostingString,
						parm1,
						parm2,
						parm3,
						parm4,
						parm5,
						parm6,
						parm7
					)) {
					throw new PostingParametersException(KEY_ERROR_POST_PARAM_SAME_ENTRY, "Denna post finns redan sparad!");			
				}
				

				int ppID = 0;
				if (sppID != null) {
					ppID = Integer.parseInt(sppID);
				}
				pp = null;
				if (ppID != 0) {				
					pp = home.findPostingParameter(ppID);
				}
			} catch (FinderException e) {
				pp = null;
			}
			
			try {
				if (pp == null) {
					pp = home.create();
				}
				pp.setPeriodeFrom(periodeFrom);
				pp.setPeriodeTo(periodeTo);
				pp.setChangedSign(changedSign);
				pp.setChangedDate(IWTimestamp.getTimestampRightNow());
				pp.setPostingString(ownPostingString);
				pp.setDoublePostingString(doublePostingString);
				pp.setActivity(parm1);
				pp.setRegSpecType(parm2);
				pp.setCompanyType(parm3);
				pp.setCommuneBelonging(parm4);
				pp.setSchoolYear1(parm5);
				pp.setSchoolYear2(parm6);
				pp.setStudyPath(parm7);
				pp.store();
			} catch (CreateException ce) {
				throw new PostingParametersException(KEY_ERROR_POST_PARAM_CREATE, "Kan ej skapa parameter");			
			}
	}

	/*
	 * Compares a Posting Parameter with stored parameters
	 * @author Kjell
	 */
	private boolean searchPP(Date from, Date to, String ownPosting, String doublePosting, 
								int code1, int code2, String code3, int code4, int code5, int code6, int code7) {
	
		try {
			if (ownPosting == null || doublePosting == null) {
				return false;
			}			
			PostingParametersHome home = getPostingParametersHome();
			PostingParameters pp = null;
			try {
				pp = home.findPostingParameter(from, to, ownPosting, doublePosting, 
						code1, code2, code3, code4, code5, code6, code7);
			} catch (FinderException e) {}
			
			if (pp != null) {
				return true;
			}
/*			
			Iterator iter = null;
			
			try {
				Collection ppCol = home.findAllPostingParameters();
				iter = ppCol.iterator();
			} catch (FinderException e) {}
			
			if (iter == null) {
				return false;
			}
			while (iter.hasNext())  {
				PostingParameters pp = (PostingParameters) iter.next();
				int eq = 0;
				
				if (pp.getPostingString() != null) {
					if (pp.getPostingString().compareTo(ownPosting) == 0) {
						eq++;
					}
				}
				
				if (pp.getDoublePostingString() != null) {
					if (pp.getDoublePostingString().compareTo(doublePosting) == 0) {
						eq++;
					}
				}
				
				if (pp.getPeriodeFrom() != null) {
					if (pp.getPeriodeFrom().compareTo(from) == 0) {
						eq++;
					}
				}
				
				if (pp.getPeriodeTo() != null) {
					if (pp.getPeriodeTo().compareTo(to) == 0) {
						eq++;
					}
				}
				
				if (pp.getActivity() == null) {
					if (code1 == 0) {
						eq++;
					}
				} else if (Integer.parseInt(pp.getActivity().getPrimaryKey().toString()) == code1) {
					eq++;
				}
				
				if (pp.getRegSpecType() == null) {
					if (code2 == 0) {
						eq++;
					}
				} else if (Integer.parseInt(pp.getRegSpecType().getPrimaryKey().toString()) == code2) {
					eq++;
				}
				
				if (pp.getCompanyType() == null) {
					if (code3.compareTo("0") == 0) {
						eq++;
					}
				} else if (pp.getCompanyType().getPrimaryKey().toString().compareTo(code3) == 0) {
					eq++;
				}	
			
				if (pp.getCommuneBelonging() == null) {
					if (code4 == 0) {
						eq++;
					}
				} else if (Integer.parseInt(pp.getCommuneBelonging().getPrimaryKey().toString()) == code4) {
					eq++;				
				}

				if (pp.getSchoolYear1() == null) {
					if (code5 == 0) {
						eq++;
					}
				} else if (Integer.parseInt(pp.getSchoolYear1().getPrimaryKey().toString()) == code5) {
					eq++;				
				}
					
				if (pp.getSchoolYear2() == null) {
					if (code6 == 0) {
						eq++;
					}
				} else if (Integer.parseInt(pp.getSchoolYear2().getPrimaryKey().toString()) == code6) {
					eq++;				
				}	

				if (pp.getStudyPath() == null) {
					if (code7 == 0) {
						eq++;
					}
				} else if (Integer.parseInt(pp.getStudyPath().getPrimaryKey().toString()) == code7) {
					eq++;				
				}	
				
				if (eq == 11) {
					return true;
				}
			}
*/
		} catch (RemoteException e) {
			return false;
		}
		return false;
	}

	/**
	 * Deletes a posting parameter
	 * @param id PostingParameter ID
	 * @author Kjell
	 * 
	 */
	public void deletePostingParameter(int ppID) throws java.rmi.RemoteException {
		try {
			PostingParameters pp = (PostingParameters) findPostingParameter(ppID);
			pp.remove();
			pp.store();	
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}


	/**
	 * Gets posting parameters for a certain periode
	 * @param from periode (4 digits)
	 * @param to periode (4 digits)
	 * @return collection of posting parameters
	 * @author Kjell
	 * 
	 */
	public Collection findPostingParametersByPeriode(Date from, Date to) {
		try {
			PostingParametersHome home = getPostingParametersHome();
			return home.findPostingParametersByPeriode(from, to);				
		} catch (RemoteException e) {
			return null;
		} catch (FinderException e) {
			return null;
		}
	}	

	/**
	 * Gets all posting parameters
	 * @return collection of posting parameters
	 * @author Kjell
	 */
	public Collection findAllPostingParameters() {
		try {
			PostingParametersHome home = getPostingParametersHome();
			return home.findAllPostingParameters();				
		} catch (RemoteException e) {
			return null;
		} catch (FinderException e) {
			return null;
		}
	}	
	
	
	/**
	 * Gets a posting parameter by the ID
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters# 
	 * @return PostingParameters
	 * @author Kjell
	 */
	public Object findPostingParameter(int id) throws FinderException {
		// Move this
		try {
			PostingParametersHome home = getPostingParametersHome();
			return home.findPostingParameter(id);				
		} catch (RemoteException e) {
			return null;
		} catch (FinderException e) {
			return null;
		}
	}
	/**
	 * Gets a posting parameter by a periode
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters# 
	 * @return PostingParameters
	 * @author Kjell
	 */
	public Object findPostingParameterByPeriode(Date from, Date to) throws FinderException {
		// Move this
		try {
			PostingParametersHome home = getPostingParametersHome();
			return home.findPostingParametersByPeriode(from, to);				
		} catch (RemoteException e) {
			return null;
		} catch (FinderException e) {
			return null;
		}
	}



	/**
	 * Gets a posting field length for a specific date and field number
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingField# 
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingString# 
	 * @return int length of field 
	 * @author Kjell
	 */
	public int getPostingFieldByDateAndFieldNo(Date date, int fieldNo) {
		try {
			PostingStringHome psHome = getPostingStringHome();
			PostingFieldHome pfHome = getPostingFieldHome();
			PostingString ps = psHome.findPostingStringByDate(date); 
			int psID = Integer.parseInt(ps.getPrimaryKey().toString());
			PostingField pf = pfHome.findFieldByPostingStringAndFieldNo(psID, fieldNo);
			return pf.getLen();				
		} catch (RemoteException e) {
			return 0;
		} catch (FinderException e) {
			return 0;
		}
	}


	/**
	 * Gets a posting fields for a specific date
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingField# 
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingString# 
	 * @return Collection PostingField 
	 * @author Kjell
	 */
	public Collection getAllPostingFieldsByDate(Date date) {
		try {
			PostingStringHome psHome = getPostingStringHome();
			PostingFieldHome pfHome = getPostingFieldHome();
			PostingString ps = psHome.findPostingStringByDate(date); 
			int psID = Integer.parseInt(ps.getPrimaryKey().toString());
			return pfHome.findAllFieldsByPostingString(psID);				
		} catch (RemoteException e) {
			return null;
		} catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Pads the string according to the rules in Posting field
	 * @param in the string to pad
	 * @param postingField holds the rules for padding
	 * @return the padded string
	 */
	public String pad(String in, PostingField postingField){
		StringBuffer ret = new StringBuffer(in);
		//Add the padding character on right side of the string until it is of right size
		if (postingField.getJustification() == JUSTIFY_LEFT) {
			while (ret.length()<postingField.getLen()){
				ret.append(postingField.getPadChar());
			}
		} else {
			while (ret.length()<postingField.getLen()){
				ret.insert(0,postingField.getPadChar());
			}
		}
		return ret.toString();
	}
	
	/**
	 * Trims the string according to the rules in Posting field
	 * @param in the string to trim
	 * @param postingField holds the rules for trimming 
	 * @return the trimmed string
	 */
	public String trim(String in, PostingField postingField) {
		String ret = "";
		int i;
		//Remove all padding characters until a non padding character is encountered
		if (postingField.getJustification() == JUSTIFY_LEFT){
			for(i=postingField.getLen();i>0 && in.charAt(i) == postingField.getPadChar();i++){}
			ret = in.substring(0,i);
		} else {
			for(i=0;i<postingField.getLen() && in.charAt(i) == postingField.getPadChar();i++){}
			ret = in.substring(i,postingField.getLen());
		}
		return ret;
	}
	
	/**
	 * Does the search in the postings
	 * @param iwc
	 * @param category
	 * @param type
	 * @param reg
	 * @param provider
	 * @param date
	 * @return the Posting strings as String[]
	 */
	public String[] getPostingStrings(IWContext iwc, SchoolCategory category, SchoolType type, Regulation reg, Provider provider, Date date) throws PostingException{
		

		String ownPosting = null, doublePosting = null;
		if (reg != null){
			int regSpecType = new Integer("" + reg.getRegSpecType().getPrimaryKey()).intValue();
		
			try{
				Commune commune = provider.getSchool().getCommune();
				CommuneBelongingTypeHome home = (CommuneBelongingTypeHome) IDOLookup.getHome(CommuneBelongingType.class);
				CommuneBelongingType cbt = commune.getIsDefault() ? home.findHomeCommune() : home.findNoHomeCommune();
							
				ExportDataMapping categoryPosting = (ExportDataMapping) IDOLookup.getHome(ExportDataMapping.class).
					findByPrimaryKeyIDO(category.getPrimaryKey());				
				//Set the posting strings
				PostingBusiness postingBusiness = (PostingBusiness) IBOLookup.getServiceInstance(iwc.getApplicationContext(), PostingBusiness.class);
		
				PostingParameters parameters;
				parameters = postingBusiness.getPostingParameter(date, ((Integer) type.getPrimaryKey()).intValue(), regSpecType, provider.getSchool().getManagementTypeId(), ((Integer) cbt.getPrimaryKey()).intValue());
		
				ownPosting = parameters.getPostingString();
				ownPosting = postingBusiness.generateString(ownPosting, provider.getOwnPosting(), date);
				ownPosting = postingBusiness.generateString(ownPosting, categoryPosting.getAccount(), date);
				postingBusiness.validateString(ownPosting,date);
		
				doublePosting = parameters.getDoublePostingString();
				doublePosting = postingBusiness.generateString(doublePosting, provider.getDoublePosting(), date);
				doublePosting = postingBusiness.generateString(doublePosting, categoryPosting.getCounterAccount(), date);
				postingBusiness.validateString(doublePosting,date);
			}catch(NullPointerException ex){
				ex.printStackTrace();
				throw new PostingException("TODO...", ex.getMessage()); 
			}catch(RemoteException ex){
				ex.printStackTrace();
				throw new PostingException("", ex.getMessage()); 				
			}catch(FinderException ex){			
				ex.printStackTrace();
				throw new PostingException("", ex.getMessage()); 				
			}catch(PostingException ex){
				ex.printStackTrace();
				throw new PostingException("", ex.getMessage()); 				
			}catch(MissingMandatoryFieldException ex){
				ex.printStackTrace();
				throw new PostingException("", ex.getMessage()); 				
			}catch(PostingParametersException ex){
				ex.printStackTrace();
				throw new PostingException("", ex.getMessage()); 				
			}
		}
		return new String[] {ownPosting, doublePosting};
	}	

	

	protected PostingParametersHome getPostingParametersHome() throws RemoteException {
		return (PostingParametersHome) com.idega.data.IDOLookup.getHome(PostingParameters.class);
	}
	
	protected PostingStringHome getPostingStringHome() throws RemoteException
	{
		return (PostingStringHome) com.idega.data.IDOLookup.getHome(PostingString.class);
	}
	
	protected PostingFieldHome getPostingFieldHome() throws RemoteException
	{
		return (PostingFieldHome) com.idega.data.IDOLookup.getHome(PostingField.class);
	}

	protected ActivityTypeHome getActivityTypeHome() throws RemoteException {
		return (ActivityTypeHome) com.idega.data.IDOLookup.getHome(ActivityType.class);
	}
	
	

}
