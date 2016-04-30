package crawlAnalysis;
import java.util.*;
import java.io.*;

public class ComputeWordFreq {
	
	
	public void print(HashMap<String, Integer> hash1) {
		HashMap<String, Integer> hash2 = new HashMap<>();
		hash2 = sortValue(hash1);
		Set set2 = hash2.entrySet();
		Iterator iterator2 = set2.iterator();
		int i=1;
		while (iterator2.hasNext() && i<=500) {//iterate through map hash2 entries
//		while(){//first 500 entries	
			Map.Entry me2 = (Map.Entry) iterator2.next();
			System.out.print(me2.getKey() + ": ");
			System.out.println(me2.getValue());
			i++;
		}
		//}
		}
	

	@SuppressWarnings("unchecked")
	public HashMap<String, Integer> sortValue(HashMap<String, Integer> map) {
		List<String> list = new LinkedList(map.entrySet());
		// Defined Custom Comparator 
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		// copy the sorted list in HashMap using LinkedHashMap to preserve the insertion order
		HashMap sortedHashMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedHashMap.put(entry.getKey(), entry.getValue());
		}
		return sortedHashMap;
	}

	

	public HashMap<String, Integer> computeWordFrequencies(List<String> list){
		int value = 0;
		String s;
		Iterator<String> e = list.iterator();
		
		
		while (e.hasNext()) {
			s = (String) e.next();
				if (Driver.hash.containsKey(s)) {
					value = Driver.hash.get(s);
					Driver.hash.replace(s, value + 1);
				}
				else {
				Driver.hash.put(s, 1);
				}

		}
		return Driver.hash;
	}
	

	

	public ComputeWordFreq() throws FileNotFoundException {}
	
}
