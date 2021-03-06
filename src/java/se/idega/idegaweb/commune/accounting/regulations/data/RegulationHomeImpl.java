package se.idega.idegaweb.commune.accounting.regulations.data;


public class RegulationHomeImpl extends com.idega.data.IDOFactory implements RegulationHome
{
 protected Class getEntityInterfaceClass(){
  return Regulation.class;
 }


 public Regulation create() throws javax.ejb.CreateException{
  return (Regulation) super.createIDO();
 }


public java.util.Collection findAllBy()throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindAllBy();
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findAllByMainRule(java.lang.String p0)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindAllByMainRule(p0);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findAllByRegulationSpecType(java.lang.String p0)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindAllByRegulationSpecType(p0);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findAllRegulations()throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindAllRegulations();
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public Regulation findRegulation(int p0)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	Object pk = ((RegulationBMPBean)entity).ejbFindRegulation(p0);
	this.idoCheckInPooledEntity(entity);
	return this.findByPrimaryKey(pk);
}

public Regulation findRegulationOverlap(java.lang.String p0,java.sql.Date p1,java.sql.Date p2,se.idega.idegaweb.commune.accounting.regulations.data.Regulation p3)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	Object pk = ((RegulationBMPBean)entity).ejbFindRegulationOverlap(p0,p1,p2,p3);
	this.idoCheckInPooledEntity(entity);
	return this.findByPrimaryKey(pk);
}

public java.util.Collection findRegulations(java.sql.Date p0,java.sql.Date p1,java.lang.String p2,int p3,int p4,int p5,int p6)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulations(p0,p1,p2,p3,p4,p5,p6);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findRegulationsByNameNoCase(java.lang.String p0)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulationsByNameNoCase(p0);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findRegulationsByNameNoCaseAndCategory(java.lang.String p0,java.lang.String p1)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulationsByNameNoCaseAndCategory(p0,p1);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findRegulationsByNameNoCaseAndDate(java.lang.String p0,java.sql.Date p1)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulationsByNameNoCaseAndDate(p0,p1);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findRegulationsByNameNoCaseDateAndCategory(java.lang.String p0,java.sql.Date p1,java.lang.String p2)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulationsByNameNoCaseDateAndCategory(p0,p1,p2);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findRegulationsByPeriod(java.sql.Date p0,java.sql.Date p1)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulationsByPeriod(p0,p1);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findRegulationsByPeriod(java.sql.Date p0,java.sql.Date p1,java.lang.String p2,int p3,int p4)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulationsByPeriod(p0,p1,p2,p3,p4);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

public java.util.Collection findRegulationsByPeriodAndOperationId(java.sql.Date p0,java.lang.String p1)throws javax.ejb.FinderException{
	com.idega.data.IDOEntity entity = this.idoCheckOutPooledEntity();
	java.util.Collection ids = ((RegulationBMPBean)entity).ejbFindRegulationsByPeriodAndOperationId(p0,p1);
	this.idoCheckInPooledEntity(entity);
	return this.getEntityCollectionForPrimaryKeys(ids);
}

 public Regulation findByPrimaryKey(Object pk) throws javax.ejb.FinderException{
  return (Regulation) super.findByPrimaryKeyIDO(pk);
 }



}