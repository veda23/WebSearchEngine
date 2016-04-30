package business;

public class Result {
	
	private String URL;
	private String relevantText;
	private String title;
	
	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getRelevantText() {
		return relevantText;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	Result(String url, String relevantText) {
		this.URL = url;
		this.relevantText = relevantText;
	}
	
	Result(String url, String relevantText, String title) {
		this.URL = url;
		this.relevantText = relevantText;
		this.title = title;
	}

}
