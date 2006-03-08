/**
 * 
 */
package se.idega.idegaweb.commune.accounting.school.business;

import com.idega.business.IBOHome;


/**
 * <p>
 * TODO Dainis Describe Type StudyPathBusinessHome
 * </p>
 *  Last modified: $Date: 2006/03/08 10:56:51 $ by $Author: dainis $
 * 
 * @author <a href="mailto:Dainis@idega.com">Dainis</a>
 * @version $Revision: 1.5 $
 */
public interface StudyPathBusinessHome extends IBOHome {

	public StudyPathBusiness create() throws javax.ejb.CreateException, java.rmi.RemoteException;
}
