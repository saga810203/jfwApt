package org.jfw.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.JavaFileObject;

import org.jfw.apt.annotation.web.RequestMapping;
import org.jfw.apt.annotation.web.WebHandler;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.RequestHandler;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.RequestMethod;

public class WebHandlerSupported implements CodeGenerateHandler {
	
	
	private static List<RequestEntry> reqs = new ArrayList<RequestEntry>();

	protected Map<String, Object> env;
	protected TypeElement ref;
	protected Messager messager;
	protected Filer filer;
	protected String packageName = "";
	protected String className;
	protected String uri="";
	protected WebHandler wh;

	protected StringBuilder sb;
	private boolean threadSafe;
	
	

	public RequestHandler[] createHandler() throws AptException {
		Class<? extends RequestHandler>[] clss = wh.handler();
		RequestHandler[]  handlers  = new RequestHandler[clss.length];
		for (int i = 0; i < clss.length; ++i) {
			try {
				handlers[i] = clss[i].newInstance();
			} catch (Exception ee) {
				String m = ee.getMessage();
				throw new AptException(ref, "can't create RequestHandler instance:" + m == null ? "" : m);
			}
		}
		return handlers;
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


	public String getTargetClassPackage() {
		if (this.packageName.length() == 0)
			return "";
		return this.packageName.substring(0, this.packageName.length() - 1);
	}

	public String getTargetClassName() {
		return this.className + "WebHandler";
	}

	protected void writeContent() throws AptException {
		for(Element ele:this.ref.getEnclosedElements()){
			if(ele.getKind()==ElementKind.METHOD){
				RequestMapping rm = ele.getAnnotation(RequestMapping.class);
				if(rm==null) continue;
				
				String vuri =Utils.emptyToNull(rm.value());
				if(vuri==null) vuri ="";				
				vuri = this.uri==null?"":this.uri+vuri;	
				if(vuri.length()==0 || !vuri.startsWith("/"))
					throw new AptException(ele,"invalid annotation @RequestMapping(value)");
				
				RequestMappingCodeGenerator rmcg = new RequestMappingCodeGenerator();
				rmcg.fillMeta((ExecutableElement)ele);
				rmcg.setWebHandlerSupported(this);
				rmcg.setUri(vuri);
				rmcg.writeMethod(sb);
				

				RequestEntry re = new RequestEntry();
				re.setMethod(rmcg.getWebMethodName());
				re.setUri(vuri);
				re.setRequestMethods(rm.method());
				re.setClassName(this.packageName+this.className);
				reqs.add(re);
			}
		}
	}

	protected void writeFile() throws AptException {
		String tpn = this.getTargetClassPackage();
		String tcn = this.getTargetClassName();
		try {


			this.sb = new StringBuilder();
			if (this.packageName.length() > 0) {
				sb.append("package ").append(packageName).append(";\r\n");
			}
			sb.append("public class ").append(className + "WebHandler").append(" {");
			sb.append("private static String handlerClassName = null;");
			if (!this.threadSafe) {
				sb.append("private static Class<?> handlerClass = null;\r\n");
			} else {
				sb.append("private ").append(this.packageName).append(this.className).append(" handler = null;\r\n");
			}
			sb.append("public static void build(){");
			if (this.threadSafe) {
				sb.append("try{").append(tcn).append(".handler =(").append(packageName).append(className)
						.append(")Class.forName(").append(tcn)
						.append(".handlerClassName).newInstance();}catch(Exception e){throw new RuntimeException(\"create object instance error with class name:\"+")
						.append(tcn).append(".handlerClassName");
				sb.append(",e);}\r\n");
			} else {
				sb.append("try{").append(tcn).append(".handlerClass = Class.forName(").append(tcn)
						.append(".handlerClassName);\r\n")
						.append("}catch(Exception e){throw new RuntimeException(\"create class with class name:\"+")
						.append(tcn).append(".handlerClassName");
				sb.append(",e);}\r\n");
			}
			sb.append("}\r\n");

			this.writeContent();
			sb.append("\r\n}");
			JavaFileObject jfo = this.filer.createSourceFile(tpn.length() == 0 ? "" : (tpn + ".") + tcn,this.ref);
			Writer w = jfo.openWriter();
			try {
				w.write(sb.toString());
			} finally {
				w.close();
			}
		} catch (IOException e) {
			throw new AptException(this.ref, "write java sorce file(" + tpn + "." + tcn + ") error:" + e.getMessage());
		}
	}

	@Override
	public void handle(TypeElement ref, AnnotationMirror am, Object annotationObj) throws AptException {
		
		
		
		this.ref = ref;
		if (null == ref.getEnclosedElements() || ref.getEnclosingElement().getKind() != ElementKind.PACKAGE)
			throw new AptException(ref, "@WebHandler annotation target not internal class");
		wh = ref.getAnnotation(WebHandler.class);
		String vuri =Utils.emptyToNull(wh.value());
		DeclaredType dt =(DeclaredType) ref.asType();
		if(!dt.getTypeArguments().isEmpty()) throw new AptException(ref,"@WebHandler annotation target not Parameterized class");
		if(vuri==null){
			vuri ="";
		}else{
			if(!vuri.startsWith("/")){
				throw new AptException(ref,"@WebHandler'value must be startsWith '/'");
			}
		}
		
		
		
		this.threadSafe = wh.threadSafe();
		Class<? extends RequestHandler>[] clss = wh.handler();
		if (clss == null || clss.length == 0)
			throw new AptException(ref, "@WebHandler'handler not null or empty array");
		String cn = ref.getQualifiedName().toString();
		int index = cn.lastIndexOf(".");
		if (index > 0) {
			this.packageName = cn.substring(0, index + 1);
			this.className = cn.substring(index + 1);
		} else {
			this.className = cn;
		}
		this.writeFile();
	}
	public boolean isThreadSafe() {
		return threadSafe;
	}
	
	
	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}


	public static class RequestEntry{
		private String ClassName;
		private String uri;
		private String method;
		private RequestMethod[] requestMethods;
		
		public String getClassName() {
			return ClassName;
		}
		public void setClassName(String className) {
			ClassName = className;
		}
		public String getUri() {
			return this.uri;
		}
		public void setUri(String uri) {
			this.uri = uri;
		}
		public String getMethod() {
			return method;
		}
		public void setMethod(String method) {
			this.method = method;
		}
		public RequestMethod[] getRequestMethods() {
			return requestMethods;
		}
		public void setRequestMethods(RequestMethod[] rm) {
			this.requestMethods = rm;
		}
	
		
	}


	@Override
	public CodeGenerateAllAfterEventByType getStaticAfterEvent() {
		return new CodeGenerateAllAfterEventByType() {
			
			@Override
			public void execute(JfwProccess jp) throws AptException {
				StringBuilder sb = new StringBuilder();
				for(RequestEntry re:reqs){
					for(RequestMethod rm: re.getRequestMethods()){
						sb.append(rm.toString()).append(":").append(re.getUri()).append(";").append(re.getClassName()).append(".").append(re.getMethod())
						.append("\r\n");
					}
				}
				jp.saveResourceFile("jfw_web_dispatcher.properties", sb.toString());
			}
		};
	}

}
