/*
 * $Id: ActivityTypeBMPBean.java,v 1.2 2003/08/18 13:29:30 kjell Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 * 
 */
package se.idega.idegaweb.commune.accounting.regulations.data;
    
import java.sql.Date;
import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.GenericEntity;
import com.idega.data.IDOLegacyEntity;
import com.idega.data.IDOQuery;

/**
 * Activity types ("Forskola", "skola", "blabla") etc. Used for the posting.
 * 
 * @see se.idega.idegaweb.commune.accounting.posting.data.PostingParametersBMPBean 
 * <p>
 * $Id: ActivityTypeBMPBean.java,v 1.2 2003/08/18 13:29:30 kjell Exp $
 * 
 * @author <a href="http://www.lindman.se">Kjell Lindman</a>
 * @version $Revision: 1.2 $
 */
public class ActivityTypeBMPBean extends GenericEntity implements ActivityType
{
	private static final String ENTITY_NAME = "cacc_activity_type";

	private static final String COLUMN_ACTIVITY_TYPE = "activity_type";

	public String getEntityName() {
		return ENTITY_NAME;
	}

	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addAttribute(COLUMN_ACTIVITY_TYPE, "Activity type", true, true, String.class);
		setAsPrimaryKey (getIDColumnName(), true);
	}

	public void setActivityType(String type) { 
		setColumn(COLUMN_ACTIVITY_TYPE, type); 
	}
	
	public String getActivityType() {
		return (String) getStringColumnValue(COLUMN_ACTIVITY_TYPE);
	}

	public Collection ejbFindAllActivityTypes() throws FinderException {
		IDOQuery sql = idoQuery();
		sql.appendSelectAllFrom(this);
		sql.append(getEntityName());
		return idoFindPKsBySQL(sql.toString());
	}

	public Object ejbFindActivityType(int activityID) throws FinderException {
		IDOQuery sql = idoQuery();
		sql.appendSelectAllFrom(this).appendWhereEquals(getIDColumnName(), activityID);
		return idoFindOnePKByQuery(sql);
	}

}
