/*
    Copyright © 2013 Esdras Bispo Jr. and Renata Wassermann
   
    This file is part of BWEvaluator.

    BWEvaluator is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BWEvaluator is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BWEvaluator.  If not, see <http://www.gnu.org/licenses/>.
 */

package br.usp.ime.liamf.BWEvaluator.factory;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;

import br.usp.ime.liamf.BWEvaluator.eval.Ontology;

public class QueryFactory {
	
	private OntModel ontoA;
	private OntModel ontoB;
	private Set<CorrespondenceTriple> correspondences;
	
	//CONSTRUCTORS
	public QueryFactory(){}
	public QueryFactory(OntModel ontoA, OntModel ontoB, Alignment align) throws AlignmentException{
		
		if(ontoA == null || ontoB == null){
			throw new IllegalArgumentException("Invalid parameter! OntModel was not instantied.");
		}
		
		String ontoAname = ontoA.getNsPrefixURI("");
		String ontoBname = ontoB.getNsPrefixURI("");
		String alignOnto1 = align.getOntology1URI().toString() + "#";
		String alignOnto2 = align.getOntology2URI().toString() + "#";
		
		if(ontoAname.equals(alignOnto1) && ontoBname.equals(alignOnto2)){
			this.setOntoA(ontoA);
			this.setOntoB(ontoB);
			this.setCorrespondences(align);
		}else{
			throw new IllegalArgumentException("Mismatching in ontology URIs! It is necessary two ontologies are referenced into alignment.");
		}			
				
	}
	public QueryFactory(Ontology ontoA, Ontology ontoB, Alignment align) throws AlignmentException{
		
		if(ontoA == null || ontoB == null){
			throw new IllegalArgumentException("Invalid parameter! OntModel was not instantied.");
		}
		
		String ontoAname = ontoA.getURI();
		String ontoBname = ontoB.getURI();
		String alignOnto1 = align.getOntology1URI().toString() + "#";
		String alignOnto2 = align.getOntology2URI().toString() + "#";
		
		if(ontoAname.equals(alignOnto1) && ontoBname.equals(alignOnto2)){
			this.setOntoA(ontoA.getOnto());
			this.setOntoB(ontoB.getOnto());
			this.setCorrespondences(align);
		}else{
			throw new IllegalArgumentException("Mismatching in ontology URIs! It is necessary two ontologies are referenced into alignment.");
		}			
				
	}
	
	//GETS
	public OntModel getOntoA(){
		return this.ontoA;
	}
	public OntModel getOntoB(){
		return this.ontoB;
	}
	public Set<CorrespondenceTriple> getCorrespondences(){
		return this.correspondences;
	}
	
	//SETS
	public void setOntoA(OntModel ontoA){
		this.ontoA = ontoA;
	}
	public void setOntoB(OntModel ontoB){
		this.ontoB = ontoB;
	}
	public void setCorrespondences(Alignment align){
		Enumeration<Cell> refMap = align.getElements();
        
        Set<CorrespondenceTriple> map = new HashSet<CorrespondenceTriple>();
        
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
	
	//OTHER METHODS
	public Set<String> generateQueriesFrom(String locate){
		
		OntModel queryOnto = null;
        String fullEntityName = "";
        Set<String> queries = new HashSet<String>();
        
		for(CorrespondenceTriple tr : this.correspondences){
			if( locate.equals("source") ){
				queryOnto = this.ontoA;
				fullEntityName = tr.getA();
			}else if( locate.equals("target") ){
				queryOnto = this.ontoB;
				fullEntityName = tr.getB();
			}else{
				throw new IllegalArgumentException("Invalid parameter! Use 'source' or 'target' only.");
			}
			
			//Extract the short name of entity A
			String broken[] = tr.getA().split("#|/");
			int pos = broken.length - 1;
			String entityA = broken[pos]; 
			
			//Extract the relation between entities
			String relation = tr.getR();
			
			//Extract the short name of entity B					
			broken = tr.getB().split("#|/");
			pos = broken.length - 1;
			String entityB = broken[pos];
			
			//Extract the short name of entity chosen
			broken = fullEntityName.split("#|/");
			pos = broken.length - 1;
			String shortEntityName = broken[pos];
			
			//Extract the base name of entity chosen
			broken = fullEntityName.split("#|/");
			pos = broken.length - 1;					
			int lastSize = broken[pos].length();
			int fullSize = fullEntityName.length();
			String queryOntoBasename = fullEntityName.substring(0, fullSize-lastSize);
				
			OntClass c = queryOnto.getOntClass(fullEntityName);
			
			if(c != null){
				//Create the query from extracted data
                String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
                query += "prefix onto: <" + queryOntoBasename + ">\n";
                query += "\n";
                query += "# List all individuals of " + shortEntityName + " class\n";
                query += "# "+ entityA + " " + relation + " " + entityB + "\n";
                query += "\n";
                query += "select ?name\n";
                query += "where { ?name rdf:type onto:" + shortEntityName + " }\n";
                
                queries.add(query);
                
			}else{
				
				OntProperty p = queryOnto.getOntProperty(fullEntityName);
				
				if(p != null){
					//Create the query from extracted data
					String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
			        query += "prefix onto: <" + queryOntoBasename + ">\n";
			        query += "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
			        query += "\n";
			        query += "# List all individuals of " + shortEntityName + " domain\n";
			        query += "# "+ entityA + " " + relation + " " + entityB + "\n";
			        query += "\n";
			        query += "select ?name\n";
			        query += "where { onto:" + shortEntityName + " rdfs:domain ?obj.\n";
			        query += "\t\t ?name rdf:type ?obj}";
                    
                    queries.add(query);
                    
                    query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
			        query += "prefix onto: <" + queryOntoBasename + ">\n";
			        query += "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
			        query += "\n";
			        query += "# List all individuals of " + shortEntityName + " range\n";
			        query += "# "+ entityA + " " + relation + " " + entityB + "\n";
			        query += "\n";
			        query += "select ?name\n";
			        query += "where { onto:" + shortEntityName + " rdfs:range ?obj.\n";
			        query += "\t\t ?name rdf:type ?obj}";
                    
                    queries.add(query);
				}
			}
		}
		return queries;
	}
}
