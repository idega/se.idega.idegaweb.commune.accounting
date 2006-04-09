/*
 * $Id: ExtendedDropdownDouble.java,v 1.8 2006/04/09 11:53:33 laddi Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.regulations.presentation;

import java.util.*;
import java.lang.reflect.*;

import com.idega.presentation.*;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.SelectOption;
import com.idega.data.GenericEntity;
import se.idega.idegaweb.commune.presentation.CommuneBlock;

/**
 * @author originally Laddis (SelectDropDownDouble) 
 * @author Kelly 
 * 
 * Thanks Anders for helping me with this :-)
 *  
 * The purpose with this is to present a double drop down menu that gets populated 
 * from many different beans, business collections etc. 
 *
 * Modifications as follows:
 * 
 * Can serve many selectors on one web page ;-) (hehe Laddi) 
 * Checkout getKey and getValue. I use reflection to make this
 * functional for ConditionHolders (Pointing to either Generic entities or 
 * Business methods)
 * Supports localize on values = getLocalizationKey
 * 
 * @see se.idega.idegaweb.commune.accounting.business.ConditionHolder#
 * @see se.idega.idegaweb.commune.accounting.business.RegulationsBusiness#
 * 
 * If you interested in how to use this. See findAllOperations in RegulationsBusiness and 
 * RegulationListEditor
 * 
 * Kelly
 * 
 */ 
public class ExtendedDropdownDouble extends InterfaceObject {
	private String _styleClass;
	private String _primarySelected;
	private String _secondarySelected;
	private String primaryName;
	private String secondaryName;
	private DropdownMenu primary;
	private DropdownMenu secondary;
	private Collection _primaryCollection;
	protected Map _secondaryMap;
	protected Map _methodNameMap;
	private int _spaceBetween;
	private ExtendedDropdownDouble _objectToDisable;
	private String _disableValue;
	private boolean _disabled;
	static int _nameCounter = 0;
	private CommuneBlock _parent = null;
		
    public ExtendedDropdownDouble(CommuneBlock parent)
    {
        this.primaryName = "primary";
        this.secondaryName = "secondary";
        this.primary = null;
        this.secondary = null;
        this._spaceBetween = 3;
        this._disabled = false;
        this._parent = parent;
    }

    public ExtendedDropdownDouble(CommuneBlock parent, String primaryName, String secondaryName)
    {
        this.primaryName = "primary";
        this.secondaryName = "secondary";
        this.primary = null;
        this.secondary = null;
        this._spaceBetween = 3;
        this._disabled = false;
        this.primaryName = primaryName;
        this.secondaryName = secondaryName;
		this._parent = parent;
    }

    public void main(IWContext iwc)
        throws Exception
    {
        if(getStyleAttribute() != null)
        {
            getPrimaryDropdown().setStyleAttribute(getStyleAttribute());
            getSecondaryDropdown().setStyleAttribute(getStyleAttribute());
        }
        addElementsToPrimary();
        getPrimaryDropdown().setOnChange("setDropdownOptions"+_nameCounter+"(this, findObj('" + this.secondaryName + "'), -1);");
        if(this._objectToDisable != null)
        {
            getSecondaryDropdown().setToDisableWhenSelected(this._objectToDisable.getPrimaryName(), this._disableValue);
            getSecondaryDropdown().setToDisableWhenSelected(this._objectToDisable.getSecondaryName(), this._disableValue);
        }
        getPrimaryDropdown().setDisabled(this._disabled);
        getSecondaryDropdown().setDisabled(this._disabled);
        Table table = new Table();
        table.setCellpadding(0);
        table.setCellspacing(0);
        add(table);
        int column = 1;
        table.add(getPrimaryDropdown(), column++, 1);
        if(this._spaceBetween > 0) {
					table.setWidth(column++, this._spaceBetween);
				}
        table.add(getSecondaryDropdown(), column, 1);
        if(this._styleClass != null)
        {
            getPrimaryDropdown().setStyleClass(this._styleClass);
            getSecondaryDropdown().setStyleClass(this._styleClass);
        }
        Script script = getParentPage().getAssociatedScript();
        script.addFunction("setDropdownOptions"+_nameCounter, getSelectorScript());
        if(this._secondarySelected == null) {
					this._secondarySelected = "-1";
				}
        getParentPage().setOnLoad("setDropdownOptions"+_nameCounter+"(findObj('" + this.primaryName + "'),findObj('" + this.secondaryName + "'), '" + this._secondarySelected + "')");
		_nameCounter++;
    }

    private void addElementsToPrimary()
    {
        if(this._primaryCollection != null)
        {
            Iterator iter = this._primaryCollection.iterator();
            boolean hasSelected = false;
            while(iter.hasNext()) 
            {
                SelectOption option = (SelectOption)iter.next();
                getPrimaryDropdown().addOption(option);
                if(!hasSelected)
                {
                    getPrimaryDropdown().setSelectedOption(option.getValueAsString());
                    hasSelected = true;
                }
            }
            if(this._primarySelected != null) {
							getPrimaryDropdown().setSelectedElement(this._primarySelected);
						}
        }
    }

    private String getSelectorScript()
    {
        StringBuffer s = new StringBuffer();
        s.append("function setDropdownOptions"+_nameCounter+"(input, inputToChange, selected) {").append("\n\t");
        s.append("var dropdownValues = new Array();").append("\n\t");
        int column = 0;
        if(this._secondaryMap != null)
        {
            for(Iterator iter = this._secondaryMap.keySet().iterator(); iter.hasNext();)
            {
                column = 0;
                String key = (String)iter.next();
                Map map = (Map)this._secondaryMap.get(key);
				String methodName = (String)this._methodNameMap.get(key);
                s.append("\n\t").append("dropdownValues[\"" + key + "\"] = new Array();").append("\n\t");
                String secondKey;
                String value;
                for(Iterator iterator = map.keySet().iterator(); iterator.hasNext(); s.append("dropdownValues[\"" + key + "\"][" + column++ + "] = new Option('" + value + "','" + secondKey + "');").append("\n\t"))
                {
                    Object element = iterator.next();
                    secondKey = getKey(element);
                    value = getValue(map.get(element), methodName);
                }

            }

        }
        s.append("\n\t");
        s.append("var chosen = input.options[input.selectedIndex].value;").append("\n\t");
        s.append("inputToChange.options.length = 0;").append("\n\n\t");
        s.append("var array = dropdownValues[chosen];").append("\n\t");
        s.append("for (var a=0; a < array.length; a++)").append("{\n\t\t");
        s.append("var index = inputToChange.options.length;").append("\n\t\t");
        s.append("inputToChange.options[index] = array[a];").append("\n\t\t");
        s.append("var option = inputToChange.options[index];").append("\n\t\t");
        s.append("if (option.value == selected)").append("\n\t\t\t");
        s.append("option.selected = true;").append("\n\t\t");
        s.append("else").append("\n\t\t\t");
        s.append("option.selected = false;").append("\n\t");
        s.append("}").append("\n").append("}");
        return s.toString();
    }

    protected String getKey(Object key)
    {
		if (key instanceof GenericEntity) {
			GenericEntity ge = (GenericEntity) key;
			return ge.getPrimaryKey().toString();
		} else if (key instanceof Object []){
			Object [] o = (Object []) key;
			return o[0].toString();
		} else {
			return key.toString();
		}
    }

    protected String getValue(Object value, String methodName)
    {
		if (value instanceof GenericEntity) {
			// Bean OBJ
			Object o = value;
			Class c = o.getClass();
			String s = "";
			try {
				Method m = c.getMethod(methodName, null);					
				if (methodName.compareTo("getLocalizationKey") == 0) {
					s  = localize((String)m.invoke(o, null), (String)m.invoke(o, null));
				} else {
					s  = (String)m.invoke(o, null);
				}
				
			} catch (Exception e) {
			}
			return s;
		} else if (value instanceof Object []){
			// Direct call to Business
			Object [] o = (Object []) value;
			return o[1].toString();
		} else { 
			// Some dummy data
			return value.toString();
    	}
    }

    public void addMenuElement(String value, String nameKey, Map values, String dataMethodName)
    {
        if(this._primaryCollection == null) {
					this._primaryCollection = new Vector();
				}
        if(this._secondaryMap == null) {
            this._secondaryMap = new HashMap();
			this._methodNameMap = new HashMap();
		}
        this._primaryCollection.add(new SelectOption(localize(nameKey, nameKey), value));
        
        this._secondaryMap.put(value, values);
		this._methodNameMap.put(value, dataMethodName);
    }

    public void addEmptyElement(String primaryDisplayString, String secondaryDisplayString)
    {
        Map map = new HashMap();
        map.put("-1", secondaryDisplayString);
        addMenuElement("-1", primaryDisplayString, map, "");
    }

    public DropdownMenu getPrimaryDropdown()
    {
        if(this.primary == null) {
					this.primary = new DropdownMenu(this.primaryName);
				}
        return this.primary;
    }

    public DropdownMenu getSecondaryDropdown()
    {
        if(this.secondary == null) {
					this.secondary = new DropdownMenu(this.secondaryName);
				}
        return this.secondary;
    }

    public String getPrimaryName()
    {
        return this.primaryName;
    }

    public String getSecondaryName()
    {
        return this.secondaryName;
    }


    public void setSpaceBetween(int spaceBetween)
    {
        this._spaceBetween = spaceBetween;
    }

    public void setPrimaryName(String string)
    {
        this.primaryName = string;
    }

    public void setSecondaryName(String string)
    {
        this.secondaryName = string;
    }

    public void setSelectedValues(String primaryValue, String secondaryValue)
    {
        this._primarySelected = primaryValue;
        this._secondarySelected = secondaryValue;
    }

    public void setToEnableWhenNotSelected(ExtendedDropdownDouble doubleDropdown, String disableValue)
    {
        this._objectToDisable = doubleDropdown;
        this._disableValue = disableValue;
    }

    public void setDisabled(boolean disabled)
    {
        this._disabled = disabled;
    }

    public void setStyleClass(String styleClass)
    {
        this._styleClass = styleClass;
    }

    protected Map getSecondaryMap()
    {
        return this._secondaryMap;
    }

	public String localize(String textKey, String defaultText) {
		if (this._parent != null) {
			return this._parent.localize(textKey, defaultText);
		} else {
			return defaultText;
		}
	}
 
	/* (non-Javadoc)
	 * @see com.idega.presentation.ui.InterfaceObject#handleKeepStatus(com.idega.presentation.IWContext)
	 */
	public void handleKeepStatus(IWContext iwc) {
	}

	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#isContainer()
	 */
	public boolean isContainer() {
		return false;
	}
}