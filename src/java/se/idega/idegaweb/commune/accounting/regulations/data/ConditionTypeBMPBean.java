/*
 * $Id: ConditionTypeBMPBean.java,v 1.3 2003/10/15 12:11:55 kjell Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 * 
 */
package se.idega.idegaweb.commune.accounting.regulations.data;
    
import java.util.Collection;

import javax.ejb.FinderException;

import com.idega.data.GenericEntity;
import com.idega.data.IDOQuery;
import com.idega.data.IDOLookup;

/**
 * Holds Special types ("H�rleds", "Manuell", "Moms") 
 * 
 * <p>
 * $Id: ConditionTypeBMPBean.java,v 1.3 2003/10/15 12:11:55 kjell Exp $
 * 
 * @author <a href="http://www.lindman.se">Kjell Lindman</a>
 * @version $Revision: 1.3 $
 */
public class ConditionTypeBMPBean extends GenericEntity implements ConditionType {
	
	private static final String ENTITY_NAME = "cacc_condition_type";
	private static final String COLUMN_CONDITION_TYPE = "condition_type";

	public String getEntityName() {
		return ENTITY_NAME;
	}

	public void insertStartData () throws Exception {
		super.insertStartData ();
        
		System.out.println ("��� Invoked " + ENTITY_NAME + ".insertStartData ()");

		ConditionTypeHome home
				= (ConditionTypeHome) IDOLookup.getHome(ConditionType.class);
		final String [] data = { "harleds", "manuell", "moms", "formel" };
		for (int i = 0; i < data.length; i++) {
			ConditionType condType = home.create();
			condType.setConditionType(ENTITY_NAME + "." + data[i]);
			condType.store();
		}
	}

	public void initializeAttributes() {
		addAttribute(getIDColumnName());
		addAttribute(COLUMN_CONDITION_TYPE, "Condition type", true, true, String.class);
		setAsPrimaryKey (getIDColumnName(), true);
	}

	public void setConditionType(String type) { 
		setColumn(COLUMN_CONDITION_TYPE, type); 
	}
	
	public String getConditionType() {
		return getStringColumnValue(COLUMN_CONDITION_TYPE);
	}

	public void setLocalizationKey(String type) { 
		setColumn(COLUMN_CONDITION_TYPE, type); 
	}
	
	public String getLocalizationKey() {
		return getStringColumnValue(COLUMN_CONDITION_TYPE);
	}

	public Collection ejbFindAllConditionTypes() throws FinderException {
		IDOQuery sql = idoQuery();
		sql.appendSelectAllFrom(this);
		sql.appendOrderBy(COLUMN_CONDITION_TYPE);
		return idoFindPKsBySQL(sql.toString());
	}

	public Object ejbFindConditionType(int id) throws FinderException {
		IDOQuery sql = idoQuery();
		sql.appendSelectAllFrom(this).appendWhereEquals(getIDColumnName(), id);
		return idoFindOnePKByQuery(sql);
	}

}
