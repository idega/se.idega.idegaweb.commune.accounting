/*
 * $Id: PostingFieldBMPBean.java,v 1.9 2003/12/02 09:40:26 sigtryggur Exp $
 *
 * Copyright (C) 2002 Idega hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.posting.data;

import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.GenericEntity;
import com.idega.data.IDOQuery;

/**
 * Holds descriptive information about fields in strings holding accounting information, 
 * sent to external accounting systems.
 * 
 * From Kravspecifikation Check & Peng 13.3
 * @author Joakim
 * @see PostingField
 */

public class PostingFieldBMPBean extends GenericEntity implements PostingField
{
	private static final String ENTITY_NAME = "cacc_posting_field";

	private static final String COLUMN_CP_POSTING_STRING_ID = "CP_POSTING_STRING_ID";
	private static final String COLUMN_ORDER_NR = "ORDER_NR";
	private static final String COLUMN_FIELD_TITLE = "FIELD_TITLE";
	private static final String COLUMN_LEN = "LEN";
	private static final String COLUMN_JUSTIFICATION = "JUSTIFICATION";
	private static final String COLUMN_MANDATORY = "MANDATORY";
	private static final String COLUMN_PAD_CHAR = "PAD_CHAR";
	private static final String COLUMN_FIELD_TYPE = "FIELD_TYPE";
	
	private static final String FIELD_TYPE_ALPHA = "ALPHA";
	private static final String FIELD_TYPE_NUMERIC = "NUMERIC";
	private static final String FIELD_TYPE_ALPHA_NUMERIC = "ALPHANUM";
	
	public static final int JUSTIFY_LEFT = 0;
	public static final int JUSTIFY_RIGHT = 1;


	/**
	 * @see com.idega.data.IDOLegacyEntity#getEntityName()
	 */
	public String getEntityName() {
		return ENTITY_NAME;
	}

	/**
	 * @see com.idega.data.IDOLegacyEntity#initializeAttributes()
	 */
	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addAttribute(COLUMN_CP_POSTING_STRING_ID, "", true, true, java.lang.Integer.class);
		addAttribute(COLUMN_ORDER_NR, "", true, true, java.lang.Integer.class);
		addAttribute(COLUMN_FIELD_TITLE, "", true, true, java.lang.String.class, 1000);
		addAttribute(COLUMN_LEN, "", true, true, java.lang.Integer.class);
		addAttribute(COLUMN_JUSTIFICATION, "", true, true, java.lang.Integer.class);
		addAttribute(COLUMN_MANDATORY,"",true,true,java.lang.Boolean.class);
		addAttribute(COLUMN_PAD_CHAR, "", true, true, java.lang.String.class, 1);
		addAttribute(COLUMN_FIELD_TYPE, "", true, true, java.lang.String.class);
		
		addManyToOneRelationship(COLUMN_CP_POSTING_STRING_ID,PostingString.class);
		setNullable(COLUMN_CP_POSTING_STRING_ID, false);
		setNullable(COLUMN_ORDER_NR, false);
		setNullable(COLUMN_FIELD_TITLE, false);
		setNullable(COLUMN_LEN, false);
		setNullable(COLUMN_JUSTIFICATION, false);
		setNullable(COLUMN_MANDATORY, false);
		setNullable(COLUMN_PAD_CHAR, false);
		setNullable(COLUMN_FIELD_TYPE, false);
	}

	public int getPostingStringId() {
		return getIntColumnValue(COLUMN_CP_POSTING_STRING_ID);	
	}
	
	public int getOrderNr() {
		return getIntColumnValue(COLUMN_ORDER_NR);	
	}
	
	public String getFieldTitle() {
		return getStringColumnValue(COLUMN_FIELD_TITLE);	
	}

	public int getLen() {
		return getIntColumnValue(COLUMN_LEN);	
	}
	
	public int getJustification() {
		return getIntColumnValue(COLUMN_JUSTIFICATION);	
	}
	
	public boolean getIsMandatory() {
		return getBooleanColumnValue(COLUMN_MANDATORY, false);
	}
	
	public char getPadChar() {
		return getCharColumnValue(COLUMN_PAD_CHAR);
	}

	public String getFieldType() {
		return getStringColumnValue(COLUMN_FIELD_TYPE);
	}

	public void setPostingStringId(int postingStringId) {
		setColumn(COLUMN_CP_POSTING_STRING_ID, postingStringId);
	}

	public void setOrderNr(int orderNr) {
		setColumn(COLUMN_ORDER_NR, orderNr);
	}

	public void setFieldTitle(String title) {
		setColumn(COLUMN_FIELD_TITLE, title);
	}

	public void setLen(int len) {
		setColumn(COLUMN_LEN, len);
	}

	public void setJustification(int justification) {
		setColumn(COLUMN_JUSTIFICATION, justification);
	}

	public void setIsMandatory(boolean mandatory) {
		setColumn(COLUMN_FIELD_TITLE, mandatory);
	}

	public void setPadChar(char padChar) {
		setColumn(COLUMN_PAD_CHAR, padChar);
	}

	public void setFieldTypeAlpha() {
		setColumn(COLUMN_FIELD_TYPE, FIELD_TYPE_ALPHA);
	}

	public void setFieldTypeNumeric() {
		setColumn(COLUMN_FIELD_TYPE, FIELD_TYPE_NUMERIC);
	}

	public void setFieldTypeAlphaNumeric() {
		setColumn(COLUMN_FIELD_TYPE, FIELD_TYPE_ALPHA_NUMERIC);
	}

	/**
	 * Checks if field is alphabetic only
	 * @author Kelly
	 */
	public boolean isAlpha() {
		return getStringColumnValue(COLUMN_FIELD_TYPE).compareTo(FIELD_TYPE_ALPHA) == 0 ? true : false; 
	}

	/**
	 * Checks if field is numeric only
	 * @author Kelly
	 */
	public boolean isNumeric() {
		return getStringColumnValue(COLUMN_FIELD_TYPE).compareTo(FIELD_TYPE_NUMERIC) == 0 ? true : false; 
	}

	/**
	 * Checks if field is alphanumeric
	 * @author Kelly
	 */
	public boolean isAlphaNumeric() {
		return getStringColumnValue(COLUMN_FIELD_TYPE).compareTo(FIELD_TYPE_ALPHA_NUMERIC) == 0 ? true : false; 
	}
	
	public Collection ejbFindAllFieldsByPostingString(int PostingStringId) throws FinderException {
		IDOQuery sql = idoQuery();
		sql.appendSelectAllFrom(this);
		sql.appendWhereEquals(COLUMN_CP_POSTING_STRING_ID, PostingStringId);
		sql.appendOrderBy(COLUMN_ORDER_NR);

		return idoFindPKsByQuery(sql);
	}		
	
	public Object ejbFindFieldByPostingStringAndFieldNo(int PostingStringId, int fieldNo) throws FinderException {
		IDOQuery sql = idoQuery();
		sql.appendSelectAllFrom(this);
		sql.appendWhereEquals(COLUMN_CP_POSTING_STRING_ID, PostingStringId);
		sql.appendAndEquals(COLUMN_ORDER_NR, fieldNo);
		
		return idoFindOnePKByQuery(sql);
	}		
	
}
