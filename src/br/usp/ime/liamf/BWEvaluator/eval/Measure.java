package br.usp.ime.liamf.BWEvaluator.eval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

public class Measure {
	
	//ATTRIBUTES
    private int proposedEntities = 0;
    private int referenceEntities = 0;
    private float truePositiveEntities = 0.0f;
    
    //GETs and SETs
    public float getPrecision(){
        if(this.proposedEntities != 0)
            return this.truePositiveEntities / this.proposedEntities;
        return 0;
    }
    public float getRecall(){
        if(this.referenceEntities != 0)
            return this.truePositiveEntities / this.referenceEntities;
        return 0;
    }
    public Set<Map<String, RDFNode>> getListFrom(ResultSet rs){
    	
    	Set<Map<String, RDFNode>> list = new HashSet<Map<String, RDFNode>>();
        
    	for ( ; rs.hasNext() ; ){
            QuerySolution soln = rs.next() ;
            
            List<String> projections = rs.getResultVars();
            
            Map<String, RDFNode> tuple = new HashMap<String, RDFNode>();
            for( String proj : projections ){
            	RDFNode node = soln.get(proj);
            	tuple.put(proj, node);
            }
            list.add(tuple);
        }
    	
    	return list;
    }
    
    //OTHER METHODS
    public void updateMeasures(ResultSet propAnswerRS, ResultSet refAnswerRS){
    	
    	Set<Map<String, RDFNode>> propAnswerList = getListFrom(propAnswerRS);
    	Set<Map<String, RDFNode>> refAnswerList = getListFrom(refAnswerRS);
    	
        int intersect = 0;
        
        for( Map<String, RDFNode> refAnswer : refAnswerList){
            for ( Map<String, RDFNode> propAnswer : propAnswerList){
              if( compare(refAnswer, propAnswer) ){
                  intersect++;
                  break;
              }
            }
        }

        this.truePositiveEntities += intersect;
        this.proposedEntities += propAnswerList.size();
        this.referenceEntities += refAnswerList.size();
    }
    public boolean compare(Map<String, RDFNode> a, Map<String, RDFNode> b){
    	
    	if( a.keySet().size() == b.keySet().size() ){
	    	for( String aKey : a.keySet() ){
	    		if( b.containsKey(aKey) ){
	    			if( isDifferent( a.get(aKey), b.get(aKey) ) ){
	    				return false;
	    			}
	    		}else{
	    			return false;
	    		}
	    	}
    	}else{
    		return false;
    	}
    	
    	return true;
    }    
    public boolean isDifferent(RDFNode a, RDFNode b){
    	
    	String labelA = "";
    	String brokenA[] = a.toString().split("#",2);
    	if(brokenA.length > 1)
    		labelA = brokenA[1];
    	else
    		labelA = brokenA[0];
    	
    	String labelB = "";
    	String brokenB[] = b.toString().split("#",2);
    	if(brokenB.length > 1)
    		labelB = brokenB[1];
    	else
    		labelB = brokenB[0];
    	
    	if( labelA.equals(labelB) )
    		return false;
    	
    	return true;
    }
    
}
