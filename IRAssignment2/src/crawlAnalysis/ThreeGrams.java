package crawlAnalysis;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Class for Part C of the assignment, related to computing the frequency of each 3-gram in
 * a list of tokens.
 * @author tanoojp
 *
 */
public class ThreeGrams {

	HashMap<String,Integer> counts;
	
	public ThreeGrams() {
		this.counts = new HashMap<>();
	}

	/**
	 * Computes the frequency of each 3-gram in the list provided as a parameter
	 * and returns a HashMap<String, Integer> where each key->value pair is 
	 * 3-gram->frequency_of_3-gram.
	 * 
	 * Running time: O(n log n) where n: number of tokens in the list.
	 * @param tokens
	 * @return HashMap<String, Integer> containing the frequency of each 3-gram.
	 * @return
	 */
	void computeThreeGramFrequencies(List<String> tokens)
	{
		for(int i = 0; i<tokens.size()-3; i++)
		{
			StringBuilder str = new StringBuilder();
			str.append(tokens.get(i));
			str.append(" ");
			str.append(tokens.get(i+1));
			str.append(" ");
			str.append(tokens.get(i+2));
			String s = str.toString();
			if(!counts.containsKey(s))
			{
				counts.put(s, 1);
			}
			else
			{
				counts.put(s, counts.get(s)+1);
			}
		}
	}
	
	/**
	 * Prints out the 3-grams and their frequencies in descending order of frequency.
	 * It makes use of a {@link DescendingValueComparator} for sorting the 3-grams based on
	 * their associated frequency values from the map.
	 * 
	 * Running time: O(n log n) where n: number of unique 3-grams in the map.
	 * @param counts
	 * @param counts
	 */
	public void print()
	{
		String[] keys = new String[counts.size()];
		keys = counts.keySet().toArray(keys);
		Arrays.sort(keys, new DescendingValueComparator<String>(counts));
		for(int i = 0; i<20; i++)
		{
			System.out.print(counts.get(keys[i])+ "\t");
			System.out.println(keys[i]);
		}
	}
}
