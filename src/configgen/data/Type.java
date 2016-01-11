package configgen.data;

import configgen.FlatStream;
import configgen.type.Config;
import configgen.type.Field;
import configgen.type.Struct;
import org.w3c.dom.Element;

import java.util.HashSet;

public abstract class Type {
//	public final static String UNLIMIT_STR = "unlimit";
//	public final static int UNLIMIT_VALUE= -1;
	public final static String NULL_STR = "null";
	public final static int NULL_VALUE = -1;
	
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
				FList d = new FList(host, define);
				d.load(is);
				return d;
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
				final String subType = base.getNamespace() + "." + is.getString();
				Struct real = Struct.get(subType);
				if(real == null || !real.getFullName().equals(subType))
					error("dynamic type:" + subType + " unknown");
				if(!Struct.isDeriveFrom(subType, baseType))
					error("dynamic type:" + subType + " isn't sub type of:" + baseType);
				return new FStruct(host, define, subType, is);
			} else {
				return new FStruct(host, define, baseType, is);
			}
		}
		
		error("unknown type:" + type);
		return null;
	}
	
	public static Type create(FStruct host, Field define, String value) {
		final String type = define.getType();
		if(define.isRaw()) {
			if(type.equals("bool")) {
				return new FBool(host, define, value);
			} else if(type.equals("int")) {
				return new FInt(host, define, value);
			} else if(type.equals("long")) {
				return new FLong(host, define, value);
			} else if(type.equals("float")) {
				return new FFloat(host, define, value);
			} else if(type.equals("string")) {
				return new FString(host, define, value);
			}
		} else if(define.isEnum()) {
			return new FEnum(host, define, value);
		} else {
			error("unknown type:" + type);
		}
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
				FList d = new FList(host, define);
				d.load(node);
				return d;
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
				final String subType = base.getNamespace() + "." + node.getAttribute("type");
				Struct real = Struct.get(subType);
				if(real == null || !real.getFullName().equals(subType))
					error("dynamic type:" + subType + " unknown");
				if(!Struct.isDeriveFrom(subType, baseType))
					error("dynamic type:" + subType + " isn't sub type of:" + baseType);
				return new FStruct(host, define, subType, node);
			} else {
				return new FStruct(host, define, baseType, node);
			}
		}
		
		error("unknown type:" + type);
		return null;
	}
	
	public abstract boolean isNull();
	public abstract void accept(Visitor visitor);
	
	public void errorRef(Type value) {
		System.out.println("struct:" + host.getType() + " field:" + define.getName() + " value:" + value + " can't find in config:" + define.getRef());
	}

	public void verifyData() {
		if(isNull()) return;
		final String ref = define.getRef();
		if(ref.isEmpty()) return;
		HashSet<Type> validValues = Config.getData(ref);
		if(!validValues.contains(this))
			errorRef(this);	
	}

	
}
