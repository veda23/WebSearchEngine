package business;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Token {

	HashSet<String> stopWords;
	
	public void print(List<String> list1)
	{
		for(String s1:list1)
		{
		System.out.println(s1);
		}
	}
	
	public ArrayList<String> tokenizeFile(String s){
		ArrayList<String> list1=new ArrayList<>(); 	
	try{
					 
		
		//String s = null;
		BufferedReader bf=new BufferedReader(new StringReader(s));
	
		String line1=bf.readLine();
		if(line1==null)
		{
			return list1;
		}
	
		for(String word: line1.split("\\s"))//include all alphabets [a-z],[A-Z],numbers 0-9,exclude underscore
		{
			word = word.replaceAll("[^a-z0-9]+", "");
			word = word.toLowerCase();
			if (!(word.trim().isEmpty())&& !(word.length()==1) && !stopWords.contains(word) )//exclude whitespaces after '.'
				list1.add(word);
		}
		
		
		bf.close();
	}
	catch(IOException e)
	{
		e.printStackTrace();
	}
	
	return list1;
	}

	Token(String stopWordsFileDirectory) throws FileNotFoundException
	{
		stopWords=new HashSet<>();
		BufferedReader bf=new BufferedReader(new FileReader(stopWordsFileDirectory + "\\stopwords.txt"));
		String line1;
		try {
			while((line1=bf.readLine())!=null){
				for(String s1:line1.split("\\W|\\_")){
					if(s1==null)
						continue;
					s1 = s1.toLowerCase();
					if (!(s1.trim().isEmpty()))//exclude whitespaces after '.'
						stopWords.add(s1);
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	}

}
