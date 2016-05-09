package br.usp.ime.liamf.BWEvaluator.eval;

import java.io.File;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class BasicAlgorithm {
	
	private Alignment refAlign;
	private Alignment propAlign;
	private Measure measure;
	private Ontology ontoA;
	private Ontology ontoB;
	
	//CONSTRUCTORS	
	public BasicAlgorithm(){}
	public BasicAlgorithm(String ontoAPath, String ontoBPath, String refAlignPath, String propAlignPath) throws AlignmentException{
		this.ontoA = new Ontology();
		this.ontoA.setBaseOnto(ontoAPath);
		this.ontoA.setOnto();
		
		this.ontoB = new Ontology();
		this.ontoB.setBaseOnto(ontoBPath);
		this.ontoB.setOnto();
		
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment refAlign = aparser.parse( new File( refAlignPath ).toURI() );
		this.refAlign = refAlign;
		
		aparser = new AlignmentParser(0);
		Alignment propAlign = aparser.parse( new File( propAlignPath ).toURI() );
		this.propAlign = propAlign;
		
		this.measure = new Measure();
	}
	public BasicAlgorithm(String ontoAPath, String ontoBPath, String propAlignPath) throws AlignmentException{
		this.ontoA = new Ontology();
		this.ontoA.setBaseOnto(ontoAPath);
		this.ontoA.setOnto();
		
		this.ontoB = new Ontology();
		this.ontoB.setBaseOnto(ontoBPath);
		this.ontoB.setOnto();
		
		AlignmentParser aparser = new AlignmentParser(0);
		Alignment propAlign = aparser.parse( new File( propAlignPath ).toURI() );
		this.propAlign = propAlign;
		
		this.measure = new Measure();
	}
	
	//GETS
	public Alignment getRefAlign(){
		return this.refAlign;
	}
	public Alignment getPropAlign(){
		return this.propAlign;
	}
	public Measure getMeasure(){
		return this.measure;
	}
	public Ontology getOntoA(){
		return this.ontoA;
	}
	public Ontology getOntoB(){
		return this.ontoB;
	}
	
	//SETS
	public void setRefAlign(Alignment refAlign){
		this.refAlign = refAlign;
	}
	public void setPropAlign(Alignment propAlign){
		this.propAlign = propAlign;
	}
	public void setMeasure(Measure measure){
		this.measure = measure;
	}
	public void setOntoA(Ontology ontoA){
		this.ontoA = ontoA;
	}
	public void setOntoB(Ontology ontoB){
		this.ontoB = ontoB;
	}
		
}
