/*
 * $Id: PaymentAuthorizationBusinessBean.java,v 1.3 2004/03/23 12:08:45 roar Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.invoice.business;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;

import se.idega.idegaweb.commune.accounting.invoice.data.ConstantStatus;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentHeader;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentHeaderHome;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecord;
import se.idega.idegaweb.commune.accounting.invoice.data.PaymentRecordHome;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.childcare.data.ChildCareContract;
import se.idega.idegaweb.commune.childcare.data.ChildCareContractHome;
import se.idega.idegaweb.commune.message.business.MessageBusiness;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.business.SchoolUserBusiness;
import com.idega.block.school.data.School;
import com.idega.business.IBOLookup;
import com.idega.business.IBOServiceBean;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;


/**
 * This business handles the logic for Payment authorisation
 * 
 * <p>
 * $Id: PaymentAuthorizationBusinessBean.java,v 1.3 2004/03/23 12:08:45 roar Exp $
 *
 * @author Kelly
 */
public class PaymentAuthorizationBusinessBean extends IBOServiceBean implements PaymentAuthorizationBusiness {

	private final static String KP = "payment_authorization."; // key prefix 

	public final static String KEY_AUTH_MESSAGE_SUBJECT = KP + "auth_subject";

	/**
	 * Authorizes the payments. 
	 * Sets authorization date in PaymentRecord
	 * and sets status i PaymentRecord from U to P
	 * @return 
	 */
	public void authorizePayments(User user) {
	
		try {
			School provider = getCommuneUserBusiness().getProviderForUser(user);
			int providerID = Integer.parseInt(provider.getPrimaryKey().toString());
			Iterator payments; 
			payments = getPaymentHeaderHome().
					findByStatusAndSchoolId(ConstantStatus.BASE, providerID).iterator();
			while (payments.hasNext()) {
				Date today = new Date(System.currentTimeMillis());
				PaymentHeader ph = (PaymentHeader) payments.next();
				Iterator records = getPaymentRecordHome().findByPaymentHeader(ph).iterator();
				while(records.hasNext()){
					PaymentRecord pr = (PaymentRecord) records.next();
					pr.setStatus(ConstantStatus.PRELIMINARY);
					pr.store();
				}

				ph.setStatus(ConstantStatus.PRELIMINARY);
				ph.setSignaturelID(user);
				ph.setDateAttested(today);
				ph.store();
			}
							
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hasAuthorizablePayments(User user) {
		boolean ret = false;
		try {
			School provider = getCommuneUserBusiness().getProviderForUser(user);
			int providerID = Integer.parseInt(provider.getPrimaryKey().toString());
			Collection payments = getPaymentHeaderHome().
					findByStatusAndSchoolId(ConstantStatus.BASE, providerID);
			if (! payments.isEmpty()) {
				ret = true;
			}
							
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String getProviderNameForUser(User user) {
		String name = ""; 
		try {
			School provider = getCommuneUserBusiness().getProviderForUser(user);
			name = provider.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}	
	
	/**
	 * Returns PaymentHeaderHome home 
	 */	
	protected SchoolUserBusiness getSchoolUserBusiness(IWContext iwc) throws RemoteException {
		return (SchoolUserBusiness) IBOLookup.getServiceInstance(iwc, SchoolUserBusiness.class);
	}
		
		 
	/**
	 * Returns PaymentHeaderHome home 
	 */	
	protected PaymentHeaderHome getPaymentHeaderHome() throws RemoteException {
			return (PaymentHeaderHome) IDOLookup.getHome(PaymentHeader.class);
	}
	/**
	 * Returns ChildCareContracts home 
	 */	
	protected ChildCareContractHome getChildCareContractHome() throws RemoteException {
			return (ChildCareContractHome) IDOLookup.getHome(ChildCareContract.class);
	}

	/**
	 * Returns Payment Records home 
	 */	
	protected PaymentRecordHome getPaymentRecordHome() throws RemoteException {
			return (PaymentRecordHome) IDOLookup.getHome(PaymentRecord.class);
	}

	/**
	 * Returns school business. 
	 */	
	protected SchoolBusiness getSchoolBusiness() throws RemoteException {
		return (SchoolBusiness) this.getServiceInstance(SchoolBusiness.class);
	}

	/**
	 * Returns user business. 
	 */	
	protected UserBusiness getUserBusiness() throws RemoteException {
		return (UserBusiness) this.getServiceInstance(UserBusiness.class);
	}

	/**
	 * Returns message business. 
	 */	
	protected MessageBusiness getMessageBusiness() throws RemoteException {
		return (MessageBusiness) this.getServiceInstance(MessageBusiness.class);
	}
	
	/**	
	 * Returns school commune business 
	 */	
	protected CommuneUserBusiness getCommuneUserBusiness() throws RemoteException {
			return (CommuneUserBusiness) getServiceInstance(CommuneUserBusiness.class);
	}
}
