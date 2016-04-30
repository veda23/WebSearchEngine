package pagerank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import business.PageContent;
import pagerank.Page;

public class DatabaseConnector {

	MongoClient mongoclient;
	MongoDatabase database;
	MongoCollection<Document> index1;
	MongoCollection<Document> pages;
	MongoCollection<Document> pageRanks;
	
	public DatabaseConnector() {

		mongoclient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		database = mongoclient.getDatabase("IR_DB");

		index1 = database.getCollection("index");
		pages = database.getCollection("Pages");
		pageRanks = database.getCollection("PageRanks");
	}
	
	public Document getIndexEntry(String term)
	{
		FindIterable<Document> iterable = index1.find(new Document("term", term));
		MongoCursor<Document> iterator = iterable.iterator();
		return iterator.next();
	}
	
	public long getNumberOfDocuments()
	{
		return pages.count();
	}

	public PageContent getPageContentFromDB(Integer id) {
		FindIterable<Document> iterable = pages.find(new Document("pageId", id));
		MongoCursor<Document> iterator = iterable.iterator();
		Document d = iterator.next();
		PageContent p = new PageContent();
		p.setUrl(d.getString("Url"));
		p.setContent(d.getString("Content"));
		p.setHtmlContent(d.getString("HtmlContent"));
		return p;
	}
	
	public HashMap<String, Page> getPageGraph()
	{
		HashMap<String, Page> pageGraph = new HashMap<>();
		FindIterable<Document> iterable = pages.find();
		MongoCursor<Document> iterator = iterable.iterator();		
		while(iterator.hasNext())
		{
			Document d = iterator.next();
			String url = d.getString("Url");
			Set<String> out = new HashSet<>();
			out.addAll((Collection<? extends String>) d.get("outgoingLinks"));
			Page p = new Page(url, out);
			pageGraph.put(url, p);
		}
		return pageGraph;
	}
	
	public void savePageRanks(HashMap<String, Double> pageRanks)
	{
		ArrayList<Document> list = new ArrayList<>();
		int i = 0;
		for(Entry<String, Double> e : pageRanks.entrySet())
		{
			Document d = new Document();
			d.put("Url", e.getKey());
			d.put("PageRank", e.getValue());
			d.put("NPR", e.getValue()/400.494);
			MongoCursor<Document> iterator = pages.find(new Document("Url", e.getKey())).iterator();
			if(iterator.hasNext()){
				Document page = iterator.next();
				d.put("pageId", page.get("pageId"));
			}
			list.add(d);
		}
		System.out.println("Saving: " + new Date());
		this.pageRanks.insertMany(list);
	}
	
	public HashMap<String, Double> getPageRanks()
	{
		HashMap<String, Double> pageRanks = new HashMap<>();
		FindIterable<Document> iterable = this.pageRanks.find();
		MongoCursor<Document> iterator = iterable.iterator();
		while(iterator.hasNext())
		{
			Document d = iterator.next();
			pageRanks.put(d.getString("Url"), d.getDouble("PageRank"));
		}
		return pageRanks;
	}
}
