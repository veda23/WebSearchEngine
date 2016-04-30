package linkanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TagMap {
	
	String term;
	HashSet<Integer> title=new HashSet();
	HashSet<Integer> meta=new HashSet();
	HashSet<Integer>heading1=new HashSet();
	HashSet<Integer> heading2=new HashSet();
	HashSet<Integer> heading3=new HashSet();
	HashSet<Integer> anchor=new HashSet();
	HashMap<Integer ,Integer> anchor_ref=new HashMap<>();
	
	
	HashMap<Integer,Integer> getAnchorUrl(){
		return anchor_ref;
	}
	HashSet<Integer> getTitle(){
		return title;
	}

	HashSet<Integer> getMeta(){
		return meta;
	}
	
	HashSet<Integer> getAnchor(){
		return anchor;
	}
	
	HashSet<Integer> getHeading1(){
		return heading1;
	}
	
	HashSet<Integer> getHeading2(){
		return heading1;
	}
	
	HashSet<Integer> getHeading3(){
		return heading1;
	}
	
}
