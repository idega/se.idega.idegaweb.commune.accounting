package se.idega.idegaweb.commune.accounting.invoice.data;


public interface BatchRunErrorHome extends com.idega.data.IDOHome
{
 public BatchRunError create() throws javax.ejb.CreateException;
 public BatchRunError findByPrimaryKey(Object pk) throws javax.ejb.FinderException;
 public java.util.Collection findAllOrdered()throws javax.ejb.FinderException;
 public java.util.Collection findByBatchRun(se.idega.idegaweb.commune.accounting.invoice.data.BatchRun p0, boolean p1)throws javax.ejb.FinderException;

}