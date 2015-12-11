package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.jfw.apt.exception.AptException;

public class OrmDefine {
	private List<PersistentObject> virtualTables = new ArrayList<PersistentObject>();
	private List<PersistentObject> tables = new ArrayList<PersistentObject>();
	private List<PersistentObject> extendTables = new ArrayList<PersistentObject>();
	private List<PersistentObject> views = new ArrayList<PersistentObject>();
	private List<PersistentObject> extendViews = new ArrayList<PersistentObject>();

	private static void addPersistentObject(List<PersistentObject> list, PersistentObject po) {
		if (!list.contains(po))
			list.add(po);
	}

	public void addPersistentObject(PersistentObject po) {
		PersistentObjectKind kind = po.getKind();
		if (kind == PersistentObjectKind.VIRTUAL_TABLE) {
			addPersistentObject(this.virtualTables, po);
		} else if (kind == PersistentObjectKind.TABLE) {
			addPersistentObject(this.tables, po);
		} else if (kind == PersistentObjectKind.EXTEND_TABLE) {
			addPersistentObject(this.extendTables, po);
		} else if (kind == PersistentObjectKind.VIEW) {
			addPersistentObject(this.views, po);
		} else {
			addPersistentObject(this.extendViews, po);
		}
	}

	public PersistentObject getPersistentObject(String javaName) {
		PersistentObject po = null;
		if (po == null) {
			po = getPersistentObject(virtualTables, javaName);
		}
		if (po == null) {
			po = getPersistentObject(tables, javaName);
		}
		if (po == null) {
			po = getPersistentObject(extendTables, javaName);
		}
		if (po == null) {
			po = getPersistentObject(views, javaName);
		}
		if (po == null) {
			po = getPersistentObject(extendViews, javaName);
		}
		return po;
	}

	public static PersistentObject getPersistentObject(List<PersistentObject> list, String javaName) {
		for (PersistentObject po : list) {
			if (javaName.equals(po.getJavaName()))
				return po;
		}
		return null;
	}

	public PersistentObject getSupperPersistentObject(TypeElement ref, PersistentObjectKind... seache) {
		TypeMirror supper = ref.getSuperclass();
		if (supper.getKind() == TypeKind.NONE)
			return null;
		DeclaredType dt = (DeclaredType) supper;

		Element ele = dt.asElement();
		if (ele.getKind() == ElementKind.INTERFACE)
			return null;
		TypeElement supperEle = (TypeElement) ele;

		String javaname = supperEle.getQualifiedName().toString();

		PersistentObject po = this.getPersistentObject(javaname);
		if (po == null)
			return null;
		for (PersistentObjectKind pok : seache) {
			if (po.getKind() == pok)
				return po;
		}
		return getSupperPersistentObject(supperEle, seache);
	}

	public PersistentObject getPersistentObject(TypeElement ref, PersistentObjectKind... seache) {
		PersistentObject po = this.getPersistentObject(ref.getQualifiedName().toString());
		if (po != null) {
			for (PersistentObjectKind pok : seache) {
				if (pok == po.getKind())
					return po;
			}
		}
		return this.getSupperPersistentObject(ref, seache);
	}

	public void initPersistentObjects() throws AptException {
		for (PersistentObject po : this.virtualTables) {
			po.init(this);
		}
		for (PersistentObject po : this.tables) {
			po.init(this);
		}
		for (PersistentObject po : this.extendTables) {
			po.init(this);
		}
		for (PersistentObject po : this.views) {
			po.init(this);
		}
		for (PersistentObject po : this.extendViews) {
			po.init(this);
		}
	}

	public void warnMessage(Messager messager) {
		for (PersistentObject po : this.virtualTables) {
			po.warnMessage(messager);
		}
		for (PersistentObject po : this.tables) {
			po.warnMessage(messager);
		}
		for (PersistentObject po : this.extendTables) {
			po.warnMessage(messager);
		}
		for (PersistentObject po : this.views) {
			po.warnMessage(messager);
		}
		for (PersistentObject po : this.extendViews) {
			po.warnMessage(messager);
		}
	}
}
