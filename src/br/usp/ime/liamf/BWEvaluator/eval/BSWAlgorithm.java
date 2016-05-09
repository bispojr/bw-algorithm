package br.usp.ime.liamf.BWEvaluator.eval;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;

import org.apache.jena.query.ResultSet;

public class BSWAlgorithm extends BasicAlgorithm{
	 
	//CONSTRUCTORS
	public BSWAlgorithm(String ontoAPath, String ontoBPath, String refAlignPath, String propAlignPath) throws AlignmentException{
		super(ontoAPath, ontoBPath, refAlignPath, propAlignPath);
	}
	
	//OTHER METHODS
	public void evaluateTo(String local, Set<Map<String,String>> queries) throws AlignmentException{
		if(local.equals("target")){
			evaluationBox("target", queries);
		}
		else if(local.equals("source")){
			evaluationBox("source", queries);
		}
		else if(local.equals("bidirectional")){
			throw new IllegalArgumentException("Few parameters! 'biredictional' local requires two query sets.");
		}
		else{
			throw new IllegalArgumentException("Invalid parameter! This local does not exist.");
		}
	}
	public void evaluate(String local, Set<Map<String,String>> queriesA, Set<Map<String,String>> queriesB) throws AlignmentException{
		if(local.equals("bidirectional")){
			evaluationBox("target", queriesA);
			evaluationBox("source", queriesB);			
		}else if(local.equals("toSource")){
			throw new IllegalArgumentException("Too many parameters! 'toSource' local requires only one query set.");
		}
		else if(local.equals("toTarget")){
			throw new IllegalArgumentException("Too many parameters! 'toTarget' local requires only one query set.");
		}
		else{
			throw new IllegalArgumentException("Invalid parameter! This local does not exist.");
		}
	}
	public void evaluationBox(String kind, Set<Map<String,String>> queries) throws AlignmentException {
		
		QueryRewriting queryRewriting = new QueryRewriting( getOntoA(), getOntoB(), getPropAlign() );
        
        for(Map<String, String> queryPair: queries){
        	
        	String inputQuery = queryPair.get("input");
        	String referenceQuery = queryPair.get("reference");
        	
        	if(kind.equals("target")){
	            String transQuery = queryRewriting.rewriteQueryTo(inputQuery, "target");
	            ResultSet propAnswer = getOntoB().ask(transQuery);
	            ResultSet refAnswer = getOntoB().ask(referenceQuery);
	            getMeasure().updateMeasures(propAnswer, refAnswer);
        	}
        	
        	if(kind.equals("source")){
        		String transQuery = queryRewriting.rewriteQueryTo(inputQuery, "source");
	            ResultSet propAnswer = getOntoA().ask(transQuery);
	            ResultSet refAnswer = getOntoA().ask(referenceQuery);
	            getMeasure().updateMeasures(propAnswer, refAnswer);
        	}
        }
	}
}
