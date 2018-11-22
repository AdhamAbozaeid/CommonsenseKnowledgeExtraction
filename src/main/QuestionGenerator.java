package main;

import java.util.StringTokenizer;

import methods.SentenceParser;
import module.graph.helper.GraphPassingNode;

public class QuestionGenerator {
	
	private enum relation {
		NONE,
		AGENT,
		RECEIPENT,
	};
	
	protected String Y1;
	private String X;
	private String sentence;
	private String question;
	private String verb1;
	private String verb2;
	private int x1_index;
	private int x2_index;
	private int y1_index;
	private int y2_index;
	private int connective_index;
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
			try {
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

			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
				continue;
			}
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
			qg.connective_index = 5;
			qg.verb1_index = 2;
			qg.verb2_index = 7;			
			qg.x1_relation = relation.RECEIPENT;
			qg.x2_relation = relation.AGENT;
			qg.y_relation = relation.AGENT;
			qg.generateQuestions();
	}

	private void generateSentence() throws Exception {
		int i;
		String[] tokens = sentence.split(" ");
		tokens[x1_index] = "Tom";
		if(x2_relation == relation.AGENT)
			tokens[x2_index] = "he";
		else if (x2_relation == relation.RECEIPENT)
			tokens[x2_index] = "him";
		else
			throw new Exception("Unknown X relation");
		
		if(y_relation != relation.NONE)
			tokens[y1_index] = "John";
		
		sentence = "";
		for(i=0; i<tokens.length; i++)
			sentence += " "+tokens[i];
		// Build question
		if(x2_relation == relation.AGENT) {
			question = "who";
			for(i=verb2_index; i<tokens.length; i++) {
				question += " "+tokens[i];
			}
			question += "?";
		}
	}

	private boolean isPerson(String entity, GraphPassingNode gpn) {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void extractSemanticRelation() {
		// TODO Auto-generated method stub
		
	}

	private void extractKnowledge() {
		// TODO Auto-generated method stub
		
	}

	private void retrieveKonwledgeRecords() {
		// TODO Auto-generated method stub
		
	}
}
