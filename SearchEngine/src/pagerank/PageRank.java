package pagerank;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import business.DescendingValueComparatorDouble;

class PageRank {
	HashMap<String,Page> pageGraph;
	HashMap<String,Double> previousRanks;
	DatabaseConnector db;
	public static final double D = 0.85;

	public PageRank() {
		db = new DatabaseConnector();
		pageGraph = db.getPageGraph();
		previousRanks = new HashMap<>();
	}

	public void doOneIteration()
	{
		for(Page p: pageGraph.values())
		{
			Double previousRank = previousRanks.get(p.getUrl());
			double noOfOutgoingLinks = p.outGoingLinks.size();
			previousRank = previousRank==null? noOfOutgoingLinks:previousRank;
			double rankComponent = previousRank/noOfOutgoingLinks;
			for(String outgoing: p.outGoingLinks)
			{
				Page outPage = pageGraph.get(outgoing);
				if(outPage!=null)
				{
					outPage.setRank(outPage.getRank()+rankComponent); 
				}
			}
		}
		for(Page p: pageGraph.values())
		{
			double temp = new Double(p.rank);
			temp = (1-D) + D*temp;
			previousRanks.put(p.Url, temp);
			p.rank = 0;
		}
	}

	public void save()
	{
		db.savePageRanks(previousRanks);
	}
	
	public static void main(String[] args) {
		generatePageRanksTable(500);
//		PageRank p = new PageRank();
//		System.out.println(new Date());
//		HashMap<String, Double> ranks = p.db.getPageRanks();
//		System.out.println(new Date());
	}

	private static void generatePageRanksTable(int iterations) {
		System.out.println(new Date());
		PageRank p = new PageRank();
		System.out.println(new Date());
		for(int i = 0; i<iterations; i++) 
		{
			if(i%100==0)
				System.out.println(i);
			p.doOneIteration();
		}
		HashMap<String, Double> previousRanksMap = p.previousRanks;
		String[] a = new String[previousRanksMap.size()];
		a = previousRanksMap.keySet().toArray(a);
		Arrays.sort(a, new DescendingValueComparatorDouble<>(previousRanksMap));
		for(int i = 0; i<250; i++)
		{
			System.out.println(a[i] + " : "+previousRanksMap.get(a[i]));
		}
		System.out.println(new Date());
		p.save();
	}
}
