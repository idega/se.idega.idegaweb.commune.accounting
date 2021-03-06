/*
 * Copyright (C) 2003 Idega software. All Rights Reserved.
 *
 * This software is the proprietary information of Idega software.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.export.ifs.data;

import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.GenericEntity;
import com.idega.data.IDOQuery;

/**
 * @author palli
 */
public class IFSCheckRecordBMPBean extends GenericEntity implements IFSCheckRecord {
	private static final String ENTITY_NAME = "cacc_ifs_check_record";

	private static final String COLUMN_CHECK_HEADER_ID = "header_id";
	private static final String COLUMN_ERROR_CONCERNS = "error_concerns_desc";
	private static final String COLUMN_ERROR = "error_key";

	private static final String EVENT_MISSING_AMOUNT = "cacc_ifs_check_missing_amount_on_check";

	/* (non-Javadoc)
	 * @see com.idega.data.GenericEntity#getEntityName()
	 */
	public String getEntityName() {
		return ENTITY_NAME;
	}

	/* (non-Javadoc)
	 * @see com.idega.data.GenericEntity#initializeAttributes()
	 */
	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addManyToOneRelationship(COLUMN_CHECK_HEADER_ID, IFSCheckHeader.class);
		addAttribute(COLUMN_ERROR_CONCERNS, "Comments on check", true, true, java.lang.String.class);
		addAttribute(COLUMN_ERROR, "Key to localized description of error", true, true, java.lang.String.class);
	}

	public void setHeaderId(int id) {
		setColumn(COLUMN_CHECK_HEADER_ID, id);
	}

	public void setHeader(IFSCheckHeader header) {
		setColumn(COLUMN_CHECK_HEADER_ID, header);
	}

	public int getHeaderId() {
		return getIntColumnValue(COLUMN_CHECK_HEADER_ID);
	}

	public IFSCheckHeader getHeader() {
		return (IFSCheckHeader) getColumnValue(COLUMN_CHECK_HEADER_ID);
	}

	public void setErrorAmountMissing() {
		setColumn(COLUMN_ERROR, EVENT_MISSING_AMOUNT);
	}

	public void setError(String key) {
		setColumn(COLUMN_ERROR, key);
	}

	public String getErrorAmountMissing() {
		return EVENT_MISSING_AMOUNT;
	}

	public String getError() {
		return getStringColumnValue(COLUMN_ERROR);
	}

	public void setErrorConcerns(String desc) {
		setColumn(COLUMN_ERROR_CONCERNS, desc);
	}

	public String getErrorConcerns() {
		return getStringColumnValue(COLUMN_ERROR_CONCERNS);
	}

	public Collection ejbFindAll() throws FinderException {
		IDOQuery query = idoQuery();
		query.appendSelectAllFrom(this);

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByHeaderId(int id) throws FinderException {
		IDOQuery query = idoQuery();
		query.appendSelectAllFrom(this);
		query.appendWhereEquals(COLUMN_CHECK_HEADER_ID, id);

		return idoFindPKsByQuery(query);
	}

	public Collection ejbFindAllByHeader(IFSCheckHeader header) throws FinderException {
		return ejbFindAllByHeaderId(((Integer) header.getPrimaryKey()).intValue());
	}

}