package linkanalysis;

import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.*;

public class Token {
	
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
	    if(line1==null){
	    	return list1;
	    }
		for(String s1: line1.split("\\W|\\_"))//include all alphabets [a-z],[A-Z],numbers 0-9,exclude underscore
		{
			s1 = s1.toLowerCase();
			if (!(s1.trim().isEmpty()))//exclude whitespaces after '.'
				list1.add(s1);
		}
	    //}
		
		bf.close();
	}
	catch(IOException e)
	{
		e.printStackTrace();
	}
	
	return list1;
	}
}
