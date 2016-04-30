package business;

import org.bson.Document;
import org.jsoup.Jsoup;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class DatabaseConnector {

	MongoClient mongoclient;
	MongoDatabase database;
	MongoCollection<Document> index;
	MongoCollection<Document> tagIndex;
	MongoCollection<Document> pages;
	MongoCollection<Document> pageRanks;
	
	public DatabaseConnector() {

		mongoclient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		database = mongoclient.getDatabase("IR_DB");

		index = database.getCollection("index");
		tagIndex = database.getCollection("TagIndex");
		pages = database.getCollection("Pages");
		pageRanks = database.getCollection("PageRanks");
	}
	
	Document getIndexEntry(String term)
	{
		FindIterable<Document> iterable = index.find(new Document("term", term));
		MongoCursor<Document> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}
	
	Document getIndexEntryImproved(String term)
	{
		FindIterable<Document> iterable = tagIndex.find(new Document("Term", term));
		MongoCursor<Document> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}
	
	long getNumberOfDocuments()
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
		org.jsoup.nodes.Document doc=Jsoup.parse(d.getString("HtmlContent"));
		p.setTitle(doc.title());
		return p;
	}
	
	public void closeDBConnection() {
		mongoclient.close();
	}

	public int getPageIdForUrl(String url) {
		int pageId = -1;
		FindIterable<Document> iterable = pages.find(new Document("Url", url));
		MongoCursor<Document> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			pageId = iterator.next().getInteger("pageId");
		}
		return pageId;
	}

	public double getPageRankForPage(String url) {
		double pageRank = 0;
		FindIterable<Document> iterable = pageRanks.find(new Document("Url", url));
		MongoCursor<Document> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			pageRank = iterator.next().getDouble("NPR");
		}
		return pageRank;
	}
	
	public double getPageRankForPage(int pageId) {
		double pageRank = 0;
		FindIterable<Document> iterable = pageRanks.find(new Document("pageId", pageId));
		MongoCursor<Document> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			pageRank = iterator.next().getDouble("NPR");
		}
		return pageRank;
	}

	public String getUrlForPageId(int pageId) {
		String pageUrl = null;
		FindIterable<Document> iterable = pages.find(new Document("pageId", pageId));
		MongoCursor<Document> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			pageUrl = iterator.next().getString("Url");
		}
		return pageUrl;
	}	
}
