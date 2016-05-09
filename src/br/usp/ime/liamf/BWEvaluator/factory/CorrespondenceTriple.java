package br.usp.ime.liamf.BWEvaluator.factory;

public class CorrespondenceTriple {
	
	private String a;
	private String b;
	private String r;
	
	public CorrespondenceTriple(String a, String r, String b){
		this.a = a;
		this.r = r;
		this.b = b;
	}
	
	//GETS
	public String getA(){
		return this.a;
	}
	public String getB(){
		return this.b;
	}
	public String getR(){
		return this.r;
	}
	public String get(String field){
		if(field.equals("a"))
			return this.a;
		if(field.equals("r"))
			return this.r;
		if(field.equals("b"))
			return this.b;
		throw new IllegalArgumentException("Invalid parameter! This field does not exist.");
	}
	
	//SETS
	public void setA(String a){
		this.a =  a;
	}
	public void setB(String b){
		this.b =  b;
	}
	public void setR(String r){
		this.r = r;
	}
	public void set(String field, String value){
		if(field.equals("a"))
			this.a = value;
		else if(field.equals("r"))
			this.r = value;
		else if(field.equals("b"))
			this.b = value;
		else
			throw new IllegalArgumentException("Invalid parameter! This field does not exist.");
	}
}
