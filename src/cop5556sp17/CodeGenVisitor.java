package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;
public class CodeGenVisitor implements ASTVisitor, Opcodes {

	private static final int IMINUS = 0;
	private static final int ITIMES = 0;

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int slotNumber = 1;    //Slot counter for local variables
	int globalParamSlots=0;    //Slot counter for global variable
	
	
	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		
		//visit all the global variables 
		for (ParamDec dec : program.getParams()){
			dec.setSlot(globalParamSlots++);
			cw.visitField(0, dec.getIdent().getText(), dec.getType().getJVMTypeDesc(), null, null);
			
			dec.visit(this, mv);
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		
		
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {

		binaryChain.getE0().visit(this, 0);
		if (binaryChain.getArrow().isKind(Kind.BARARROW)) {
			mv.visitInsn(DUP);
		} else if (binaryChain.getE0().getType() == TypeName.FILE) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",PLPRuntimeImageIO.readFromFileDesc, false);
		} 
		else if (binaryChain.getE0().getType() == URL) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",PLPRuntimeImageIO.readFromURLSig, false);
		}
		
		if (binaryChain.getArrow().isKind(Kind.BARARROW)) {
			binaryChain.getE1().visit(this, 3);
		} else {
			binaryChain.getE1().visit(this, 1);
		}
		if (binaryChain.getE1() instanceof IdentChain) {
			IdentChain identChain = (IdentChain) binaryChain.getE1();
			if (identChain.getD() instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getD().getIdent().getText(),identChain.getD().getType().getJVMTypeDesc());
			} else if (identChain.getD().getType()==TypeName.INTEGER) {
					mv.visitVarInsn(ILOAD, identChain.getD().getSlot());
			} else {
					mv.visitVarInsn(ALOAD, identChain.getD().getSlot());
				}
			
		}

		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
		TypeName leftExpression; 
		TypeName rightExpression;
		int opcode;
		
		Label trueLabel = new Label();
		Label falseLabel = new Label();
		
		switch(binaryExpression.getOp().kind){
		
		case MINUS:
			
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			
			leftExpression= binaryExpression.getE0().getType();
			
			
			if (leftExpression == TypeName.INTEGER) {
				mv.visitInsn(ISUB);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
			}
			break;
		
		case PLUS:
			
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			
			leftExpression = binaryExpression.getE0().getType();

			if (leftExpression== TypeName.INTEGER) {
				mv.visitInsn(IADD);

			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
			}
			
			break;
		
		case DIV:
			
			
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			
			leftExpression = binaryExpression.getE0().getType();
			rightExpression = binaryExpression.getE1().getType();
			
			if ((leftExpression == TypeName.INTEGER) && (rightExpression == TypeName.INTEGER)) {
				mv.visitInsn(IDIV);
				
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
				
			}
			
			break;
			
		case TIMES:
			
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			
			leftExpression = binaryExpression.getE0().getType();
			rightExpression = binaryExpression.getE1().getType();
			
			 if ((leftExpression == TypeName.INTEGER) && (rightExpression== TypeName.IMAGE)) {
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
		
			} 
			 else if ((leftExpression == TypeName.INTEGER) && (rightExpression == TypeName.INTEGER)) {
				mv.visitInsn(IMUL);

			} 
			 else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
	
			}
	
			break;
			
		case EQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			leftExpression = binaryExpression.getE0().getType();
			rightExpression = binaryExpression.getE1().getType();
			
			
			if (leftExpression== TypeName.INTEGER || leftExpression == TypeName.BOOLEAN) {
				opcode = IF_ICMPNE;
				
			}
			else{
				opcode = IF_ACMPNE;
			}
			
				falseLabel = new Label();
				trueLabel = new Label();
				mv.visitJumpInsn(opcode, falseLabel);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, trueLabel);
				mv.visitLabel(falseLabel);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(trueLabel);
			
			break;
			
		case NOTEQUAL:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			leftExpression = binaryExpression.getE0().getType();
			rightExpression = binaryExpression.getE1().getType();
			
			
			if (leftExpression== TypeName.INTEGER || leftExpression == TypeName.BOOLEAN) {
				opcode = IF_ICMPEQ;
				
			}
			else{
				opcode = IF_ICMPEQ;
			}
			mv.visitJumpInsn(opcode, falseLabel);
			mv.visitInsn(ICONST_1);
			
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			
			mv.visitLabel(trueLabel);
			break;
		
		case GE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IF_ICMPLT, falseLabel);
			mv.visitInsn(ICONST_1);
			
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			
			mv.visitLabel(trueLabel);
			break;
		
		case LE:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IF_ICMPGT	, falseLabel);
			mv.visitInsn(ICONST_1);
			
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			
			mv.visitLabel(trueLabel);
			break;
			
		case LT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IF_ICMPGE, falseLabel);
			mv.visitInsn(ICONST_1);
			
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			
			mv.visitLabel(trueLabel);
			break;	
			
		case GT:
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IF_ICMPLE, falseLabel);
			mv.visitInsn(ICONST_1);
			
			mv.visitJumpInsn(GOTO, trueLabel);
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			
			mv.visitLabel(trueLabel);
			break;	
			
		case MOD:
			leftExpression = binaryExpression.getE0().getType();
			rightExpression = binaryExpression.getE1().getType();
			binaryExpression.getE0().visit(this, arg);
			binaryExpression.getE1().visit(this, arg);
			
			if ((leftExpression == TypeName.INTEGER) && (rightExpression == TypeName.INTEGER)) {
				mv.visitInsn(IREM);
			
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
			} 
			break;
			
		case OR: 
			binaryExpression.getE0().visit(this, arg);
			Label or_l1 = new Label();
			mv.visitJumpInsn(IFNE, or_l1);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFNE, or_l1);
			mv.visitInsn(ICONST_0);
			Label or_l2 = new Label();
			mv.visitJumpInsn(GOTO, or_l2);
			mv.visitLabel(or_l1);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(or_l2);
			break;
			
		case AND:
			
			Label and_labelTrue = new Label();
			Label and_l2False = new Label();
			binaryExpression.getE0().visit(this, arg);
			mv.visitJumpInsn(IFEQ, and_labelTrue);
			binaryExpression.getE1().visit(this, arg);
			mv.visitJumpInsn(IFEQ, and_labelTrue);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, and_l2False);
			mv.visitLabel(and_labelTrue);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(and_l2False);
			break;
		
		}
		
      //TODO  Implement this
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		
		Label b = new Label();
		Label bE = new Label();
		mv.visitLabel(b);
	
		ArrayList<Dec> d = block.getDecs();
		for(int i=0;i<block.getDecs().size();i++){
			d.get(i).visit(this, mv);
		}
		
		ArrayList<Statement> statement = block.getStatements();
		for(int i=0;i<block.getStatements().size();i++){
			statement.get(i).visit(this, mv);
		}
		
		
		mv.visitLabel(bE);
		
		for(int i = 0;i<block.getDecs().size() ;i++){
			
			mv.visitLocalVariable(d.get(i).getIdent().getText(), d.get(i).getType().getJVMTypeDesc(), null, b, bE, d.get(i).getSlot());
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		
		if(booleanLitExpression.getValue()){
			mv.visitInsn(ICONST_1);
		}
		else{
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		
		Token firstToken = constantExpression.getFirstToken();
		if (firstToken.isKind(Kind.KW_SCREENHEIGHT)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",PLPRuntimeFrame.getScreenHeightSig, false);
		} else if (firstToken.isKind(Kind.KW_SCREENWIDTH)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",PLPRuntimeFrame.getScreenWidthSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		
		declaration.setSlot(slotNumber++);
		//TODO Implement this
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		
		Kind k = filterOpChain.getFirstToken().kind;
		switch (k) {
		
		case OP_CONVOLVE:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig,false);
			break;
			
		case OP_BLUR:
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
			break;

		case OP_GRAY:
			int count = (int) arg;
			if ( count != 3) {
				mv.visitInsn(ACONST_NULL);
			}
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
			break;
		
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		switch (frameOpChain.getFirstToken().kind) {
		
		case KW_XLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc,false);
			break;
		case KW_YLOC:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc,false);
			break;
		case KW_SHOW:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc,false);
			break;
		case KW_HIDE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc,false);
			break;
		case KW_MOVE:
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc,false);
			break;

		}
		return null;
		
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
	
		
		if ((int) arg == 1) {
			if (identChain.getD() instanceof ParamDec && identChain.getD().getType() == TypeName.INTEGER) {
				
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getD().getIdent().getText(),identChain.getD().getType().getJVMTypeDesc());
					identChain.getD().setIntializedFlag(true);
			}
			else if(identChain.getD() instanceof ParamDec && identChain.getD().getType() == TypeName.FILE){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getD().getIdent().getText(),identChain.getD().getType().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",PLPRuntimeImageIO.writeImageDesc, false);
					mv.visitInsn(POP);
					identChain.getD().setIntializedFlag(true);
			}
			 else {
				if(identChain.getD().getType()==TypeName.IMAGE){
					mv.visitVarInsn(ASTORE, identChain.getD().getSlot());
					identChain.getD().setIntializedFlag(true);
				}
				else if(identChain.getD().getType()==TypeName.FILE){
					mv.visitVarInsn(ALOAD, identChain.getD().getSlot());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getD().setIntializedFlag(true);
				}
				else if(identChain.getD().getType()==TypeName.FRAME){
					if (identChain.getD().isIntializedFlag()) {
						mv.visitVarInsn(ALOAD, identChain.getD().getSlot());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig, false);
					} else {
						mv.visitInsn(ACONST_NULL);
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitVarInsn(ASTORE, identChain.getD().getSlot());
						identChain.getD().setIntializedFlag(true);
					}
				}
				else if(identChain.getD().getType()==TypeName.INTEGER){
					mv.visitVarInsn(ISTORE, identChain.getD().getSlot());
					identChain.getD().setIntializedFlag(true);
				}
				
			}
		} else {
			if (identChain.getD() instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getD().getIdent().getText(),
						identChain.getD().getType().getJVMTypeDesc());

			} else if (identChain.getD().getType() == FRAME) {
					if (identChain.getD().isIntializedFlag()) {

						mv.visitVarInsn(ALOAD, identChain.getD().getSlot());
		
					} else {
						mv.visitInsn(ACONST_NULL);
					}
			}
			 else {
					mv.visitVarInsn(ALOAD, identChain.getD().getSlot());
				}
		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		
		if(identExpression.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(), identExpression.getDec().getType().getJVMTypeDesc());
		}
		else if(identExpression.getType() == TypeName.INTEGER || identExpression.getType() == TypeName.BOOLEAN){
			mv.visitVarInsn(ILOAD, identExpression.getDec().getSlot());
		}
		else{
			mv.visitVarInsn(ALOAD, identExpression.getDec().getSlot());
		}
		//TODO Implement this
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		
		if(identX.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(), identX.getDec().getType().getJVMTypeDesc());
		}
		else if (identX.getDec().getType() == IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
		} else if (identX.getDec().getType() == TypeName.INTEGER || identX.getDec().getType() == TypeName.BOOLEAN) {
				mv.visitVarInsn(ISTORE, identX.getDec().getSlot());	
		}else {
				mv.visitVarInsn(ASTORE, identX.getDec().getSlot());
		}
		
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Label falseif = new Label();
		Label trueIf = new Label();
		ifStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFEQ, falseif); 
		mv.visitLabel(trueIf);
		ifStatement.getB().visit(this, arg); 
		mv.visitLabel(falseif);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		Kind k = imageOpChain.getFirstToken().kind;
		switch (k) {
		
		case OP_HEIGHT:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", "()I", false);
			break;
			
		case OP_WIDTH:
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", "()I", false);
			break;

		case KW_SCALE:
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
			break;

		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
	
		mv.visitLdcInsn(new Integer(intLitExpression.value));
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		MethodVisitor mv = (MethodVisitor) arg;

	

		switch (paramDec.getType()) {
			case INTEGER:
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramDec.getSlot());
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
				break;
		
			case BOOLEAN:
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramDec.getSlot());
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
				break;
				
			case URL:
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramDec.getSlot());
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
				break;	
				
			case FILE:
				mv.visitVarInsn(ALOAD, 0);
				mv.visitTypeInsn(NEW, "java/io/File");
				mv.visitInsn(DUP);
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(paramDec.getSlot());
				mv.visitInsn(AALOAD);
				mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
				mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
				break;
			}
		return null;
		
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
	
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression expression : tuple.getExprList()) {
			expression.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		
		Label trueL = new Label();
		Label falseL = new Label();
		
		Expression e = whileStatement.getE();
		Block b = whileStatement.getB();
		mv.visitJumpInsn(GOTO, trueL);
		mv.visitLabel(falseL);
		b.visit(this, arg);
		mv.visitLabel(trueL);
		e.visit(this, arg);
		mv.visitJumpInsn(IFNE, falseL);
		
		
		return null;
	}

}