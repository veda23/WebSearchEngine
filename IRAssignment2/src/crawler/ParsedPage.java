package crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParsedPage {
	
	String url;
	String content;
	String htmlContent;
	String subdomain;
	int noOfWords;
	Set<String> outGoingURLs;
	Map<String, String> metaTags;
	
	public Map<String, String> getMetaTags() {
		return metaTags;
	}
	public void setMetaTags(Map<String, String> metaTags) {
		this.metaTags = metaTags;
	}
	HashMap<String, Set<String>> tagValues = new HashMap<>();
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSubdomain() {
		return subdomain;
	}
	public void setSubdomain(String subdomain) {
		this.subdomain = subdomain;
	}
	public int getNoOfWords() {
		return noOfWords;
	}
	public void setNoOfWords(int noOfWords) {
		this.noOfWords = noOfWords;
	}
	public Set<String> getOutGoingURLs() {
		return outGoingURLs;
	}
	public void setOutGoingURLs(Set<String> outGoingURLs) {
		this.outGoingURLs = outGoingURLs;
	}
	public HashMap<String, Set<String>> getTagValues() {
		return tagValues;
	}
	public void setTagValues(HashMap<String, Set<String>> tagValues) {
		this.tagValues = tagValues;
	}
	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	public String getHtmlContent() {
		return htmlContent;
	}
	
}
