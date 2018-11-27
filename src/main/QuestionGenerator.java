package main;

import methods.SentenceParser;
import module.graph.helper.ClassesResource;
import module.graph.helper.GraphPassingNode;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import java.util.*;

import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionGenerator {
	// Need to declare the variables that Ananta is using in extractKnowledge below

	private String Y1, Y2, X, sentence, question, verb1, verb2, x1_relation, x2_relation, y1_relation, y2_relation;
	private int x1_index, x2_index, y1_index, y2_index, verb2_index;
	MongoClient mongo_client;

	public boolean processKnowledge(Document doc) {
		try {
			GraphPassingNode gpn;
			SentenceParser sp = SentenceParser.getInstance();

			// Extract verb1, verb2, x1_relation, x2_relation
			if (doc != null)
				if (!extractKnowledge(doc)) {
					System.err.println("Failed to extract Semantic parametres from knowledge");
					return false;
				}

			// Run parser on example sentence,
			gpn = sp.parse(sentence);
			// get x1_index, x2_index, y1, y1_index (agent or recp according to x1_relation)
			// Needs semantic_graph, verb1, verb2, x1_relation, x2_relation
			if (!extractSemanticRelation(gpn)) {
				System.err.println("Failed to extract Semantic Relations");
				return false;
			}
			/*
			 * System.out.println(X); System.out.println(x1_index); System.out.println(X);
			 * System.out.println(x2_index); if(Y1 != "") { System.out.println(Y1);
			 * System.out.println(y1_index); } if(Y2 != "") { System.out.println(Y2);
			 * System.out.println(y2_index); }
			 */
			if (!isPerson(X, gpn, x1_index)) {
				System.out.println("Entity " + X + " isn't agent");
				return false;
			}
			if (!isPerson(Y1, gpn, y1_index))
				y1_index = -1;

			// Needs x1_index, x2_index, x1_relation, x2_relation, y1_index, y1_relation
			modifySentence(); // Generate Sentence and Question.

		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean extractKnowledge(Document doc) throws JSONException {
		boolean s1hasagent, s1hasrecp, s2hasagent, s2hasrecp;
		JSONObject d = new JSONObject(doc.toJson());

		s1hasagent = d.getJSONObject("knowledge").has("agent");
		s1hasrecp = d.getJSONObject("knowledge").has("recipient");
		verb1 = d.getJSONObject("knowledge").getString("value");
		s2hasagent = d.getJSONObject("knowledge").getJSONObject("causes").has("agent");
		s2hasrecp = d.getJSONObject("knowledge").getJSONObject("causes").has("recipient");
		verb2 = d.getJSONObject("knowledge").getJSONObject("causes").getString("value");
		sentence = (String) d.getJSONArray("texts").get(0);
		if (s1hasagent == true) {
			x1_relation = "agent";
		} else if (s1hasrecp == true) {
			x1_relation = "recipient";
		} else
			return false;
		if (s2hasagent == true) {
			x2_relation = "agent";
		} else if (s2hasrecp == true) {
			x2_relation = "recipient";
		} else
			return false;

		return true;
	}

	private void modifySentence() throws Exception {
		int i;
		String[] tokens = sentence.split(" ");
		tokens[x1_index] = "Tom";
		tokens[x2_index] = "he";

		if (y1_relation != null && y1_index != -1)
			tokens[y1_index] = "John";

		sentence = "";
		for (i = 0; i < tokens.length; i++)
			sentence += " " + tokens[i];
		// Build question
		if (x2_relation.equals("agent")) {
			question = "who";
		} else if (x2_relation.equals("recipient")) {
			question = "who was";
		} else {
			throw new Exception("Unknown X relation");
		}
		for (i = verb2_index; i < tokens.length; i++) {
			question += " " + tokens[i];
		}
		question += "?";
	}

	private boolean isPerson(String entity, GraphPassingNode gpn, int index) {
		HashMap<String, ArrayList<String>> wordSenseMap = gpn.getWordSenseMap();
		ClassesResource cr = gpn.getConClassRes();
		HashMap<String, String> sm = cr.getSuperclassesMap();
		index++;
		String subClass = sm.get(entity + "-" + index);

		if (subClass == null) {
			System.err.println("failed to get subclass for entity " + entity + "-" + index);
			return false;
		}
		if (subClass.contains("person"))
			return true;
		System.err.println("subclass for entity " + entity + "-" + index + "is" + subClass);
		return false;
	}

	private boolean extractSemanticRelation(GraphPassingNode gpn) {
		String[] split;
		
		x1_index = -1;
		x2_index = -1;
		verb2_index = -1;
		X = null;
		Y1 = null;
		Y2 = null;
		y1_relation = null;
		y2_relation = null;
		for (String s : gpn.getAspGraph()) {
			if (s.contains(verb1) && s.contains("instance_of")) {
				split = s.split(",");
				split = split[0].split("-");
				split = split[0].split("\\(");
				verb1 = split[split.length - 1];
			}
			if (s.contains(verb2) && s.contains("instance_of")) {
				split = s.split(",");
				split = split[0].split("-");
				verb2_index = Integer.parseInt(split[split.length - 1])-1;
				split = split[0].split("\\(");
				verb2 = split[split.length - 1];
			}
		}
		for (String s : gpn.getAspGraph()) {
			if (s.contains(verb1) && s.contains(x1_relation)) {
				split = s.split("\\D+");
				x1_index = Integer.parseInt(split[split.length - 1]) - 1;

				split = s.split(",");
				split = split[split.length - 1].split("-");
				X = split[0];
				// System.out.println(X);
				// System.out.println(x1_index);
			} else if (s.contains(verb2) && s.contains(x2_relation)) {
				split = s.split("\\D+");
				x2_index = Integer.parseInt(split[split.length - 1]) - 1;
				// System.out.println(verb2_index);
			} else if (s.contains(verb1) && s.contains("agent") || s.contains(verb1) && s.contains("recipient")) {
				split = s.split(",");
				split = split[split.length - 1].split("-");
				if (!split[0].equals(X)) {
					Y1 = split[0];
					if (s.contains("agent")) {
						y1_relation = "agent";
					} else {
						y1_relation = "recipient";
					}
				}
				split = s.split("\\D+");
				y1_index = Integer.parseInt(split[split.length - 1]) - 1;
				// System.out.println(Y1);
				// System.out.println(y1_index);
			} else if (s.contains(verb2) && s.contains("agent") || s.contains(verb2) && s.contains("recipient")) {
				split = s.split(",");
				split = split[split.length - 1].split("-");
				if (!split[0].equals(X)) {
					Y2 = split[0];
					if (s.contains("agent")) {
						y2_relation = "agent";
					} else {
						y2_relation = "recipient";
					}
				}
				split = s.split("\\D+");
				y2_index = Integer.parseInt(split[split.length - 1]) - 1;
				// System.out.println(Y1);
				// System.out.println(y1_index);
			}

		}

		/* Validate extracted parameters */
		if (x1_index == -1 || x2_index == -1 || verb2_index == -1 || X == null)
			return false;
		if (Y1 != null && y1_relation == null)
			return false;
		if (Y2 != null && y2_relation == null)
			return false;

		return true;
	}

	public static void main(String[] args) {
		QuestionGenerator qg = new QuestionGenerator();

		qg.testExamples();
//		qg.printSemantic("Jon needs to think about that some more because Jon usually likes to tweak them before sending");
//		qg.printSemantic("Mike was arrested by Paul because Mike was caught by Jan");

//		try {
//			FindIterable<Document> docs = qg.retrieveKonwledgeRecords();
//
//			for (Document doc : docs) {
//				if (qg.processKnowledge(doc)) {
//					System.out.println("* " + qg.sentence);
//					System.out.println("Q: " + qg.question);
//				} else {
//					System.out.println("failed to process knowledge");
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		qg.mongo_client.close();
	}

	private FindIterable<Document> retrieveKonwledgeRecords() throws JSONException {
		QuestionGenerator qg = new QuestionGenerator();

		int port_no = 27017;
		String host_name = "localhost", db_name = "knetdb", db_coll_name = "maintable";
		String client_url = "mongodb://" + host_name + ":" + port_no + "/" + db_name;
		MongoClientURI uri = new MongoClientURI(client_url);
		mongo_client = new MongoClient(uri);
		MongoDatabase db = mongo_client.getDatabase(db_name);
		MongoCollection<Document> coll = db.getCollection(db_coll_name);

		FindIterable<Document> docs = qg.findKnowledgeInKB(coll);
		return docs;

	}

	public FindIterable<Document> findKnowledgeInKB(MongoCollection<Document> col) {
		FindIterable<Document> docs = col.find(Filters.eq("type", "action causes action"));
		return docs;
	}

	@SuppressWarnings("unused")
	private void testExamples() {
		String[] records = { "" };
		// Example 1
		sentence = "Mike was arrested by Paul because Mike killed Jan";
		verb1 = "arrest";
		verb2 = "kill";
		// qg.Y1 = "paul";
		// X = "Mike";
		// qg.x1_index = 0;
		// qg.x2_index = 6;
		// qg.y1_index = 4;
		// qg.connective_index = 5;
		// qg.verb1_index = 2;
		// qg.verb2_index = 7;
		x1_relation = "recipient";
		x2_relation = "agent";
		// qg.y1_relation = "agent";
		// qg.y2_relation = "agent";
		if (processKnowledge(null)) {
			System.out.println("* " + sentence);
			System.out.println("Q: " + question);
		}

		// Example 2
		sentence = "Mike was arrested by Paul because Mike was caught by Jan";
		verb1 = "arrest";
		verb2 = "catch";
		// X = "Mike";
		x1_relation = "recipient";
		x2_relation = "recipient";
		if (processKnowledge(null)) {
			System.out.println("* " + sentence);
			System.out.println("Q: " + question);
		}

		// Example 3
		sentence = "Jon needs to think about that some more because Jon usually likes to tweak them before sending";
		verb1 = "think";
		verb2 = "tweak";
		// qg.Y1 = null;
		// X = "Jon";
		// qg.x1_index = 0;
		// qg.x2_index = 9;
		// qg.y1_index = -1;
		// qg.connective_index = 8;
		// qg.verb1_index = 3;
		// qg.verb2_index = 13;
		x1_relation = "agent";
		x2_relation = "agent";
		// qg.y1_relation = "recipient";
		// qg.y2_relation = "recipient";
		if (processKnowledge(null)) {
			System.out.println("* " + sentence);
			System.out.println("Q: " + question);
		}
	}

	@SuppressWarnings("unused")
	private void printSemantic(String sentence) {
		SentenceParser sp = SentenceParser.getInstance();
		GraphPassingNode graphNode = sp.parse(sentence);
		for (String s : graphNode.getAspGraph()) {
			System.out.println(s);
		}
		System.out.println("\n\n*********\nWordSene Graph\n*********");
		HashMap<String, ArrayList<String>> wordSenseMap = graphNode.getWordSenseMap();
		// wordSenseMap.entrySet()
		for (Map.Entry<String, ArrayList<String>> entry : wordSenseMap.entrySet())
			System.out.println(entry.getKey() + "=" + entry.getValue());
	}

}