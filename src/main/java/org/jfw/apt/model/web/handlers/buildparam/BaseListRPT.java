package org.jfw.apt.model.web.handlers.buildparam;

import java.util.Locale;

import org.jfw.apt.Utils;

public class BaseListRPT extends AbstractRequestParamTransfer {

    public void transferToParams(String classType) {
    	String cn = classType;
        if (cn.equals("java.util.List<java.lang.Integer>")) {
            this.sb.append("Integer.valueOf(params[i])");
        }  else if (cn.equals("java.util.List<java.lang.Byte>")) {
            this.sb.append("Byte.valueOf(params[i])");
        } else if (cn.equals("java.util.List<java.lang.Short>")) {
            this.sb.append("Short.valueOf(params[i])");
        } else if (cn.equals("java.util.List<java.lang.Long>")) {
            this.sb.append("Long.valueOf(params[i])");
        }  else if (cn.equals("java.util.List<java.lang.Double>")) {
            this.sb.append("Double.valueOf(params[i])");
        } else if (cn.equals("java.util.List<java.lang.Float>")) {
            this.sb.append("Float.valueOf(params[i])");
        } else if (cn.equals("java.util.List<java.lang.Boolean>")) {
            this.sb.append("\"1\".equals(params[i])|| \"true\".equalsIgnoreCase(params[i])||\"yes\".equalsIgnoreCase(params[i])");
        } else if (cn.equals("java.util.List<java.lang.String>")) {
            this.sb.append("params[i]");
        } else if (cn.equals("java.util.List<java.math.BigInteger>")) {
            this.sb.append("new java.math.BigInteger(params[i])");
        } else if (cn.equals("java.util.List<java.math.BigDecimal>")) {
            this.sb.append("new java.math.BigDecimal(params[i])");
        } else {
            throw new IllegalArgumentException("noSupported class type" + cn);
        }
    }





    @Override
    public void bulidParam() {
    	String paramName =Utils.emptyToNull(this.annotation.value());
    	if(paramName==null) paramName = this.mpe.getName();
        this.rmcg.readParameters(sb,paramName);
        String vn = this.mpe.getName();
        String tn = this.mpe.getTypeName();
        String en = tn.substring("java.util.List<".length(), tn.length()-1);
        String dv = this.annotation.defaultValue();
        if (this.annotation.required()) {
            sb.append("if(null==params || params.length==0){");
            this.raiseNoFoundError(paramName);
            sb.append("}\r\n");
            sb.append(tn).append(" ").append(this.mpe.getName()).append(" = new java.util.ArrayList")
            .append("<").append(en).append(">();");
            
            sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(vn).append(".add(");
            this.transferToParams( this.mpe.getTypeName());
            sb.append(");\r\n}\r\n");
        } else {
        	 sb.append(tn).append(" ").append(this.mpe.getName()).append(";");
            sb.append("if(null!=params && params.length!=0){");
            sb.append(this.mpe.getName()).append(" = new java.util.ArrayList<")
            .append(en).append(">();");
            sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(vn)
                    .append(".add(");
            this.transferToParams( this.mpe.getTypeName());
            sb.append(");\r\n}\r\n");
            sb.append("}else{\r\n");
            sb.append(vn)
                    .append(" = ")
                    .append(dv==null ||dv.trim().length() == 0 ? "null" : this.annotation
                            .defaultValue());
            sb.append(";\r\n}\r\n");
        }
    }

    @Override
    public void bulidBeanProterty() {
    	this.checkRequestFieldParamName();
		this.rmcg.readParameters(sb, this.frp.getParamName().trim());
        
        String localName = this.rmcg.getTempalteVariableName();
        
        String tn = this.frp.getValueClassName();
        String en = tn.substring("java.util.List<".length(), tn.length()-1);
        String dv = this.frp.getDefaultValue();
        if (this.frp.isRequired()) {
            sb.append("if(null==params || params.length==0){");
            this.raiseNoFoundError(this.frp.getParamName().trim());
            sb.append("}\r\n");
            sb.append(tn).append(" ").append(localName).append(" = new java.util.ArrayList<").append(en).append(">()");
            
            sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(localName).append(".add(");
            this.transferToParams(this.frp.getValueClassName());
            sb.append(");\r\n}\r\n");
            sb.append(this.mpe.getName()).append(".set");
			String fed = this.frp.getValue().trim();
			sb.append(fed.substring(0, 1).toUpperCase(Locale.US));
			if (fed.length() > 1)
				sb.append(fed.substring(1));
			sb.append("(");
			sb.append(localName);
			sb.append(");\r\n");
        } else {
        	 sb.append(tn).append(" ").append(localName).append(";");
            sb.append("if(null!=params && params.length!=0){");
            sb.append(localName).append(" = new java.util.ArrayList<")
            .append(en).append(">();");
            sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(localName)
                    .append(".add(");
            this.transferToParams(this.frp.getValueClassName());
            sb.append(");\r\n}\r\n");
            sb.append("}else{\r\n");
            sb.append(localName)
                    .append(" = ")
                    .append(dv==null ||dv.trim().length() == 0 ? "null" : this.annotation
                            .defaultValue());
            sb.append(";\r\n}\r\n");
            sb.append(this.mpe.getName()).append(".set");
			String fed = this.frp.getValue().trim();
			sb.append(fed.substring(0, 1).toUpperCase(Locale.US));
			if (fed.length() > 1)
				sb.append(fed.substring(1));
			sb.append("(");
			sb.append(localName);
			sb.append(");\r\n");
        }
    }
}
