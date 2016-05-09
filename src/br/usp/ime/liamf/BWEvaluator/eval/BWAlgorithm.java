package br.usp.ime.liamf.BWEvaluator.eval;

import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;

import org.apache.jena.query.ResultSet;

public class BWAlgorithm extends BasicAlgorithm{	
	
	//CONSTRUCTORS
	public BWAlgorithm(){}
	public BWAlgorithm(String ontoAPath, String ontoBPath, String refAlignPath, String propAlignPath) throws AlignmentException{
		super(ontoAPath, ontoBPath, refAlignPath, propAlignPath);
	}
	public BWAlgorithm(String ontoAPath, String ontoBPath, String propAlignPath) throws AlignmentException{
		super(ontoAPath, ontoBPath, propAlignPath);
	}
	
	//OTHER METHODS
	public void evaluate(String local, Set<String> queries) throws AlignmentException{
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
	public void evaluate(String local, Set<String> queriesA, Set<String> queriesB) throws AlignmentException{
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
	public void evaluationBox(String kind, Set<String> queries) throws AlignmentException {
        QueryRewriting queryRewriting = new QueryRewriting( getOntoA(), getOntoB(), getPropAlign() );
        for(String query: queries){
        	if(kind.equals("target")){
	            String transQuery = queryRewriting.rewriteQueryTo(query, "target");
	            ResultSet transAnswer = getOntoB().ask(transQuery);
	            ResultSet propAnswer = translateInstances(transAnswer, invertAlignment(""));
	            ResultSet refAnswer = getOntoA().ask(query);
	            getMeasure().updateMeasures(propAnswer, refAnswer);
        	}
        	if(kind.equals("source")){
	            String transQuery = queryRewriting.rewriteQueryTo(query, "source");
	            ResultSet transAnswer = getOntoA().ask(transQuery);
	            ResultSet propAnswer = translateInstances(transAnswer, invertAlignment(""));
	            ResultSet refAnswer = getOntoB().ask(query);
	            getMeasure().updateMeasures(propAnswer, refAnswer);
        	}
        }
    }
	
	//TO BE IMPLEMENTED...
	public ResultSet translateInstances(ResultSet resp, String alignName){
        return resp;
    }
    public String invertAlignment(String alignName){
        return alignName;
    }
}
