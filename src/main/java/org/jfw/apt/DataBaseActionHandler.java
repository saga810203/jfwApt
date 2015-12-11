package org.jfw.apt;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.orm.DBOperateCG;

public class DataBaseActionHandler extends AbstractCodeGenerateHandler {
	

	@Override
	public boolean isSupportedInterFace() {
		return false;
	}

	@Override
	public boolean isSupportedAbstractClass() {
		return true;
	}

	@Override
	public boolean isSupportedNoAbstractClass() {
		return false;
	}

	@Override
	protected void writeContent(StringBuilder sb) throws AptException {
		sb.append(this.className).append(" ").append(Utils.classNameToInstanceName(this.className)).append(" = new ");
		sb.append(this.className).append("();\r\n");	
		for(Element ele:this.ref.getEnclosedElements()){
			if(ele.getKind()==ElementKind.METHOD){
				 List<? extends AnnotationMirror> ans= ele.getAnnotationMirrors();
				 boolean handled = false;
				 for(AnnotationMirror anm:ans){
					for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:anm.getElementValues().entrySet()){
						ExecutableElement ee = entry.getKey();
						if(ee.getSimpleName().toString()=="handlerClass"){
							Object obj = entry.getValue().getValue();
							if((obj instanceof Class)&&(DBOperateCG.class.isAssignableFrom((Class<?>)obj))){
								DBOperateCG dbocg = null;
								try{
									dbocg =(DBOperateCG) ((Class<?>)obj).newInstance();
								}catch(Exception e){
									throw new AptException(ele,"create Object instance with "+((Class<?>)obj).getName()+"error:"+e.getMessage());
								}
								for(Map.Entry<String,Object> een:this.env.entrySet()){
									dbocg.setAttribute(een.getKey(), een.getValue());
								}
								sb.append("\r\n").append(dbocg.getCode((ExecutableElement) ele)).append("\r\n");
								handled = true;
								break;
							}						
						}
					}
					if(handled) break;
				 }
			}
		}
		
	}

}
