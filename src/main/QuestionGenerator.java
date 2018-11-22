package methods;

import methods.SentenceParser;
import module.graph.helper.GraphPassingNode;

public class QuestionGenerator {
	
	private enum relation {
		NONE(0),
		AGENT,
		RECEIPENT,
	};
	
	private String Y1;
	private String X;
	private String sentence;
	private String verb1;
	private String verb2;
	private int x1_index;
	private int x2_index;
	private int y1_index;
	private int y2_index;
	private relation x1_relation;
	private relation x2_relation;
	private relation y_relation;
	private GraphPassingNode gpn;
	
	public void generateQuestions() {
		retrieveKonwledgeRecords();
		//foreach record in records{
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
			generateSentence(); //Generate Sentence and Question. 
		//}
	}
	
	public static void main(String[] args) {
			QuestionGenerator qg = new QuestionGenerator();
			qg.generateQuestions();
	}

	private static void generateSentence() {
		// TODO Auto-generated method stub
		
	}
	private static boolean isPerson(String entity, GraphPassingNode gpn) {
		// TODO Auto-generated method stub
		
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
