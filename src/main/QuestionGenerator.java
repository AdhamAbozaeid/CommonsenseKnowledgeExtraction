package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import methods.SentenceParser;
import module.graph.helper.GraphPassingNode;

public class QuestionGenerator {
	
	private enum relation {
		NONE,
		AGENT,
		RECIPIENT,
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
	
	public boolean processKnowledge() {
		try {
			SentenceParser sp = SentenceParser.getInstance();
			
			// Extract verb1, verb2, x1_relation, x2_relation
			extractKnowledge(); 
			
			// Run parser on example sentence,
			gpn = sp.parse(sentence);
			// get x1_index, x2_index, y1, y1_index (agent or recp according to x1_relation)
			// Needs semantic_graph, verb1, verb2, x1_relation, x2_relation
			extractSemanticRelation();
			
			if (!isPerson(X, gpn, x1_index))
				return false;
			if (!isPerson(Y1, gpn, y1_index))
				y1_index = -1;
			
			//Needs x1_index, x2_index, x1_relation, x2_relation, y1_index, y1_relation
			//Adham
			generateSentence(); //Generate Sentence and Question. 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
			QuestionGenerator qg = new QuestionGenerator();
			String[] records = {""};
			// Example 1
			qg.sentence = "Mike was arrested by Paul because Mike killed Jan";
			qg.verb1 = "arrest";
			qg.verb2 = "kill";
			qg.Y1 = "paul";
			qg.X = "mike";
			qg.x1_index = 0;
			qg.x2_index = 6;
			qg.y1_index = 4;
			qg.connective_index = 5;
			qg.verb1_index = 2;
			qg.verb2_index = 7;			
			qg.x1_relation = relation.RECIPIENT;
			qg.x2_relation = relation.AGENT;
			qg.y_relation = relation.AGENT;
			if(qg.processKnowledge()) {
				System.out.println("* "+ qg.sentence);
				System.out.println("Q: "+ qg.question);
			}
			
			// Example 2
			qg.sentence = "Mike was arrested by Paul because Mike was caught by Jan";
			qg.verb1 = "arrest";
			qg.verb2 = "catch";
			qg.Y1 = "paul";
			qg.X = "mike";
			qg.x1_index = 0;
			qg.x2_index = 6;
			qg.y1_index = 4;
			qg.connective_index = 5;
			qg.verb1_index = 2;
			qg.verb2_index = 8;			
			qg.x1_relation = relation.RECIPIENT;
			qg.x2_relation = relation.RECIPIENT;
			qg.y_relation = relation.AGENT;
			if(qg.processKnowledge()) {
				System.out.println("* "+ qg.sentence);
				System.out.println("Q: "+ qg.question);
			}
			
			// Example 3
			qg.sentence = "Jon needs to think about that some more because Jon usually likes to tweak them before sending";
			qg.verb1 = "think";
			qg.verb2 = "tweak";
			qg.Y1 = "";
			qg.X = "jon";
			qg.x1_index = 0;
			qg.x2_index = 9;
			qg.y1_index = -1;
			qg.connective_index = 8;
			qg.verb1_index = 3;
			qg.verb2_index = 13;			
			qg.x1_relation = relation.AGENT;
			qg.x2_relation = relation.AGENT;
			qg.y_relation = relation.NONE;
			
			qg.retrieveKonwledgeRecords();
			//foreach record in records{
			for (String record : records) {
				if(qg.processKnowledge()) {
					System.out.println("* "+ qg.sentence);
					System.out.println("Q: "+ qg.question);
				}
			}
	}

	private void generateSentence() throws Exception {
		int i;
		String[] tokens = sentence.split(" ");
		tokens[x1_index] = "Tom";
		tokens[x2_index] = "he";
		
		if(y_relation != relation.NONE && y1_index != -1)
			tokens[y1_index] = "John";
		
		sentence = "";
		for(i=0; i<tokens.length; i++)
			sentence += " "+tokens[i];
		// Build question
		if(x2_relation == relation.AGENT) {
			question = "who";
		} else if(x2_relation == relation.RECIPIENT) {
			question = "who was";
		} else {
			throw new Exception("Unknown X relation");
		}
		for(i=verb2_index; i<tokens.length; i++) {
			question += " "+tokens[i];
		}
		question += "?";
	}

	private boolean isPerson(String entity, GraphPassingNode gpn, int index) {
		HashMap<String,ArrayList<String>> wordSenseMap = gpn.getWordSenseMap();
		ArrayList<String> valueList;
		index +=1;
//		for(String s : gpn.getAspGraph()){
//			System.out.println(s);
//		}
		valueList = wordSenseMap.get(entity+"_"+index);
		if(valueList == null)
			return false;
		if(valueList.get(1) == null)
			return false;
		return valueList.get(1).contains("person");
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
