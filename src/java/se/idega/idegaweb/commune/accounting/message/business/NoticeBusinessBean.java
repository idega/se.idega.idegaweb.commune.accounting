/*
 * $Id: NoticeBusinessBean.java,v 1.11 2004/04/13 14:06:33 anders Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.message.business;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.idega.idegaweb.commune.accounting.school.data.Provider;
import se.idega.idegaweb.commune.message.business.MessageBusiness;
import se.idega.idegaweb.commune.message.data.Message;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolCategory;
import com.idega.block.school.data.SchoolType;
import com.idega.block.school.data.SchoolUser;
import com.idega.core.contact.data.Email;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;

/** 
 * Business logic for notice messages.
 * <p>
 * Last modified: $Date: 2004/04/13 14:06:33 $ by $Author: anders $
 *
 * @author Anders Lindman
 * @version $Revision: 1.11 $
 */
public class NoticeBusinessBean extends com.idega.business.IBOServiceBean implements NoticeBusiness  {

	private final static String KP = "notice_error."; // key prefix 

	public final static String KEY_EMPTY_BODY = KP + "empty_body";
	public final static String KEY_OPERATIONAL_FIELDS_EMPTY = KP + "operational_fields_empty";
	public final static String KEY_SYSTEM_ERROR = KP + "system_error";

	public final static String DEFAULT_EMPTY_BODY = "P�minnelsen kan inte vara tom.";
	public final static String DEFAULT_OPERATIONAL_FIELDS_EMPTY = "Minst en huvudverksamhet m?ste v?ljas.";
	public final static String DEFAULT_SYSTEM_ERROR = "P�minnelsen kunde inte skickas p.g.a. tekniskt fel.";
	
	/**
	 * Send message and e-mail to all administrators for schools belonging
	 * to the specified operational fields.
	 * @param subject the message subject
	 * @param body the message body
	 * @param operationalFields the operational field ids
	 * @return a collection of {school_name, headmaster} 
	 * @throws NoticeException if incomplete parameters or technical send error
	 */
	public Collection sendNotice(String subject, String body, String[] operationalFields) throws NoticeException {
		if (body.equals("")) {
			throw new NoticeException(KEY_EMPTY_BODY, DEFAULT_EMPTY_BODY);
		}
		if (operationalFields == null) {
			throw new NoticeException(KEY_OPERATIONAL_FIELDS_EMPTY, DEFAULT_OPERATIONAL_FIELDS_EMPTY);
		}
		Map schoolCategories = new HashMap();
		for (int i = 0; i < operationalFields.length; i++) {
			schoolCategories.put(operationalFields[i], operationalFields[i]);
		}
		
		Collection c = new ArrayList();
		try {
			SchoolBusiness sb = getSchoolBusiness();
			SchoolCategory childCareCategory = sb.getCategoryChildcare();
			String childCareCategoryId = childCareCategory.getCategory();
			HashMap messageReceivers = new HashMap();
			HashMap emailReceivers = new HashMap();
			Collection schoolTypes = sb.findAllSchoolTypes();
			Iterator iter = schoolTypes.iterator();
			while (iter.hasNext()) {
				SchoolType st = (SchoolType) iter.next();
				String sc = st.getSchoolCategory();				
				if (!schoolCategories.containsKey(sc)) {
					continue;
				}
				int schoolTypeId = ((Integer) st.getPrimaryKey()).intValue();
				Collection schools = sb.findAllSchoolsByType(schoolTypeId);
				Iterator iter2 = schools.iterator();
				while (iter2.hasNext()) {
					School school = (School) iter2.next();
					Collection users = sb.getSchoolUsers(school);
					Iterator iter3 = users.iterator();
					while (iter3.hasNext()) {
						SchoolUser schoolUser = (SchoolUser) iter3.next();
						User user = schoolUser.getUser();
						Provider provider = new Provider(school);
						if (!sc.equals(childCareCategoryId) || 
								(!school.getCentralizedAdministration() && !provider.getPaymentByInvoice())) {
							if (messageReceivers.get(user.getPrimaryKey()) == null) {
								String[] s = new String[2];
								s[0] = school.getName();
								s[1] = user.getName();
								c.add(s);
								boolean sendEMail = true;
								Email email = getUserBusiness().getUserMail(user);
								String emailAddress = null;
								if (email != null) {
									emailAddress = email.getEmailAddress();
								}
								if (emailAddress != null) {
									if (emailReceivers.get(emailAddress) != null) {
										sendEMail = false;
									}
									emailReceivers.put(emailAddress, user);
								}
								Message message = getMessageBusiness().createUserMessage(null, user, null, null, subject, body, body, false, null, false, sendEMail);
								message.store();
								messageReceivers.put(user.getPrimaryKey(), user);
							}
						}
					}
				}
			}
		} catch (RemoteException e) {
			throw new NoticeException(KEY_SYSTEM_ERROR, DEFAULT_SYSTEM_ERROR);
		}
		return c;
	}

	/**
	 * Returns school business. 
	 */	
	public SchoolBusiness getSchoolBusiness() throws RemoteException {
		return (SchoolBusiness) this.getServiceInstance(SchoolBusiness.class);
	}

	/**
	 * Returns user business. 
	 */	
	public UserBusiness getUserBusiness() throws RemoteException {
		return (UserBusiness) this.getServiceInstance(UserBusiness.class);
	}

	/**
	 * Returns message business. 
	 */	
	public MessageBusiness getMessageBusiness() throws RemoteException {
		return (MessageBusiness) this.getServiceInstance(MessageBusiness.class);
	}
}
