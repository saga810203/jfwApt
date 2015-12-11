package org.jfw.apt.annotation.orm;

import org.jfw.apt.orm.core.enums.DataElement;

public @interface SqlValue {
	DataElement de();
	String paramName() default "";
	String attributeName() default "";
	// read value from method parameter
	String paramExpression() default "";
	boolean nullable() default false;
	String valueExpression() default "";
}
