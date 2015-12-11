package org.jfw.apt;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.orm.Column;
import org.jfw.apt.orm.core.OrmHandler;

public class Utils {
	private static Map<Class<?>, Class<?>> wrapClass = new HashMap<Class<?>, Class<?>>();
	private static Map<String, String> wrapClassName = new HashMap<String, String>();

	private Utils() {
	}

	public static boolean isPrimitive(String className) {
		return wrapClassName.containsKey(className);
	}
	public static OrmHandler getOrmHandler(Column col,Element ref) throws AptException{
		try{
		return col.getDataElement().getHandlerClass().newInstance();
		} catch (Exception ee) {
			String m = ee.getMessage();
			throw new AptException(ref, "can't create ormHandler instance:" + m == null ? "" : m);
		}
	}

	public static boolean isPrimitive(Class<?> clazz) {
		return wrapClass.containsKey(clazz);
	}

	public static String getWrapClass(String className) {
		return wrapClassName.get(className);
	}
	public static void addSqlToStringBuilder(String s,StringBuilder sb){
		for(int i = 0 ; i < s.length() ; ++i){
			char c = s.charAt(i);
			if(c=='\\' || c=='"') sb.append("\\");
			sb.append(c);
		}
	}
	public static String emptyToNull(String str){
		if(str==null || str.trim().length() == 0) return null;
				return str.trim();
	}

	public static String getClassName(Class<?> clazz) {
		if (clazz.isArray()) {
			return getClassName(clazz.getComponentType()) + "[]";
		}
		return clazz.getName();
	}

	public static String getTypeName(Type type) {
		if (type instanceof Class) {
			Class<?> cl = (Class<?>) type;
			if (cl.isArray()) {
				return getTypeName(cl.getComponentType()) + "[]";
			} else {
				return cl.getName();
			}
		} else if (type instanceof GenericArrayType) {
			return getTypeName(((GenericArrayType) type).getGenericComponentType()) + "[]";
		} else if (type instanceof ParameterizedType) {
			StringBuilder sb = new StringBuilder();
			ParameterizedType pt = (ParameterizedType) type;
			sb.append(getTypeName(pt.getRawType())).append("<");
			Type[] ts = pt.getActualTypeArguments();
			for (int i = 0; i < ts.length; ++i) {
				if (i != 0)
					sb.append(",");
				sb.append(getTypeName(ts[i]));
			}
			sb.append(">");
			return sb.toString();
		} else if (type instanceof TypeVariable) {
			StringBuilder sb = new StringBuilder();
			java.lang.reflect.TypeVariable<?> tt = (java.lang.reflect.TypeVariable<?>) type;
			sb.append(tt.getName()).append(" extends ");
			Type[] ts = tt.getBounds();
			for (int i = 0; i < ts.length; ++i) {
				if (i != 0)
					sb.append(" & ");
				sb.append(getTypeName(ts[i]));
			}
			return sb.toString();
		} else if (type instanceof WildcardType) {

			java.lang.reflect.WildcardType wt = (java.lang.reflect.WildcardType) type;
			if (wt.getLowerBounds().length > 0) {
				return "? super " + getTypeName(wt.getLowerBounds()[0]);
			} else if (Object.class != wt.getUpperBounds()[0]) {
				return "? extends " + getTypeName(wt.getUpperBounds()[0]);
			} else {
				return "?";
			}

		}
		return null;
	}

	public static String classNameToInstanceName(String className) {
		return className.substring(0, 1).toLowerCase(Locale.ENGLISH) + className.substring(1);
	}

	public static String getSupportedClassName(Class<? extends OrmHandler> clazz)
			throws InstantiationException, IllegalAccessException {
		return getClassName(clazz.newInstance().supportsClass());
	}

	public static void checkPersistentObjectName(String name, Element ele) throws AptException {
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')))
				throw new AptException(ele, "invalid name");
		}
	}

	public static String javaNameConverToDbName(String name) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (c >= 'A' && c <= 'Z' && i != 0) {
				sb.append("_").append(c);
			} else {
				sb.append(Character.toUpperCase(c));
			}
		}
		return sb.toString();
	}

	public static PackageElement getPackage(Element type) {
		while (type.getKind() != ElementKind.PACKAGE) {
			type = type.getEnclosingElement();
		}
		return (PackageElement) type;
	}

	public static String getReturnTypeName(TypeMirror t, Element ele) throws AptException {
		TypeKind td = t.getKind();
		if (td == TypeKind.BOOLEAN) {
			return "boolean";
		} else if (td == TypeKind.BYTE) {
			return "byte";
		} else if (td == TypeKind.SHORT) {
			return "short";
		} else if (td == TypeKind.INT) {
			return "int";
		} else if (td == TypeKind.LONG) {
			return "long";
		} else if (td == TypeKind.CHAR) {
			return "char";
		} else if (td == TypeKind.FLOAT) {
			return "float";
		} else if (td == TypeKind.DOUBLE) {
			return "double";
		} else if (td == TypeKind.DOUBLE) {
			return "double";
		} else if (td == TypeKind.VOID) {
			return "void";
		} else if (td == TypeKind.ARRAY) {
			return getReturnTypeName(((ArrayType) t).getComponentType(), ele) + "[]";
		} else if (td == TypeKind.DECLARED) {
			DeclaredType dt = (DeclaredType) t;
			String name = ((TypeElement) dt.asElement()).getQualifiedName().toString();

			List<? extends TypeMirror> list = dt.getTypeArguments();
			if (list.isEmpty())
				return name;
			ArrayList<String> as = new ArrayList<String>();
			for (int i = 0; i < list.size(); ++i) {
				TypeMirror tm = list.get(i);
				if (tm.getKind().isPrimitive() || tm.getKind() == TypeKind.VOID) {
					throw new AptException(ele, "invalid ParameterizedType");
				}
				as.add(getReturnTypeName(tm, ele));
			}
			StringBuilder sb = new StringBuilder();
			sb.append(name).append("<");
			for (int i = 0; i < as.size(); ++i) {
				if (i != 0)
					sb.append(",");
				sb.append(as.get(i));
			}
			sb.append(">");
			return sb.toString();
		} else if (td == TypeKind.TYPEVAR) {
			TypeParameterElement element = (TypeParameterElement) ((TypeVariable) t).asElement();

			List<String> strs = new ArrayList<String>();
			for (TypeMirror typeMirror : element.getBounds()) {
				String s = getReturnTypeName(typeMirror, ele);
				if (s.equals("java.lang.Object"))
					continue;
				strs.add(s);
			}
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < strs.size(); ++i) {
				if (i == 0) {
					sb.append(element.getSimpleName().toString()).append(" extends ");
				} else {
					sb.append(" & ");
				}
				sb.append(strs.get(i));
			}
			return sb.toString();
		} else if (td == TypeKind.WILDCARD) {
			WildcardType mirror = (WildcardType) t;
			TypeMirror extendsBound = mirror.getExtendsBound();
			if (extendsBound == null) {
				TypeMirror superBound = mirror.getSuperBound();
				if (superBound == null) {
					return "?";
				} else {
					return "? super " + getReturnTypeName(superBound, ele);
				}
			} else {
				String cn = getReturnTypeName(extendsBound, ele);
				if ("java.lang.Object".equals(cn))
					return "?";
				return "? extends " + cn;
			}
		} else {
			throw new AptException(ele, "unknow exception for TypeKind");
		}
	}

	static {
		wrapClass.put(int.class, Integer.class);
		wrapClass.put(byte.class, Byte.class);
		wrapClass.put(short.class, Short.class);
		wrapClass.put(float.class, Float.class);
		wrapClass.put(double.class, Double.class);
		wrapClass.put(boolean.class, Boolean.class);
		wrapClass.put(char.class, Character.class);
		wrapClass.put(long.class, Long.class);
		for (Map.Entry<Class<?>, Class<?>> en : wrapClass.entrySet()) {
			wrapClassName.put(en.getKey().getName(), en.getValue().getName());
		}
	}

}
