package wyvern.tools.typedAST.core.declarations;

import wyvern.tools.errors.FileLocation;
import wyvern.tools.typedAST.abs.Declaration;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.util.TreeWriter;

import java.util.Map;
import java.util.Optional;

public class PropDeclaration extends Declaration {
	private final String name;
	private final Type type;
	private final Optional<TypedAST> getter;
	private final Optional<TypedAST> setter;

	public PropDeclaration(String name, Type type, Optional<TypedAST> getter, Optional<TypedAST> setter) {
		this.name = name;
		this.type = type;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	protected Type doTypecheck(Environment env) {
		return null;
	}

	@Override
	protected Environment doExtend(Environment old, Environment against) {
		return null;
	}

	@Override
	public Environment extendWithValue(Environment old) {
		return null;
	}

	@Override
	public void evalDecl(Environment evalEnv, Environment declEnv) {

	}

	@Override
	public Environment extendType(Environment env, Environment against) {
		return null;
	}

	@Override
	public Environment extendName(Environment env, Environment against) {
		return null;
	}

	@Override
	public Type getType() {
		return null;
	}

	@Override
	public Map<String, TypedAST> getChildren() {
		return null;
	}

	@Override
	public TypedAST cloneWithChildren(Map<String, TypedAST> newChildren) {
		return null;
	}

	@Override
	public FileLocation getLocation() {
		return null;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {

	}
}
