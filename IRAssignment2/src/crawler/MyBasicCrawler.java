package crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyBasicCrawler extends WebCrawler {
	
	private ExecutorService threadService;
	private File file;
	private Writer w;
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp3|zip|gz))$");
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<org.bson.Document> pages;
	private MongoCollection<org.bson.Document> counters;
	static int count=1;

	public MyBasicCrawler() throws IOException {
		threadService = Executors.newFixedThreadPool(10);
		this.file = new File("src//log.txt");
		w = new FileWriter(file);
		mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		database = mongoClient.getDatabase("IR_DB");
		pages = database.getCollection("Pages");
        counters = database.getCollection("Counters");
		 if (counters.count() == 0) 
		 {
			 //init counters
			 org.bson.Document document = new org.bson.Document();
			 document.append("_id", "pageId");
			 document.append("seq", 1);
			 counters.insertOne(document);
	    }
	}

	
	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String urlString = url.getURL();
		if(urlString==null)
			return false;
		String href = urlString.toLowerCase();
		if(href.contains("duttgroup") || href.contains("mailman") 
				|| href.contains("~eppstein") || href.contains("flamingo")
				|| href.contains("//contact//student-affairs//contact//student-affairs")
				|| href.contains("prospective") || href.contains("drzaius")
				|| href.contains("archive") || href.contains("?"))
		{
			try {
				w.write("Not visiting page: " + href);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}
		return !FILTERS.matcher(href).matches() && href.contains("ics.uci.edu");
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		ParsedPage p = createParsedPage(page);
		try {
			w.write(new Date() + " Count: " + count++ 
					+"\t" + "Read Page: " + p.url + "\r\n");
		
		threadService.submit(new DatabaseTask(p));
		w.write(new Date() + "\tEnd visit: "+ p.url+"\r\n");
		w.flush();
		} catch (IOException e) {
			System.err.println("Failed to write to log file");
			e.printStackTrace();
		}
	}

	public Object getNextSequence() {

	    org.bson.Document searchQuery = new org.bson.Document("_id", "pageId");
	    org.bson.Document increase = new org.bson.Document("seq", 1);
	    org.bson.Document updateQuery = new org.bson.Document("$inc", increase);
	    org.bson.Document result = counters.findOneAndUpdate(searchQuery, updateQuery);

	    return result.get("seq");
	}
	
	
	private ParsedPage createParsedPage(Page page) {
		ParsedPage p = new ParsedPage();	

		HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
		String text = htmlParseData.getText();
		String htmlContent = htmlParseData.getHtml();
		String[] words = text.split("[,;. \n]+");
		StringBuilder str = new StringBuilder();
		int noOfWords = 0;
		for(int i = 0; i<words.length; i++)
		{
			String s = words[i];
			s = s.toLowerCase();
			s = s.replaceAll("[^a-z0-9]+", "");
			if(!s.isEmpty())
			{
				str.append(s);
				str.append(" ");
				noOfWords++;
			}
		}
		Set<String> outgoingURLs = new HashSet<>();
		for(WebURL url: htmlParseData.getOutgoingUrls())
		{
			String href = url.getURL().toLowerCase();
			if(!href.contains("?") && 
					!FILTERS.matcher(href).matches() && 
					href.contains("ics.uci.edu") )
			{
				outgoingURLs.add(href);
			}
		}
		HashMap<String, Set<String>> tagValues = new HashMap<>();
		Document doc = Jsoup.parse(htmlParseData.getHtml());
		
		if(doc!=null)
		{
			Elements tags = doc.select("h1");
			Set<String> values = new HashSet<>();
			for (Element e :tags)
			{
				values.add(e.text());
			}
			tagValues.put("h1", values);
			
			tags = doc.select("h2");
			values = new HashSet<>();
			for (Element e :tags)
			{
				values.add(e.text());
			}
			tagValues.put("h2", values);
			
			tags = doc.select("h3");
			values = new HashSet<>();
			for (Element e :tags)
			{
				values.add(e.text());
			}
			tagValues.put("h3", values);
			
			tags = doc.select("title");
			values = new HashSet<>();
			for (Element e :tags)
			{
				values.add(e.text());
			}
			tagValues.put("title", values);
		}

		String subDomain = page.getWebURL().getSubDomain();
		subDomain = subDomain.replace("www", "");
		subDomain = subDomain.replace(".ics", "");
		
		p.setSubdomain(subDomain);
		p.setUrl(page.getWebURL().getURL());
		p.setContent(str.toString());
		p.setHtmlContent(htmlContent);
		p.setNoOfWords(noOfWords);
		p.setOutGoingURLs(outgoingURLs);
		p.setMetaTags(htmlParseData.getMetaTags());
		p.setTagValues(tagValues);
		
		return p;
	}
	
	class DatabaseTask implements Runnable
	{
		ParsedPage p;
		
		DatabaseTask(ParsedPage p)
		{
			this.p = p;
		}
		
		@Override
		public void run() {
			saveToMongoDB(p);
		}
/*		*//**
		 * This function will contain the logic to persist the parsed page
		 * @param p
		 *//*
		public void savePageToDatabase(ParsedPage p){
		 try{
			 Class.forName("com.mysql.jdbc.Driver").newInstance();//create new instance of jdbc driver
			 Connection connect=DriverManager.getConnection("jdbc:mysql:///IRTest","root","sqlpwd");//create connection to database IRTest
			 Set<String> outurls= new HashSet<>();
			 outurls=p.getOutGoingURLs();
			 String s,s1,s2, s3;
			 s=p.getContent();
			 s1=p.getUrl();
			 s2=p.getSubdomain();
			 s3= p.getHtmlContent();
			 int n;
			 n=p.getNoOfWords();
			 String sql="{call usp_InsertEntryToPages(?,?,?,?,?)}";
			 CallableStatement statement=connect.prepareCall(sql);
			 statement.setString(1, s1);
			 statement.setString(2, s);
			 statement.setString(3, s3);
			 statement.setString(4, s2);
			 statement.setInt(5, n);
			 statement.execute();
			 System.out.println("Page table updated for page: " + s1);
			 w.write("\nThread: " +Thread.currentThread().getName() + " Page table updated for page: " + s1);
			 w.flush();
		
			 String sql1="{call usp_InsertEntryToLinks(?,?)}";
			 CallableStatement statement1=connect.prepareCall(sql1);
			 Iterator<String> t= outurls.iterator();
				while(t.hasNext()){
					statement1.setString(1,(String)s1);
					//System.out.println((String)t.next());
					statement1.setString(2,t.next());//check
					statement1.execute();
				}
			 //System.out.println("Pagelink table updated");
		 }
		 catch(SQLException se){
		
			 se.printStackTrace();
			 
		 }
		 catch(Exception e){
		
			 e.printStackTrace();
		
		 }
		
		}*/
		
		private void saveToMongoDB(ParsedPage p) {
			org.bson.Document page = new org.bson.Document();
		    Object nextSequence = getNextSequence();
			page.append("pageId", nextSequence);
			page.put("Url", p.getUrl());
			page.put("Subdomain", p.getSubdomain());
			page.put("Content", p.getContent());
			page.put("HtmlContent", p.getHtmlContent());
			page.put("nWords", p.getNoOfWords());
			page.put("outgoingLinks", p.getOutGoingURLs());
			MongoCollection<org.bson.Document> pages =  database.getCollection("Pages");
			pages.insertOne(page);
			System.out.println("Thread: " + Thread.currentThread().getName() + " Saved page: " + nextSequence.toString() + " " + p.getUrl()+"\r\n");
		}
	}
}