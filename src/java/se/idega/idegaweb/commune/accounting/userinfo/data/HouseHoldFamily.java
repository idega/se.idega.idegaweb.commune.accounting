/*
 * Created on Dec 18, 2003
 *
 */
package se.idega.idegaweb.commune.accounting.userinfo.data;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


import com.idega.core.location.data.Address;
import com.idega.user.data.User;

/**
 * HouseHoldFamily
 * @author aron 
 * @version 1.0
 */
public class HouseHoldFamily {
	
	User head = null;
	User spouse = null;
	User cohabitant = null;
	Collection parentialChildren = null;
	Collection custodyChildren = null;
	Address address = null;
	
	public HouseHoldFamily(){
	
	}
	
	public HouseHoldFamily(User head){
		this.head = head;
	}
	
	public User getHeadOfFamily(){
		return this.head;
	}
	
	public void setHeadOfFamily(User head){
		this.head = head;
	}
	public User getSpouse(){
		return this.spouse;
	}
	public void setSpouse(User spouse){
		this.spouse = spouse;
	}
	public User getCohabitant(){
		return this.cohabitant;
	}
	public void setCohabitant(User cohabitant){
		this.cohabitant = cohabitant;
	}
	public Collection getParentialChildren(){
		return this.parentialChildren;
	}
	public void setParentialChildren(Collection children){
		this.parentialChildren = children;
	}
	public Collection getCustodyChildren(){
		return this.custodyChildren;
	}
	public void setCustodyChildren(Collection children){
		this.custodyChildren = children;
	}
	public Address getAddress(){
		return this.address;
	}
	public void setAddress(Address address){
		this.address = address;
	}
	
	public boolean hasSpouse(){
		return this.spouse!=null;
	}
	public boolean hasCohabitant(){
		return this.cohabitant!=null;
	}
	public boolean hasAddress(){
		return this.address!=null;
	}
	public boolean  hasParentialChildren(){
		return this.parentialChildren!=null && !this.parentialChildren.isEmpty();
	}
	public boolean  hasCustodyChildren(){
		return this.custodyChildren!=null && !this.custodyChildren.isEmpty();
	}
	
	public boolean hasChildren(){
		return hasParentialChildren() || hasCustodyChildren();
	}
	
	public Collection getChildren(){
		Collection children = new Vector();
		Map childMap = new Hashtable();
		if(hasParentialChildren()){
			for (Iterator iter = this.parentialChildren.iterator(); iter.hasNext();) {
				User child = (User) iter.next();
				if(!childMap.containsKey(child.getPrimaryKey())){
					children.add(child);
					childMap.put(child.getPrimaryKey(),child.getPrimaryKey());
				}
			}
		}
		if(hasCustodyChildren()){
			for (Iterator iter = this.custodyChildren.iterator(); iter.hasNext();) {
				User child = (User) iter.next();
				if(!childMap.containsKey(child.getPrimaryKey())){
					children.add(child);
					childMap.put(child.getPrimaryKey(),child.getPrimaryKey());
				}
			}
		}
		return children;
	}
	
}
