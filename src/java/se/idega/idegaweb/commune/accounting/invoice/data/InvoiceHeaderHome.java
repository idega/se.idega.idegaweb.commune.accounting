package se.idega.idegaweb.commune.accounting.invoice.data;


public interface InvoiceHeaderHome extends com.idega.data.IDOHome
{
 public InvoiceHeader create() throws javax.ejb.CreateException;
 public InvoiceHeader findByPrimaryKey(Object pk) throws javax.ejb.FinderException;

}