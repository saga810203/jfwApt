package org.jfw.apt.model.web.handlers.buildparam;

import java.util.Locale;


public class BaseListHeaderRPT  extends AbstractRequestHeaderTransfer {

    public void transferToParams() {
    	String cn = this.mpe.getTypeName();
        if (cn.equals("java.util.List<java.lang.Integer>")) {
            this.sb.append("Integer.valueof(params[i])");
        }  else if (cn.equals("java.util.List<java.lang.Byte>")) {
            this.sb.append("Byte.valueof(params[i])");
        } else if (cn.equals("java.util.List<java.lang.Short>")) {
            this.sb.append("Short.valueof(params[i])");
        } else if (cn.equals("java.util.List<java.lang.Long>")) {
            this.sb.append("Long.valueof(params[i])");
        }  else if (cn.equals("java.util.List<java.lang.Double>")) {
            this.sb.append("Double.valueof(params[i])");
        } else if (cn.equals("java.util.List<java.lang.Float>")) {
            this.sb.append("Float.valueof(params[i])");
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
        this.rmcg.readHeaders(sb,this.annotation.value().trim());
        String vn = this.mpe.getName();
        String tn = this.mpe.getTypeName();
        String en = tn.substring("java.util.List<".length(), tn.length()-1);
        String dv = this.annotation.defaultValue();
        if (this.annotation.required()) {
            sb.append("if(null==params || params.length==0){");
            this.raiseNoFoundError(this.annotation.value().trim());
            sb.append("}\r\n");
            sb.append(tn).append(" ").append(this.mpe.getName()).append(" = new java.util.ArrayList")
            .append("<").append(en).append(">()");
            
            sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(vn).append(".add(");
            this.transferToParams();
            sb.append(");\r\n}\r\n");
        } else {
        	 sb.append(tn).append(" ").append(this.mpe.getName()).append(";");
            sb.append("if(null!=params && params.length!=0){");
            sb.append(this.mpe.getName()).append(" = new java.util.ArrayList<")
            .append(en).append(">();");
            sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(vn)
                    .append("add(");
            this.transferToParams();
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
        this.rmcg.readHeaders(sb,this.annotation.value().trim());
        
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
            this.transferToParams();
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
                    .append("add(");
            this.transferToParams();
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
