package org.jfw.apt.annotation.orm;

public @interface CustomDMLSQL {
	String value();
	SqlValue[] sqlValues() default {};
}
