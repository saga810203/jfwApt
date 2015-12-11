package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.CustomDMLSQLOperateCG;

public @interface CustomDMLSQL {
	String value();
	SqlValue[] sqlValues() default {};
	Class<?> handlerClass() default CustomDMLSQLOperateCG.class;
}
