package crawlAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
//import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import crawler.ParsedPage;


@SuppressWarnings("unused")
public class Driver {
	private static MongoClient mongoClient;
	private static MongoDatabase database;
	private static MongoCollection<org.bson.Document> pages;
	
	public Writer w;
	
	static HashMap<String, Integer> hash = new HashMap<>();

	public static void main(String[] args) throws IOException {
		ParsedPage p=new ParsedPage();
		try{

			HashSet<String> result=new HashSet<>();
			mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
			database = mongoClient.getDatabase("IR_DB");
			pages = database.getCollection("Pages");
						
			String s;
			ComputeWordFreq wf=new ComputeWordFreq();
			ThreeGrams tg = new ThreeGrams();
			Token token=new Token();
			ArrayList<String> list=new ArrayList<>(); 

			FindIterable<Document> iterable = pages.find();
			MongoCursor<Document> iterator = iterable.iterator();

/*			while (iterator.hasNext()) {
				try {

					Document myDoc = iterator.next();
					s = (String) myDoc.get("Content");
					list = token.tokenizeFile(s);

					wf.computeWordFrequencies(list);
					tg.computeThreeGramFrequencies(list);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}*/
//			printFrequentWords(wf);
//			printFrequentThreeGrams(tg);
			Driver d = new Driver();
			File f = new File("subdomains.txt");
			d.w = new FileWriter(f);
//			d.printNoOfUniquePages(pages);
//			d.printLargestPage(pages);
			d.getSubdomains(pages);
		}
		catch (MongoException e) {
			e.printStackTrace();
		} finally {
		}
	}

	private void printLargestPage(MongoCollection<Document> pages) {
		BasicDBObject b = new BasicDBObject();
		b.put("nWords", -1);
		Document d = pages.find().sort(b).first();
		System.out.println("\n Page with maximum words: " + d.get("Url"));
		System.out.println("No of Words: " + d.get("nWords"));
	}

	private void printFrequentThreeGrams(ThreeGrams tg) {
		tg.print();
	}

	private void printFrequentWords(ComputeWordFreq wf) {
		wf.print(hash);
	}

	private void printNoOfUniquePages(MongoCollection<Document> pages) {
		long n = pages.count();
		System.out.println("\nNo of unique pages: " + n);
	}

	private void getSubdomains(MongoCollection<Document> pages) throws IOException
	{
		// db.Pages.aggregate( [ { $group : { _id : "$Subdomain" , count:{$sum:1}} } ] )
		Document d = 
		        new Document("$group", new Document("_id", "$Subdomain").append("count", new Document("$sum", 1)));
		List<Document> l = new ArrayList();
		l.add(d);
		AggregateIterable<Document> iterable = pages.aggregate(l);
		Map<String, Integer> subdomains = new TreeMap<>();
		for(Document doc: iterable)
		{
			Integer n = (Integer) doc.get("count");
			String subdomain = (String) doc.get("_id");
			if(!subdomain.trim().isEmpty())
				subdomains.put(subdomain, n);
		}
		Set<Entry<String,Integer>> set2 = subdomains.entrySet();
		Iterator<Entry<String, Integer>> iterator2 = set2.iterator();
		while (iterator2.hasNext()) {
			Map.Entry me2 = (Map.Entry) iterator2.next();
			this.w.write("\n" + me2.getKey() + ": " + me2.getValue());
			System.out.print(me2.getKey() + ": ");
			System.out.println(me2.getValue());
		}
	}
	
}
