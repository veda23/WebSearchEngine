package business;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import analyzer.GoogleSearchResults;

public class Search {

	Token t;
	DatabaseConnector db;
	long noOfDocuments;
	
	public Search(String stopWordsFileDirectory) throws FileNotFoundException
	{	
		t = new Token(stopWordsFileDirectory);
		db = new DatabaseConnector();
		noOfDocuments = db.getNumberOfDocuments();
	}
	
	public List<Result> getSearchResults(String query)
	{
		query = query.toLowerCase();
		System.out.println(new Date() + " Running query...");
		ArrayList<Integer> pageIds = new ArrayList<>();
		List<Document> indexEntries = getIndexEntiresQuery(query);
		HashMap<Integer, Double> scoreMap = getTF_IDFScoreMap(indexEntries);
		Integer[] keys = new Integer[scoreMap.size()];
		keys = scoreMap.keySet().toArray(keys);
		Arrays.sort(keys, new DescendingValueComparatorDouble<Integer>(scoreMap));
		for(int i = 0; i<keys.length; i++)
		{
			pageIds.add(keys[i]);
		}
		System.out.println(new Date() + " Got pageId's, retrieving page URL's and content...");
		int val = Math.min(pageIds.size(), 10);
		List<PageContent> pages = getPageContentsFromPageIds(pageIds.subList(0, val));
		List<Result> results = new ArrayList<>();
		System.out.println(new Date() + " Done, results created...");
		for(int i = 0; i<pages.size(); i++)
		{
//			System.out.println(pages.get(i).url + ": "+ scoreMap.get(pageIds.get(i)));
			String textSnippet = getTextSnippet(pages.get(i), t.tokenizeFile(query), pageIds.get(i));
//			System.out.println(textSnippet);
			Result r = new Result(pages.get(i).url, textSnippet);
			results.add(r);
		}
		db.closeDBConnection();
		return results;
	}
	
	public List<Result> getSearchResultsImproved(String query)
	{
		query = query.toLowerCase();
		System.out.println(new Date() + " Running query...");
		ArrayList<Integer> pageIds = new ArrayList<>();
		List<Document> indexEntries = getIndexEntiresQuery(query);
		
		HashMap<Integer, Double> scoreMap = getTF_IDFScoreMap(indexEntries);
		System.out.println(scoreMap.get(1));
		getLinkAnalsisScoreMap(indexEntries, scoreMap);
		System.out.println(scoreMap.get(1));
//		getPageRankScoreMap(scoreMap);
		System.out.println(scoreMap.get(1));
		
		Integer[] keys = new Integer[scoreMap.size()];
		keys = scoreMap.keySet().toArray(keys);
		Arrays.sort(keys, new DescendingValueComparatorDouble<Integer>(scoreMap));
		for(int i = 0; i<keys.length; i++)
		{
			pageIds.add(keys[i]);
		}
		
		System.out.println(new Date() + " Got pageId's, retrieving page URL's and content...");
		int val = Math.min(pageIds.size(), 10);
		
		//Start
		List<Integer> trimmed = pageIds.subList(0, val);
		HashMap<Integer, Double> trimmedMap = new HashMap<>();
		for(Integer i: trimmed)
		{
			trimmedMap.put(i, scoreMap.get(i));
		}
		getPageRankScoreMap(trimmedMap);
		Integer[] trimmedKeys = new Integer[trimmedMap.size()];
		trimmedKeys = trimmedMap.keySet().toArray(trimmedKeys);
		Arrays.sort(trimmedKeys, new DescendingValueComparatorDouble<Integer>(trimmedMap));
		pageIds.clear();
		for(int i = 0; i<trimmedKeys.length; i++)
		{
			pageIds.add(trimmedKeys[i]);
		}
		//End
		
		List<PageContent> pages = getPageContentsFromPageIds(pageIds.subList(0, val));
		List<Result> results = new ArrayList<>();
		System.out.println(new Date() + " Done, results created...");
		for(int i = 0; i<pages.size(); i++)
		{
//			System.out.println(pages.get(i).url + ": "+ scoreMap.get(pageIds.get(i)));
			String textSnippet = getTextSnippet(pages.get(i), t.tokenizeFile(query), pageIds.get(i));
//			System.out.println(textSnippet);
			Result r = new Result(pages.get(i).url, textSnippet, pages.get(i).getTitle());
			results.add(r);
		}
		db.closeDBConnection();
		return results;
	}

	private void getPageRankScoreMap(HashMap<Integer, Double> scoreMap) {
		for(Integer pageId : scoreMap.keySet())
		{
//			String pageUrl = getUrlForPageId(pageId);
			double pageRank = db.getPageRankForPage(pageId);
			// TODO
			double pageRankScore = pageRank;
			scoreMap.put(pageId, scoreMap.get(pageId) + pageRankScore);
		}
	}

	private List<Document> getIndexEntiresQuery(String query)
	{
		List<Document> entries = new ArrayList<>();
		List<String> queryWords = t.tokenizeFile(query);
		for(String term: queryWords)
		{
			Document d = db.getIndexEntry(term);
			if (d != null) {
				entries.add(d);
			}
		}
		return entries;
	}
	
	private List<Document> getIndexEntiresQueryImproved(String query)
	{
		List<Document> entries = new ArrayList<>();
		List<String> queryWords = t.tokenizeFile(query);
		for(String term: queryWords)
		{
			Document d = db.getIndexEntryImproved(term);
			if (d != null) {
				entries.add(d);
			}
		}
		return entries;
	}
	
	private HashMap<Integer, Double> getTF_IDFScoreMap(List<Document> entries) {
		HashMap<Integer, Double> scoreMap = new HashMap<>();
		for(Document d: entries)
		{
			if(d==null)
			{
				continue;
			}
			double df = d.getInteger("df");
			df = noOfDocuments/df;
			double idf = Math.log(df);
			List<Document> termlist =  (List<Document>) d.get("termlist");
			for(Object o: termlist)
			{
				Integer pageId = ((Document) o).getInteger("pageId");
				double tf = ((Document) o).getDouble("tf");
				if(scoreMap.containsKey(pageId))
				{
					scoreMap.put(pageId, scoreMap.get(pageId)+tf*idf/3);
				}
				else
				{
					scoreMap.put(pageId, tf*idf);
				}
			}
		}
		return scoreMap;
	}
	
	private void getLinkAnalsisScoreMap(List<Document> entries, HashMap<Integer, Double> scoreMap) {
		for(Document d1: entries)
		{
			if(d1==null)
			{
				continue;
			}
			
			String term = d1.getString("term");
			Document d = db.getIndexEntryImproved(term);
			if (d == null) {
				continue;
			}
			List<Document> anchorUrls = (List<Document>) d.get("Anchor Url");
			for(Document anchorUrl : anchorUrls)
			{
//				 String url = anchorUrl.getString("Url");
				 int pageId = anchorUrl.getInteger("Url", -1);
				 int count = anchorUrl.getInteger("Count");
//				 int pageId = getDocumentIdForUrl(url);
				 if (pageId > 0) {
					 // TODO: Generate score for anchor count 
					 double anchorCountScore = count*1.5;
					if (scoreMap.containsKey(pageId)) {
						scoreMap.put(pageId, scoreMap.get(pageId) + anchorCountScore);
					} else {
						scoreMap.put(pageId, (double)anchorCountScore);
					}
				 }
			}
			List<Integer> heading1 = (List<Integer>) d.get("Heading1");
			// TODO: headings ka score
			double h1Score = 0.5;
			for(Integer pageId : heading1)
			{
				if (scoreMap.containsKey(pageId)) {
					scoreMap.put(pageId, scoreMap.get(pageId) + h1Score);
				} else {
					scoreMap.put(pageId, h1Score);
				}
			}
			
			List<Integer> titleContainingDocs = (List<Integer>) d.get("Title");
			// TODO: Title score
			int titleScore = 10;
			for(Integer pageId : titleContainingDocs)
			{
				if (scoreMap.containsKey(pageId)) {
					scoreMap.put(pageId, scoreMap.get(pageId) + titleScore);
				} else {
					scoreMap.put(pageId, (double)titleScore);
				}
			}
		}
	}
	
	public HashMap<String, Integer> urlToPageId = new HashMap<String, Integer>();

	private int getDocumentIdForUrl(String url) {
		if (urlToPageId.containsKey(url)) {
			return urlToPageId.get(url);
		}
		
		int pageId = db.getPageIdForUrl(url);
		if (pageId > 0) {
			urlToPageId.put(url, pageId);
		}
		return pageId;
	}
	
	public HashMap<Integer, String> pageIdToUrl = new HashMap<Integer, String>();

	private String getUrlForPageId(int pageId) {
		if (pageIdToUrl.containsKey(pageId)) {
			return pageIdToUrl.get(pageId);
		}
		
		String pageUrl = db.getUrlForPageId(pageId);
		pageIdToUrl.put(pageId, pageUrl);
		return pageUrl;
	}

	List<PageContent> getPageContentsFromPageIds(List<Integer> pageIds)
	{
		List<PageContent> pages = new ArrayList<>();
		for(Integer i: pageIds)
		{
			PageContent page = db.getPageContentFromDB(i);
			if(page==null)
			{
				continue;
			}
			pages.add(page);
		}
		return pages;
	}

	private HashMap<Integer, HashMap<String, List<Integer>>> getPositions(List<Document> indexEntries)
	{
		HashMap<Integer, HashMap<String, List<Integer>>> positions = new HashMap<>();
		for(Document entry: indexEntries)
		{
			String word = entry.getString("term");
			List<Document> termlist =  (List<Document>) entry.get("termlist");
			for(Object o: termlist)
			{
				Integer pageId = ((Document) o).getInteger("pageId");
				List<Integer> positionList = (List<Integer>) ((Document) o).get("position");
				if(positions.containsKey(pageId))
				{
					positions.get(pageId).put(word, positionList);
				}
				else
				{
					HashMap<String, List<Integer>> wordList = new HashMap<>();
					wordList.put(word, positionList);
					positions.put(pageId, wordList);
				}
			}
		}
		return positions;
	}
	
	String getTextSnippet(PageContent pc, List<String> query, int pageId)
	{
		String firstWord = query.get(0);
		Integer firstStart = null;
		Integer firstEnd = null;
		String contentList[] = pc.content.split("\\W");
		Integer firstPos=null, secondPos=null;
		for(int i = 0; i<contentList.length; i++)
		{
			if(contentList[i].equals(firstWord))
			{
				firstPos=i;
				if(secondPos!=null)
					break;
			}
			if(query.size()>1 && contentList[i].equals(query.get(1)))
			{
				secondPos = i;
				if(firstPos!=null)
					break;
			}
		}
		StringBuilder snippet = new StringBuilder();
		
		if(firstPos!=null)
		{
			firstStart = Math.max(0, firstPos-10);
			firstEnd = firstPos+10;
		}
		if(query.size()>1 && secondPos!=null)
		{
			String secondWord = query.get(1);
			Integer secondStart = null;
			Integer secondEnd = null;
					
			secondStart = Math.max(0, secondPos-10);
			secondEnd = secondPos+10;
			if(firstStart!=null && secondStart!=null)
			{
				if(firstStart<secondEnd && secondStart<firstEnd)
				{
					snippet.append(getSubText(firstStart, secondEnd, pc.content));
					return snippet.toString();
				}
				else if(secondStart<firstEnd && firstStart<secondEnd)
				{
					snippet.append(getSubText(secondStart, firstEnd, pc.content));
					return snippet.toString();
				}
				else
				{
					//Non overlapping
					snippet.append(getSubText(firstStart, firstEnd, pc.content));
					snippet.append("...");
					snippet.append(getSubText(secondStart, secondEnd, pc.content));
					return snippet.toString();
				}
			}
			else
			{
				if(secondStart!=null)
				{
					snippet.append(getSubText(secondStart, secondEnd, pc.content));
					return snippet.toString();
				}
			}
		}
		else
		{
			if(firstStart!=null)
				snippet.append(getSubText(firstStart, firstEnd, pc.content));
		}
		return snippet.toString();
		
	}
	
	private String getSubText(Integer secondStart, Integer secondEnd, String content) {
		String contentList[] = content.split("\\W");
		StringBuilder s = new StringBuilder();
		secondEnd = Math.min(secondEnd, contentList.length);
		for(int i = secondStart; i<secondEnd; i++)
		{
			s.append(contentList[i]);
			s.append(" ");
		}
		return s.toString();
	}

	public static void main(String[] args) throws FileNotFoundException {
		Search s = new Search("src/business");
		String[] queries = {"mondego", "machine learning", "software engineering", "security", "student affairs", "graduate courses", "Crista Lopes", "REST", "computer games", "information retrieval"};
		int i = 1;
		for (String query : queries) {
			if (i == 7) {
			s = new Search("src/business");
			List<Result> results = s.getSearchResultsImproved(query);
			System.out.println(query);
			for (Result result : results) {
				System.out.println(result.getURL());
				System.out.println();
			}
			System.out.println("-----------------------------------------------");
			break;
			}
			i++;
		}
		
	}
	

}
