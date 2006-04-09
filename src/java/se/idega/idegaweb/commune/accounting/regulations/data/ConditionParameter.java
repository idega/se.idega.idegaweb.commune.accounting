package se.idega.idegaweb.commune.accounting.regulations.data;

/**
 * @author Joakim
 * Wrapper for the parameter pair that need to go in the Collection that is 
 * sent as an in parameter to RegulationBusinessBean whenever a 'condition' is used.
 */
public class ConditionParameter {
	private String condition;
	private Object interval;
	
	
	public ConditionParameter(){
	}
	
	public ConditionParameter(String c, Object i){
		this.condition = c;
		this.interval = i;
	}
	
	public String getCondition(){
		return this.condition;
	}
	
	public Object getInterval() {
		return this.interval;
	}

	public void setCondition(String string) {
		this.condition = string;
	}

	public void setInterval(Object object) {
		this.interval = object;
	}

	public String toString () {
		return "{" + this.condition + ", " + this.interval + "}";
	}
}
