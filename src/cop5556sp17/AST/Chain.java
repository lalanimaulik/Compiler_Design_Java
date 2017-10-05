package cop5556sp17.AST;

import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
	
	private TypeName type = null;
	
	public TypeName getType() {
		return type;
	}

	public void setType(TypeName type) {
		this.type = type;
	}

	public Chain(Token firstToken) {
		super(firstToken);
	}

}
