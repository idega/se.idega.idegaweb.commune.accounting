package se.idega.idegaweb.commune.accounting.regulations.data;

import se.idega.idegaweb.commune.accounting.invoice.data.RegularPaymentEntry;

/**
 * Object to be returned by the Regulation as a responce to a query for a specific 
 * row/instance in the regulation framework
 * 
 * @author Joakim
 */
public class PostingDetail {
	private String term;
	private float amount;
	private float vat=0;
	private int vatRuleRegulationID=-1;
	private VATRegulation vatRegulation;
	private String ruleSpecType;
	private int orderID;
	private float vatAmount=0;
	
	public PostingDetail(){
	}

	public PostingDetail(RegularPaymentEntry regularPaymentEntry){
		setAmount(regularPaymentEntry.getAmount());
		if(regularPaymentEntry.getRegSpecType()!=null){
			setRuleSpecType(regularPaymentEntry.getRegSpecType().getRegSpecType());
		}
		setTerm(regularPaymentEntry.getPlacing());
		//setVATPercent(regularPaymentEntry.getVATAmount());
		setVATAmount(regularPaymentEntry.getVATAmount());
		int vatRuleRegulationId= regularPaymentEntry.getVatRuleRegulationId();
		if(vatRuleRegulationId!=-1){
			setVatRuleRegulationId(vatRuleRegulationId);
			
		}
		setOrderID(999);
	}
	/* This constructor is not used anywhere
	 * 
	public PostingDetail(String t, float amount, float vatPercent, int vatRuleRegulationID, String rst){
		term = t;
		this.amount = amount;
		setVATPercent(vatPercent);
		this.vatRuleRegulationID = vatRuleRegulationID;
		ruleSpecType = rst;
		setVATAmount(getVATPercentage()*getAmount());
	}
	*/
	
	public float getAmount() {
		return this.amount;
	}

	public String getTerm() {
		return this.term;
	}

	public void setAmount(float i) {
		this.amount = i;
	}

	public void setTerm(String string) {
		this.term = string;
	}

	public float getVATPercent() {
		return this.vat;
	}

	public void setVATPercent(float f) {
		this.vat = f;
	}

	public int getVatRuleRegulationId() {
		return this.vatRuleRegulationID;
	}

	public void setVatRuleRegulationId(int i) {
		this.vatRuleRegulationID = i;
	}

	public String getRuleSpecType() {
		return this.ruleSpecType;
	}

	public void setRuleSpecType(String i) {
		this.ruleSpecType = i;
	}

	public int getOrderID() {
		return this.orderID;
	}

	public void setOrderID(int i) {
		this.orderID = i;
	}

	/**
	 * @return Returns the vatRegulation.
	 */
	public VATRegulation getVATRegulation() {
		return this.vatRegulation;
	}

	/**
	 * @param vatRegulation The vatRegulation to set.
	 */
	public void setVATRegulation(VATRegulation vatRegulation) {
		this.vatRegulation = vatRegulation;
	}
	
	public float getVATPercentage(){
		return getVATPercent()/100;
	}
	
	public float getVATAmount(){
		return this.vatAmount;
	}
	
	public void setVATAmount(float VATAmount){
		this.vatAmount=VATAmount;
	}

}
