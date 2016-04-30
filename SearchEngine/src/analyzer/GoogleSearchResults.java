package analyzer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import business.Result;
import business.Search;
import sun.util.logging.resources.logging;

public class GoogleSearchResults {
	private String charset = "UTF-8";
	private String userAgent = "UCI - University of California, Irvine (http://ics.uci.edu)"; // Change this to your company's name and bot homepage!
	private String google = "http://www.google.com/search?start=";
	private String queryStringParameterName = "&q=";
	private ArrayList<String> googleTop5Results = new ArrayList<String>();
	
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp3|zip|gz))$");
	
	private HashSet<String> googleFirstPage = new HashSet<String>();
	private HashSet<String> googleSecondPage = new HashSet<String>();
	private HashSet<String> googleThirdPage = new HashSet<String>();
//	private HashSet<String> googleFourthPage = new HashSet<String>();
	private HashMap<String, Integer> relevanceScoreMapping = new HashMap<String, Integer>();

	private void getSearchResults(String originalQuery) throws UnsupportedEncodingException, IOException {
		String search = originalQuery + " site:ics.uci.edu";

		/*boolean searchDone = getSearchResultsForPage(search, 0, googleFirstPage)
		&& getSearchResultsForPage(search, 10, googleSecondPage)
		&& getSearchResultsForPage(search, 20, googleThirdPage);
//		&& getSearchResultsForPage(search, 30, googleFourthPage);*/
		
		getTop5GoogleResults(search, googleTop5Results);

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

	private boolean getSearchResultsForPage(String search, int pageNum, HashSet<String> searchResullts)
					throws IOException, UnsupportedEncodingException {
		boolean hasMore = false;
		String googleUrl = google + pageNum + queryStringParameterName;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Elements links = Jsoup.connect(googleUrl + URLEncoder.encode(search, charset)).userAgent(userAgent).get().select(".g>.r>a");
		
		for (Element link : links) {
//		    String title = link.text();
		    String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
		    url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

		    if (!url.startsWith("http")) {
		        continue; // Ads/news/etc.
		    }
		    searchResullts.add(url.toLowerCase());
		}
		return links.size() == 10;
	}
	
	private boolean getTop5GoogleResults(String search, ArrayList<String> searchResullts)
			throws IOException, UnsupportedEncodingException {
boolean hasMore = false;
String googleUrl = google + 0 + queryStringParameterName;
try {
	Thread.sleep(5000);
} catch (InterruptedException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
Elements links = Jsoup.connect(googleUrl + URLEncoder.encode(search, charset)).userAgent(userAgent).get().select(".g>.r>a");
int count = 0;
for (Element link : links) {
//    String title = link.text();
    String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
    url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

    if (!url.startsWith("http") && !shouldVisit(url)) {
        continue; // Ads/news/etc.
    }
    count++;
    if (count > 5) {
    	break;
    }
    searchResullts.add(getComparableUrl(url.toLowerCase()));
}
return links.size() == 10;
}
	
	private int getRelevanceScore(String url) {
		int relevanceScore = 0;
		if (googleFirstPage.contains(url)) {
			relevanceScore = 3;
		} else if (googleSecondPage.contains(url)) {
			relevanceScore = 2;
		} else if (googleThirdPage.contains(url)) {
			relevanceScore = 3;
		}
		return relevanceScore;		
	}
	
	private int getRelevanceScoreFromTop5(String url) {
		int relevanceScore = 0;
		for (int i = 0; i < googleTop5Results.size(); i++) {
			if (googleTop5Results.get(i).equalsIgnoreCase(url)) {
				relevanceScore = 5 - i;
				break;
			}
		}
		return relevanceScore;		
	}
	
	private double getDGScore(int rank, int relevance) {
		double dgScore = 0;
		if (rank == 0) {
			dgScore =  relevance;
		} else {
			dgScore = ((double) relevance) / (Math.log(1 + rank) / Math.log(2));
		}
		return dgScore;
	}
	
	public double getNDCGScore(String query, List<Result> ourResults) {
		double ndcgScore = 0;
		if (ourResults.size() < 1) {
			return ndcgScore;
		}
		
		try {
			LinkedList<Double> dcgScores = new LinkedList<Double>();
			LinkedList<Double> idealDCGScores = new LinkedList<Double>();
			ArrayList<Integer> relevanceScores = new ArrayList<Integer>();
			
			double runningDCGScore = 0;
			getSearchResults(query);
			int maxNumOfResults = Math.min(5, ourResults.size());
			for (int i = 0; i < maxNumOfResults; i++) {
				System.out.println("Before: " + ourResults.get(i).getURL());
				String resultUrl = getComparableUrl(ourResults.get(i).getURL());
				System.out.println("After: " + resultUrl);
				int relevance = getRelevanceScoreFromTop5(resultUrl);
				relevanceScoreMapping.put(resultUrl, relevance);
				relevanceScores.add(relevance);
				runningDCGScore += getDGScore(i, relevance);
				dcgScores.add(runningDCGScore);
			}
			
			Collections.sort(relevanceScores, Collections.reverseOrder());
			runningDCGScore = 0;
			for (int i = 0; i < relevanceScores.size(); i++) {
				runningDCGScore += getDGScore(i, relevanceScores.get(i));
				idealDCGScores.add(runningDCGScore);
			}
			if (idealDCGScores.get(idealDCGScores.size() - 1) != 0) {				
				ndcgScore = dcgScores.get(dcgScores.size() - 1) / idealDCGScores.get(idealDCGScores.size() - 1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ndcgScore;
	}
	
	private String getComparableUrl(String url) {
		url = url.toLowerCase();
//		System.out.println(url);
		url = url.replaceAll("https://", "").replaceAll("http://", "").replaceAll("index.html", "").replaceAll("index.jsp", "").replaceAll("index.php", "").replaceAll("index", "");
//		System.out.println(url);
		return url;
	}

	public static void main(String[] args) {
		try {
			String[] queries = {"mondego", "machine learning", "software engineering", "security", "student affairs", "graduate courses", "Crista Lopes", "REST", "computer games", "information retrieval"};
//			String[] queries = {"security"};
			for (String query : queries) {
				List<Result> results = (new Search("src/business")).getSearchResultsImproved(query);
//				Result fifthResult = results.remove(4);
//				results.add(5, fifthResult);
				System.out.println(query + ": " + (new GoogleSearchResults()).getNDCGScore(query, results));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
