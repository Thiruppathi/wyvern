package wyvern.tools.typedAST.core.values;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.abs.AbstractValue;
import wyvern.tools.typedAST.core.expressions.Invocation;
import wyvern.tools.typedAST.interfaces.*;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.extensions.Bool;
import wyvern.tools.util.TreeWriter;

import java.util.Hashtable;
import java.util.Map;

public class BooleanConstant extends AbstractValue implements InvokableValue, CoreAST {
	private boolean value;
	
	public BooleanConstant(boolean b) {
		this.value = b;
	}

	@Override
	public Type getType() {
		return Bool.getInstance();
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
		writer.writeArgs(this.value);
	}

	public boolean getValue() {
		return this.value;
	}

	@Override
	public Value evaluateInvocation(Invocation exp, Environment env) {
		BooleanConstant argValue = (BooleanConstant) exp.getArgument().evaluate(env);
		String operator = exp.getOperationName();
		switch(operator) {
			case "&&": return new BooleanConstant(value && argValue.value);
			case "||": return new BooleanConstant(value || argValue.value);
			default: throw new RuntimeException("forgot to typecheck!");
		}
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Map<String, TypedAST> getChildren() {
		Hashtable<String, TypedAST> children = new Hashtable<>();
		return children;
	}

	@Override
	public TypedAST cloneWithChildren(Map<String, TypedAST> nc) {
		return new BooleanConstant(value);
	}

	private FileLocation location = FileLocation.UNKNOWN;
	public FileLocation getLocation() {
		return this.location;
	}
}
