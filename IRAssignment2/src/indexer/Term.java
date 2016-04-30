package indexer;

import java.util.List;

public class Term {
	final String word;
	List<TermOccurrence> occurrences;
	Double idf;	//This will be generated directly on the DB
	
	Term(String word)
	{
		this.word = word;
	}
	
	public List<TermOccurrence> getOccurrences() {
		return occurrences;
	}
	public void setOccurrences(List<TermOccurrence> occurrences) {
		this.occurrences = occurrences;
	}
	public Double getIdf() {
		return idf;
	}
	public void setIdf(Double idf) {
		this.idf = idf;
	}
	public String getWord() {
		return word;
	}
	
	
}
