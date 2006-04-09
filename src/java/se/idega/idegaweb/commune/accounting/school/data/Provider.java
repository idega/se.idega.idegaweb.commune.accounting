/*
 * $Id: Provider.java,v 1.11 2006/04/09 11:53:33 laddi Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.school.data;

import java.rmi.RemoteException;
import javax.ejb.FinderException;

import se.idega.idegaweb.commune.care.data.ProviderAccountingProperties;
import se.idega.idegaweb.commune.care.data.ProviderAccountingPropertiesHome;
import se.idega.idegaweb.commune.care.data.ProviderType;

import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolHome;

/**
 * This class is a holder for a school bean and provider accounting information.
 * <p>
 * Last modified: $Date: 2006/04/09 11:53:33 $ by $Author: laddi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.11 $
 */
public class Provider {

	private School school = null;
	private ProviderAccountingProperties properties = null;
			
	/**
	 * Constructs a new provider object with the specified school
	 * @param school the school for the provider
	 */
	public Provider(School school) {
		try {
			this.school = school;
			if (school != null) {
				ProviderAccountingPropertiesHome h = (ProviderAccountingPropertiesHome) com.idega.data.IDOLookup.getHome(ProviderAccountingProperties.class);
				this.properties = h.findByPrimaryKey(school.getPrimaryKey()); 
			}
		} catch (RemoteException e) {
		} catch (FinderException e) {}
	}
			
	/**
	 * Constructs a new provider object by retrieving the school
	 * and accounting properties for the provider.
	 * @param schoolId the school id for the provider
	 */
	public Provider(int schoolId) {
		try {
			SchoolHome home = (SchoolHome) com.idega.data.IDOLookup.getHome(School.class);
			this.school = home.findByPrimaryKey(new Integer(schoolId));
			if (this.school != null) {
				ProviderAccountingPropertiesHome h = (ProviderAccountingPropertiesHome) com.idega.data.IDOLookup.getHome(ProviderAccountingProperties.class);
				this.properties = h.findByPrimaryKey(new Integer(schoolId)); 
			}
		} catch (RemoteException e) {
		} catch (FinderException e) {}
	}
	
	/**
	 * Returns the school object for this provider.
	 */
	public School getSchool() {
		return this.school;
	}
	
	/**
	 * Returns the accounting properties for this provider.
	 */
	public ProviderAccountingProperties getAccountingProperties() {
		return this.properties;
	}
	
	public int getProviderTypeId() {
		if (this.properties != null) {
			return this.properties.getProviderTypeId();
		} else {
			return -1;
		}
	}

	public ProviderType getProviderType() {
		if (this.properties != null) {
			return this.properties.getProviderType();
		} else {
			return null;
		}
	}

	public String getStatisticsType() {
		if (this.properties != null) {
			String s = this.properties.getStatisticsType();
			if (s != null) {
				return s;
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

	public boolean getPaymentByInvoice() {
		if (this.properties != null) {
			return this.properties.getPaymentByInvoice();
		} else {
			return false;
		}
	}

	public boolean getStateSubsidyGrant() {
		if (this.properties != null) {
			return this.properties.getStateSubsidyGrant();
		} else {
			return false;
		}
	}

	public String getPostgiro() {
		if (this.properties != null) {
			return this.properties.getPostgiro();
		} else {
			return "";
		}
	}

	public String getBankgiro() {
		if (this.properties != null) {
			return this.properties.getBankgiro();
		} else {
			return "";
		}
	}
	
	public String getGiroText() {
		if (this.properties != null) {
			return this.properties.getGiroText();
		} else {
			return "";
		}		
	}

	public String getOwnPosting() {
		if (this.properties != null) {
			return this.properties.getOwnPosting();
		} else {
			return null;
		}
	}

	public String getDoublePosting() {
		if (this.properties != null) {
			return this.properties.getDoublePosting();
		} else {
			return null;
		}
	}
	
	public String toString(){
		return getSchool().getName();
	}
}
