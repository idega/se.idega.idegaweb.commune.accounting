/*
 * $Id: BatchDeadlinePlacementHelper.java,v 1.4 2006/04/09 11:53:32 laddi Exp $
 * Created on 26.11.2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package se.idega.idegaweb.commune.accounting.business;

import java.util.Date;

import javax.ejb.FinderException;

import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWResourceMessage;
import com.idega.util.IWCalendar;
import com.idega.util.IWTimestamp;
import com.idega.util.TimePeriod;

import se.idega.idegaweb.commune.accounting.invoice.data.BatchDeadline;
import se.idega.idegaweb.commune.accounting.invoice.data.BatchDeadlineHome;
import se.idega.idegaweb.commune.care.business.DefaultPlacementHelper;
import se.idega.idegaweb.commune.care.business.PlacementHelper;

/**
 * 
 *  Last modified: $Date: 2006/04/09 11:53:32 $ by $Author: laddi $
 * 
 * @author <a href="mailto:aron@idega.com">aron</a>
 * @version $Revision: 1.4 $
 */
public class BatchDeadlinePlacementHelper extends DefaultPlacementHelper implements PlacementHelper {
    
    private TimePeriod validPeriod = null;
    private BatchDeadline deadline = null;
    
    public Date getEarliestPlacementDate() {
      IWTimestamp stamp = new IWTimestamp(super.getEarliestPlacementDate()); 
			TimePeriod period = getValidPeriod();
       if(period!=null){
				 IWTimestamp firstTimestamp = period.getFirstTimestamp();
				 if (stamp.isEarlierThan(firstTimestamp)) {
					 return firstTimestamp.getDate();
				 }
       }
       return stamp.getDate();
    }
    public IWResourceMessage getEarliestPlacementMessage() {
        // TODO Auto-generated method stub
        return super.getEarliestPlacementMessage();
    }
    public Date getLatestPlacementDate() {
        TimePeriod period = getValidPeriod();
        if(period!=null){
            return period.getLastTimestamp().getDate();
        }
        return null;
    }
    public IWResourceMessage getLatestPlacementMessage() {
        return null;
    }
    public boolean hasEarliestPlacementDate() {
        TimePeriod period = getValidPeriod();
        if(period!=null){
            return period.getFirstTimestamp()!=null;
        }
        return false;
    }
    public boolean hasLatestPlacementDate() {
        TimePeriod period = getValidPeriod();
        if(period!=null){
            return period.getLastTimestamp()!=null;
        }
        return false;
    }
    
    public BatchDeadline getCurrentDeadline(){
        try {
            BatchDeadlineHome deadlineHome = (BatchDeadlineHome)IDOLookup.getHome(BatchDeadline.class);
            this.deadline = deadlineHome.findCurrent();
            return this.deadline;
        } catch (IDOLookupException e) {
           
        } catch (FinderException e) {
          
        }
        return null;
    }
    
    public TimePeriod getValidPeriod(){
        if(this.validPeriod==null){
	        BatchDeadline deadline = getCurrentDeadline();
	        if(deadline!=null){
	            IWCalendar cal = new IWCalendar();
	            int today = cal.getDay();
	            int deadlineDay = deadline.getDeadlineDay();
	            if(today<deadlineDay ){
	                this.validPeriod =  new TimePeriod(new IWTimestamp(1,cal.getMonth(),cal.getYear()),new IWTimestamp(deadlineDay,cal.getMonth(),cal.getYear()));
	            }
	            else{
	              IWTimestamp stamp = new IWTimestamp();
	              stamp.addMonths(1);
	              this.validPeriod =  new TimePeriod(new IWTimestamp(1,stamp.getMonth(),stamp.getYear()),new IWTimestamp(cal.getLengthOfMonth(stamp.getMonth(),stamp.getYear()),stamp.getMonth(),stamp.getYear()));
	                
	            }
	            
	        }
        }
        return this.validPeriod;
    }
    
    public boolean hasDeadlinePassed(){
        BatchDeadline deadline = getCurrentDeadline();
        IWTimestamp today = new IWTimestamp();
        if(deadline!=null && today.getDay()<=deadline.getDeadlineDay()) {
					return false;
				}
        return true;
    }
    
    
        
    
    public IWResourceMessage getMessageWhenDeadlinePassed() {
        return new IWResourceMessage("care.deadline_msg_for_passedby_date","Chosen period has been invoiced. Earliest possible date is the first day of next month.");
    }
}
