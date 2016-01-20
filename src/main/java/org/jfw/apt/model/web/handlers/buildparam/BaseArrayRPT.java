package org.jfw.apt.model.web.handlers.buildparam;

import java.util.Locale;

public class BaseArrayRPT extends AbstractRequestParamTransfer {

	public void transferToParams() {
		String cn = this.mpe.getTypeName();

		if (cn.equals("int[]")) {
			this.sb.append("Integer.parseInt(params[i])");
		} else if (cn.equals("java.lang.Integer[]")) {
			this.sb.append("Integer.valueof(params[i])");
		} else if (cn.equals("byte[]")) {
			this.sb.append("Byte.parseByte(params[i])");
		} else if (cn.equals("java.lang.Byte[]")) {
			this.sb.append("Byte.valueof(params[i])");
		} else if (cn.equals("short[]")) {
			this.sb.append("Short.parseShort(params[i])");
		} else if (cn.equals("java.lang.Short[]")) {
			this.sb.append("Short.valueof(params[i])");
		} else if (cn.equals("long[]")) {
			this.sb.append("Long.parseLong(params[i])");
		} else if (cn.equals("java.lang.Long[]")) {
			this.sb.append("Long.valueof(params[i])");
		} else if (cn.equals("double[]")) {
			this.sb.append("Double.parseDouble(params[i])");
		} else if (cn.equals("java.lang.Double[]")) {
			this.sb.append("Double.valueof(params[i])");
		} else if (cn.equals("float[]")) {
			this.sb.append("Float.parseFloat(params[i])");
		} else if (cn.equals("java.lang.Float[]")) {
			this.sb.append("Float.valueof(params[i])");
		} else if (cn.equals("boolean[]") || cn.equals("java.lang.Boolean[]")) {
			this.sb.append(
					"\"1\".equals(params[i])|| \"true\".equalsIgnoreCase(params[i])||\"yes\".equalsIgnoreCase(params[i])");
		} else if (cn.equals("java.lang.String[]")) {
			this.sb.append("params[i]");
		} else if (cn.equals("java.math.BigInteger[]")) {
			this.sb.append("new java.math.BigInteger(params[i])");
		} else if (cn.equals("java.math.BigDecmal[]")) {
			this.sb.append("new java.math.BigDecimal(params[i])");
		} else {
			throw new IllegalArgumentException("not supperted class type:" + cn);
		}
	}

	@Override
	public void bulidParam() {
		String vTypeName = this.mpe.getTypeName();
		this.rmcg.readParameters(sb, this.annotation.value().trim());
		if (this.annotation.required()) {
			sb.append("if(null==params || params.length==0){");
			this.raiseNoFoundError(this.annotation.value().trim());
			sb.append("}\r\n").append(vTypeName).append(" ").append(this.mpe.getName()).append(" = new ")
					.append(vTypeName.substring(1, vTypeName.length() - 1)).append("params.length];\r\n");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(this.mpe.getName()).append("[i]=");
			this.transferToParams();
			sb.append(";\r\n}\r\n");
		} else {
			sb.append(vTypeName).append(" ").append(this.mpe.getName()).append(";\r\n");

			sb.append("if(null!=params && params.length!=0){");
			sb.append(this.mpe.getName()).append(" = new ").append(vTypeName.substring(0, vTypeName.length() - 1))
					.append("params.length];");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(this.mpe.getName()).append("[i]=");
			this.transferToParams();
			sb.append(";\r\n}\r\n");
			sb.append("}else{\r\n");

			String dv = this.annotation.defaultValue();

			sb.append(this.mpe.getName()).append(" = ")
					.append((dv == null || dv.trim().length() == 0) ? "null" : this.annotation.defaultValue())
					.append(";\r\n");
			sb.append(";\r\n}\r\n");
		}
	}

	@Override
	public void bulidBeanProterty() {
		String vTypeName = this.mpe.getTypeName();
		String localName = this.rmcg.getTempalteVariableName();
		this.checkRequestFieldParamName();
		this.rmcg.readParameters(sb, this.frp.getParamName().trim());
		if (this.frp.isRequired()) {
			sb.append("if(null==params || params.length==0){");
			this.raiseNoFoundError(this.frp.getParamName().trim());
			sb.append("}\r\n");
			sb.append(vTypeName).append(" ").append(localName).append(" = new ")
					.append(vTypeName.substring(1, vTypeName.length() - 1)).append("params.length];\r\n");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(localName).append("[i]=");
			this.transferToParams();
			sb.append(";\r\n}\r\n");
			sb.append(this.mpe.getName()).append(".set");
			String fed = this.frp.getValue().trim();
			sb.append(fed.substring(0, 1).toUpperCase(Locale.US));
			if (fed.length() > 1)
				sb.append(fed.substring(1));
			sb.append("(");
			sb.append(localName);
			sb.append(");\r\n");
		} else {
			sb.append("if(null!=params && params.length!=0){\r\n");
			sb.append(vTypeName).append(" ").append(localName).append(" = new ")
			.append(vTypeName.substring(1, vTypeName.length() - 1)).append("params.length];\r\n");
			sb.append("for( int i = 0 ; i < params.length ; ++i){\r\n").append(localName).append("[i]=");
			this.transferToParams();
			sb.append(";\r\n}\r\n");
			sb.append(this.mpe.getName()).append(".set");
			String fed = this.frp.getValue().trim();
			sb.append(fed.substring(0, 1).toUpperCase(Locale.US));
			if (fed.length() > 1)
				sb.append(fed.substring(1));
			sb.append("(");
			sb.append(localName);
			sb.append(");\r\n");
			sb.append("\r\n}\r\n");
			String dv = this.frp.getDefaultValue();
			if (dv!=null&& dv.trim().length() != 0) {
				sb.append("else{");
				sb.append(this.mpe.getName()).append(".set");
				fed = this.frp.getValue().trim();
				sb.append(fed.substring(0, 1).toUpperCase(Locale.US));
				if (fed.length() > 1)
					sb.append(fed.substring(1));
				sb.append("(");
				sb.append(dv);
				sb.append(");\r\n");
				sb.append("\r\n}\r\n");
			}
		}
	}
}
