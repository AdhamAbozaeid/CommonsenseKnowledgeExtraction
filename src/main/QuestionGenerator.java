package main;

import methods.SentenceParser;
import module.graph.helper.GraphPassingNode;

public class QuestionGenerator {
	
	private enum relation {
		NONE(0),
		AGENT,
		RECEIPENT,
	};
	
	protected String Y1;
	private String X;
	private String sentence;
	private String verb1;
	private String verb2;
	private int x1_index;
	private int x2_index;
	private int y1_index;
	private int y2_index;
	private int verb1_index;
	private int verb2_index;
	private relation x1_relation;
	private relation x2_relation;
	private relation y_relation;
	private GraphPassingNode gpn;
	
	public void generateQuestions() {
		int i ,num_records=1;

		retrieveKonwledgeRecords();
		//foreach record in records{
		for(i=0; i<num_records; i++) {
			SentenceParser sp = SentenceParser.getInstance();
			
			// Extract verb1, verb2, x1_relation, x2_relation
			extractKnowledge(); 
			
			// Run parser on example sentence,
			gpn = sp.parse(sentence);
			// get x1_index, x2_index, y1, y1_index (agent or recp according to x1_relation)
			// Needs semantic_graph, verb1, verb2, x1_relation, x2_relation
			extractSemanticRelation();
			
			if(!isPerson(X, gpn))
				continue;
			if(!isPerson(Y1, gpn))
				y1_index = -1;
			
			//Needs x1_index, x2_index, x1_relation, x2_relation, y1_index, y1_relation
			//Adham
			generateSentence(); //Generate Sentence and Question. 
		}
	}
	
	public static void main(String[] args) {
			QuestionGenerator qg = new QuestionGenerator();
			qg.sentence = "Mike was arrested by Paul becuase Mike killed Jan";
			qg.verb1 = "arrest";
			qg.verb2 = "kill";
			qg.Y1 = "Paul";
			qg.X = "Mike";
			qg.x1_index = 0;
			qg.x2_index = 6;
			qg.y1_index = 4;
			qg.verb1_index = 2;
			qg.verb2_index = 7;			
			qg.x1_relation = relation.RECEIPENT;
			qg.x2_relation = relation.AGENT;
			qg.y_relation = relation.AGENT;
			qg.generateQuestions();
	}

	private static void generateSentence() {
		// TODO Auto-generated method stub
		
	}
	private static boolean isPerson(String entity, GraphPassingNode gpn) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private static void extractSemanticRelation() {
		// TODO Auto-generated method stub
		
	}

	private static void extractKnowledge() {
		// TODO Auto-generated method stub
		
	}

	private static void retrieveKonwledgeRecords() {
		// TODO Auto-generated method stub
		
	}
}
