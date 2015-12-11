package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.UpdateOperateCG;

public @interface Update {
	boolean dynamicValue() default false;
	String value() default "PrimaryKey";
	Class<?> handlerClass() default UpdateOperateCG.class;
}
