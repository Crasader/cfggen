package configgen.data;

import java.util.HashSet;

import org.w3c.dom.Element;
import configgen.FlatStream;
import configgen.type.Alias;
import configgen.type.Config;
import configgen.type.Field;
import configgen.type.Struct;

public abstract class Type {
	protected final Field define;
	protected final FStruct host;
	
	public Type(FStruct host, Field define) {
		this.host = host;
		this.define = define;
	}
	
	public final Field getDefine() {
		return define;
	}
	
	public final FStruct getHost() {
		return host;
	}

	public  Type pop(FlatStream is) { return this;}
	
	static void error(String err) {
		throw new RuntimeException(err);
	}
	
	public static Type create(FStruct host, Field define, FlatStream is) {
		final String type = define.getType();
		if(define.isRaw()) {
			if(type.equals("bool")) {
				return new FBool(host, define, is);
			} else if(type.equals("int")) {
				return new FInt(host, define, is);
			} else if(type.equals("long")) {
				return new FLong(host, define, is);
			} else if(type.equals("float")) {
				return new FFloat(host, define, is);
			} else if(type.equals("string")) {
				return new FString(host, define, is);
			}
		} else if(define.isContainer()) {
			if(type.equals("list")) {
				return new FList(host, define, is);
			} else if(type.equals("set")) {
				return new FSet(host, define, is);
			} else if(type.equals("map")) {
				return new FMap(host, define, is);
			}
			
		} else if(define.isEnum()) {
			return new FEnum(host, define, is);
		} else if(define.isStruct()) {
			final String baseType = define.getType();
			final Struct base = Struct.get(define.getType());
			if(base.isDynamic()) {
				final String subType = is.getString();
				final String realType = Alias.getOriginName(subType);
				if(realType == null) {
					error("dynamic sub type:" + subType + " unknown");
				}
				Struct real = Struct.get(realType);
				if(real == null)
					error("dynamic type:" + realType + " unknown");
				if(!Struct.isDeriveFrom(realType, baseType))
					error("dynamic type:" + realType + " isn't sub type of:" + baseType);
				return new FStruct(host, define, realType, is);
			} else {
				return new FStruct(host, define, baseType, is);
			}
		}
		
		error("unknown type:" + type);
		return null;
	}
	

	public static Type create(FStruct host, Field define, Element node) {
		final String type = define.getType();
		if(define.isRaw()) {
			if(type.equals("bool")) {
				return new FBool(host, define, node);
			} else if(type.equals("int")) {
				return new FInt(host, define, node);
			} else if(type.equals("long")) {
				return new FLong(host, define, node);
			} else if(type.equals("float")) {
				return new FFloat(host, define, node);
			} else if(type.equals("string")) {
				return new FString(host, define, node);
			}
		} else if(define.isContainer()) {
			if(type.equals("list")) {
				return new FList(host, define, node);
			} else if(type.equals("set")) {
				return new FSet(host, define, node);
			} else if(type.equals("map")) {
				return new FMap(host, define, node);
			}
		} else if(define.isEnum()) {
			return new FEnum(host, define, node);
		} else if(define.isStruct()) {
			final String baseType = define.getType();
			final Struct base = Struct.get(define.getType());
			if(base.isDynamic()) {
				final String subType = node.getAttribute("type");
				final String realType = Alias.getOriginName(subType);
				if(realType == null) {
					error("dynamic sub type:" + subType + " unknown");
				}
				Struct real = Struct.get(realType);
				if(real == null)
					error("dynamic type:" + realType + " unknown");
				if(!Struct.isDeriveFrom(realType, baseType))
					error("dynamic type:" + realType + " isn't sub type of:" + baseType);
				return new FStruct(host, define, realType, node);
			} else {
				return new FStruct(host, define, baseType, node);
			}
		}
		
		error("unknown type:" + type);
		return null;
	}
	
	public abstract void accept(Visitor visitor);
	
	public void errorRef(Type value) {
		System.out.println("struct:" + host.getType() + " field:" + define.getName() + " value:" + value + " can't find in config:" + define.getRef());
	}

	public void verifyData() {
		final String ref = define.getRef();
		if(ref.isEmpty()) return;
		HashSet<Type> validValues = Config.getData(ref);
		if(!validValues.contains(this))
			errorRef(this);	
	}

	
}
