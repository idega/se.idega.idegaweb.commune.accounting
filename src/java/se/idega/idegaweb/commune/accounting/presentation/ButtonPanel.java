/*
 * $Id: ButtonPanel.java,v 1.17 2006/04/09 11:53:33 laddi Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.presentation;

import com.idega.core.builder.data.ICPage;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SubmitButton;

/**
 * A class for button panels in Check & Peng application forms.
 * 
 * <p>
 * Last modified: $Date: 2006/04/09 11:53:33 $
 *
 * @author <a href="http://www.ncmedia.com">Anders Lindman</a>
 * @version $Revision: 1.17 $
 * @see ApplicationForm
 */
public class ButtonPanel extends AccountingBlock {

	private Table table = null;
	private int buttonColumn = 1;
	AccountingBlock parent = null; 

	/**
	 * Constructs an empty button panel.
	 */
	public ButtonPanel(AccountingBlock parent) {
		this.parent = parent;
		this.table = new Table();
		this.table.setCellpadding(getCellpadding());
		this.table.setCellspacing(getCellspacing());
		super.add(this.table);
	}

	/**
	 * Adds a localized and formatted submit button to the panel.
	 * The buttoms are added from left to right.
	 * @param parameter the form parameter name for the button
	 * @param textKey the text to localize
	 * @param defaultText the default localized text
	 */
	public void addLocalizedButton(String parameter, String textKey, String defaultText) {
		SubmitButton button = new SubmitButton(parameter, localize(textKey, defaultText));
		addButton(button);
	}

  // Added by G�ran Borgman 20030902
  /**
   * Adds a localized and formatted submit button to the panel.
   * The buttoms are added from left to right.
   * @param parameter the form parameter name for the button
   * @param parameterValue the form parameter value sent with this parameter
   * @param textKey the text to localize
   * @param defaultText the default localized text
   */
  public void addLocalizedButton(String parameterName, String parameterValue, String textKey, String defaultText) {
    SubmitButton button = new SubmitButton(localize(textKey, defaultText), parameterName, parameterValue);
    addButton(button);
  }

	/**
	 * Adds a localized and formatted button to the panel with a window to open.
	 * The buttoms are added from left to right.
	 * @param parameter the form parameter name for the button
	 * @param textKey the text to localize
	 * @param defaultText the default localized text
	 * @param windowClass the class of the window to open when clicked
 	 */
	public void addLocalizedButton(String parameter, String textKey, String defaultText, Class windowClass) {
		GenericButton button = new GenericButton(parameter, localize(textKey, defaultText));
		button.setWindowToOpen(windowClass);
		addButton (button);
	}

	/**
	 * Adds a localized and formatted button to the panel with a page to open.
	 * The buttoms are added from left to right.
	 * @param parameter the form parameter name for the button
	 * @param textKey the text to localize
	 * @param defaultText the default localized text
	 * @param page an IBPage to be opened when clicked
	 * @author <a href="http://www.lindman.se">Kjell Lindman</a>
	 */
	public void addLocalizedButton(String parameter, String textKey, String defaultText, ICPage page) {
		GenericButton button = new GenericButton(parameter, localize(textKey, defaultText));
		button.setPageToOpen(page);
		addButton(button);
	}


	/**
	 * Adds a localized and formatted button to the panel with 
	 * a confirm message and parameter.
	 * The buttoms are added from left to right.
	 * @param parameter the form parameter name for the button
	 * @param textKey the text to localize
	 * @param defaultText the default localized text
	 * @param checkboxParameter the parameter name of the checkbox to confirm
	 * @param comfirmTextKey the text key for the confirm message
	 * @param confirmDefaultText the default text for the confirm message 
	 * @author <a href="http://www.lindman.se">Kjell Lindman</a>
	 */
	public void addLocalizedConfirmButton(
			String parameter,
			String textKey,
			String defaultText,
			String checkboxParameter,
			String confirmTextKey,
			String confirmDefaultText) {
		SubmitButton button = getLocalizedButton(parameter, textKey, defaultText);
		button.setToEnableWhenChecked(checkboxParameter);
		button.setSubmitConfirm(localize(confirmTextKey, confirmDefaultText));
		addButton(button);
	}

	/**
	 * Adds a button to the panel.
	 */
	public void addButton(GenericButton button) {
		button = getButton(button);
		this.table.add(button, this.buttonColumn , 1);
		this.buttonColumn++;
	}
	
	/**
	 * Adds a object to the panel.
	*/
	public void add(PresentationObject object) {
	  this.table.add(object, this.buttonColumn , 1);
	  this.buttonColumn++;
    }
	
	public String localize(String textKey, String defaultText) {
		if (this.parent != null) {
			return this.parent.localize(textKey, defaultText);
		} else {
			return defaultText;
		}
	}
	
	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.accounting.presentation.AccountingBlock#init(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
	}
}
