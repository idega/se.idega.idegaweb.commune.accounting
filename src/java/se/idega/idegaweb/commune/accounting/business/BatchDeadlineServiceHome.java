/*
 * $Id: BatchDeadlineServiceHome.java,v 1.1 2004/11/22 16:40:26 aron Exp $
 * Created on 12.11.2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package se.idega.idegaweb.commune.accounting.business;



import com.idega.business.IBOHome;

/**
 * 
 *  Last modified: $Date: 2004/11/22 16:40:26 $ by $Author: aron $
 * 
 * @author <a href="mailto:aron@idega.com">aron</a>
 * @version $Revision: 1.1 $
 */
public interface BatchDeadlineServiceHome extends IBOHome {
    public BatchDeadlineService create() throws javax.ejb.CreateException,
            java.rmi.RemoteException;

}
