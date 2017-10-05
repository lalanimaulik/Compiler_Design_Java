package cop5556sp17;

import java.util.*;

import org.junit.experimental.max.MaxCore;

import cop5556sp17.AST.Dec;

public class SymbolTable {
	
	
	public class hashmap{
		
		String key;
		Dec d;
		hashmap next ;
		int scope;
		
		hashmap(String key, Dec d, int scope){
			
			this.key = key;
			this.d = d;
			this.scope = scope;
			this.next = null;
		}
		
		public int getScope() {
			return scope;
		}
	}
	
	HashMap<String , hashmap> lb = new HashMap<String,hashmap>();
 	
	Stack<Integer> s = new Stack<Integer>();
	int nextScope = 1;
	
	int currentScope = 0;
	
	
	//TODO  add fields

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		currentScope  = nextScope;
		nextScope=nextScope+1;
		s.push(currentScope);
		//TODO:  IMPLEMENT THIS
	}
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){

		
		
	/*	int maxScope = s.peek();
		
		for(String key: lb.keySet()){
			hashmap map = lb.get(key);
			
			if(map.scope==maxScope){
				lb.replace(key, map.next);
			}
			while(map.next!=null){
				
				if(map.next.scope==maxScope){
					map.next = map.next.next;
				}
				map = map.next;
			}		
			
		}
*/		
		
		
		s.pop();
		if(s.size()>0)
			currentScope = s.peek();
		else 
			currentScope =0;
		
		//TODO:  IMPLEMENT THIS
	}
	
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		
		hashmap newNode = new hashmap(ident, dec, currentScope);
		hashmap existingNode = null;
		
		if(lb.containsKey(ident)){
			
			existingNode = lb.get(ident);
			while(existingNode.next!=null){
				
				existingNode = existingNode.next;
			}
			existingNode.next= newNode;
			lb.put(ident, existingNode);
			
		}
		else{
			lb.put(ident, newNode);
		}
		
		return true;
	}
	
	public hashmap lookup(String ident){
		
		hashmap existing = null;
		hashmap maxDec = null ;
		
		if(lb.containsKey(ident)){
			
			existing = lb.get(ident);
			
			int maxScope = existing.scope;
			maxDec = existing;
			
			while(existing.next!=null){
				
				existing = existing.next;
				
				if(existing.scope > maxScope){
					
					maxScope = existing.scope;
					maxDec = existing;
				}
			}
		}
		
		//TODO:  IMPLEMENT THIS
		return maxDec;
	}
		
	public SymbolTable() {
		s.push(currentScope);
		//TODO:  IMPLEMENT THIS
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}
	
	


}
