package br.usp.ime.liamf.BWEvaluator.eval;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;

public class Ontology {
	
	private OntModel onto;
	private OntModel baseOnto;
	private OntModelSpec reasonerSpec = OntModelSpec.OWL_MEM_RDFS_INF;
	private OntModelSpec baseSpec = OntModelSpec.OWL_MEM;
	
	//GETS
	public OntModel getBaseOnto(){
		return this.baseOnto;
	}
	public OntModel getOnto(){
		return this.onto;
	}
	public String getURI(){
		return this.onto.getNsPrefixURI("");
	}
	
	//SETS
	public void setBaseOnto(String ontoPath){
		this.baseOnto = ModelFactory.createOntologyModel( this.baseSpec );
		this.baseOnto.read( ontoPath, "RDF/XML" );
	}
	public void setOnto(){
		this.onto = ModelFactory.createOntologyModel(this.reasonerSpec, this.baseOnto);
	}
	
	//OTHER METHODS
	public ResultSet ask(String query){
		Query queryFactory = QueryFactory.create(query);
		QueryExecution qe = QueryExecutionFactory.create(queryFactory, this.onto);		
		ResultSet results = qe.execSelect();
		
		return results;
	}
}
