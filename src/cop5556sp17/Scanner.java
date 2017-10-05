package cop5556sp17;

import java.util.*;

public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			  String text = "";
			int startPos = pos;
			
			while(startPos <pos +length){
				text = text +(chars.charAt(startPos));
				startPos++;
			}
			return text;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			
			int i =0;
			while(i< lineNumber.size() && lineNumber.get(i)<=pos){
				i++;
			}
			int column = pos-lineNumber.get(i-1);
			LinePos l = new LinePos(i-1, column);
			return l;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		public boolean isKind(Kind k){
			
			if(this.kind==k)
				return true;
			else{
				return false;
			}
		}
		

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 * @throws IllegalNumberException 
		 */
		public int intVal() throws NumberFormatException, IllegalNumberException{
			//TODO IMPLEMENT THIS
			int value =0;
			try{
				value = Integer.parseInt(getText());
				
			}
			catch(Exception e){
				
				throw new IllegalNumberException( "Number range out of bound or Illigal number");
			}
			return value;
		}
		
	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		lineNumber = new ArrayList<Integer>();
		lineNumber.add(0);

	}

	public static enum State {
		START("start"), IN_IDENT("ident"), IN_DIGIT("digit"), AFTER_ARROW("-"), AFTER_BAR("|"), AFTER_LT("<"), 
		AFTER_NOT("!"),AFTER_GT(">"),AFTER_EQ("="), AFTER_SLASH("/"), AFTER_STAR("*");
		
		String text;
		
		State(String t){
			this.text= t;
		}
		
	}
	
	
	

	
	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		
		
		int pos = 0; 
		State state = State.START;
		
		int length = chars.length();
		int startPos=0;
		int ch;
		
		while (pos <= length) {
			
			ch = pos < length ? chars.charAt(pos) : -1;
			switch (state) { 
				case START: 
					pos = skipWhiteSpace(pos); 
					ch = pos < length ? chars.charAt(pos) : -1;
					
					startPos = pos;
					
					switch (ch) {
						case -1: {tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}  break;
						case '0': {tokens.add(new Token(Kind.INT_LIT, pos, 1)); pos++;}  break;
						case ';': {tokens.add(new Token(Kind.SEMI, pos, 1)); pos++;}  break;
						case ',': {tokens.add(new Token(Kind.COMMA, pos, 1)); pos++;}  break;
						case '(': {tokens.add(new Token(Kind.LPAREN, pos, 1)); pos++;}  break;
						case ')': {tokens.add(new Token(Kind.RPAREN, pos, 1)); pos++;}  break;
						case '{': {tokens.add(new Token(Kind.LBRACE, pos, 1)); pos++;}  break;
						case '}': {tokens.add(new Token(Kind.RBRACE, pos, 1)); pos++;}  break;
						case '&': {tokens.add(new Token(Kind.AND, pos, 1)); pos++;}  break;
						case '+': {tokens.add(new Token(Kind.PLUS, pos, 1)); pos++;}  break;
						case '%': {tokens.add(new Token(Kind.MOD, pos, 1)); pos++;}  break;
						case '*': {tokens.add(new Token(Kind.TIMES, pos, 1)); pos++;}  break;
						case '/': {state = State.AFTER_SLASH;pos++;}  break;
						case '-': {state = State.AFTER_ARROW;pos++;}  break;
						case '|': {state = State.AFTER_BAR;pos++;}  break;
						case '<': {state = State.AFTER_LT;pos++;}  break;
						case '>': {state = State.AFTER_GT;pos++;}  break;
						case '=': {state = State.AFTER_EQ;pos++;}  break;
						case '!': {state = State.AFTER_NOT;pos++;}  break;
						case '\n': {lineNumber.add(pos+1);pos++;}  break;
						case '\t': {pos++;} break;
						case '\r': {pos++;} break;
						default: {
							
							if (Character.isDigit(ch)) {state = State.IN_DIGIT;pos++;} 
							else if (Character.isJavaIdentifierStart(ch)) {
								state = State.IN_IDENT;pos++; } 
							else {throw new IllegalCharException( "illegal char " +ch+" at pos "+pos); }
						}
					}
					
					break; 
				case IN_DIGIT:
					if (Character.isDigit(ch)) {
						pos++;
					} else {
						
						 	String text = "";
							int temp = startPos;
							
							while(temp <pos){
								text = text +(chars.charAt(temp));
								temp++;
							}
							try{
								Integer.parseInt(text);
								
							}
							catch(Exception e){
								throw new IllegalNumberException( "Number larger than Integer at pos "+pos); 
								
							};
							
							
						tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos)); state = State.START;
					}
					break;
				case IN_IDENT:
					if (Character.isJavaIdentifierPart(ch)) {
						pos++;
					} else {
						
						    // text from token
						    String text = "";
							int temp = startPos;
							
							while(temp <pos){
								text = text +(chars.charAt(temp));
								temp++;
							}
							
							
						switch(text){
						
							case "integer":
								tokens.add(new Token(Kind.KW_INTEGER, startPos, pos - startPos)); state = State.START;
							    break;
							case "boolean":
								tokens.add(new Token(Kind.KW_BOOLEAN, startPos, pos - startPos)); state = State.START;
								break;
							case "image":
								tokens.add(new Token(Kind.KW_IMAGE, startPos, pos - startPos)); state = State.START;
								break;
							case "url":
								tokens.add(new Token(Kind.KW_URL, startPos, pos - startPos)); state = State.START;
								break;	
							case "file":
								tokens.add(new Token(Kind.KW_FILE, startPos, pos - startPos)); state = State.START;
								break;	
							case "frame":
								tokens.add(new Token(Kind.KW_FRAME, startPos, pos - startPos)); state = State.START;
								break;	
							case "while":
								tokens.add(new Token(Kind.KW_WHILE, startPos, pos - startPos)); state = State.START;
								break;
							case "if":
								tokens.add(new Token(Kind.KW_IF, startPos, pos - startPos)); state = State.START;
								break;
							case "true":
								tokens.add(new Token(Kind.KW_TRUE, startPos, pos - startPos)); state = State.START;
								break;
							case "false":
								tokens.add(new Token(Kind.KW_FALSE, startPos, pos - startPos)); state = State.START;
								break;
							case "blur":
								tokens.add(new Token(Kind.OP_BLUR, startPos, pos - startPos)); state = State.START;
								break;
							case "gray":
								tokens.add(new Token(Kind.OP_GRAY, startPos, pos - startPos)); state = State.START;
								break;
							case "convolve":
								tokens.add(new Token(Kind.OP_CONVOLVE, startPos, pos - startPos)); state = State.START;
								break;
							case "screenheight":
								tokens.add(new Token(Kind.KW_SCREENHEIGHT, startPos, pos - startPos)); state = State.START;
								break;
							case "screenwidth":
								tokens.add(new Token(Kind.KW_SCREENWIDTH, startPos, pos - startPos)); state = State.START;
								break;
							case "width":
								tokens.add(new Token(Kind.OP_WIDTH, startPos, pos - startPos)); state = State.START;
								break;
							case "height":
								tokens.add(new Token(Kind.OP_HEIGHT, startPos, pos - startPos)); state = State.START;
								break;
							case "xloc":
								tokens.add(new Token(Kind.KW_XLOC, startPos, pos - startPos)); state = State.START;
								break;
							case "yloc":
								tokens.add(new Token(Kind.KW_YLOC, startPos, pos - startPos)); state = State.START;
								break;
							case "hide":
								tokens.add(new Token(Kind.KW_HIDE, startPos, pos - startPos)); state = State.START;
								break;
							case "show":
								tokens.add(new Token(Kind.KW_SHOW, startPos, pos - startPos)); state = State.START;
								break;
							case "move":
								tokens.add(new Token(Kind.KW_MOVE, startPos, pos - startPos)); state = State.START;
								break;
							case "sleep":
								tokens.add(new Token(Kind.OP_SLEEP, startPos, pos - startPos)); state = State.START;
								break;
							case "scale":
								tokens.add(new Token(Kind.KW_SCALE, startPos, pos - startPos)); state = State.START;
								break;
							case "eof":
								tokens.add(new Token(Kind.EOF, startPos, pos - startPos)); state = State.START;
								break;
							default:{
								tokens.add(new Token(Kind.IDENT, startPos, pos - startPos)); state = State.START;
							}
						}
							
					
					}	
					break; 
				default:  assert false; 
				
				case AFTER_ARROW:
					
					if(pos < chars.length() && chars.charAt(pos)=='>'){
						tokens.add(new Token(Kind.ARROW, startPos,2)); state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.MINUS, startPos,1)); state = State.START;
					}
					break;
					
				case AFTER_BAR:
					
					int flag = 0;
					if(pos<chars.length() && chars.charAt(pos)=='-'){
						if(chars.charAt(++pos)=='>'){
						tokens.add(new Token(Kind.BARARROW, startPos,3)); state = State.START;
						pos++;
						}
						else{
							pos--;
							flag=1;
						}
					}
					if(chars.charAt(pos-1)=='|'|| flag==1){
						tokens.add(new Token(Kind.OR, startPos,1)); state = State.START;
					}
					
					break;
					
				case AFTER_LT:
					
					if(pos<chars.length() && chars.charAt(pos)=='='){
						tokens.add(new Token(Kind.LE, startPos,2)); state = State.START;
						pos++;
					}
					else if(pos<chars.length() && chars.charAt(pos)=='-'){
						tokens.add(new Token(Kind.ASSIGN, startPos,2)); state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.LT, startPos,1)); state = State.START;
					}
					break;
					
				case AFTER_GT:
					
					if(pos<chars.length() && chars.charAt(pos)=='='){
						tokens.add(new Token(Kind.GE, startPos,2)); state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.GT, startPos,1)); state = State.START;
					}
					break;
					
				case AFTER_EQ:
					
					if(pos<chars.length() && chars.charAt(pos)=='='){
						tokens.add(new Token(Kind.EQUAL, startPos,2)); state = State.START;
						pos++;
					}
					else {throw new IllegalCharException( "Missing = after character: " +ch+" at pos: "+pos); }
					
					break;
				
				case AFTER_NOT:
					
					if(pos<chars.length() && chars.charAt(pos)=='='){
						tokens.add(new Token(Kind.NOTEQUAL, startPos,2)); state = State.START;
						pos++;
					}
					else{
						tokens.add(new Token(Kind.NOT, startPos,1)); state = State.START;
					}
					break;
					
				case AFTER_SLASH:
					
					if(chars.charAt(pos) == '*'){
						pos++;
						int forWhile = pos;
						
						
						while(!(chars.substring(forWhile,(forWhile+2)).equals("*/"))){
							
								if((forWhile+2)==length){
									//throw new IllegalCharException( "illegal char " +ch+" at pos "+pos);
									break;
								}
								if(chars.charAt(forWhile)=='\n'){
									lineNumber.add(forWhile+1);
								}
								pos++;
								forWhile = pos;
						}
						pos=pos+2;	
						state = State.START;
					}
					else{
						tokens.add(new Token(Kind.DIV, startPos,1)); state = State.START;
					}
					break;
					
				/*case AFTER_STAR:
					
					if(chars.charAt(pos) == '/'){
						throw new IllegalCharException( "illegal char " +ch+" at pos "+pos);
					}
					else{
						tokens.add(new Token(Kind.TIMES, pos, 1)); 
						pos++;
						state = State.START;
					}
					*/
			}
		}
		
		return this;  
	}

	public int skipWhiteSpace(int pos){
		
		if(pos== chars.length()){
			return pos;
		}
		
		while(chars.charAt(pos)==' '){
			pos++;
			if(chars.length()==pos){
				break;
			}
		}
		
		return pos;
	}

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	final ArrayList<Integer> lineNumber ;
	
	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}


	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
	}
	
	

}
