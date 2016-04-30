package linkanalysis;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;

import org.bson.Document;
//import org.bson.Document;
//import 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;


//title,anchor text,anchor links,metadata,headings in form of alist
public class Parse {
	
	static HashMap<String,TagMap> tagmap=new HashMap<>();
	static MongoClient mongoclient;
	static MongoDatabase database;
	static MongoCollection<org.bson.Document> pages;
	static MongoCollection<org.bson.Document> tagindex;
	
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp3|zip|gz))$");
	
	@SuppressWarnings("resource")
	public Parse() {
		

		}

	public static void createDatabaseConnection()
	{
		mongoclient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		database = mongoclient.getDatabase("IR_DB");
		database.getCollection("TagIndex").drop();
		database.createCollection("TagIndex");
		tagindex = database.getCollection("TagIndex");
		pages = database.getCollection("Pages");
	}
	
	public static void saveTags()
	{
		ArrayList<Document> list = new ArrayList<>();
		for(Entry<String, TagMap> e : tagmap.entrySet())
		{
			Document d = new Document();
			//TagMap map=new TagMap();
			d.put("Term", e.getKey());
			TagMap value = e.getValue();
			d.put("Anchors",value.getAnchor());
//			d.put("Anchor Url", value.getAnchorUrl());
			HashSet<Document> subDocs = new HashSet();
			for(Entry<Integer, Integer> entry: value.getAnchorUrl().entrySet())
			{
				Document subDoc = new Document();
				subDoc.put("Url", entry.getKey());
				subDoc.put("Count", entry.getValue());
				subDocs.add(subDoc);
			}
			d.put("Anchor Url", subDocs);
			d.put("Heading1", value.getHeading1());
			d.put("Heading2", value.getHeading2());
			d.put("Title", value.getTitle());
			d.put("MetaData", value.getMeta());
			list.add(d);
		}
		System.out.println(new Date());
		tagindex.insertMany(list);
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		createDatabaseConnection();
		FindIterable<org.bson.Document> iterable = null;
		MongoCursor<org.bson.Document> iterator;

		iterable=pages.find();
		iterator=iterable.iterator();
		iterable.noCursorTimeout(true);
		iterable.batchSize(1000);

		System.out.println(new Date());
		while(iterator.hasNext()){//for each page

			org.bson.Document content= iterator.next();
			String HTMLcontent=(String)content.getString("HtmlContent");
			Integer pageid=(Integer)content.getInteger("pageId");
			String pageUrl = content.getString("Url");

			if (!urlToPageId.containsKey(pageUrl)) {
				urlToPageId.put(pageUrl, pageid);
			}
			
			Parse p=new Parse();
			p.parseAnchor(HTMLcontent,pageid);
			p.parseTitle(HTMLcontent, pageid);
			p.parseHeading1(HTMLcontent,pageid);
			p.parseHeading2(HTMLcontent,pageid);
			p.parseMeta(HTMLcontent,pageid);
			//p.print(tagmap);
			
			


		}
		System.out.println(new Date());
		saveTags();

	}

	public void parseMeta(String HTMLcontent, Integer pageid) throws FileNotFoundException {


		ArrayList<String> meta_list=new ArrayList<>();


		org.jsoup.nodes.Document doc=Jsoup.parse(HTMLcontent);
		Elements metadata=doc.select("meta");
		for(Element meta:metadata){
			String meta_content=meta.attr("content");
			//System.out.println(meta_content);

			//System.out.println(HTMLcontent);

			Token token=new Token();
			meta_list=token.tokenizeFile(meta_content);
			Iterator iterate=meta_list.iterator();
			while(iterate.hasNext()){
				String meta_term=(String)iterate.next();
				//System.out.println(meta_term);
				TagMap tag_meta=new TagMap();

				if(tagmap.containsKey(meta_term)){
					tag_meta=tagmap.get(meta_term);
					tag_meta.meta.add(pageid);
					tagmap.put(meta_term, tag_meta);
				}
				else{
					tagmap.put(meta_term, tag_meta);
				}

			}

		}
	}


	public void parseHeading1(String HTMLcontent, Integer pageid) throws FileNotFoundException {

		ArrayList<String> head1_list=new ArrayList<>();

		org.jsoup.nodes.Document doc=Jsoup.parse(HTMLcontent);
		Elements heading1=doc.select("h1");

		for(Element head1:heading1){

			//System.out.println(head1_content);

			//System.out.println(HTMLcontent);

			Token token=new Token();
			head1_list=token.tokenizeFile(head1.text());
			//System.out.println(head1_list);
			Iterator iterate=head1_list.iterator();
			while(iterate.hasNext()){
				String head1_term=(String)iterate.next();
				//System.out.println(head1_term);
				TagMap tag_head1=new TagMap();
				tag_head1.heading1.add(pageid);
				if(tagmap.containsKey(head1_term)){
					tag_head1=tagmap.get(head1_term);
					tag_head1.heading1.add(pageid);
					tagmap.put(head1_term, tag_head1);
				}
				else{
					tagmap.put(head1_term, tag_head1);
				}

			}


		}
	}


	public void parseHeading2(String HTMLcontent, Integer pageid) throws FileNotFoundException {

		ArrayList<String> head2_list=new ArrayList<>();

		org.jsoup.nodes.Document doc=Jsoup.parse(HTMLcontent);
		Elements heading2=doc.select("h2");
		//System.out.println(HTMLcontent);

		for(Element head2:heading2){
			String head2_content=head2.text();
			//System.out.println(head2_content);


			Token token=new Token();
			head2_list=token.tokenizeFile(head2_content);
			Iterator iterate=head2_list.iterator();
			while(iterate.hasNext()){
				String head2_term=(String)iterate.next();
				//System.out.println(head2_term);
				TagMap tag_head2=new TagMap();
				tag_head2.heading2.add(pageid);
				if(tagmap.containsKey(head2_term)){
					tag_head2=tagmap.get(head2_term);
					tag_head2.heading1.add(pageid);
					tagmap.put(head2_term, tag_head2);

				}
				else{
					tagmap.put(head2_term, tag_head2);
				}

			}



		}
	}
	
	private boolean shouldIndexAnchorText(String anchorText) {
		if(anchorText==null)
			return false;
		String anchorTextLowerCase = anchorText.toLowerCase();
		
		if (false
				|| anchorTextLowerCase.startsWith("http://")
				|| anchorTextLowerCase.startsWith("https://")
				|| anchorTextLowerCase.startsWith("www.")) {
			return false;
		}
		
		return true;
	}
	
	public boolean shouldVisit(String urlString) {
		if(urlString==null)
			return false;
		String href = urlString.toLowerCase();
		if(false 
//				|| href.contains("duttgroup") 
//				|| href.contains("mailman") 
//				|| href.contains("~eppstein")
//				|| href.contains("flamingo")
				|| href.contains("//contact//student-affairs//contact//student-affairs")
//				|| href.contains("prospective")
//				|| href.contains("drzaius")
//				|| href.contains("archive") 
				|| href.contains("?"))
		{
			return false;
		}
		return !FILTERS.matcher(href).matches() && href.contains("ics.uci.edu");
	}
	
	public static HashMap<String, Integer> urlToPageId = new HashMap<String, Integer>();

	private int getPageIdForUrl(String url) {
		if (urlToPageId.containsKey(url)) {
			return urlToPageId.get(url);
		}
		
		int pageId = -1;
		FindIterable<Document> iterable = pages.find(new Document("Url", url));
		MongoCursor<Document> iterator = iterable.iterator();
		if (iterator.hasNext()) {
			pageId = iterator.next().getInteger("pageId");
		}
		if (pageId > 0) {
			urlToPageId.put(url, pageId);
		}
		return pageId;
	}

	void parseAnchor(String HTMLcontent, Integer pageid) throws FileNotFoundException{


		org.jsoup.nodes.Document doc=Jsoup.parse(HTMLcontent);
		ArrayList<String> alist=new ArrayList<>();

		Token t=new Token();
		int count=0;
		Elements link=doc.select("a");
		for(Element e:link){
			String text=e.text();//get anchor text
			String href=e.attr("href");//get anchor url
			// Check if URL is within the ICS subdomain
			if (shouldVisit(href)) {
				int pageIdOfLinkedPage = getPageIdForUrl(href);
				if (pageIdOfLinkedPage > 0) {
					alist = t.tokenizeFile(text);// tokenise anchor text
					Iterator iterate_list = alist.iterator();
					while (iterate_list.hasNext()) {
						String a = (String) iterate_list.next();
						if (!(a.equalsIgnoreCase("click") || a.equalsIgnoreCase("here") || a.equalsIgnoreCase("this")
								|| a.equalsIgnoreCase("pdf") || a.equalsIgnoreCase("doc")
								|| a.equalsIgnoreCase("ppt"))) {
							TagMap tag_term = new TagMap();
							tag_term.anchor.add(pageid);
							if (tagmap.containsKey(a)) {
								tag_term = tagmap.get(a);
								tag_term.anchor.add(pageid);
								tagmap.put(a, tag_term);
							} else {
								tagmap.put(a, tag_term);
							}

							if (tag_term.anchor_ref.containsKey(href)) {
								// update url map
								count = tag_term.anchor_ref.get(href);
								tag_term.anchor_ref.put(pageIdOfLinkedPage, ++count);
							} else {
								tag_term.anchor_ref.put(pageIdOfLinkedPage, 1);
							}
						}
					}
				}
			}			
		}
	}
	
	void parseTitle(String HTMLcontent,Integer pageid) throws FileNotFoundException{

		ArrayList<String> title_list=new ArrayList<>();

		org.jsoup.nodes.Document doc=Jsoup.parse(HTMLcontent);
		String title=doc.title();
		//System.out.println(title);

		//System.out.println(HTMLcontent);

		Token token=new Token();
		title_list=token.tokenizeFile(title);
		Iterator iterate=title_list.iterator();
		while(iterate.hasNext()){
			String title_term=(String)iterate.next();
			//System.out.println(title_term);
			TagMap tag_title=new TagMap();
			tag_title.title.add(pageid);
			if(tagmap.containsKey(title_term)){
				tag_title=tagmap.get(title_term);
				tag_title.title.add(pageid);
				tagmap.put(title_term, tag_title);

			}
			else{
				tagmap.put(title_term, tag_title);
			}

		}
	}
	
	public void print(HashMap<String, TagMap> hash) {
		Set set = hash.entrySet();
		Iterator iterator = set.iterator();
		int i=1;
		while (iterator.hasNext()) {//iterate through map hash2 entries
			Map.Entry me2 = (Map.Entry) iterator.next();
			System.out.print(me2.getKey() + ": ");
			System.out.println(me2.getValue().toString());
			
		}
		
	}
}
		
		