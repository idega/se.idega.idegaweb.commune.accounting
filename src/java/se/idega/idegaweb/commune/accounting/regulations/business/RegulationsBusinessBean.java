/*
 * $Id: RegulationsBusinessBean.java,v 1.92 2003/12/10 17:11:40 palli Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */

package se.idega.idegaweb.commune.accounting.regulations.business;

import is.idega.idegaweb.member.business.MemberFamilyLogic;
import is.idega.idegaweb.member.business.NoCustodianFound;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import se.idega.idegaweb.commune.accounting.invoice.business.RegularInvoiceBusiness;
import se.idega.idegaweb.commune.accounting.invoice.data.RegularInvoiceEntry;
import se.idega.idegaweb.commune.accounting.regulations.data.ActivityType;
import se.idega.idegaweb.commune.accounting.regulations.data.ActivityTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.CommuneBelongingType;
import se.idega.idegaweb.commune.accounting.regulations.data.CommuneBelongingTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.Condition;
import se.idega.idegaweb.commune.accounting.regulations.data.ConditionHome;
import se.idega.idegaweb.commune.accounting.regulations.data.ConditionParameter;
import se.idega.idegaweb.commune.accounting.regulations.data.ConditionType;
import se.idega.idegaweb.commune.accounting.regulations.data.ConditionTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.MainRule;
import se.idega.idegaweb.commune.accounting.regulations.data.MainRuleHome;
import se.idega.idegaweb.commune.accounting.regulations.data.PaymentFlowType;
import se.idega.idegaweb.commune.accounting.regulations.data.PaymentFlowTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.PostingDetail;
import se.idega.idegaweb.commune.accounting.regulations.data.ProviderType;
import se.idega.idegaweb.commune.accounting.regulations.data.ProviderTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.Regulation;
import se.idega.idegaweb.commune.accounting.regulations.data.RegulationHome;
import se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType;
import se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.SpecialCalculationType;
import se.idega.idegaweb.commune.accounting.regulations.data.SpecialCalculationTypeHome;
import se.idega.idegaweb.commune.accounting.regulations.data.VATRule;
import se.idega.idegaweb.commune.accounting.regulations.data.VATRuleHome;
import se.idega.idegaweb.commune.accounting.regulations.data.YesNo;
import se.idega.idegaweb.commune.accounting.regulations.data.YesNoHome;
import se.idega.idegaweb.commune.accounting.resource.data.Resource;
import se.idega.idegaweb.commune.accounting.resource.data.ResourceHome;
import se.idega.idegaweb.commune.accounting.userinfo.business.UserInfoService;
import se.idega.idegaweb.commune.accounting.userinfo.data.BruttoIncome;
import se.idega.idegaweb.commune.childcare.data.ChildCareContract;

import com.idega.block.school.data.SchoolCategoryBMPBean;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolManagementType;
import com.idega.block.school.data.SchoolManagementTypeHome;
import com.idega.block.school.data.SchoolType;
import com.idega.block.school.data.SchoolTypeHome;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

/**
 * @author Kelly Lindman
 * 
 */
public class RegulationsBusinessBean extends com.idega.business.IBOServiceBean implements RegulationsBusiness {

	private final static String KP = "regulation_spec_type_error."; // key prefix 
	private final static String LP = "cacc_regulation."; // Localization prefex
	public final static String KEY_CANNOT_DELETE_REG_SPEC_TYPE = KP + "cannot_delete_reg_spec_type_regulation";
	public final static String DEFAULT_CANNOT_DELETE_REG_SPEC_TYPE = "Kunde inte radera regelspecifikationstypen";
	public final static String KEY_CANNOT_SAVE_REG_SPEC_TYPE = KP + "cannot_save_reg_spec_type_regulation";
	public final static String DEFAULT_CANNOT_SAVE_REG_SPEC_TYPE = "Kunde inte spara regelspecifikationstypen";
	public final static String KEY_ERROR_PARAM_DATE_ORDER = KP + "from_date_lare_than_to_date";
	public final static String KEY_ERROR_REGULATION_CREATE = KP + "cannot_create";
	public final static String KEY_ERROR_PARAM_NAME_EMPTY = KP + "name_empty";
	public final static String KEY_ERROR_PARAM_ORDER_EMPTY = KP + "order_empty";
	public final static String KEY_GENERAL_ERROR = KP + "general_error";
	public final static String DEFAULT_GENERAL_ERROR = "Systemfel";
	public final static String KEY_ERROR_PARAM_MAIN_OPERATION = "main_op_error";
	public final static String KEY_ERROR_PARAM_REG_SPEC_EMPTY = "reg_spec_empty";
	public final static String KEY_ERROR_PARAM_OVERLAP = "overlap_error";
	public final static String KEY_ERROR_PARAM_DATE_MISSING = "missing_date_error";
	public final static String KEY_ERROR_PARAM_PAYMENT_FLOW_TYPE = "payment_flow_error";
	public final static String KEY_ERROR_PARAM_CONDITION_TYPE = "condition_type_error";
	public final static String KEY_ERROR_REG_SPEC_TYPE = "reg_spec_type_error";
	public final static String KEY_ERROR_VAT_ELIGIBLE = "vat_eligible_error";
	public String getBundleIdentifier() {
		return se.idega.idegaweb.commune.accounting.presentation.AccountingBlock.IW_ACCOUNTING_BUNDLE_IDENTIFER;
	}

	/**
	 * Save regulation. Saves the regultion. If non existing, creates it.
	 *  
	 * @param regID the regulation id
	 * @param periodFrom from date
	 * @param periodTo to date
	 * @param name name of this regulation
	 * @param amount
	 * @param conditionOrder
	 * @param operation
	 * @param paymentFlowType in/out 1/2
	 * @param vatEligible TAX Eligible Yes/No 1/2
	 * @param regSpecType
	 * @param conditionType
	 * @param specialCalculation
	 * @param vatRule
	 * @param changedBy  
	 * @return int of the PK ID for the created/saved regulation
	 * 
	 * @author kelly
	 */
	public int saveRegulation(
		String regID,
		Date periodFrom,
		Date periodTo,
		String name,
		String amount,
		String conditionOrder,
		String operation,
		String paymentFlowType,
		String vatEligible,
		String regSpecType,
		String conditionType,
		String specialCalculation,
		String vatRule,
		String changedBy,
		String discount,
		String maxAmountdiscount)
		throws RegulationException, RemoteException {

		RegulationHome home = null;
		Regulation r = null;
		int amountVal = 0;
		float discountVal = 0;
		float maxAmountDiscountVal = 0;

		Integer conditionOrderID = null;
		Integer regSpecTypeID = null;
		Integer conditionTypeID = null;
		Integer specialCalculationID = null;
		Integer vatRuleID = null;

		//		verify that 
		//		"fr�n datum", 
		//		"tom datum", 
		//		"villkorstyp", 
		//		"villkorsordning", 
		//		"regelspecificeringstyp", 
		//		"momsers�ttning" have values when push SAVE-button.

		if (operation.compareTo("0") == 0) {
			throw new RegulationException(KEY_ERROR_PARAM_MAIN_OPERATION, "Huvudverksamhet m�ste v�ljas");
		}

		if (paymentFlowType.compareTo("0") == 0) {
			throw new RegulationException(KEY_ERROR_PARAM_PAYMENT_FLOW_TYPE, "Str�m m�ste v�ljas");
		}

		if (name.length() == 0) {
			throw new RegulationException(KEY_ERROR_PARAM_NAME_EMPTY, "Namn saknas!");
		}

		if (periodTo == null || periodFrom == null) {
			throw new RegulationException(KEY_ERROR_PARAM_DATE_MISSING, "Datum saknas!");
		}

		if (conditionType.compareTo("0") == 0) {
			throw new RegulationException(KEY_ERROR_PARAM_CONDITION_TYPE, "Villkorstyp m�ste v�ljas");
		}

		if (conditionOrder.length() == 0) {
			throw new RegulationException(KEY_ERROR_PARAM_ORDER_EMPTY, "Villkorsordning saknas!");
		}

		if (regSpecType.compareTo("0") == 0) {
			throw new RegulationException(KEY_ERROR_REG_SPEC_TYPE, "Regelspecificationstyp m�ste v�ljas");
		}

		if (vatEligible.compareTo("0") == 0) {
			throw new RegulationException(KEY_ERROR_VAT_ELIGIBLE, "Momsers�ttning m�ste v�ljas");
		}

		if (periodFrom.after(periodTo)) {
			throw new RegulationException(KEY_ERROR_PARAM_DATE_ORDER, "Fr�n datum kan ej vara senare �n tom datum!");
		}

		try {
			home = (RegulationHome) IDOLookup.getHome(Regulation.class);

			if (amount == null)
				amount = "0";
			if (discount == null)
				discount = "0";
			if (maxAmountdiscount == null)
				maxAmountdiscount = "0";

			if (conditionOrder == null)
				conditionOrder = "";
			if (regSpecType == null)
				regSpecType = "";
			if (conditionType == null)
				conditionType = "";
			if (specialCalculation == null)
				specialCalculation = "";
			if (vatRule == null)
				vatRule = "";

			if (amount.length() != 0) {
				try {
					amountVal = Integer.parseInt(amount);
				}
				catch (NumberFormatException e) {
					amountVal = 0;
				}
			}
			if (discount.length() != 0) {
				try {
					discountVal = Float.parseFloat(discount);
				}
				catch (NumberFormatException e) {
					discountVal = 0;
				}
			}
			if (maxAmountdiscount.length() != 0) {
				try {
					maxAmountDiscountVal = Float.parseFloat(maxAmountdiscount);
				}
				catch (NumberFormatException e) {
					maxAmountDiscountVal = 0;
				}
			}

			conditionOrderID = conditionOrder.length() != 0 ? new Integer(conditionOrder) : null;
			regSpecTypeID = regSpecType.length() != 0 ? new Integer(regSpecType) : null;
			conditionTypeID = conditionType.length() != 0 ? new Integer(conditionType) : null;
			specialCalculationID = specialCalculation.length() != 0 ? new Integer(specialCalculation) : null;
			vatRuleID = vatRule.length() != 0 ? new Integer(vatRule) : null;

			int rID = 0;
			if (regID != null) {
				rID = Integer.parseInt(regID);
			}
			r = null;
			if (rID != 0) {
				r = home.findRegulation(rID);
			}
		}
		catch (FinderException e) {
			r = null;
		}

		if (isRegulationOverlap(name, periodFrom, periodTo, r)) {
			throw new RegulationException(KEY_ERROR_PARAM_OVERLAP, "�verlappande perioder");
		}

		try {
			if (r == null) {
				r = home.create();
			}
			r.setPeriodFrom(periodFrom);
			r.setPeriodTo(periodTo);
			r.setName(name);
			r.setAmount(amountVal);
			r.setDiscount(discountVal);
			r.setMaxAmountDiscount(maxAmountDiscountVal);
			r.setChangedDate(IWTimestamp.getTimestampRightNow());

			if (vatEligible != null) {
				r.setVATEligible(Integer.parseInt(vatEligible));
			}
			if (paymentFlowType != null) {
				r.setPaymentFlowType(Integer.parseInt(paymentFlowType));
			}
			if (operation != null) {
				r.setOperation(operation);
			}
			if (conditionOrderID != null) {
				r.setConditionOrder(conditionOrderID.intValue());
			}
			if (regSpecTypeID != null) {
				r.setRegSpecType(regSpecTypeID.intValue());
			}
			if (conditionTypeID != null) {
				r.setConditionType(conditionTypeID.intValue());
			}
			if (specialCalculationID != null) {
				r.setSpecialCalculation(specialCalculationID.intValue());
			}
			if (vatRuleID != null) {
				r.setVATRegulation(vatRuleID.intValue());
			}
			if (changedBy != null) {
				r.setChangedSign(changedBy);
			}
			r.store();
		}
		catch (CreateException ce) {
			throw new RegulationException(KEY_ERROR_REGULATION_CREATE, "Kan ej skapa regel");
		}
		int id = 0;
		if (r != null) {
			id = Integer.parseInt(r.getPrimaryKey().toString());
		}
		return id;
	}

	/**
	 * Save condition. If non existing, creates it.
	 *  
	 * @param regulation_id the regulation id
	 * @param idx the index that this condition has (among the big 5) Can be expanded.
	 * @param operation_id the operation index in the lists
	 * @param interval_id the interval index in the lists
	 * @author kelly
	 */
	public void saveCondition(String regulation_id, String idx, String operation_id, String interval_id) throws RegulationException, RemoteException {

		ConditionHome home = null;
		Condition c = null;

		Integer regulationID = null;
		Integer index = null;
		Integer operationID = null;
		Integer intervalID = null;

		try {
			regulationID = regulation_id.length() != 0 ? new Integer(regulation_id) : null;
			index = idx.length() != 0 ? new Integer(idx) : null;
			operationID = operation_id.length() != 0 ? new Integer(operation_id) : null;
			intervalID = interval_id.length() != 0 ? new Integer(interval_id) : null;
			c = null;
			home = (ConditionHome) IDOLookup.getHome(Condition.class);
			c = (Condition) findConditionByRegulationAndIndex(regulationID, index);
		}
		catch (Exception e) {
		}

		try {
			if (c == null) {
				c = home.create();
			}
			c.setConditionID(operationID.intValue());
			if (intervalID != null) {
				c.setIntervalID(intervalID.intValue());
			}
			if (index != null) {
				c.setIndex(index.intValue());
			}
			if (regulationID != null) {
				c.setRegulationID(regulationID.intValue());
			}
			c.store();
		}
		catch (Exception e) {
		}

	}

	/**
	 * gets a posting detail
	 *  
	 * @param xxx  
	 * @return yyy
	 * 
	 * @author kelly
	 */
	public PostingDetail getPostingDetailByOperationFlowPeriodConditionTypeRegSpecType(
		String operation,
		String flow,
		Date period,
		Collection condition,
		String regSpecType,
		int totalSum,
		ChildCareContract contract) {

		PostingDetail postingDetail = new PostingDetail();
		IWBundle bundle = getIWApplicationContext().getApplication().getBundle(getBundleIdentifier());
		IWResourceBundle iwrb = bundle.getResourceBundle(getIWApplicationContext().getApplication().getSettings().getDefaultLocale());

		Collection items = findRegulationsByPeriod(period, period);
		if (items != null) {
			Iterator iter = items.iterator();
			int match = 0;
			while (iter.hasNext()) {
				Regulation r = (Regulation) iter.next();
				if (flow.compareTo(r.getPaymentFlowType().getLocalizationKey()) == 0) {
					match++;
				}
				if (regSpecType.compareTo(r.getRegSpecType().getLocalizationKey()) == 0) {
					match++;
				}
				match += checkConditions(r, condition);
				if (match == (2 + condition.size())) {
					// match
					postingDetail.setAmount(r.getAmount().intValue());
					postingDetail.setTerm(iwrb.getLocalizedString(r.getLocalizationKey()));
					break;
				}
				match = 0;
			}
		}
		return postingDetail;
	}

	private int checkConditions(Regulation r, Collection c) {
		//If there are no conditions then the rule of course is allowed
		if (c == null || c.isEmpty())
			return 1;

		//Find all the conditions on this rule
		Collection cond = null;
		try {
			cond = getConditionHome().findAllConditionsByRegulation(r);
		}
		catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
		catch (FinderException e) {
			e.printStackTrace();
			return 0;
		}

		//If there are no conditions on the rule then the rule does satisfies the conditions
		if (cond == null || cond.isEmpty())
			return 1;

		//Go through each condition sent in and try to see if the rule satisfies the conditions it needs
		Iterator it = c.iterator();
		while (it.hasNext()) {
			ConditionParameter param = (ConditionParameter) it.next();
			String condition = param.getCondition();

			//Checking each type of condition
			if (condition.equals(RuleTypeConstant.CONDITION_ID_OPERATION)) {
				String value = (String) param.getInterval();
				Iterator i = cond.iterator();
				boolean match = true;
				while (i.hasNext() && match) {
					Condition regCond = (Condition) i.next();
					if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_OPERATION)) {
						int id = regCond.getIntervalID();
						try {
							SchoolType act = getSchoolTypeHome().findByPrimaryKey(new Integer(id));
							if (!act.getLocalizationKey().equals(value))
								match = false;
						}
						catch (RemoteException e1) {
							e1.printStackTrace();
							return 0;
						}
						catch (FinderException e1) {
							e1.printStackTrace();
							return 0;
						}
					}
				}

				if (!match)
					return 0;
			}
			else if (condition.equals(RuleTypeConstant.CONDITION_ID_RESOURCE)) {
				String value = (String) param.getInterval();
				Iterator i = cond.iterator();
				boolean match = true;
				while (i.hasNext() && match) {
					Condition regCond = (Condition) i.next();
					if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_RESOURCE)) {
						int id = regCond.getIntervalID();
						try {
							int resourceKey = ((Integer)getResourceHome().findResourceByName(value).getPrimaryKey()).intValue();
							if (id != resourceKey)
								match = false;
						}
						catch (RemoteException e1) {
							e1.printStackTrace();
							return 0;
						}
						catch (FinderException e1) {
							e1.printStackTrace();
							return 0;
						}
					}
				}

				if (!match)
					return 0;
			}
			
			else if (condition.equals(RuleTypeConstant.CONDITION_ID_AGE_INTERVAL)) {
				Integer value = (Integer) param.getInterval();
				Iterator i = cond.iterator();
				boolean match = true;
				while (i.hasNext() && match) {
					Condition regCond = (Condition) i.next();
					if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_AGE_INTERVAL)) {
						int id = regCond.getIntervalID();

						switch (id) {
							case 1 :
								if (1 > value.intValue() || value.intValue() > 2)
									match = false;
								break;
							case 2 :
								if (3 > value.intValue() || value.intValue() > 5)
									match = false;
								break;
							case 3 :
								if (4 > value.intValue() || value.intValue() > 5)
									match = false;
								break;
							case 4 :
								if (6 != value.intValue())
									match = false;
								break;
							case 5 :
								if (value.intValue() < 7)
									match = false;
								break;
						}
					}
				}

				if (!match)
					return 0;
			}
			else if (condition.equals(RuleTypeConstant.CONDITION_ID_HOURS)) {
				Integer value = (Integer) param.getInterval();
				Iterator i = cond.iterator();
				boolean match = true;
				while (i.hasNext() && match) {
					Condition regCond = (Condition) i.next();
					if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_HOURS)) {
						int id = regCond.getIntervalID();
						//I'll just use the fact that this is hardcoded.						
						switch (id) {
							case 1 :
								if (1 > value.intValue() || value.intValue() > 25)
									match = false;
								break;
							case 2 :
								if (26 > value.intValue() || value.intValue() > 35)
									match = false;
								break;
							case 3 :
								if (value.intValue() < 36)
									match = false;
								break;
							case 4 :
								if (value.intValue() > 24)
									match = false;
								break;
							case 5 :
								if (value.intValue() < 25)
									match = false;
								break;
							case 6 :
								if (value.intValue() > 13)
									match = false;
								break;
							case 7 :
								if (value.intValue() < 14)
									match = false;
								break;
						}
					}
				}

				if (!match)
					return 0;
			}
			else if (condition.equals(RuleTypeConstant.CONDITION_ID_SIBLING_NR)) {
				Integer value = (Integer) param.getInterval();
				Iterator i = cond.iterator();
				boolean match = true;
				while (i.hasNext() && match) {
					Condition regCond = (Condition) i.next();
					if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_SIBLING_NR)) {
						int id = regCond.getIntervalID();
						//I'll just use the fact that this is hardcoded.						
						switch (id) {
							case 1 :
								if (1 != value.intValue())
									match = false;
								break;
							case 2 :
								if (2 != value.intValue())
									match = false;
								break;
							case 3 :
								if (3 != value.intValue())
									match = false;
								break;
							case 4 :
								if (value.intValue() < 4)
									match = false;
								break;
						}
					}
				}

				if (!match)
					return 0;
			}
			else if (condition.equals(RuleTypeConstant.CONDITION_ID_EMPLOYMENT)) {
				Integer value = (Integer) param.getInterval();
				Iterator i = cond.iterator();
				boolean match = true;
				while (i.hasNext() && match) {
					Condition regCond = (Condition) i.next();
					if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_EMPLOYMENT)) {
						int id = regCond.getIntervalID();
							if (value.intValue() != id)
								match = false;
					}
				}

				if (!match)
					return 0;
			}
			else if (condition.equals(RuleTypeConstant.CONDITION_ID_SCHOOL_YEAR)) {
				String value = (String) param.getInterval();
				Iterator i = cond.iterator();
				boolean match = true;
				while (i.hasNext() && match) {
					Condition regCond = (Condition) i.next();
					if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_SCHOOL_YEAR)) {
						int id = regCond.getIntervalID();
						//I'll just use the fact that this is hardcoded.						
						switch (id) {
							case 1 :
								try {
									int intValue = Integer.parseInt(value);
									if (1 > intValue || intValue > 3)
										match = false;
								}
								catch (Exception e) {
								}
								break;
							case 2 :
								try {
									int intValue = Integer.parseInt(value);
									if (1 > intValue || intValue > 6)
										match = false;
								}
								catch (Exception e) {
								}
								break;
							case 3 :
								try {
									int intValue = Integer.parseInt(value);
									if (4 > intValue || intValue > 6)
										match = false;
								}
								catch (Exception e) {
								}
								break;
							case 4 :
								try {
									int intValue = Integer.parseInt(value);
									if (7 > intValue || intValue > 9)
										match = false;
								}
								catch (Exception e) {
								}
								break;
							case 5 :
								if (!"S1".equals(value) && !"S2".equals(value) && !"S3".equals(value))
									match = false;
								break;
							case 6 :
								if (!"S4".equals(value) && !"S5".equals(value) && !"S6".equals(value))
									match = false;
								break;
							case 7 :
								if (!"S7".equals(value) && !"S8".equals(value) && !"S9".equals(value) && !"S10".equals(value))
									match = false;
								break;
							case 8 :
								if (!"G1".equals(value) && !"G2".equals(value) && !"G3".equals(value))
									match = false;
								break;
							case 9 :
								if (!"G1".equals(value))
									match = false;
								break;
							case 10 :
								if (!"G2".equals(value))
									match = false;
								break;
							case 11 :
								if (!"G3".equals(value))
									match = false;
								break;
							case 12 :
								if (!"G4".equals(value))
									match = false;
								break;
							case 13 :
								if (!"G1".equals(value) && !"G2".equals(value) && !"G3".equals(value) && !"G4".equals(value))
									match = false;
								break;
							case 14 :
								if (!"GS1".equals(value) && !"GS2".equals(value) && !"GS3".equals(value) && !"GS4".equals(value))
									match = false;
								break;
							case 15 :
								if (!"GS1".equals(value))
									match = false;
								break;
							case 16 :
								if (!"GS2".equals(value))
									match = false;
								break;
							case 17 :
								if (!"GS3".equals(value))
									match = false;
								break;
							case 18 :
								if (!"GS4".equals(value))
									match = false;
								break;
						}
					}
				}

				if (!match)
					return 0;
			}
		}

		return 1;
	}

	/**
	 * Gets a Condition by Regulation ID and Index
	 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParameters# 
	 * @param regulationID Regulation ID
	 * @param index the index of the condition
	 * @return Condition
	 * @author Kelly
	 */
	public Object findConditionByRegulationAndIndex(Integer regulationID, Integer index) throws FinderException {
		try {
			ConditionHome home = getConditionHome();
			return home.findAllConditionsByRegulationAndIndex(regulationID.intValue(), index.intValue());
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Activity types
	 * @return collection of Activity Types = School types
	 * @see import com.idega.block.school.data.SchoolType#
	 * @author Kelly
	 */
	public Collection findAllActivityTypes() {
		try {
			SchoolTypeHome home = getSchoolTypeHome();
			return home.findAllSchoolTypes();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Commune belonging types
	 * @return collection of Commune belonging types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.CommuneBelongingType 
	 * @author Kelly
	 */
	public Collection findAllCommuneBelongingTypes() {
		try {
			CommuneBelongingTypeHome home = getCommuneBelongingTypeHome();
			return home.findAllCommuneBelongingTypes();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Company Types
	 * @return collection of Company Types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.CompanyType 
	 * @author Kelly
	 */
	public Collection findAllCompanyTypes() {
		try {
			SchoolManagementTypeHome home = getSchoolManagementTypeHome();
			return home.findAllManagementTypes();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Regulation specification types
	 * @return collection of Regulation specification types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType 
	 * @author Kelly
	 */
	public Collection findAllRegulationSpecTypes() throws RegulationException {
		try {
			RegulationSpecTypeHome home = getRegulationSpecTypeHome();
			return home.findAllRegulationSpecTypes();
		}
		catch (RemoteException e) {
			throw new RegulationException(KEY_GENERAL_ERROR, DEFAULT_GENERAL_ERROR);
		}
		catch (FinderException e) {
			throw new RegulationException(KEY_GENERAL_ERROR, DEFAULT_GENERAL_ERROR);
		}
	}

	/**
	 * Saves a Regulation specification type.
	 * @param regSpecTypeId The regulation specification id
	 * @param regSpecTypeKey localized key
	 * @param mainRuleId the MainTule relational id
	 * @throws RegulationException if invalid parameters
	 * @author Kelly
	 */
	public void saveRegulationSpecType(int regSpecTypeId, String regSpecTypeKey, int mainRuleId) throws RegulationException {

		boolean create = false;
		RegulationSpecTypeHome home = null;
		RegulationSpecType rst = null;

		if (regSpecTypeKey.length() == 0) {
			throw new RegulationException(KEY_ERROR_PARAM_REG_SPEC_EMPTY, "Regelspecifikcationstyp saknas!");
		}

		try {
			home = getRegulationSpecTypeHome();
			rst = home.findByPrimaryKey(new Integer(regSpecTypeId));
		}
		catch (FinderException e) {
			create = true;
		}
		catch (RemoteException e) {
			throw new RegulationException(KEY_CANNOT_SAVE_REG_SPEC_TYPE, DEFAULT_CANNOT_SAVE_REG_SPEC_TYPE);
		}
		try {
			if (create) {
				rst = home.create();
			}
			rst.setMainRule(mainRuleId);
			rst.setLocalizationKey(regSpecTypeKey);
			rst.store();
		}
		catch (CreateException e) {
			throw new RegulationException(KEY_CANNOT_SAVE_REG_SPEC_TYPE, DEFAULT_CANNOT_SAVE_REG_SPEC_TYPE);
		}

	}

	/**
	 * Deletes the regulation spec type object with the specified id.
	 * @param id the RegSpecType id
	 * @throws RegulationException if the regulation could not be deleted
	 */
	public void deleteRegulationSpecType(int id) throws RegulationException {
		try {
			RegulationSpecTypeHome home = getRegulationSpecTypeHome();
			RegulationSpecType rst = home.findByPrimaryKey(new Integer(id));
			rst.remove();
		}
		catch (RemoteException e) {
			throw new RegulationException(KEY_CANNOT_DELETE_REG_SPEC_TYPE, DEFAULT_CANNOT_DELETE_REG_SPEC_TYPE);
		}
		catch (FinderException e) {
			throw new RegulationException(KEY_CANNOT_DELETE_REG_SPEC_TYPE, DEFAULT_CANNOT_DELETE_REG_SPEC_TYPE);
		}
		catch (RemoveException e) {
			throw new RegulationException(KEY_CANNOT_DELETE_REG_SPEC_TYPE, DEFAULT_CANNOT_DELETE_REG_SPEC_TYPE);
		}
	}

	/**
	 * Gets all payment flow types.
	 * @return collection of payment flow types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.PaymentFlowType 
	 * @author anders
	 */
	public Collection findAllPaymentFlowTypes() {
		try {
			PaymentFlowTypeHome home = getPaymentFlowTypeHome();
			return home.findAll();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all provider types.
	 * @return collection of provider types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.ProviderType 
	 * @author anders
	 */
	public Collection findAllProviderTypes() {
		try {
			ProviderTypeHome home = getProviderTypeHome();
			return home.findAll();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Main Rules
	 * @return collection of provider types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.MainRuleBMPBean# 
	 * @author Kelly
	 */
	public Collection findAllMainRules() {
		try {
			MainRuleHome home = getMainRuleHome();
			return home.findAllMainRules();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets a Regulation Specification
	 * @return RegulationSpecType
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType#
	 * @author Kelly
	 */
	public Object findRegulationSpecType(int id) {
		try {
			RegulationSpecTypeHome home = getRegulationSpecTypeHome();
			return home.findRegulationSpecType(id);
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Conditions on a certain regulation
	 * @return collection of conditions
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType#
	 * @author Kelly
	 */
	public Collection findAllConditionsByRegulation(Regulation r) {
		try {
			ConditionHome home = getConditionHome();
			return home.findAllConditionsByRegulation(r);
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Conditions on a certain regulation
	 * @return collection of conditions
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.RegulationSpecType#
	 * @author Kelly
	 */
	public Collection findAllConditionsByRegulationID(int id) {
		try {
			ConditionHome home = getConditionHome();
			return home.findAllConditionsByRegulationID(id);
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all Regulations
	 * @return collection of Regulations
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.Regulation
	 * @author Kelly
	 */
	public Collection findAllRegulations() {
		try {
			RegulationHome home = getRegulationHome();
			return home.findAllRegulations();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/*
	 * Checks if dates are in overlap of stored Regulations
	 * @return true if there is an overlap
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.Regulation
	 * @author Kelly
	 */
	private boolean isRegulationOverlap(String name, Date from, Date to, Regulation r) {

		try {
			RegulationHome home = getRegulationHome();
			if (home.findRegulationOverlap(name, from, to, r) == null) {
				return false;
			}
			else {
				return true;
			}
		}
		catch (RemoteException e) {
			return false;
		}
		catch (FinderException e) {
			return false;
		}
	}

	/**
	 * Gets all VAT Rules
	 * @return collection of VAT Rules
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.Regulation
	 * @author Kelly
	 */
	public Collection findAllVATRules() {
		try {
			VATRuleHome home = getVATRuleHome();
			Collection c = home.findAllVATRules();
			if (c == null) {
				VATRule vr = home.create();
				vr.store();
			}
			return home.findAllVATRules();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
		catch (CreateException e) {
			return null;
		}
	}

	/**
	 * Gets regulations for a certain periode
	 * @param from periode (Date)
	 * @param to periode (Date)
	 * @return collection of Regulations
	 * @author Kelly
	 * 
	 */
	public Collection findRegulationsByPeriod(Date from, Date to) {
		try {
			RegulationHome home = getRegulationHome();
			return home.findRegulationsByPeriod(from, to);
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets regulations for a certain periode, operationID, FLowTypeID and SortByID
	 * @param from periode (Date)
	 * @param to periode (Date)
	 * @param operationID
	 * @param flowTypeID
	 * @param sortByID
	 * @return collection of Regulations
	 * @author Kelly
	 * 
	 */
	public Collection findRegulationsByPeriod(Date from, Date to, String operationID, int flowTypeID, int sortByID) {

		try {
			RegulationHome home = getRegulationHome();
			return home.findRegulationsByPeriod(from, to, operationID, flowTypeID, sortByID);
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets a Regulation
	 * @return Regulations
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.Regulation
	 * @author Kelly
	 */
	public Regulation findRegulation(int id) {
		try {
			RegulationHome home = getRegulationHome();
			return home.findRegulation(id);
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Finds all sibling values
	 * These are not put in an entity bean since Lotta Ringborg 
	 * tells me they shall be fixed and never changed.
	 * @return Collection of sibling numbers
	 * @author Kelly
	 */
	public Collection findAllSiblingValues() {
		ArrayList arr = new ArrayList();

		arr.add(new Object[] { new Integer(1), "1" });
		arr.add(new Object[] { new Integer(2), "2" });
		arr.add(new Object[] { new Integer(3), "3" });
		arr.add(new Object[] { new Integer(4), ">=4" });

		return arr;
	}

	/**
	 * Finds all hour values
	 * These are not put in an entity bean since Lotta Ringborg 
	 * tells me they shall be fixed and never changed.
	 * @return Collection of hour intervals
	 * @author Kelly
	 */
	public Collection findAllHourIntervals() {
		ArrayList arr = new ArrayList();
		int index = 1;
		arr.add(new Object[] { new Integer(index++), "1-25" });
		arr.add(new Object[] { new Integer(index++), "26-35" });
		arr.add(new Object[] { new Integer(index++), ">=36" });
		arr.add(new Object[] { new Integer(index++), "<=24" });
		arr.add(new Object[] { new Integer(index++), ">=25" });
		arr.add(new Object[] { new Integer(index++), "<=13" });
		arr.add(new Object[] { new Integer(index++), ">=14" });
		arr.add(new Object[] { new Integer(index++), "1-15" });
		arr.add(new Object[] { new Integer(index++), "16-25" });

		return arr;
	}

	/**
	 * Finds all age intervals
	 * These are not put in an entity bean since Lotta Ringborg 
	 * tells me they shall be fixed and never changed.
	 * 
	 * If these change please fix the method checkAgeIntervals as well.
	 * 
	 * @return Collection of hour intervals
	 * @author Kelly
	 */
	public Collection findAllAgeIntervals() {
		ArrayList arr = new ArrayList();

		arr.add(new Object[] { new Integer(1), "1-2" });
		arr.add(new Object[] { new Integer(2), "3-5" });
		arr.add(new Object[] { new Integer(3), "4-5" });
		arr.add(new Object[] { new Integer(4), "6" });
		arr.add(new Object[] { new Integer(5), ">=7" });
		arr.add(new Object[] { new Integer(6), "5-8" });
		arr.add(new Object[] { new Integer(7), "6-13" });

		return arr;
	}

	/**
	 * Finds all school year intervalls hardcoded
	 * These are not put in an entity bean since Lotta Ringborg 
	 * tells me they shall be fixed and never changed.
	 * @return Collection of hour intervals
	 * @author Kelly
	 */
	public Collection findAllSchoolYearIntervals() {
		ArrayList arr = new ArrayList();

		arr.add(new Object[] { new Integer(1), "1-3" });
		arr.add(new Object[] { new Integer(2), "1-6" });
		arr.add(new Object[] { new Integer(3), "4-6" });
		arr.add(new Object[] { new Integer(4), "7-9" });
		arr.add(new Object[] { new Integer(5), "S1-S3" });
		arr.add(new Object[] { new Integer(6), "S4-S6" });
		arr.add(new Object[] { new Integer(7), "S7-S10" });
		arr.add(new Object[] { new Integer(8), "G1-G3" });
		arr.add(new Object[] { new Integer(9), "G1" });
		arr.add(new Object[] { new Integer(10), "G2" });
		arr.add(new Object[] { new Integer(11), "G3" });
		arr.add(new Object[] { new Integer(12), "G4" });
		arr.add(new Object[] { new Integer(13), "G1-G4" });
		arr.add(new Object[] { new Integer(14), "GS1-GS4" });
		arr.add(new Object[] { new Integer(15), "GS1" });
		arr.add(new Object[] { new Integer(16), "GS2" });
		arr.add(new Object[] { new Integer(17), "GS3" });
		arr.add(new Object[] { new Integer(18), "GS4" });

		return arr;
	}

	/**
	 * Gets a yes/no to use in the regulation framework.
	 * @return Yes no collection
	 * @see se.idega.idegaweb.commune.accounting.regulations.presentation.RegulationListEditor#
	 * @see getAllOperations
	 * @author Kelly
	 */
	public Collection getYesNo() {
		try {
			YesNoHome home = getYesNoHome();
			return home.findAllYesNoValues();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Finds all Max Amounts
	 * These are not put in an entity bean since Lotta Ringborg 
	 * tells me they shall be fixed and never changed.
	 * @return Collection of Max Amounts
	 * @author Kelly
	 */
	public Collection findAllMaxAmounts() {
		ArrayList arr = new ArrayList();
		for (int i = 1; i <= 100; i++) {
			String s = i + "%";
			arr.add(new Object[] { new Integer(i), s });
		}
		return arr;
	}

	/**
	 * Finds all discount values
	 * These are not put in an entity bean since Lotta Ringborg 
	 * tells me they shall be fixed and never changed.
	 * @return Collection of discount values
	 * @author Kelly
	 */
	public Collection findAllDiscountValues() {
		ArrayList arr = new ArrayList();
		for (int i = 0; i <= 100; i++) {
			String s = i + "%";
			arr.add(new Object[] { new Integer(i), "-" + s });
		}
		return arr;
	}

	/**
	 * Deletes a regulation
	 * @param id Regulation ID
	 * @author Kelly
	 * 
	 */
	public void deleteRegulation(int id) throws java.rmi.RemoteException {
		try {
			Regulation r = findRegulation(id);
			r.remove();
			r.store();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Gets all Special Calculation types
	 * @return collection of Special calculation types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.SpecialCalculationType#
	 * @author Kelly
	 */
	public Collection findAllSpecialCalculationTypes() {
		try {
			SpecialCalculationTypeHome home = getSpecialCalculationTypeHome();
			Collection c = home.findAllSpecialCalculationTypes();
			if (c == null) {
				SpecialCalculationType sct = home.create();
				sct.store();
			}
			return home.findAllSpecialCalculationTypes();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
		catch (CreateException e) {
			return null;
		}
	}

	/**
	 * Gets all ConditionTypes
	 * @return collection of condition types
	 * @see se.idega.idegaweb.commune.accounting.regulations.data.ConditionType#
	 * @author Kelly
	 */
	public Collection findAllConditionTypes() {
		try {
			ConditionTypeHome home = getConditionTypeHome();
			return home.findAllConditionTypes();
		}
		catch (RemoteException e) {
			return null;
		}
		catch (FinderException e) {
			return null;
		}
	}

	/**
	 * Gets all possible Condition selections. This is used to get certain data from different 
	 * parts of the system. This could be placed in a bean later...
	 * As of now I just set the values here.
	 * 
	 * Values are:
	 * 
	 * Operation ID
	 * Real term (initial localized value)
	 * Localization key
	 * Class where the collection can be retrieved
	 * Method to get the collection
	 * Method to get the data in the bean (If blank it means the data is just a collection of objects []
	 * 
	 * @return collection of ConditionHolders
	 * @see se.idega.idegaweb.commune.accounting.regulations.business.ConditionHolder#
	 * @see se.idega.idegaweb.commune.accounting.regulations.presentation.RegulationListEditor#
	 * @author Kelly
	 * TODO: getSession.getOperationalField() 
	 * 
	 */
	public Collection findAllConditionSelections(String operationID) {
		// LP = Localization path
		ArrayList arr = new ArrayList();
		arr.add(
			new ConditionHolder(RuleTypeConstant.CONDITION_ID_OPERATION, "Verksamhet", LP + "verksamhet", "com.idega.block.school.business.SchoolBusiness", "findAllSchoolTypes", "getLocalizationKey", ""));

		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_RESOURCE,
				"Resurs",
				LP + "resurs",
				"se.idega.idegaweb.commune.accounting.resource.business.ResourceBusiness",
				"findAllResources",
				"getResourceName",
				""));
		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_VAT,
				"Momssats",
				LP + "momssats",
				"se.idega.idegaweb.commune.accounting.regulations.business.VATBusiness",
				"findAllVATRegulations",
				"getDescription",
				operationID));

		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_SCHOOL_YEAR,
				"�rskurs",
				LP + "aarskurs",
				"se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness",
				"findAllSchoolYearIntervals",
				"",
				""));

		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_HOURS,
				"Timmar",
				LP + "timmar",
				"se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness",
				"findAllHourIntervals",
				"",
				""));

		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_SIBLING_NR,
				"Syskonnr",
				LP + "syskonnr",
				"se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness",
				"findAllSiblingValues",
				"",
				""));

		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_AGE_INTERVAL,
				"�lder",
				LP + "alder",
				"se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness",
				"findAllAgeIntervals",
				"",
				""));

		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_STADSBIDRAG,
				"Statsbidragsber�ttigad",
				LP + "statsbidragsberattigad",
				"se.idega.idegaweb.commune.accounting.regulations.business.RegulationsBusiness",
				"getYesNo",
				"getLocalizationKey",
				""));
		arr.add(new ConditionHolder(RuleTypeConstant.CONDITION_ID_COMMUNE, "Kommun", LP + "kommun", "com.idega.core.location.business.CommuneBusiness", "getCommunes", "getCommuneName", ""));
		if (operationID.compareTo(SchoolCategoryBMPBean.CATEGORY_HIGH_SCHOOL) == 0) {
			arr.add(
				new ConditionHolder(
					RuleTypeConstant.CONDITION_ID_STUDY_PATH,
					"Studiev�g",
					LP + "studievag",
					"se.idega.idegaweb.commune.accounting.school.business.StudyPathBusiness",
					"findAllStudyPaths",
					"getCode",
					""));
		}

		arr.add(
			new ConditionHolder(
				RuleTypeConstant.CONDITION_ID_EMPLOYMENT,
				"Arbetssituation",
				LP + "employment",
				"se.idega.idegaweb.commune.childcare.business.ChildCareBusiness",
				"findAllEmploymentTypes",
				"getLocalizationKey",
				""));

		return (Collection) arr;

	}

	/**
	 * Function that will return term (the descriptive text for a charge), the amount, VAT
	 * and the VAT Type using the return object postingDetail. The regulation is selected
	 * where all the inputparameters are fulfilled.
	 * 
	 * @param operation (Huvudverksamhet)
	 * @param flow (Str�m)
	 * @param period (date when the reule is valid)
	 * @param conditionType (Villkorstyp)
	 * @param condition (Collection of conditions)
	 * @param regSpecType (RegelSpec.Typ)
	 * @param totalSum total sum calculated so far. Sometimes needed for calculation to return
	 * @param contract The contract archive
	 * @return postingDetail
	 */
	public PostingDetail getPostingDetailByOperationFlowPeriodConditionTypeRegSpecType(
		String operation,
		String flow,
		Date period,
		String conditionType,
		String regSpecType,
		Collection condition,
		float totalSum,
		ChildCareContract contract)
		throws RegulationException {

		PostingDetail postingDetail = null;

		//Insert code here to create postingDetail
		try {
			RegulationHome home = getRegulationHome();
			int flowID = -1;
			int condTypeID = -1;
			int regSpecTypeID = -1;

			try {
				PaymentFlowType pfType = getPaymentFlowTypeHome().findByLocalizationKey(flow);
				if (pfType != null)
					flowID = ((Integer) pfType.getPrimaryKey()).intValue();
			}
			catch (Exception e) {
				System.out.println("WARNING: Could not find flow: "+flow);
				e.printStackTrace();
				flowID = -1;
			}

			try {
				ConditionType cType = getConditionTypeHome().findByConditionType(conditionType);
				if (cType != null)
					condTypeID = ((Integer) cType.getPrimaryKey()).intValue();
			}
			catch (Exception e) {
				System.out.println("WARNING: Could not find conditionType: "+conditionType);
				e.printStackTrace();
				condTypeID = -1;
			}

			try {
				RegulationSpecType sType = getRegulationSpecTypeHome().findByRegulationSpecType(regSpecType);
				if (sType != null)
					regSpecTypeID = ((Integer) sType.getPrimaryKey()).intValue();
			}
			catch (Exception e) {
				System.out.println("WARNING: Could not find regSpecType: "+regSpecType);
				e.printStackTrace();
				regSpecTypeID = -1;
			}

			Collection reg = home.findRegulations(period, period, operation, flowID, condTypeID, regSpecTypeID,-1);
			System.out.println("Found "+reg.size()+" regulations from initial query");
			if (reg != null && !reg.isEmpty()) {
				List match = new Vector();
				Iterator it = reg.iterator();
				while (it.hasNext()) {
					Regulation regulation = (Regulation) it.next();
//					System.out.println("Checking Regulation "+regulation.getName());
					int i = checkConditions(regulation, condition);
					if (i == 1){
						System.out.println("Regulation found "+regulation.getName());
						match.add(regulation);
					}
				}

				if (match.size() == 1) {
					Regulation res = (Regulation) match.get(0);
					try {
						postingDetail = getPostingDetailFromRegulation(res, condition, contract, null, period, totalSum);
					}
					catch(BruttoIncomeException e) {
					}
					catch(LowIncomeException e) {
					}
				}
				else if (match.size() > 1) {
					Iterator regIterator = match.iterator();
					while (regIterator.hasNext()) {
						Regulation tmpreg = (Regulation) regIterator.next();
						System.out.println("Too many regulations found: "+tmpreg.getName());
					}
					throw new RegulationException("reg_exp_to_many_results", "Too many regulation match conditions");
				} else{
					System.out.println("No regulations found");
				}
			} else{
				System.out.println("Initial query for regulations did not find anything.");
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
			postingDetail = null;
		}
		catch (FinderException e) {
			e.printStackTrace();
			postingDetail = null;
		}

		return postingDetail;
	}

	//	public PostingDetail getPostingDetailByOperationFlowPeriodConditionTypeRegSpecType(
	//		String operation,
	//		String flow,
	//		Date period,
	//		String conditionType,
	//		String regSpecType,
	//		Collection condition,
	//		float totalSum,
	//		ChildCareContract contract) {

	private PostingDetail getPostingDetailFromRegulation(Regulation reg, Collection conditions, ChildCareContract contract, SchoolClassMember placement, Date period, float total_sum) throws BruttoIncomeException, LowIncomeException {
		PostingDetail ret = null;
		if (reg.getSpecialCalculation() != null) {
			String type = reg.getSpecialCalculation().getSpecialCalculationType();
			if (type.equals("cacc_sp_calc_type.subv")) {
				PostingDetail d = null;
				try {
					d =
						getPostingDetailByOperationFlowPeriodConditionTypeRegSpecType(
							(String) reg.getOperation().getPrimaryKey(),
							reg.getPaymentFlowType().getLocalizationKey(),
							period,
							RuleTypeConstant.FORMULA,
							RegSpecConstant.CHECKTAXA,
							conditions,
							total_sum,
							contract);
				}
				catch (RegulationException e) {
					e.printStackTrace();
				}
				catch (EJBException e) {
					e.printStackTrace();
				}
				if (d != null) {
					ret = new PostingDetail();
					ret.setAmount(Math.round(d.getAmount()-total_sum));
					ret.setRuleSpecType(d.getRuleSpecType());
					ret.setTerm(reg.getName());
					//				ret.setVat(32.0f);
					//				ret.setVatRegulationID(1);
				}
			}
			else if (type.equals("cacc_sp_calc_type.syskon")) {
				float amount = reg.getDiscount() * total_sum / 100;
				ret = new PostingDetail();
				ret.setAmount(Math.round(amount));
				ret.setRuleSpecType(reg.getLocalizationKey());
				ret.setTerm(reg.getName());
				//				ret.setVat(32.0f);
				//				ret.setVatRegulationID(1);
			}
			else if (type.equals("cacc_sp_calc_type.maxtaxa")) {
				User child = null;
				if (contract != null) {
					child = contract.getChild();
				}
				else if (placement != null) {
					child = placement.getStudent();
				}

				boolean missingIncome = true;
				float income = 0;
				if (child != null) {
					try {
						//get the family
						Collection cust = getMemberFamilyLogic().getCustodiansFor(child);
						if (cust != null && !cust.isEmpty()) {
							Iterator it = cust.iterator();
							while (it.hasNext()) {
								User custodian = (User) it.next();
								try {
									BruttoIncome userIncome = getUserInfoService().getBruttoIncomeHome().findLatestByUser((Integer) custodian.getPrimaryKey());
									if (userIncome != null) {
										income += userIncome.getIncome().floatValue();
										missingIncome = false;
									}
									else {
										missingIncome = true;
										break;
									}
								}
								catch (EJBException e1) {
//									e1.printStackTrace();
									missingIncome = true;
									break;
								}
								catch (FinderException e1) {
//									e1.printStackTrace();
									missingIncome = true;
									break;
								}
							}
						}
					
						float perc = reg.getMaxAmountDiscount();
						if (!missingIncome && income > 0.0f && perc > 0.0f) {							
							float amount = income * perc / 100;
							if (amount < total_sum) {
								ret = new PostingDetail();
								ret.setAmount(Math.round(amount - total_sum));
								ret.setRuleSpecType(reg.getRegSpecType().getLocalizationKey());
								ret.setTerm(reg.getName());
								//			ret.setVat(32.0f);
								//			ret.setVatRegulationID(1);
							}
						}
						else {
							throw new BruttoIncomeException("reg_exp.no_brutto_income","Brutto income not registered");
						}
					}
					catch (NoCustodianFound e) {
						throw new BruttoIncomeException("reg_exp.no_brutto_income","Brutto income not registered");
					}
					catch (RemoteException e) {
						throw new BruttoIncomeException("reg_exp.no_brutto_income","Brutto income not registered");
					}
				}
			}
			else if (type.equals("cacc_sp_calc_type.laginkomst")) {
				User child = null;
				if (contract != null) {
					child = contract.getChild();
				}
				else if (placement != null) {
					child = placement.getStudent();
				}
				
				if (child != null) {
					try {
						Collection low = getRegularInvoiceBusiness().findRegularLowIncomeInvoicesForPeriodeAndCategory(period,((Integer)child.getPrimaryKey()).intValue(), reg.getOperation());
						if (low != null && !low.isEmpty()) {
							Iterator lowIt = low.iterator();
							if (lowIt.hasNext()) {
								RegularInvoiceEntry entry = (RegularInvoiceEntry) lowIt.next();
								
								ret = new PostingDetail();
								ret.setAmount(Math.round(entry.getAmount() - total_sum));
								ret.setRuleSpecType(reg.getRegSpecType().getLocalizationKey());
								ret.setTerm(reg.getName());
								//			ret.setVat(32.0f);
								//			ret.setVatRegulationID(1);
								
							}
							else {
								throw new LowIncomeException("reg_exp.no_low_income_entry","No low income entry for this child");
							}
						}
						else {
							throw new LowIncomeException("reg_exp.no_low_income_entry","No low income entry for this child");
						}
					}
					catch (Exception e) {
						throw new LowIncomeException("reg_exp.no_low_income_entry","No low income entry for this child");
					}
				}
				else {
					throw new LowIncomeException("reg_exp.no_low_income_entry","No low income entry for this child");
				}
							
			}
		}
		else {
			ret = new PostingDetail();
			ret.setAmount(Math.round(reg.getAmount().floatValue()));
			ret.setRuleSpecType(reg.getRegSpecType().getLocalizationKey());
			ret.setTerm(reg.getName());
			//			ret.setVat(32.0f);
			//			ret.setVatRegulationID(1);
		}

		return ret;
	}

	protected MemberFamilyLogic getMemberFamilyLogic() throws RemoteException {
		return (MemberFamilyLogic) getServiceInstance(MemberFamilyLogic.class);
	}

	protected UserInfoService getUserInfoService() throws RemoteException {
		return (UserInfoService) getServiceInstance(UserInfoService.class);
	}

	/**
	 * Function to return all the regulations that fit the description/selection 
	 * according to the input parameters and sorted by the condition order.
	 * 
	 * @param operation
	 * @param flow
	 * @param period
	 * @param conditionType
	 * @param condition
	 * @return ArrayList containing the regulations 
	 */
	public Collection getAllRegulationsByOperationFlowPeriodConditionTypeRegSpecType(
	String operation, String flow, Date period, String conditionType, String mainRule, Collection condition) {
		Collection match = new ArrayList();

		try {
			RegulationHome home = getRegulationHome();
			int flowID = -1;
			int condTypeID = -1;
			int regSpecTypeId = -1;
			int mainRuleId = -1;

			try {
				PaymentFlowType pfType = getPaymentFlowTypeHome().findByLocalizationKey(flow);
				if (pfType != null)
					flowID = ((Integer) pfType.getPrimaryKey()).intValue();
			}
			catch (Exception e) {
				flowID = -1;
			}

			try {
				ConditionType cType = getConditionTypeHome().findByConditionType(conditionType);
				if (cType != null)
					condTypeID = ((Integer) cType.getPrimaryKey()).intValue();
			}
			catch (Exception e) {
				condTypeID = -1;
			}
			
			try {
				MainRule mRule = getMainRuleHome().findMainRuleByName(mainRule);
				if (mRule != null)
					mainRuleId = ((Integer) mRule.getPrimaryKey()).intValue();
			}
			catch(Exception e) {
				mainRuleId = -1;
			}

			Collection reg = home.findRegulations(period, period, operation, flowID, condTypeID, regSpecTypeId, mainRuleId);
			if (reg != null && !reg.isEmpty()) {
				Iterator it = reg.iterator();
				while (it.hasNext()) {
					Regulation regulation = (Regulation) it.next();
					int i = checkConditions(regulation, condition);
					if (i == 1)
						match.add(regulation);
				}
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (FinderException e) {
			e.printStackTrace();
		}

		return match;
	}

	/**
	 * 
	 * @param regulation
	 * @return
	 */

	public SchoolType getSchoolType(Regulation regulation) {
		Collection cond = null;
		try {
			cond = getConditionHome().findAllConditionsByRegulation(regulation);
		}
		catch (RemoteException ex) {
			ex.printStackTrace();
			return null;
		}
		catch (FinderException ex) {
			ex.printStackTrace();
			return null;
		}

		Iterator i = cond.iterator();
		boolean match = false;
		while (i.hasNext() && !match) {
			Condition regCond = (Condition) i.next();
			if (regCond.getConditionID() == Integer.parseInt(RuleTypeConstant.CONDITION_ID_OPERATION)) {
				int id = regCond.getIntervalID();
				try {
					return getSchoolTypeHome().findByPrimaryKey(new Integer(id));
				}
				catch (RemoteException ex) {
					ex.printStackTrace();
					return null;
				}
				catch (FinderException ex) {
					ex.printStackTrace();
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Returns PostingDetail (the text, sum, vat and vat type) calculated for the specific regulation
	 * and contract
	 * 
	 * @param totalSum
	 * @param contract
	 * @return PostingDetail
	 */
	public PostingDetail getPostingDetailForContract(float totalSum, ChildCareContract contract, Regulation regulation, Date period, Collection condition) throws BruttoIncomeException, LowIncomeException {
		return getPostingDetailFromRegulation(regulation, condition, contract, null, period, totalSum);
	}

	/**
	 * Returns PostingDetail (the text, sum, vat and vat type) calculated for the specific regulation
	 * and School Placement
	 * 
	 * @param totalSum
	 * @param schoolClassMember
	 * @param regulation
	 * @return
	 */
	public PostingDetail getPostingDetailForPlacement(float totalSum, SchoolClassMember schoolClassMember, Regulation regulation, Date period, Collection condition) throws BruttoIncomeException, LowIncomeException {
		return getPostingDetailFromRegulation(regulation, condition, null, schoolClassMember, period, totalSum);
	}

	/**
	 * I Need this before we can use replaceAll with regular expressions in 1.4
	 * 
	 * @author Kelly
	 */
	public String replaceToDot(String s) {

		String replace = s;
		int dot = s.indexOf(".");
		if (dot > 0) {
			replace = s.substring(dot + 1);
		}
		return replace;
	}

	protected RegularInvoiceBusiness getRegularInvoiceBusiness() throws RemoteException {
		return (RegularInvoiceBusiness) getServiceInstance(RegularInvoiceBusiness.class);
	}
	
	protected ActivityTypeHome getActivityTypeHome() throws RemoteException {
		return (ActivityTypeHome) com.idega.data.IDOLookup.getHome(ActivityType.class);
	}

	protected SchoolTypeHome getSchoolTypeHome() throws RemoteException {
		return (SchoolTypeHome) com.idega.data.IDOLookup.getHome(SchoolType.class);
	}

	protected ResourceHome getResourceHome() throws RemoteException {
		return (ResourceHome) com.idega.data.IDOLookup.getHome(Resource.class);
	}

	protected SchoolManagementTypeHome getSchoolManagementTypeHome() throws RemoteException {
		return (SchoolManagementTypeHome) com.idega.data.IDOLookup.getHome(SchoolManagementType.class);
	}

	protected CommuneBelongingTypeHome getCommuneBelongingTypeHome() throws RemoteException {
		return (CommuneBelongingTypeHome) com.idega.data.IDOLookup.getHome(CommuneBelongingType.class);
	}

	protected RegulationSpecTypeHome getRegulationSpecTypeHome() throws RemoteException {
		return (RegulationSpecTypeHome) com.idega.data.IDOLookup.getHome(RegulationSpecType.class);
	}

	protected PaymentFlowTypeHome getPaymentFlowTypeHome() throws RemoteException {
		return (PaymentFlowTypeHome) com.idega.data.IDOLookup.getHome(PaymentFlowType.class);
	}

	protected ProviderTypeHome getProviderTypeHome() throws RemoteException {
		return (ProviderTypeHome) com.idega.data.IDOLookup.getHome(ProviderType.class);
	}

	protected RegulationHome getRegulationHome() throws RemoteException {
		return (RegulationHome) com.idega.data.IDOLookup.getHome(Regulation.class);
	}

	protected ConditionHome getConditionHome() throws RemoteException {
		return (ConditionHome) com.idega.data.IDOLookup.getHome(Condition.class);
	}

	protected ConditionTypeHome getConditionTypeHome() throws RemoteException {
		return (ConditionTypeHome) com.idega.data.IDOLookup.getHome(ConditionType.class);
	}

	protected VATRuleHome getVATRuleHome() throws RemoteException {
		return (VATRuleHome) com.idega.data.IDOLookup.getHome(VATRule.class);
	}

	protected YesNoHome getYesNoHome() throws RemoteException {
		return (YesNoHome) com.idega.data.IDOLookup.getHome(YesNo.class);
	}

	protected SpecialCalculationTypeHome getSpecialCalculationTypeHome() throws RemoteException {
		return (SpecialCalculationTypeHome) com.idega.data.IDOLookup.getHome(SpecialCalculationType.class);
	}

	protected MainRuleHome getMainRuleHome() throws RemoteException {
		return (MainRuleHome) com.idega.data.IDOLookup.getHome(MainRule.class);
	}
}
