package org.jfw.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import org.jfw.apt.exception.AptException;

public abstract class AbstractCodeGenerateHandler implements CodeGenerateHandler {
	protected Map<String, Object> env;
	protected TypeElement ref;
	protected Object annotationObj;
	protected AnnotationMirror annotationMirror;
	protected Messager messager;
	protected Filer filer;
	protected String packageName;
	protected String className;

	public abstract boolean isSupportedInterFace();

	public abstract boolean isSupportedAbstractClass();

	public abstract boolean isSupportedNoAbstractClass();

	public boolean isSupportedParameterizedType() {
		return false;
	}

	@Override
	public void setEnv(Map<String, Object> env) {
		this.env = env;
		this.messager = (Messager) env.get(Messager.class.getName());
		this.filer = (Filer) env.get(Filer.class.getName());
	}

	public static String getAnnotationClassName(AnnotationMirror am) {
		TypeElement type = (TypeElement) am.getAnnotationType().asElement();
		return type.getQualifiedName().toString();
	}

	private boolean isAbstract(TypeElement ref) {
		Set<Modifier> modifiers = ref.getModifiers();
		return modifiers.contains(Modifier.ABSTRACT);
	}

	public String getSourceFileName() {
		String originName = this.ref.getQualifiedName().toString();
		String typeName = null;
		int index = originName.lastIndexOf(".");
		String packageName = "";
		if (index > 0) {
			packageName = originName.substring(0, index + 1);
			typeName = originName.substring(index + 1);
		} else {
			typeName = originName;
		}
		if (this.ref.getKind() == ElementKind.INTERFACE) {
			return packageName + "impl." + typeName + "Impl";
		} else {
			return packageName + "extend." + typeName + "Extend";
		}

	}

	protected void writeSouceFile() throws AptException {
		String originName = this.ref.getQualifiedName().toString();
		String typeName = null;
		int index = originName.lastIndexOf(".");
		packageName = "";
		className = null;
		if (index > 0) {
			packageName = originName.substring(0, index + 1);
			typeName = originName.substring(index + 1);

		} else {
			typeName = originName;
		}
		if (this.ref.getKind() == ElementKind.INTERFACE) {
			packageName = packageName + "impl";
			className = typeName + "Impl";
		} else {
			packageName = packageName + "extend";
			className = typeName + "Extend";
		}

		try {
			JavaFileObject jfo = this.filer.createSourceFile(packageName + "." + className, (Element[]) null);
			StringBuilder sb = new StringBuilder();
			sb.append("package ").append(packageName).append(";\r\n").append("public class ").append(className);
			if (this.ref.getKind() == ElementKind.INTERFACE) {
				sb.append(" implements ").append(originName);
			} else {
				sb.append(" extends ").append(originName);
			}
			sb.append(" {");
			this.writeContent(sb);
			sb.append("\r\n}");

			Writer w = jfo.openWriter();
			try {
				w.write(sb.toString());
			} finally {
				w.close();
			}
		} catch (IOException e) {
			throw new AptException(this.ref,
					"write java sorce file(" + packageName + "." + typeName + ") error:" + e.getMessage());
		}

	}

	protected abstract void writeContent(StringBuilder sb) throws AptException;

	@Override
	public void handle(TypeElement ref, AnnotationMirror am, Object annotationObj) throws AptException {
		this.ref = ref;
		this.annotationObj = annotationObj;
		this.annotationMirror = am;
		ElementKind kind = ref.getKind();

		if (kind == ElementKind.INTERFACE && (!this.isSupportedInterFace())) {
			throw new AptException(ref, "type can't supported Annotation " + getAnnotationClassName(am));
		}
		if (kind == ElementKind.CLASS && isAbstract(ref) && (!this.isSupportedAbstractClass())) {
			throw new AptException(ref, "type can't supported Annotation " + getAnnotationClassName(am));
		}
		if (kind == ElementKind.CLASS && (!isAbstract(ref)) && (!this.isSupportedNoAbstractClass())) {
			throw new AptException(ref, "type can't supported Annotation " + getAnnotationClassName(am));
		}

	}

}
