package br.usp.ime.liamf.BWEvaluator.eval;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import br.usp.ime.liamf.BWEvaluator.factory.CorrespondenceTriple;

public class QueryRewriting {
	
	private Ontology ontoA;
	private Ontology ontoB;
	private List<CorrespondenceTriple> correspondences;
	private static Logger theLogger = Logger.getLogger(QueryRewriting.class.getName());
	
	//CONSTRUCTORS
	public QueryRewriting(){}
	public QueryRewriting(Ontology ontoA, Ontology ontoB, Alignment align) throws AlignmentException{
		
		if(ontoA == null || ontoB == null){
			throw new IllegalArgumentException("Invalid parameter! Ontology was not instantied.");
		}
		
		String ontoAname = ontoA.getURI();
		String ontoBname = ontoB.getURI();
		
		String alignOnto1 = " ";
		String alignOnto2 = " ";
		if( align.getOntology1URI() != null )
			alignOnto1 = align.getOntology1URI().toString();
		if( align.getOntology2URI() != null )
			alignOnto2 = align.getOntology2URI().toString();
		
		//Some cases, the URI don't have '#" as last character
		if( alignOnto1.charAt( alignOnto1.length() - 1) != '#')
			alignOnto1 += "#";
		if( alignOnto2.charAt( alignOnto2.length() - 1) != '#')
			alignOnto2 += "#";
		
		if( !(ontoAname.equals(alignOnto1) && ontoBname.equals(alignOnto2)) ){
			theLogger.fine(
					"Mismatching in ontology URIs! The two ontologies should be referenced into alignment." +
					"\n[Alignment Ontology 1]: " + alignOnto1 + 
					"\n[Expected]:" + ontoAname +
					"\n\n[Alignment Ontology 2]: " + alignOnto2 + 
					"\n[Expected]:" + ontoBname
					);
		}
		
		this.ontoA = ontoA;
		this.ontoB = ontoB;
		this.setCorrespondences(align);
	}
	
	//GETS
	public Ontology getOntoA(){
		return this.ontoA;
	}
	public Ontology getOntoB(){
		return this.ontoB;
	}
	public List<CorrespondenceTriple> getCorrespondences(){
		return this.correspondences;
	}
	
	//SETS
	public void setCorrespondences(Alignment align){
		Enumeration<Cell> refMap = align.getElements();
        
        List<CorrespondenceTriple> map = new ArrayList<CorrespondenceTriple>();
        
        //Put in this.correspondences the triple set
        //of reference alignment
        while(refMap.hasMoreElements()){
            Cell c = refMap.nextElement();
            
            String a = c.getObject1().toString();
            String b = c.getObject2().toString();
            String r = c.getRelation().toString();
            
            if(r.equals("fr.inrialpes.exmo.align.impl.rel.EquivRelation@50"))
                r = "=";
            
            CorrespondenceTriple t = new CorrespondenceTriple(a,r,b);
            map.add(t);
        }
        
        this.correspondences = map;
	}
	
	public String rewriteQueryTo(String query, String local){
		
		String sourceEntityValue = "";
		String targetEntityValue = "";
		
		if( local.equals("source") ){
			sourceEntityValue = "b";
			targetEntityValue = "a";
        }else if(  local.equals("target") ){
        	sourceEntityValue = "a";
			targetEntityValue = "b";
        }else{
			throw new IllegalArgumentException("Invalid parameter! Only 'source' and 'target' are valid parameters.");
		}
		
		Query originalQuery = QueryFactory.create(query);
		
		final Query translatedQuery = QueryFactory.make();
		if (originalQuery.isDistinct())
        	translatedQuery.setDistinct(true);
		translatedQuery.setQuerySelectType();
		translatedQuery.setPrefixMapping( originalQuery.getPrefixMapping() );
        translatedQuery.addProjectVars(originalQuery.getProjectVars());
        
        final String roleList[] = {"subject", "predicate", "object"};
        final List<CorrespondenceTriple> correspondences = this.correspondences;
        final String sourceEntity = sourceEntityValue;
        final String targetEntity = targetEntityValue;
        
        //Get mapping to translate
        final List<List<Map<String, String>>> mapping = new ArrayList<List<Map<String, String>>>();
		ElementWalker.walk(originalQuery.getQueryPattern(),
            new ElementVisitorBase() {
                public void visit(ElementPathBlock el) {
                    Iterator<TriplePath> triples = el.patternElts();
                    
                    while (triples.hasNext()) {
                    	
                    	Triple triple = triples.next().asTriple();
                    	
                    	for(String role : roleList){
                    		List<Map<String,String>> matches = new ArrayList<Map<String,String>>();
                    		for(CorrespondenceTriple correspondence : correspondences){
		                    	if( correspondence.get(sourceEntity).equals( getNode(triple, role) ) ){
		                    		Map<String, String> map = new HashMap<String,String>();
		                    		map.put(correspondence.get(sourceEntity), correspondence.get(targetEntity));
		                    		matches.add( map );
		                    	}
	                    	}
                    		if(matches.size() > 0){
                        		mapping.add(matches);
                    		}
                    	}
                    }
                }
            }
        );
		
		List<Map<String,String>> finalMapSet = new ArrayList<Map<String,String>>();
		for(List<Map<String, String>> mapset : mapping){
			if(finalMapSet.size() == 0){
				for(Map<String,String> map : mapset){
					Map<String, String> newMap = new HashMap<String,String>();
					for(String key : map.keySet()){
						newMap.put( key, map.get(key) );
					}
					finalMapSet.add(newMap);
				}
			}else{
				finalMapSet = mapProduct(finalMapSet, mapset);
			}
		}
		
		//Create all blocks of queries
		if(finalMapSet.size() > 1){
			ElementUnion union = new ElementUnion();
			for(final Map<String, String> map : finalMapSet){
				final ElementTriplesBlock block = new ElementTriplesBlock();
				ElementWalker.walk(originalQuery.getQueryPattern(),
		            new ElementVisitorBase() {
		                public void visit(ElementPathBlock el) {
		                    Iterator<TriplePath> triples = el.patternElts();
		                    
		                    while (triples.hasNext()) {
		                    	
		                    	Triple triple = triples.next().asTriple();
		                    	
		                    	for(String role : roleList){
		                    		if( map.containsKey( getNode(triple, role) )  ){
			                    		putNewPrefix( map.get( getNode(triple, role) ), translatedQuery );
			                    		triple = changeTranslatedTriple(
			                    					map.get( getNode(triple, role) ), 
			                    					triple, 
			                    					role
			                    				);		                    		
			                    	}
		                    	}
		                    	block.addTriple(triple);
		                    }
		                }
		            }
		        );
				union.addElement(block);
			}
			translatedQuery.setQueryPattern(union);
		}else{
			final ElementTriplesBlock block = new ElementTriplesBlock();
			
			Iterator<Map<String, String>> it = finalMapSet.iterator();
			final Map<String, String> map;
			if(it.hasNext())
				map = finalMapSet.iterator().next();
			else map = new HashMap<String,String>();
			
			ElementWalker.walk(originalQuery.getQueryPattern(),
	            new ElementVisitorBase() {
	                public void visit(ElementPathBlock el) {
	                    Iterator<TriplePath> triples = el.patternElts();
	                    
	                    while (triples.hasNext()) {
	                    	
	                    	Triple triple = triples.next().asTriple();
	                    	
	                    	for(String role : roleList){
	                    		if( map.containsKey( getNode(triple, role) )  ){
	                    			putNewPrefix( map.get( getNode(triple, role) ), translatedQuery );
		                    		triple = changeTranslatedTriple(
		                    					map.get( getNode(triple, role) ), 
		                    					triple, 
		                    					role
		                    				);		                    		
		                    	}
	                    	}
	                    	block.addTriple(triple);
	                    }
	                }
	            }
	        );
			translatedQuery.setQueryPattern(block);
		}
				
		return translatedQuery.toString();
	}
	private List<Map<String,String>> mapProduct(List<Map<String,String>> bag, List<Map<String,String>> mapset){
		
		List<Map<String,String>> newBag = new ArrayList<Map<String,String>>();
		for(Map<String,String> map : mapset){
			for(Map<String,String> bagmap : bag){
				Map<String,String> newmap = new HashMap<String,String>();
				for(String key : map.keySet()){
					newmap.put(key, map.get(key));
				}
				for(String key : bagmap.keySet()){
					newmap.put(key, bagmap.get(key));
				}
				newBag.add(newmap);
			}
		}
		return newBag;
		
	}
	private void putNewPrefix(String fullname, Query query){
		String uriBase = NodeFactory.createURI( fullname ).getNameSpace();
		
		Map<String, String> prefixMap = query.getPrefixMapping().getNsPrefixMap();
		for(String prefix : prefixMap.keySet()){
			//If it already exists a prefix, break
			if(prefixMap.get(prefix).equals( uriBase )) 
				return;
		}
		
		int i = 1;
		String prefix = "pref" + i;
		while( query.getPrefix(prefix) != null ){
			i++;
			prefix = "pref" + i;
		}
		query.setPrefix(prefix, uriBase);
	}
	private Triple changeTranslatedTriple(String resource, Triple triple, String role){
		if(role.equals("subject")){
			return 
	        		Triple.create(
	        			NodeFactory.createURI( resource ),
	        			triple.getPredicate(), 
	            		triple.getObject()
	                );
		}
		else if(role.equals("predicate")){
			return 
	        		Triple.create(
	        			triple.getSubject(),
	        			NodeFactory.createURI( resource ),
	            		triple.getObject()
	                );
		}
		else if(role.equals("object")){
			return 
	        		Triple.create(
	        			triple.getSubject(),
	        			triple.getPredicate(), 
	        			NodeFactory.createURI( resource )
	                );
		}
		else if(role.equals("none")){
			return 
	        		Triple.create(
	        			triple.getSubject(),
	        			triple.getPredicate(), 
	        			triple.getObject()
	                );
		} 
		throw new IllegalArgumentException("Invalid parameter! Only 'subject', 'predicate', 'object' and 'none' are valid parameters.");
	}
	private String getNode(Triple triple, String role){
		if(role.equals("subject"))
			return triple.getSubject().toString();
		if(role.equals("predicate"))
			return triple.getPredicate().toString();
		if(role.equals("object"))
			return triple.getObject().toString();
		throw new IllegalArgumentException("Invalid parameter! Only 'subject', 'predicate' and 'object' are valid parameters.");
	}
	
}
