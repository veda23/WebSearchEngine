package indexer;

import java.util.List;

/**
 * This class encapsulates information about the occurrence of a term in a page.
 * There will be one instance of this for every valid page-term pair. 
 * @author tanoojp
 *
 */
public class TermOccurrence {
	int termFrequency;
	int pageID;
	List<Integer> positions;
	List<HTMLTag> tags;
	
	public int getTermFrequency() {
		return termFrequency;
	}
	public void setTermFrequency(int termFrequency) {
		this.termFrequency = termFrequency;
	}
	public int getPageID() {
		return pageID;
	}
	public void setPageID(int pageID) {
		this.pageID = pageID;
	}
	public List<Integer> getPositions() {
		return positions;
	}
	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}
	public List<HTMLTag> getTags() {
		return tags;
	}
	public void setTags(List<HTMLTag> tags) {
		this.tags = tags;
	}
}