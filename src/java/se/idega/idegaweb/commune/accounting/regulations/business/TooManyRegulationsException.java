/*
 * Copyright (C) 2003 Idega software. All Rights Reserved.
 * 
 * This software is the proprietary information of Idega software. Use is
 * subject to license terms.
 *  
 */
package se.idega.idegaweb.commune.accounting.regulations.business;

import java.util.ArrayList;
import java.util.Iterator;

import se.idega.idegaweb.commune.accounting.business.AccountingException;

/**
 * @author palli
 */
public class TooManyRegulationsException extends AccountingException {
	protected ArrayList _regNames = null;

	/**
	 * @param textKey
	 * @param defaultText
	 */
	public TooManyRegulationsException(String textKey, String defaultText) {
		super(textKey, defaultText);
	}

	public void addRegulationName(String name) {
		if (this._regNames == null) {
			this._regNames = new ArrayList();
		}
		
		this._regNames.add(name);
	}
	
	public ArrayList getRegulationNames() {
		return this._regNames;
	}
	
	public String getRegulationNamesString() {
		StringBuffer names = new StringBuffer("");
		if (this._regNames != null) {
			Iterator it =this._regNames.iterator();
			while (it.hasNext()) {
				String name = (String)it.next();
				names.append(name);
				names.append(";");
			}
		}
		
		return names.toString();
	}
}