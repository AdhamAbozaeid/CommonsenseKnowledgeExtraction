package main;

import methods.SentenceParser;
import module.graph.helper.GraphPassingNode;
import utils.DataBase;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import java.util.*;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionGenerator {
	// Need to declare the variables that Ananta is using in extractKnowledge below
	
	private String Y1, Y2, X, sentence, question, verb1, verb2, x1_relation, x2_relation, y1_relation, y2_relation;
	private int x1_index, x2_index, y1_index, y2_index, verb2_index;
	private boolean s1hasagent, s1hasrecp, s2hasagent, s2hasrecp;
	private GraphPassingNode gpn;
	private String[] split;
	private static SentenceParser spa = new SentenceParser();
	
	public boolean processKnowledge() {
		try {
			SentenceParser sp = SentenceParser.getInstance();

			// Extract verb1, verb2, x1_relation, x2_relation
			//extractKnowledge(); 	This method no longer exists as the work for it is done in retrieveKonwledgeRecords itself

			// Run parser on example sentence,
			gpn = sp.parse(sentence);
			// get x1_index, x2_index, y1, y1_index (agent or recp according to x1_relation)
			// Needs semantic_graph, verb1, verb2, x1_relation, x2_relation
			extractSemanticRelation(gpn);
			/*
			System.out.println(X);
			System.out.println(x1_index);
			System.out.println(X);
			System.out.println(x2_index);
			if(Y1 != "") {
				System.out.println(Y1);
				System.out.println(y1_index);
			}
			if(Y2 != "") {
				System.out.println(Y2);
				System.out.println(y2_index);	
			}
*/
			if (!isPerson(X, gpn, x1_index))
				return false;
			if (!isPerson(Y1, gpn, y1_index))
				y1_index = -1;

			//Needs x1_index, x2_index, x1_relation, x2_relation, y1_index, y1_relation
			modifySentence(); //Generate Sentence and Question. 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void modifySentence() throws Exception {
		int i;
		String[] tokens = sentence.split(" ");
		tokens[x1_index] = "Tom";
		tokens[x2_index] = "he";

		if(y1_relation != null && y1_index != -1)
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
			if(s.contains(verb1) && s.contains(x1_relation)) {
				split = s.split("\\D+");
				x1_index = Integer.parseInt(split[split.length-1])-1;
				//System.out.println(X);
				//System.out.println(x1_index);
			}
			else if(s.contains(verb2) && s.contains(x2_relation)) {
				split = s.split("\\D+");
				x2_index = Integer.parseInt(split[split.length-1])-1;
				verb2_index = Integer.parseInt(split[split.length-2])-1;
				//System.out.println(verb2_index);
			}
			else if(s.contains(verb1) && s.contains("agent") || s.contains(verb1) && s.contains("recipient")) {
				split = s.split(",");
				split = split[split.length-1].split("-");
				if(!split[0].equals(X)) {
					Y1 = split[0].toLowerCase();
					if(s.contains("agent")) {
						y1_relation = "agent";
					}
					else {
						y1_relation = "recipient";
					}
				}
				split = s.split("\\D+");
				y1_index = Integer.parseInt(split[split.length-1])-1;
				//System.out.println(Y1);
				//System.out.println(y1_index);
			}
			else if(s.contains(verb2) && s.contains("agent") || s.contains(verb2) && s.contains("recipient")) {
				split = s.split(",");
				split = split[split.length-1].split("-");
				if(!split[0].equals(X)) {
					Y2 = split[0].toLowerCase();
					if(s.contains("agent")) {
						y2_relation = "agent";
					}
					else {
						y2_relation = "recipient";
					}
				}
				split = s.split("\\D+");
				y2_index = Integer.parseInt(split[split.length-1])-1;
				//System.out.println(Y1);
				//System.out.println(y1_index);
			}

		}
		
	}
	
//	public void generateSentence(Boolean a1, Boolean r1, String v1, Boolean a2, Boolean r2, String v2, String text) {
//		GraphPassingNode gpn = spa.parsedSentence(text);
//		Map<String, GetDetails> map = new HashMap<>();
//		
//		for(String s : gpn.getAspGraph()){
//			System.out.println(s);
//			GetDetails temp = new GetDetails(s);
//			if(!map.containsKey(temp.head)) {
//				map.put(temp.head, temp);
//			} else {
//				GetDetails deep = map.get(temp.head);
//				deep.addTailRelation(temp);
//				map.put(temp.head, deep);
//			}
//		}
//		String startTail = map.get(v1).tail.get(map.get(v1).agentIndex);
//		int startTailIndex = map.get(v1).tailI.get(map.get(v1).agentIndex);
//		System.out.println(startTail + "-" + startTailIndex);
//		for(String str : map.get(v1).getTailsByIndex(startTailIndex)) {
//			System.out.println(str);
//		}
//
//	}

	public static void main(String[] args) throws JSONException {
		QuestionGenerator qg = new QuestionGenerator();
		
		qg.retrieveKonwledgeRecords();
		
//		String[] records = {""};
//		// Example 1
//		qg.sentence = "Mike was arrested by Paul because Mike killed Jan";
//		qg.verb1 = "arrest";
//		qg.verb2 = "kill";
//		//qg.Y1 = "paul";
//		qg.X = "mike";
//		//qg.x1_index = 0;
//		//qg.x2_index = 6;
//		//qg.y1_index = 4;
//		//qg.connective_index = 5;
//		//qg.verb1_index = 2;
//		//qg.verb2_index = 7;			
//		qg.x1_relation = "recipient";//relation.RECIPIENT;
//		qg.x2_relation = "agent";//relation.AGENT;
//		//qg.y1_relation = "agent";//relation.AGENT;
//		//qg.y2_relation = "agent";//relation.AGENT;
//		if(qg.processKnowledge()) {
//			System.out.println("* "+ qg.sentence);
//			System.out.println("Q: "+ qg.question);
//		}
//
//		// Example 2
//		qg.sentence = "Mike was arrested by Paul because Mike was caught by Jan";
//		qg.verb1 = "arrest";
//		qg.verb2 = "catch";
//		//qg.Y1 = "paul";
//		qg.X = "mike";
//		//qg.x1_index = 0;
//		//qg.x2_index = 6;
//		//qg.y1_index = 4;
//		//qg.connective_index = 5;
//		//qg.verb1_index = 2;
//		//qg.verb2_index = 8;			
//		qg.x1_relation = "recipient";//relation.RECIPIENT;
//		qg.x2_relation = "recipient";//relation.AGENT;
//		//qg.y1_relation = "agent";//relation.AGENT;
//		//qg.y2_relation = "agent";//relation.AGENT;
//		if(qg.processKnowledge()) {
//			System.out.println("* "+ qg.sentence);
//			System.out.println("Q: "+ qg.question);
//		}
//
//		// Example 3
//		qg.sentence = "Jon needs to think about that some more because Jon usually likes to tweak them before sending";
//		qg.verb1 = "think";
//		qg.verb2 = "tweak";
//		//qg.Y1 = null;
//		qg.X = "jon";
//		//qg.x1_index = 0;
//		//qg.x2_index = 9;
//		//qg.y1_index = -1;
//		//qg.connective_index = 8;
//		//qg.verb1_index = 3;
//		//qg.verb2_index = 13;			
//		qg.x1_relation = "recipient";
//		qg.x2_relation = "recipient";
//		//qg.y1_relation = "agent";
//		//qg.y2_relation = "agent";

		//foreach record in records{
//		for (String record : records) {
//			if(qg.processKnowledge()) {
//				System.out.println("* "+ qg.sentence);
//				System.out.println("Q: "+ qg.question);
//			}
//		}
	}

	private void retrieveKonwledgeRecords() throws JSONException {
		QuestionGenerator qg = new QuestionGenerator();
		
		int port_no = 27017;
		String host_name = "localhost", db_name = "knetdb", db_coll_name = "maintable";
		String client_url = "mongodb://" + host_name + ":" + port_no + "/" + db_name;
		MongoClientURI uri = new MongoClientURI(client_url);
		MongoClient mongo_client = new MongoClient(uri);
		MongoDatabase db = mongo_client.getDatabase(db_name);
		MongoCollection<Document> coll = db.getCollection(db_coll_name);
		
		int i = 0;
		FindIterable<Document> docs = qg.findKnowledgeInKB(coll);
		// For final code, remove everything with variable i (In short, remove the if blocks in below for loop)
		for (Document doc : docs) {
			if(i!=1) {
				JSONObject d = new JSONObject(doc.toJson());
				s1hasagent = d.getJSONObject("knowledge").has("agent");
				s1hasrecp = d.getJSONObject("knowledge").has("recipient");
				verb1 = d.getJSONObject("knowledge").getString("value");
				s2hasagent = d.getJSONObject("knowledge").getJSONObject("causes").has("agent");
				s2hasrecp = d.getJSONObject("knowledge").getJSONObject("causes").has("recipient");
				verb2 = d.getJSONObject("knowledge").getJSONObject("causes").getString("value");
				sentence = (String) d.getJSONArray("texts").get(0);
				if(s1hasagent == true) {
					x1_relation = "agent";
				}
				else if(s1hasrecp == true) {
					x1_relation = "recipient";
				}
				else if(s2hasagent == true) {
					x2_relation = "agent";
				}
				else if(s1hasrecp == true) {
					x2_relation = "recipient";
				}
				processKnowledge();
			}
			else {
				break;
			}
			i++;
		}

	}
	
	public FindIterable<Document> findKnowledgeInKB(MongoCollection<Document> col){
		FindIterable<Document> docs = col.find(Filters.eq("type", "action causes action"));
		return docs;
	}

	// Work for this method is done in extractKnowledge method. This can be removed.
//	private void retrieveKonwledgeRecords() {
//		// TODO Auto-generated method stub
//
//	}
}