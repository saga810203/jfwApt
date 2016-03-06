package org.jfw.apt;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.orm.OrmDefine;
import org.jfw.apt.model.orm.PersistentObject;
import org.jfw.apt.out.model.BeanConfig;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JfwProccess extends javax.annotation.processing.AbstractProcessor {
	private BeanConfig beanConfig = new BeanConfig();
	private Messager messager;
	private Filer filer;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Set<? extends TypeElement> annotations;
	private RoundEnvironment roundEnv;
	private Set<? extends Element> rootElements;

	private Element currentElement = null;

	public void changeElement(Element ele) {
		if (ele != null)
			this.currentElement = ele;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.messager = processingEnv.getMessager();
		this.filer = processingEnv.getFiler();
	}

	private void handleOrm() throws AptException {
		OrmDefine od = new OrmDefine();
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			this.changeElement(ele);
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.VirtualTable.class)) {
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.VirtualTable.class));
				od.addPersistentObject(po);
			}
		}
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.Table.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.Table.class));
				od.addPersistentObject(po);
			}
		}
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendTable.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendTable.class));
				od.addPersistentObject(po);
			}
		}

		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.View.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.View.class));
				od.addPersistentObject(po);
			}
		}
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendView.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendView.class));
				od.addPersistentObject(po);
			}
		}
		od.initPersistentObjects();
		this.setAttribute(OrmDefine.class.getName(), od);
		String sql = od.generateAllDDL();
		if (sql.length() > 0)
			this.saveResourceFile("jfw_table.sql", sql);
		// od.warnMessage(messager);

	}

	private void handle(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws AptException {
		this.rootElements = roundEnv.getRootElements();
		this.annotations = annotations;
		this.roundEnv = roundEnv;
		this.handleOrm();
		this.handleCodeGenerateHandler();
	}

	// private void warnMethodInfo(TypeElement ref) throws AptException{
	// for(Element ele:ref.getEnclosedElements()){
	// if(ele.getKind()!= ElementKind.METHOD) continue;
	// ExecutableElement el =(ExecutableElement)ele;
	// this.warnMethodInfo((ExecutableElement)ele);
	//
	// }
	//
	// }

	// private void warnMethodInfo(ExecutableElement ref) throws AptException{
	// StringBuilder sb = new StringBuilder();
	// Set<Modifier> ms = ref.getModifiers();
	// for( Modifier m:ms) sb.append(m.toString()).append(" ");
	//
	// sb.append(Utils.getReturnTypeName(ref.getReturnType(), ref)).append(" ");
	// sb.append(ref.getSimpleName().toString()).append("(");
	//
	// List<? extends VariableElement> eles = ref.getParameters();
	// for(int i = 0 ; i < eles.size() ; ++i){
	// VariableElement ele = eles.get(i);
	// if(i !=0) sb.append(",");
	// sb.append(Utils.getReturnTypeName(ele.asType(), ele)).append("
	// ").append(ele.getSimpleName().toString());
	// }
	// sb.append(")");
	//
	// List<? extends TypeMirror> ths = ref.getThrownTypes();
	// for(int i = 0 ; i < ths.size() ; ++i){
	// if(i==0){
	// sb.append(" throws ");
	// }else{
	// sb.append(",");
	// }
	// sb.append(Utils.getReturnTypeName(ths.get(i), ref));
	// }
	//
	// this.messager.printMessage(Kind.WARNING, sb.toString(),ref);
	// }

	private void handleCodeGenerateHandler() throws AptException {
		Map<Class<?>, CodeGenerateAllAfterEventByType> afterEvents = new HashMap<Class<?>, CodeGenerateAllAfterEventByType>();

		for (Element ele : this.rootElements) {
			if ((ele.getKind() != ElementKind.CLASS) && (ele.getKind() != ElementKind.INTERFACE))
				continue;
			if (!(ele instanceof TypeElement))
				continue;
			TypeElement el = (TypeElement) ele;
			if (el.getNestingKind().isNested())
				continue;

			List<? extends TypeParameterElement> list = el.getTypeParameters();
			if (list != null && list.size() > 0)
				continue;
			List<? extends AnnotationMirror> ans = el.getAnnotationMirrors();
			System.out.println(el.toString());
			for (AnnotationMirror anm : ans) {
				Object obj = Utils.getReturnValueOnAnnotation("handlerClass", anm);
				
				Class<CodeGenerateHandler> cghcls = Utils.getClass(obj, CodeGenerateHandler.class);
				if(cghcls!=null){
					CodeGenerateHandler cgh = null;
					try {
						cgh =cghcls.newInstance();
//						if (!afterEvents.containsKey(cgh.getClass())) {
//							CodeGenerateAllAfterEventByType cgaaebt = cgh.getStaticAfterEvent();
//							if (cgaaebt != null) {
//								afterEvents.put(cgh.getClass(), cgaaebt);
//							}
//						}

					} catch (Exception e) {
						throw new AptException(ele, "create Object instance with " + cghcls.getName()
								+ "error:" + e.getMessage());
					}
					cgh.setEnv(this.attributes);
					this.changeElement(ele);
					cgh.handle(el, anm, this.getAnnotationObj(el, anm));
					break;
				}

			}
		}

		for (CodeGenerateAllAfterEventByType c : afterEvents.values()) {
			if (c != null)
				c.execute(this);
		}
	}

	public void saveResourceFile(String fileName, String fileContent) throws AptException {
		try {
			FileObject fo = this.filer.createResource(javax.tools.StandardLocation.SOURCE_OUTPUT, "", fileName,
					(Element[]) null);
			OutputStream os = fo.openOutputStream();
			try {
				os.write(fileContent.getBytes("UTF-8"));
				os.flush();
			} finally {
				os.close();
			}
		} catch (Exception e) {
			throw new AptException(this.currentElement, "save resource file[" + fileName + "] error:" + e.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	private Object getAnnotationObj(Element ele, AnnotationMirror am) {
		try {
			TypeElement type = (TypeElement) am.getAnnotationType().asElement();
			String cn = type.getQualifiedName().toString();
			Class<? extends Annotation> cls = (Class<? extends Annotation>) Class.forName(cn);
			return ele.getAnnotation(cls);
		} catch (Exception e) {
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
		try {
			this.handle(annotations, roundEnv);
			if(annotations.isEmpty()){
				StringBuilder sb = new StringBuilder();
				this.beanConfig.appendTo(sb);
				this.saveResourceFile("beanConfig.properties", sb.toString());
			}
		} catch (AptException e) {
			this.messager.printMessage(Kind.ERROR, e.getMessage(), e.getEle());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			String m = e.getMessage();
			if (m == null)
				m = "nullException";
			this.messager.printMessage(Kind.ERROR, m, this.currentElement);
		}
		

		return true;
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	private static Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

		files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

		Set<javax.tools.JavaFileObject.Kind> fileKinds = Collections.singleton(javax.tools.JavaFileObject.Kind.SOURCE);
		return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
	}

	public static void main(String[] args) throws Exception {
		String source = "E:\\EclipseProject\\JFW\\aptTest2\\src";

		Iterable<JavaFileObject> files = getSourceFiles(source);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		CompilationTask task = compiler.getTask(new PrintWriter(System.out), null, null, null, null, files);
		task.setProcessors(Arrays.asList(new JfwProccess()));

		task.call();
	}

	public Messager getMessager() {
		return messager;
	}

	public Filer getFiler() {
		return filer;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Set<? extends TypeElement> getAnnotations() {
		return annotations;
	}

	public RoundEnvironment getRoundEnv() {
		return roundEnv;
	}
	public BeanConfig getBeanConfig(){
		return this.beanConfig;
	}

}
