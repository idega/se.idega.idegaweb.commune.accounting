package se.idega.idegaweb.commune.accounting.school.business;


public interface ProviderBusiness extends com.idega.business.IBOService
{
 public void deleteProvider(java.lang.String p0)throws se.idega.idegaweb.commune.accounting.school.business.ProviderException, java.rmi.RemoteException;
 public se.idega.idegaweb.commune.accounting.school.data.Provider getProvider(int p0)throws se.idega.idegaweb.commune.accounting.school.business.StudyPathException, java.rmi.RemoteException;
 public void saveProvider(java.lang.String p0,java.lang.String p1,java.lang.String p2,java.lang.String p3,java.lang.String p4,java.lang.String p5,java.lang.String p6,java.lang.String p7,java.lang.String p8,java.lang.String p9,java.lang.String p10,java.util.Map p11,java.lang.String p12,java.lang.String p13,java.lang.String p14,java.sql.Date p15,java.lang.String p16,java.lang.String p17,java.lang.String p18,java.lang.String p19,java.lang.String p20,java.lang.String p21,java.lang.String p22,java.lang.String p23)throws se.idega.idegaweb.commune.accounting.school.business.ProviderException, java.rmi.RemoteException;
}
