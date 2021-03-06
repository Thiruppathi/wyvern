package wyvern.tools.typedAST.core.declarations;

import wyvern.stdlib.Globals;
import wyvern.targets.java.annotations.Val;
import wyvern.tools.errors.ErrorMessage;
import wyvern.tools.errors.FileLocation;
import wyvern.tools.errors.ToolError;
import wyvern.tools.typedAST.abs.Declaration;
import wyvern.tools.typedAST.core.binding.*;
import wyvern.tools.typedAST.core.binding.typechecking.LateNameBinding;
import wyvern.tools.typedAST.core.expressions.New;
import wyvern.tools.typedAST.core.expressions.TaggedInfo;
import wyvern.tools.typedAST.core.binding.compiler.MetadataInnerBinding;
import wyvern.tools.typedAST.core.binding.evaluation.ValueBinding;
import wyvern.tools.typedAST.core.binding.objects.TypeDeclBinding;
import wyvern.tools.typedAST.core.binding.typechecking.TypeBinding;
import wyvern.tools.typedAST.core.values.Obj;
import wyvern.tools.typedAST.interfaces.CoreAST;
import wyvern.tools.typedAST.interfaces.CoreASTVisitor;
import wyvern.tools.typedAST.interfaces.EnvironmentExtender;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.TypeResolver;
import wyvern.tools.types.extensions.ClassType;
import wyvern.tools.types.extensions.TypeType;
import wyvern.tools.util.Reference;
import wyvern.tools.util.TreeWriter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class TypeDeclaration extends Declaration implements CoreAST {
	protected DeclSequence decls;
	private Reference<Optional<TypedAST>> metadata;
	private NameBinding nameBinding;
	private TypeBinding typeBinding;
	
	private Environment declEvalEnv;
    protected Reference<Environment> declEnv = new Reference<>(Environment.getEmptyEnvironment());
	protected Reference<Environment> attrEnv = new Reference<>(Environment.getEmptyEnvironment());
	
	public static Environment attrEvalEnv = Environment.getEmptyEnvironment(); // HACK
	private Reference<Value> metaValue = new Reference<>();

	// FIXME: I am not convinced typeGuard is required (alex).
	private boolean typeGuard = false;
	@Override
	public Environment extendType(Environment env, Environment against) {
		if (!typeGuard) {
			env = env.extend(typeBinding);
			declEnv.set(decls.extendType(declEnv.get(), against));
			typeGuard = true;
		}
		return env.extend(typeBinding);
	}

	private boolean declGuard = false;
	@Override
	public Environment extendName(Environment env, Environment against) {
		if (!declGuard) {
			declEnv.set(decls.extendName(declEnv.get(), against.extend(typeBinding)));
			//declEnv.set(decls.extend(declEnv.get(), against.extend(typeBinding)));
			declGuard = true;
		}

		return env.extend(nameBinding);
	}
	
	private TaggedInfo taggedInfo;

	public TypeDeclaration(String name, DeclSequence decls, Reference<Value> metadata, TaggedInfo taggedInfo, FileLocation clsNameLine) {
    	this(name, decls, metadata, clsNameLine);
    	
		this.taggedInfo = taggedInfo;
		this.taggedInfo.setTagName(name, this, null);
		this.taggedInfo.associateWithClassOrType(this.typeBinding);
	}
	
    public TypeDeclaration(String name, DeclSequence decls, Reference<Value> metadata, FileLocation clsNameLine) {
    	// System.out.println("Initialising TypeDeclaration ( " + name + "): decls" + decls);
		this.decls = decls;
		nameBinding = new NameBindingImpl(name, null);
		typeBinding = new TypeBinding(name, null, metadata);
		Type objectType = new TypeType(this);
		attrEnv.set(attrEnv.get().extend(new TypeDeclBinding("type", this)));
		
		Type classType = new ClassType(attrEnv, attrEnv, new LinkedList<String>(), getName()); // TODO set this to a class type that has the class members
		nameBinding = new LateNameBinding(nameBinding.getName(), () -> metadata.get().getType());

		typeBinding = new TypeBinding(nameBinding.getName(), objectType, metadata);
		
		// System.out.println("TypeDeclaration: " + nameBinding.getName() + " is now bound to type: " + objectType);
		
		this.location = clsNameLine;
		this.metaValue = metadata;
	}

	@Override
	public void writeArgsToTree(TreeWriter writer) {
		//TODO: implement me
		//writer.writeArgs(definition);
	}

	@Override
	public void accept(CoreASTVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Type getType() {
		return this.typeBinding.getType();
	}

	@Override
	public Map<String, TypedAST> getChildren() {
		Map<String, TypedAST> childMap = new HashMap<>();
		childMap.put("decls", decls);
		return childMap;
	}

	@Override
	public TypedAST cloneWithChildren(Map<String, TypedAST> newChildren) {
		return new TypeDeclaration(nameBinding.getName(), (DeclSequence)newChildren.get("decls"), metaValue, location);
	}

	@Override
	public Type doTypecheck(Environment env) {
		Environment eenv = decls.extend(env, env);
		
		for (Declaration decl : decls.getDeclIterator()) {
			decl.typecheckSelf(eenv);
		}

		return this.typeBinding.getType();
	}	
	
	@Override
	protected Environment doExtend(Environment old, Environment against) {
		Environment newEnv = old.extend(nameBinding).extend(typeBinding);
		// newEnv = newEnv.extend(new NameBindingImpl("this", nameBinding.getType())); // Why is there "this" in a type (not class)?
		
		return newEnv;
	}

	@Override
	public Environment extendWithValue(Environment old) {
		Environment newEnv = old.extend(new ValueBinding(nameBinding.getName(), nameBinding.getType()));
		return newEnv;
	}

	@Override
	public void evalDecl(Environment evalEnv, Environment declEnv) {
		declEvalEnv = declEnv;
		if (metaValue.get() == null)
			metaValue.set(metadata.get().orElseGet(() -> new New(new DeclSequence(), FileLocation.UNKNOWN)).evaluate(evalEnv));
		ValueBinding vb = (ValueBinding) declEnv.lookup(nameBinding.getName());
		vb.setValue(metaValue.get());
	}

	public DeclSequence getDecls() {
		return decls;
	}

	@Override
	public String getName() {
		return nameBinding.getName();
	}

	private FileLocation location = FileLocation.UNKNOWN;
	
	@Override
	public FileLocation getLocation() {
		return location; 
	}

    public NameBinding lookupDecl(String name) {
        return declEnv.get().lookup(name);
    }


	public Reference<Environment> getDeclEnv() {
		return declEnv;
	}
}