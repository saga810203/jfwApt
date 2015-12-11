
package org.jfw.apt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;

public class AbstractMethodGenerater {
	protected Map<String,Object> attributes = new HashMap<String,Object>();
	protected String returnType;
	protected String name;
	protected List<MethodParamEntry> params = new ArrayList<MethodParamEntry>();
	protected List<String> throwables =  new ArrayList<String>() ;
	protected ExecutableElement ref;
	protected TypeElement enclosing;
	protected List<String> modifiers = new ArrayList<String>() ;
	
	
	
	
	public void fillMeta(ExecutableElement ref) throws AptException{
		this.ref = ref;
		this.modifiers.clear();
		for(Modifier m:ref.getModifiers()){
			this.modifiers.add(m.toString());
		}				
		this.enclosing = (TypeElement)ref.getEnclosingElement();		
		this.returnType = Utils.getReturnTypeName(ref.getReturnType(), ref);
		this.name = ref.getSimpleName().toString();

		List<? extends VariableElement> eles = ref.getParameters();
		this.params.clear();
		for(int i = 0 ; i < eles.size() ; ++i){
			this.params.add(MethodParamEntry.build(eles.get(i)));
		}
		
	    List<? extends TypeMirror> ths =	ref.getThrownTypes();
	    this.throwables.clear();
	    for(int i = 0 ; i < ths.size() ; ++i){
	    	this.throwables.add(Utils.getReturnTypeName(ths.get(i), ref));
	    }
	}
	
	public void setAttribute(String key,Object obj){
		this.attributes.put(key,obj);
	}
	public Object getAttribute(String key){
		return attributes.get(key);
	}
	
	private static final String TVN = AbstractMethodGenerater.class.getName()+"_TVN";
	public String getTempalteVariableName(){
		Object obj = this.attributes.get(TVN);
		int i = obj==null?0:((Integer)obj).intValue();		
		++i;
		this.attributes.put(TVN,i);
		return "tmp"+i;		
	}
	

}
