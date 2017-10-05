package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
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
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.omg.CORBA.Current;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import cop5556sp17.SymbolTable.hashmap;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		binaryChain.getE0().visit(this, arg);
		binaryChain.getE1().visit(this, arg);
		Token arrow = binaryChain.getArrow();
		
		Chain c = binaryChain.getE0();
		ChainElem ce = binaryChain.getE1();
		
		
		if(c.getType()==TypeName.URL && arrow.isKind(ARROW) && ce.getType()==TypeName.IMAGE){
			binaryChain.setType(TypeName.IMAGE);
		}
		else if(c.getType()==TypeName.FILE && arrow.isKind(ARROW) && ce.getType()==TypeName.IMAGE){
			binaryChain.setType(TypeName.IMAGE);
		}
		else if(c.getType()==TypeName.FRAME && arrow.isKind(ARROW) && (ce instanceof FrameOpChain) && (ce.firstToken.isKind(KW_XLOC)|| ce.firstToken.isKind(KW_YLOC))){
			binaryChain.setType(TypeName.INTEGER);
		}
		else if(c.getType()==TypeName.FRAME && arrow.isKind(ARROW) && (ce instanceof FrameOpChain) && (ce.firstToken.isKind(KW_SHOW)|| ce.firstToken.isKind(KW_HIDE) || ce.firstToken.isKind(KW_MOVE))){
			binaryChain.setType(TypeName.FRAME);
		}
		else if(c.getType()==TypeName.IMAGE && arrow.isKind(ARROW) && (ce instanceof ImageOpChain) && (ce.firstToken.isKind(OP_WIDTH)|| ce.firstToken.isKind(OP_HEIGHT))){
			binaryChain.setType(TypeName.INTEGER);
		}
		else if(c.getType()==TypeName.IMAGE && arrow.isKind(ARROW) && ce.getType()==TypeName.FRAME){
			binaryChain.setType(TypeName.FRAME);
		}
		else if(c.getType()==TypeName.IMAGE && arrow.isKind(ARROW) && ce.getType()==TypeName.FILE){
			binaryChain.setType(TypeName.NONE);
		}
		else if(c.getType()==TypeName.IMAGE && (arrow.isKind(ARROW) || arrow.isKind(BARARROW)) && (ce instanceof FilterOpChain) && (ce.firstToken.isKind(OP_GRAY)|| ce.firstToken.isKind(OP_BLUR) || ce.firstToken.isKind(OP_CONVOLVE))){
			binaryChain.setType(TypeName.IMAGE);
		}
		else if(c.getType()==TypeName.IMAGE && arrow.isKind(ARROW) && (ce instanceof ImageOpChain) && (ce.firstToken.isKind(KW_SCALE))){
			binaryChain.setType(TypeName.IMAGE);
		}
		else if(c.getType()==TypeName.IMAGE && arrow.isKind(ARROW) && ce instanceof IdentChain && ce.getType()==TypeName.IMAGE ){
			binaryChain.setType(TypeName.IMAGE);
		}
		else if(c.getType()==TypeName.INTEGER && arrow.isKind(ARROW) && ce instanceof IdentChain && ce.getType()==TypeName.INTEGER ){
			binaryChain.setType(TypeName.INTEGER);
		}
		else{
			throw new TypeCheckException(null);
		}
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		TypeName t1 = (TypeName) binaryExpression.getE0().visit(this, arg);
		TypeName t2 = (TypeName) binaryExpression.getE1().visit(this, arg);
		
		Expression e0 = binaryExpression.getE0();
		Expression e1 = binaryExpression.getE1();
		Token op = binaryExpression.getOp();
		
		if(e0.getType() == TypeName.INTEGER && e1.getType()== TypeName.INTEGER && (op.isKind(PLUS) || op.isKind(MINUS))){
			binaryExpression.setType(TypeName.INTEGER);
		}
		else if(e0.getType() == TypeName.IMAGE && e1.getType()==TypeName.IMAGE && (op.isKind(PLUS) || op.isKind(MINUS))){
			binaryExpression.setType(TypeName.IMAGE);
		}
		else if(e0.getType() == TypeName.INTEGER && e1.getType()==TypeName.INTEGER && (op.isKind(TIMES) || op.isKind(DIV) || op.isKind(MOD) || op.isKind(PLUS) || op.isKind(MINUS))){
			binaryExpression.setType(TypeName.INTEGER);
		}
		else if(e0.getType() == TypeName.INTEGER && e1.getType()==TypeName.IMAGE && (op.isKind(TIMES)|| op.isKind(DIV)|| op.isKind(MOD) || op.isKind(PLUS) || op.isKind(MINUS))){
			binaryExpression.setType(TypeName.IMAGE);
		}
		else if(e0.getType() == TypeName.IMAGE && e1.getType()==TypeName.INTEGER && (op.isKind(TIMES)|| op.isKind(DIV)|| op.isKind(MOD) || op.isKind(PLUS) || op.isKind(MINUS))){
			binaryExpression.setType(TypeName.IMAGE);
		}
		else if(e0.getType() == TypeName.INTEGER && e1.getType()==TypeName.INTEGER && (op.isKind(LT) || op.isKind(GT) || op.isKind(LE) || op.isKind(GE))){
			binaryExpression.setType(TypeName.BOOLEAN);
		}
		else if(e0.getType() == TypeName.BOOLEAN && e1.getType()==TypeName.BOOLEAN && (op.isKind(LT) || op.isKind(GT) || op.isKind(LE) || op.isKind(GE) || op.isKind(AND) || op.isKind(OR))){
			binaryExpression.setType(TypeName.BOOLEAN);
		}
		else if((op.isKind(EQUAL)|| op.isKind(NOTEQUAL) ) && e0.getType() ==e1.getType()){
			binaryExpression.setType(TypeName.BOOLEAN);
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		// TODO Auto-generated method stub
		
		ArrayList<Dec> decList = block.getDecs();
		ArrayList<Statement> sts= block.getStatements();
		
		for(int i = 0;i< decList.size();i++){
			decList.get(i).visit(this, arg);
		}
	
		for(int i =0; i<sts.size();i++){
			sts.get(i).visit(this, arg) ;
			
		}
		
		symtab.leaveScope();
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setType(TypeName.BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		filterOpChain.getArg().visit(this, arg);
		Tuple t = filterOpChain.getArg();
		
		if(t.getExprList().size()!=0){
			throw new TypeCheckException(null);
		}
		filterOpChain.setType(IMAGE);
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		
		if(frameOpChain.firstToken.isKind(KW_SHOW) || frameOpChain.firstToken.isKind(KW_HIDE) ){
			
			Tuple t = frameOpChain.getArg();
			
			if(t.getExprList().size()!=0){
				throw new TypeCheckException(null);
			}
			frameOpChain.setType(TypeName.NONE);
		}
		else if(frameOpChain.firstToken.isKind(KW_XLOC) || frameOpChain.firstToken.isKind(KW_YLOC) ){
			
			Tuple t = frameOpChain.getArg();
			
			if(t.getExprList().size()!=0){
				throw new TypeCheckException(null);
			}
			frameOpChain.setType(TypeName.INTEGER);
		}
		else if(frameOpChain.firstToken.isKind(KW_MOVE) ){
			
			Tuple t = frameOpChain.getArg();
			
			if(t.getExprList().size()!=2){
				throw new TypeCheckException(null);
			}
			frameOpChain.setType(TypeName.NONE);
		}
		
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Dec d = symtab.lookup(identChain.firstToken.getText()).d;
		
		if(d==null){
			throw new TypeCheckException(null);
		}
		identChain.setType(d.getType());
		identChain.setD(d);
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec d = symtab.lookup(identExpression.firstToken.getText()).d;
		
		if(d==null){
			throw new TypeCheckException(null);
		}
		identExpression.setType(d.getType());
		identExpression.setDec(d);
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		 ifStatement.getE().visit(this, arg);
		
		 Expression e = ifStatement.getE();
		
		if(e.getType()!= TypeName.BOOLEAN){
			
			throw new TypeCheckException(null);
		}
		ifStatement.getB().visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setType(INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		
		 sleepStatement.getE().visit(this, arg);
		TypeName type = sleepStatement.getE().getType();
		
		if(type!= TypeName.INTEGER){
			
			throw new TypeCheckException(null);
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		whileStatement.getE().visit(this, arg);
		
		Expression e = whileStatement.getE();
		
		if(e.getType()!= TypeName.BOOLEAN){
			
			throw new TypeCheckException(null);
		}
		
		whileStatement.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		
		Type tn = new Type();
		TypeName t = tn.getTypeName(declaration.firstToken);
		declaration.setType(t);
		
		int presentScope = symtab.currentScope;
		
		if(symtab.lookup(declaration.getIdent().getText())!=null){
			
			hashmap prevDeclaration = symtab.lookup(declaration.getIdent().getText());
			
			int scope = prevDeclaration.getScope();
			
			if(scope == presentScope){
				throw new TypeCheckException(null);
			}
			else{
				symtab.insert(declaration.getIdent().getText(), declaration);
			}
		}
		else{
		symtab.insert(declaration.getIdent().getText(), declaration);
		}
		// TODO Auto-generated method stub
		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		
		ArrayList<ParamDec> parasDec = program.getParams();
		for(int i = 0;i< parasDec.size();i++){
			parasDec.get(i).visit(this, arg);
		}
		program.getB().visit(this, arg);
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		assignStatement.getE().visit(this, arg);
		assignStatement.getVar().visit(this, arg); 
		
		
		TypeName id =  (TypeName) assignStatement.getVar().getType();
		TypeName expr =  (TypeName) assignStatement.getE().getType();
		
		if(id!= expr){
			throw new TypeCheckException(null);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		Dec d = symtab.lookup(identX.getText()).d;
	
		if(d==null){
			throw new TypeCheckException(null);
		}
		identX.setType(d.getType());
		identX.setDec(d);
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		
		Type tn = new Type();
		TypeName t = tn.getTypeName(paramDec.firstToken);
		paramDec.setType(t);
		
		int presentScope = symtab.currentScope;
		
		if(symtab.lookup(paramDec.getIdent().getText())!=null){
			
			hashmap prevDeclaration = symtab.lookup(paramDec.getIdent().getText());
			
			int scope = prevDeclaration.getScope();
			
			if(scope == presentScope){
				throw new TypeCheckException(null);
			}
			else{
				symtab.insert(paramDec.getIdent().getText(), paramDec);
			}
		}
		else{
		symtab.insert(paramDec.getIdent().getText(), paramDec);
		}
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setType(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		
		imageOpChain.getArg().visit(this, arg);
		
		if(imageOpChain.firstToken.isKind(OP_WIDTH) || imageOpChain.firstToken.isKind(OP_HEIGHT)){
			
			Tuple t = imageOpChain.getArg();
			
			if(t.getExprList().size()!=0){
				throw new TypeCheckException(null);
				
			}
			imageOpChain.setType(TypeName.INTEGER);
		}
		else if(imageOpChain.firstToken.isKind(KW_SCALE)){
			
			Tuple t = imageOpChain.getArg();
			
			if(t.getExprList().size()!=1){
				throw new TypeCheckException(null);
				
			}
			imageOpChain.setType(TypeName.IMAGE);
		}
		
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		java.util.List<Expression> decList = tuple.getExprList();
		
		for(int i=0;i<decList.size();i++ ){
			
			decList.get(i).visit(this, arg);
			
			if(decList.get(i).getType()!=TypeName.INTEGER){
				
				throw new TypeCheckException(null);
			}
		}
		return null;
	}


}
