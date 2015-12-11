package org.jfw.apt.annotation.orm;

public @interface Where {
	String sentence() default "";
	boolean and() default true;
	boolean dynamic() default false;
	SqlValue[] values() default {};
}
