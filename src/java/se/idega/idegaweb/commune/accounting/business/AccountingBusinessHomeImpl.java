package se.idega.idegaweb.commune.accounting.business;


public class AccountingBusinessHomeImpl extends com.idega.business.IBOHomeImpl implements AccountingBusinessHome
{
 protected Class getBeanInterfaceClass(){
  return AccountingBusiness.class;
 }


 public AccountingBusiness create() throws javax.ejb.CreateException{
  return (AccountingBusiness) super.createIBO();
 }



}