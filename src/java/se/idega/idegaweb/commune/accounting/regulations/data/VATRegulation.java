package se.idega.idegaweb.commune.accounting.regulations.data;


public interface VATRegulation extends com.idega.data.IDOEntity
{
 public java.lang.String getDescription();
 public java.lang.String getIDColumnName();
 public se.idega.idegaweb.commune.accounting.regulations.data.PaymentFlowType getPaymentFlowType();
 public int getPaymentFlowTypeId();
 public java.sql.Date getPeriodFrom();
 public java.sql.Date getPeriodTo();
 public se.idega.idegaweb.commune.accounting.regulations.data.ProviderType getProviderType();
 public int getProviderTypeId();
 public int getVATPercent();
 public void initializeAttributes();
 public void setDescription(java.lang.String p0);
 public void setPaymentFlowTypeId(int p0);
 public void setPeriodFrom(java.sql.Date p0);
 public void setPeriodTo(java.sql.Date p0);
 public void setProviderTypeId(int p0);
 public void setVATPercent(int p0);
}
