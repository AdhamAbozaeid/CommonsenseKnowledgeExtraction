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
	
	private String Y1;
	private String Y2;
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
	private String x1_relation;
	private String x2_relation;
	private String y1_relation;
	private String y2_relation;
	private GraphPassingNode gpn;
	private String[] split;
	public boolean processKnowledge() {
		try {
			SentenceParser sp = SentenceParser.getInstance();
			
			// Extract verb1, verb2, x1_relation, x2_relation
			extractKnowledge(gpn); 
			
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
			qg.x1_relation = "recipient";//relation.RECIPIENT;
			qg.x2_relation = "agent";//relation.AGENT;
			qg.y1_relation = "agent";//relation.AGENT;
			qg.y2_relation = "agent";//relation.AGENT;
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
			qg.x1_relation = "recipient";//relation.RECIPIENT;
			qg.x2_relation = "agent";//relation.AGENT;
			qg.y1_relation = "agent";//relation.AGENT;
			qg.y2_relation = "agent";//relation.AGENT;
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
			qg.x1_relation = "recipient";//relation.RECIPIENT;
			qg.x2_relation = "recipient";//relation.RECIPIENT;
			qg.y1_relation = "agent";//relation.AGENT;
			qg.y2_relation = "agent";//relation.AGENT;
			
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
		
		if(y_relation != null && y1_index != -1)
			tokens[y1_index] = "John";
		
		sentence = "";
		for(i=0; i<tokens.length; i++)
			sentence += " "+tokens[i];
		// Build question
		if(x2_relation.equals("agent")) {
			question = "who";
		} else if(x2_relation.equals("recipient")) {
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
	
	private void extractSemanticRelation(GraphPassingNode gpn) {
		// TODO Auto-generated method stub
		for(String s : gpn.getAspGraph()){
			//String split[] = s.split(",");
			if(s.contains(verb1) && s.contains(x1_relation)) {
				split = s.split("\\D+");
				x1_index = Integer.parseInt(split[split.length-1])-1;
				//System.out.println(X);
				//System.out.println(x1_index);
			}
			else if(s.contains(verb2) && s.contains(x2_relation)) {
				split = s.split("\\D+");
				x2_index = Integer.parseInt(split[split.length-1])-1;
				//System.out.println(X);
				//System.out.println(x2_index);
			}
			else if(s.contains(verb1) && s.contains(y1_relation)) {
				split = s.split("\\D+");
				y1_index = Integer.parseInt(split[split.length-1])-1;
				split = s.split(",");
				split = split[split.length-1].split("-");
				Y1 = split[0];
				//System.out.println(Y1);
				//System.out.println(y1_index);
			}
			else if(s.contains(verb2) && s.contains(y2_relation)) {
				split = s.split("\\D+");
				y2_index = Integer.parseInt(split[split.length-1])-1;
				split = s.split(",");
				split = split[split.length-1].split("-");
				Y2 = split[0];
				//System.out.println(Y2);
				//System.out.println(y2_index);

			}

		}
	}

	private void extractKnowledge() {
		// TODO Auto-generated method stub
		
	}

	private void retrieveKonwledgeRecords() {
		// TODO Auto-generated method stub
		
	}
}
