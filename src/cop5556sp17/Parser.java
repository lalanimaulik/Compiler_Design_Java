package cop5556sp17;

import java.util.ArrayList;
import java.util.List;


import org.junit.experimental.theories.internal.Assignments;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.Scanner.*;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 * @throws IllegalNumberException 
	 * @throws NumberFormatException 
	 */
	ASTNode parse() throws SyntaxException, NumberFormatException, IllegalNumberException {
		ASTNode e = program();
		matchEOF();
		return e;
	}

	Expression expression() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken = t;
		
		Expression e1=null;
		Expression e2=null;
		
		e1 = term();
		
		while(t.isKind(LT)|| t.isKind(LE)|| t.isKind(GT)|| t.isKind(GE)|| t.isKind(EQUAL)|| t.isKind(NOTEQUAL)){
			Token op = t;
			relOp();
			e2 = term();
			
			e1=new BinaryExpression(firstToken,e1,op,e2);
		}
		
		return e1 ;
	}

	Expression term() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken = t;
		
		Expression e1=null;
		Expression e2=null;
		
		e1 = elem();
		
		while(t.isKind(PLUS)|| t.isKind(MINUS) || t.isKind(OR)){
			Token op = t;
			weakOp();
			e2 = elem();
			
			e1 = new BinaryExpression(firstToken,e1,op,e2);
		}
		return e1;
	}

	Expression elem() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken = t;
		
		Expression e1=null;
		Expression e2=null;
		
		e1 = factor();
		
		while(t.isKind(TIMES)|| t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)){
			Token op = t;
			strongOp();
			
			e2 =factor();
			e1 = new BinaryExpression(firstToken,e1,op,e2);
		}
		return e1 ;
	}

	Expression factor() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken = t;
		
		Expression e1=null;
		
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			e1=new IdentExpression(firstToken);
			consume();
		}
			break;
		case INT_LIT: {
			e1=new IntLitExpression(firstToken);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e1=new BooleanLitExpression(firstToken);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e1=new ConstantExpression(firstToken);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e1 = expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor at line "+ t.getLinePos());
		}
		return e1;
	}

	Block block() throws SyntaxException, NumberFormatException, IllegalNumberException {
	
		Token firstToken = t;
		Dec d = null;
		Statement st = null;
		ArrayList<Dec> dec=new ArrayList<Dec>();
		ArrayList<Statement> stmt=new ArrayList<Statement>();
		
		if(t.isKind(LBRACE)){
			consume();
		}
		else{
			throw new SyntaxException("illegal factor");
		}
		
		while(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN)|| t.isKind(KW_IMAGE)|| t.isKind(KW_FRAME)|| t.isKind(KW_WHILE)
				|| t.isKind(KW_IF) || t.isKind(OP_SLEEP) || t.isKind(IDENT) || t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || 
				t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)|| t.isKind(KW_SHOW)|| t.isKind(KW_HIDE) ||
				t.isKind(KW_MOVE)|| t.isKind(KW_XLOC)|| t.isKind(KW_YLOC) ){
			
			if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN)|| t.isKind(KW_IMAGE)|| t.isKind(KW_FRAME)){
				d = dec();
				dec.add(d);
			}
			else{
				st =statement();
				stmt.add(st);
			}
		}
		Block b = new Block(firstToken,dec,stmt);
		
		match(RBRACE);
		return b;
	}

	Program program() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken = t;
		Block b =null;
		ParamDec pd;
		ArrayList<ParamDec> pds  = new ArrayList<ParamDec>();
		
		if(t.isKind(IDENT) && scanner.peek().isKind(LBRACE)){
			consume();
			b = block();
		}
		else if(t.isKind(IDENT) && (scanner.peek().isKind(KW_URL) ||scanner.peek().isKind(KW_FILE) 
				|| scanner.peek().isKind(KW_INTEGER)|| scanner.peek().isKind(KW_BOOLEAN))){
			consume();
			pd = paramDec();
			pds.add(pd);
			while(t.isKind(COMMA)){
				consume();
				pd = paramDec();
				pds.add(pd);
			}
			b = block();
		}
		else{
			throw new SyntaxException("Missing identifier at line "+ t.getLinePos());
		}
		Program p = new Program(firstToken,pds,b);
		return p;
	}

	ParamDec paramDec() throws SyntaxException {
		
		Token firstToken = t;
		
		ParamDec pd =null;
		
		Kind kind = t.kind;
		switch (kind) {
		case KW_URL: {
			
			consume();
			pd = new ParamDec(firstToken, t);
		}
			break;
		case KW_FILE: {
			
			consume();
			pd = new ParamDec(firstToken, t);
		}
			break;
		
		case KW_INTEGER: {
			consume();
			pd = new ParamDec(firstToken, t);
		}
			break;
			
		case KW_BOOLEAN: {
			consume();
			pd = new ParamDec(firstToken, t);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal param at line "+ t.getLinePos());
		}
		if(t.isKind(IDENT)){
			
			Token iden = t;
			consume();
			pd = new ParamDec(firstToken, iden);
		}
		else{
			throw new SyntaxException("Missing identifier at line "+ t.getLinePos());
		}
		return pd;
	}

	Dec dec() throws SyntaxException {
		
		Dec d = null;
		
		Token firstToken = t;
		Kind kind = t.kind;
		switch (kind) {
		case KW_INTEGER: {
			
			consume();
			d = new Dec(firstToken, t);
		}
			break;
		case KW_BOOLEAN: {
			
			consume();
			d = new Dec(firstToken, t);
		}
			break;
		
		case KW_IMAGE: {
			
			consume();
			d = new Dec(firstToken, t);
		}
			break;
			
		case KW_FRAME: {
			
			consume();
			d = new Dec(firstToken, t);
		}
			break;
		default:
			throw new SyntaxException("illegal declaration at line "+ t.getLinePos());
		}
		if(t.isKind(IDENT)){
			
			consume();
			
		}
		else{
			throw new SyntaxException("Missing identifier at line "+ t.getLinePos());
		}
		
		return d;
	}

	Statement statement() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken=t;
		Statement st = null;
		
		if(t.isKind(OP_SLEEP)){
			consume();
			st = new SleepStatement(firstToken,expression());
			
			if(t.isKind(SEMI)){
				consume();
			}
			else{
				throw new SyntaxException("Missing semi colon at line "+ t.getLinePos());
			}
		}
		else if(t.isKind(KW_WHILE)){
			st =whileStatement();
		}
		else if(t.isKind(KW_IF)){
			st = ifStatement();
		}
		else if(t.isKind(IDENT) && scanner.peek().isKind(ASSIGN)){
			
			st =assign();
			if(t.isKind(SEMI)){
				consume();
			}
			else{
				throw new SyntaxException("Missing semi colon at line "+ t.getLinePos());
			}
		}
		else if(t.isKind(IDENT) || t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE) || 
				t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)|| t.isKind(KW_SHOW)|| t.isKind(KW_HIDE) ||
				t.isKind(KW_MOVE)|| t.isKind(KW_XLOC)|| t.isKind(KW_YLOC)){
			st=chain();
			if(t.isKind(SEMI)){
				consume();
			}
			else{
				throw new SyntaxException("Missing semi colon at line "+ t.getLinePos());
			}
		}
		else{
			throw new SyntaxException("Missing identifier at line "+ t.getLinePos());
		}
		return st;
	}

	Chain chain() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken =t;
		
		Chain c =chainElem();
		
		Token a = arrowOp();
		ChainElem c2 = chainElem();
		
		c= new BinaryChain(firstToken,c, a, c2);
		
		while(t.isKind(ARROW) || t.isKind(BARARROW)){
				
			a = arrowOp();
			c2 = chainElem();
			
			c = new BinaryChain(firstToken,c,a,c2);
		}
		return c;
	}

	ChainElem chainElem() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Kind kind = t.kind;
		Token firstToken = t;
		ChainElem ce =null;
		
		switch (kind) {
		case IDENT: {
			
			ce = new IdentChain(firstToken);
			consume();
			
		}
			break;
		case OP_GRAY:
		case OP_CONVOLVE:
		case OP_BLUR: {
			
			Token f1 = filterOp();
			
			Tuple tp =arg();
			
			 ce = new FilterOpChain(firstToken, tp);
		}
			break;
		case KW_SHOW:
		case KW_HIDE:
		case KW_MOVE:
		case KW_XLOC:
		case KW_YLOC:{
			Token fr = frameOp();
			
			Tuple tp =arg();
		
			 ce = new FrameOpChain(firstToken, tp);
		}
			break;
			
		case OP_WIDTH:
		case OP_HEIGHT:
		case KW_SCALE: {
			Token io = imageOp();
			
			Tuple tp =arg();
			
			 ce = new ImageOpChain(firstToken, tp);
		}
			break;
		default:
			throw new SyntaxException("illegal chain elements at line "+ t.getLinePos());
		}
		return ce;
	}

	Tuple arg() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken = t;
		List<Expression> argsList =new ArrayList<Expression>();
		Expression x=null;
		if(t.isKind(LPAREN)){
			
			consume();
			x =expression();
			
			argsList.add(x);
			
			while(t.isKind(COMMA)){
				consume();
				x = expression();
				argsList.add(x);
			}
			match(RPAREN);
		}
		
		
		
		return new Tuple(firstToken, argsList);
	}

	WhileStatement whileStatement() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken =t;
		Expression x =null;
		Block b =null;
		WhileStatement ws = null;
		
		if(t.isKind(KW_WHILE)){
			consume();
		}
		else{
			throw new SyntaxException("Missing while keyword");
		}
		
		if(t.isKind(LPAREN)){
			consume();
			x =expression();
			match(RPAREN);
			b =block();
		}
		else{
			throw new SyntaxException("Missing bracket at line"+ t.getLinePos());
		}
		ws = new WhileStatement(firstToken, x, b);
		return ws;
	}
	
	IfStatement ifStatement() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken = t;
		Expression x =null;
		Block b =null;
		IfStatement is = null;
		
		if(t.isKind(KW_IF)){
			consume();
		}
		else{
			throw new SyntaxException("Missing bracket at line"+ t.getLinePos());
		}
		
		if(t.isKind(LPAREN)){
			consume();
			x = expression();
			match(RPAREN);
			b = block();
		}
		else{
			throw new SyntaxException("Missing bracket at line"+ t.getLinePos());
		}
		is = new IfStatement(firstToken, x, b);
		return is;
	}

	AssignmentStatement assign() throws SyntaxException, NumberFormatException, IllegalNumberException {
		
		Token firstToken =t;
		IdentLValue e=new IdentLValue(firstToken);
		Expression x =null;
		AssignmentStatement as =null;
		if(t.isKind(IDENT)){
			consume();
		}
		else{
			throw new SyntaxException("Missing bracket at line"+ t.getLinePos());
		}
		if(t.isKind(ASSIGN)){
			consume();
		}
		else{
			throw new SyntaxException("Missing expression at line"+ t.getLinePos());
		}
		x= expression();
		
		as =  new AssignmentStatement(firstToken,e,x);
		
		return as;
	}

	Token arrowOp() throws SyntaxException {
		
		Token firstToken = t;
		if(t.isKind(ARROW)){
			consume();
		}
		else if(t.isKind(BARARROW)){
			consume();
		}
		else{
			throw new SyntaxException("Missing arrow at line"+ t.getLinePos());
		}
		return firstToken;
	}

	Token filterOp() throws SyntaxException {
		
		Kind kind = t.kind;
		Token firstToken = t;
		switch (kind) {
		case OP_BLUR: {
			consume();
		}
			break;
		case OP_GRAY: {
			consume();
		}
			break;
		
		case OP_CONVOLVE: {
			consume();
		}
			break;
		
		default:
			throw new SyntaxException("illegal filter operation at line "+ t.getLinePos());
		}
		return firstToken;
	}
	
	Token frameOp() throws SyntaxException {
		
		Token firstToken = t;
		Kind kind = t.kind;
		switch (kind) {
		case KW_SHOW: {
			consume();
		}
			break;
		case KW_HIDE: {
			consume();
		}
			break;
		
		case KW_MOVE: {
			consume();
		}
			break;
			
		case KW_XLOC: {
			consume();
		}
			break;
			
		case KW_YLOC: {
			consume();
		}
			break;
			
		default:
			throw new SyntaxException("illegal frame operator at line " + t.getLinePos());
		}
		return firstToken;
	}
	
	Token imageOp() throws SyntaxException {
		
		Token firstToken= t;
		Kind kind = t.kind;
		switch (kind) {
		case OP_WIDTH: {
			consume();
		}
			break;
		case OP_HEIGHT: {
			consume();
		}
			break;
		
		case KW_SCALE: {
			consume();
		}
			break;
			
		default:
			throw new SyntaxException("illegal image op at line" + t.getLinePos());
		}
		return firstToken;
	}
	
	Token strongOp() throws SyntaxException{
		
		Token firstToken=t;
		Kind kind = t.kind;
		switch (kind) {
		case TIMES: {
			consume();
		}
			break;
		case DIV: {
			consume();
		}
			break;
		
		case AND: {
			consume();
		}
			break;
			
		case MOD: {
			consume();
		}
			break;
		
		default:
			throw new SyntaxException("illegal strong op at line" + t.getLinePos());
		}
		return firstToken;
	}
	
	Token weakOp() throws SyntaxException{
		
		Token firstToken=t;
		Kind kind = t.kind;
		switch (kind) {
		case PLUS: {
			consume();
		}
			break;
		case MINUS: {
			consume();
		}
			break;
		
		case OR: {
			consume();
		}
			break;
			
		default:
			throw new SyntaxException("illegal weak op at line" + t.getLinePos());
		}
		return firstToken;
	}
	Token relOp() throws SyntaxException{
		
		Token firstToken=t;
		Kind kind = t.kind;
		switch (kind) {
		case LT: {
			consume();
		}
			break;
		case LE: {
			consume();
		}
			break;
		
		case GT: {
			consume();
		}
			break;
			
		case GE: {
			consume();
		}
			break;
			
		case EQUAL: {
			consume();
		}
			break;
			
		case NOTEQUAL: {
			consume();
		}
			break;
			
		default:
			throw new SyntaxException("illegal relative operator at line " + t.getLinePos());
		}
		return firstToken;
	}
	
	
	
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	//private Token match(Kind... kinds) throws SyntaxException {
	//	// TODO. Optional but handy
//		return null; //replace this statement
//	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
