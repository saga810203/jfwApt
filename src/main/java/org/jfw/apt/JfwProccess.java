package org.jfw.apt;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.orm.OrmDefine;
import org.jfw.apt.model.orm.PersistentObject;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JfwProccess extends javax.annotation.processing.AbstractProcessor {
	private Messager messager;
	private Filer filer;
	private Map<String,Object> attributes = new HashMap<String,Object>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.messager = processingEnv.getMessager();
		this.filer = processingEnv.getFiler();
	}
	
	private void handle(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)throws AptException{
//		OrmDefine od =new OrmDefine();
//		for(Element ele: roundEnv.getElementsAnnotatedWith(org.jfw.apt.annotation.orm.VirtualTable.class)){
//			if(!(ele instanceof TypeElement)) throw new AptException(ele,"invalid PersistentObject with annotation @VirtualTable");
//			PersistentObject po = PersistentObject.build((TypeElement)ele, ele.getAnnotation(org.jfw.apt.annotation.orm.VirtualTable.class));
//		    od.addPersistentObject(po);
//		}
//		for(Element ele: roundEnv.getElementsAnnotatedWith(org.jfw.apt.annotation.orm.Table.class)){
//			if(!(ele instanceof TypeElement)) throw new AptException(ele,"invalid PersistentObject with annotation @Table");
//			PersistentObject po = PersistentObject.build((TypeElement)ele, ele.getAnnotation(org.jfw.apt.annotation.orm.Table.class));
//		    od.addPersistentObject(po);
//		}
//		for(Element ele: roundEnv.getElementsAnnotatedWith(org.jfw.apt.annotation.orm.ExtendTable.class)){
//			if(!(ele instanceof TypeElement)) throw new AptException(ele,"invalid PersistentObject with annotation @ExtendTable");
//			PersistentObject po = PersistentObject.build((TypeElement)ele, ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendTable.class));
//		    od.addPersistentObject(po);
//		}
//		for(Element ele: roundEnv.getElementsAnnotatedWith(org.jfw.apt.annotation.orm.View.class)){
//			if(!(ele instanceof TypeElement)) throw new AptException(ele,"invalid PersistentObject with annotation @View");
//			PersistentObject po = PersistentObject.build((TypeElement)ele, ele.getAnnotation(org.jfw.apt.annotation.orm.View.class));
//		    od.addPersistentObject(po);
//		}
//		for(Element ele: roundEnv.getElementsAnnotatedWith(org.jfw.apt.annotation.orm.ExtendView.class)){
//			if(!(ele instanceof TypeElement)) throw new AptException(ele,"invalid PersistentObject with annotation @ExtendView");
//			PersistentObject po = PersistentObject.build((TypeElement)ele, ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendView.class));
//		    od.addPersistentObject(po);
//		}
//		od.initPersistentObjects();
//		this.setAttribute(OrmDefine.class.getName(), od);
//		od.warnMessage(messager);	
		this.handleCodeGenerateHandler(annotations, roundEnv);
	}
	
	private void warnMethodInfo(TypeElement ref) throws AptException{
		for(Element ele:ref.getEnclosedElements()){
			if(ele.getKind()!= ElementKind.METHOD) continue;
			ExecutableElement el =(ExecutableElement)ele;
			this.warnMethodInfo((ExecutableElement)ele);
			
		}
		
	}
	
	
	private void warnMethodInfo(ExecutableElement ref) throws AptException{		
		StringBuilder sb = new StringBuilder();		
		Set<Modifier> ms = ref.getModifiers();
		for( Modifier m:ms) sb.append(m.toString()).append(" ");
		
		sb.append(Utils.getReturnTypeName(ref.getReturnType(), ref)).append(" ");
		sb.append(ref.getSimpleName().toString()).append("(");
		
		List<? extends VariableElement> eles = ref.getParameters();
		for(int i = 0 ; i < eles.size() ; ++i){
			VariableElement ele = eles.get(i);
			if(i !=0) sb.append(",");
			sb.append(Utils.getReturnTypeName(ele.asType(), ele)).append(" ").append(ele.getSimpleName().toString());			
		}
		sb.append(")");
		
		List<? extends TypeMirror> ths = ref.getThrownTypes();
		for(int i = 0 ; i < ths.size() ; ++i){
			if(i==0){
				sb.append(" throws ");
			}else{
				sb.append(",");
			}
			sb.append(Utils.getReturnTypeName(ths.get(i), ref));			
		}
	
		this.messager.printMessage(Kind.WARNING, sb.toString(),ref);
	}
	
	private void handleCodeGenerateHandler(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)throws AptException{
		 for(Element ele:roundEnv.getRootElements()){
			 if((ele.getKind()!=ElementKind.CLASS)&&(ele.getKind()!=ElementKind.INTERFACE))continue;
			 if(!(ele instanceof TypeElement))continue;
			 
			 
			 TypeElement el = (TypeElement)ele;
			 if(el.getNestingKind().isNested()) continue;
			 List<? extends TypeParameterElement> list =el.getTypeParameters();
			 if(list!=null && list.size()>0) continue;
			 this.warnMethodInfo(el);
			 
//			 List<? extends AnnotationMirror> ans= el.getAnnotationMirrors();
//			 boolean handled = false;
//			 for(AnnotationMirror anm:ans){
//				for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:anm.getElementValues().entrySet()){
//					ExecutableElement ee = entry.getKey();
//					if(ee.getSimpleName().toString()=="handlerClass"){
//						Object obj = entry.getValue().getValue();
//						if((obj instanceof Class)&&(CodeGenerateHandler.class.isAssignableFrom((Class<?>)obj))){
//							CodeGenerateHandler cgh = null;
//							try{
//								cgh =(CodeGenerateHandler) ((Class<?>)obj).newInstance();
//							}catch(Exception e){
//								throw new AptException(ele,"create Object instance with "+((Class<?>)obj).getName()+"error:"+e.getMessage());
//							}
//							cgh.setEnv(this.attributes);
//							cgh.handle(el,anm,this.getAnnotationObj(el, anm));
//							handled = true;
//							break;
//						}						
//					}
//				}
//				if(handled) break;
//			 }
		 }
	}
	
	@SuppressWarnings("unchecked")
	private Object getAnnotationObj(Element ele,AnnotationMirror am){
		try{
		 TypeElement type = (TypeElement) am.getAnnotationType().asElement();
		 String cn = type.getQualifiedName().toString();
		 Class<? extends Annotation> cls = (Class<? extends Annotation>)Class.forName(cn);
		 return ele.getAnnotation(cls);
		}catch(Exception e){
			return null;
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		this.attributes.clear();
		this.setAttribute(JfwProccess.class.getName(), this);
		this.setAttribute(RoundEnvironment.class.getName(), roundEnv);
		this.setAttribute(Messager.class.getName(), this.messager);
		this.setAttribute(Filer.class.getName(), this.filer);
		try{
			this.handle(annotations, roundEnv);		
		}catch(AptException e){
			this.messager.printMessage(Kind.ERROR,e.getMessage(), e.getEle());
		}
		return true;
	}
	
	
	public Object getAttribute(String key){
		return this.attributes.get(key);
	}
	public void setAttribute(String key,Object value){
		this.attributes.put(key, value);
	}

}
