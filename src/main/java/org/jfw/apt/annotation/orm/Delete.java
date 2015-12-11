package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.DeleteOperateCG;

public @interface Delete {
	String value() default "PrimaryKey";
	Class<?> target() default Object.class; 
	Class<?> handlerClass() default DeleteOperateCG.class;
}
