package se.idega.idegaweb.commune.accounting.invoice.data;


public class PaymentHeaderHomeImpl extends com.idega.data.IDOFactory implements PaymentHeaderHome
{
 protected Class getEntityInterfaceClass(){
  return PaymentHeader.class;
 }


 public PaymentHeader create() throws javax.ejb.CreateException{
  return (PaymentHeader) super.createIDO();
 }


public PaymentHeader findBySchoolCategorySchoolPeriod(com.idega.block.school.data.School p0,com.idega.block.school.data.SchoolCategory p1,java.sql.Date p2)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	Object pk = ((PaymentHeaderBMPBean)entity).ejbFindBySchoolCategorySchoolPeriod(p0,p1,p2);
	this.idoCheckInPooledEntity(entity);
	return this.findByPrimaryKey(pk);
}

 public PaymentHeader findByPrimaryKey(Object pk) throws javax.ejb.FinderException{
  return (PaymentHeader) super.findByPrimaryKeyIDO(pk);
 }



}