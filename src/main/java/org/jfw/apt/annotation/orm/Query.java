package org.jfw.apt.annotation.orm;

import org.jfw.apt.model.orm.QueryOperateCG;
import org.jfw.apt.orm.core.enums.DataElement;

public @interface Query {
    String singleColumnSql() default "";
	boolean singleRow() default false;
	DataElement singleColumn() default DataElement.invalid_de;
	String otherSentence() default "";
	Where where() default @Where();
	Class<?> handlerClass() default QueryOperateCG.class;
}
