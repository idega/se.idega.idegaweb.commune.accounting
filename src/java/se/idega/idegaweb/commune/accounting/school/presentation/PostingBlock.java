/*
 * $Id: PostingBlock.java,v 1.5 2006/04/09 11:53:33 laddi Exp $
 *
 * Copyright (C) 2003 Agura IT. All Rights Reserved.
 *
 * This software is the proprietary information of Agura IT AB.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.accounting.school.presentation;

import java.sql.Date;

import se.idega.idegaweb.commune.accounting.posting.business.PostingParametersException;
import se.idega.idegaweb.commune.accounting.posting.presentation.PostingParameterListEditor;

import com.idega.presentation.IWContext;

/** 
 * This block is a subclass of PostingParameterListEditor  
 * used for editing own posting and double posting strings.
 * <p>
 * Last modified: $Date: 2006/04/09 11:53:33 $ by $Author: laddi $
 *
 * @author Anders Lindman
 * @version $Revision: 1.5 $
 * @see se.idega.idegaweb.commune.accounting.posting.presentation.PostingParameterListEditor
 */
public class PostingBlock extends PostingParameterListEditor {

	private String ownPosting = null;
	private String doublePosting = null;
	
	/**
	 * Constructs posting block with empty fields for own and double posting strings
	 */	
	public PostingBlock() {
	}	
	
	/**
	 * Constructs posting block with fields for own and double posting strings by calling generateStrings(IWContext)
	 */	
	public PostingBlock(IWContext iwc) throws PostingParametersException{
		generateStrings(iwc);
	}
		
	/**
	 * Constructs posting block with fields for own and double posting strings.
	 */
	public PostingBlock(String ownPosting, String doublePosting) {
		this.ownPosting = ownPosting;
		this.doublePosting = doublePosting;
	}

	/**
	 * @see com.idega.presentation.Block#main()
	 */
	public void init(final IWContext iwc) {
		setDefaultParameters();
		addTempFieldParameters(iwc, new Date(System.currentTimeMillis()));
		add(getPostingParameterForm(iwc, getThisPostingParameter(iwc), this.ownPosting, this.doublePosting));
	}
}
