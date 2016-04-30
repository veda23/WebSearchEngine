package pagerank;

import java.util.Set;

public class Page {
	String Url;
	Set<String> outGoingLinks;
	double rank;
	
	public Page(String url, Set<String> outGoingLinks) {
		super();
		Url = url;
		this.outGoingLinks = outGoingLinks;
		this.rank = 0;
	}

	public String getUrl() {
		return Url;
	}

	public void setUrl(String url) {
		Url = url;
	}

	public Set<String> getOutGoingLinks() {
		return outGoingLinks;
	}

	public void setOutGoingLinks(Set<String> outGoingLinks) {
		this.outGoingLinks = outGoingLinks;
	}

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}
}
