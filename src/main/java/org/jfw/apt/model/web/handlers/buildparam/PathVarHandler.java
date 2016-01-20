package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.web.PathVar;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class PathVarHandler extends BuildParamHandler.BuildParameter {

	private int getIndexInPath(String name, String path) {
		String pathL = "{" + name.trim() + "}";
		String[] paths = path.split("/");
		for (int i = 1; i < paths.length; ++i) {
			if (pathL.equals(paths[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		PathVar pv = (PathVar) mpe.getRef().getAnnotation(PathVar.class);
		if(pv==null) return;
		
		rmcg.readURI(sb,pv.pathAttribute());
		String val = Utils.emptyToNull(pv.value());
		if (val == null ) {
			throw new AptException(mpe.getRef(),"invalid annotation @PathVar");
		}
		String path = rmcg.getUri();
		int pathIndex = getIndexInPath(val, path);
		if (pathIndex <= 0)
			throw new AptException(mpe.getRef(),"invalid annotation @PathVar ");
		
		sb.append(mpe.getTypeName()).append(" ").append(mpe.getName()).append(" = ");
		
		String lTypeName = mpe.getTypeName();
		if (lTypeName.equals(int.class.getName())) {
			sb.append("Integer.parseInt(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(Integer.class.getName())) {
			sb.append("Integer.valueof(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(byte.class.getName())) {
			sb.append("Byte.parseByte(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(Byte.class.getName())) {
			sb.append("Byte.valueof(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(short.class.getName())) {
			sb.append("Short.parseShort(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(Short.class.getName())) {
			sb.append("Short.valueof(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(long.class.getName())) {
			sb.append("Long.parseLong(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(Long.class.getName())) {
			sb.append("Long.valueof(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(double.class.getName())) {
			sb.append("Double.parseDouble(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(Double.class.getName())) {
			sb.append("Double.valueof(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(float.class.getName())) {
			sb.append("Float.parseFloat(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(Float.class.getName())) {
			sb.append("Float.valueof(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(boolean.class.getName()) || lTypeName.equals(Boolean.class.getName())) {
			sb.append("\"1\".equals(uriArray[").append(pathIndex)
					.append("])|| \"true\".equalsIgnoreCase(uriArray[").append(pathIndex)
					.append("])||\"yes\".equalsIgnoreCase(uriArray[").append(pathIndex).append("])");
		} else if (lTypeName.equals(String.class.getName())) {
			if (pv.encoding()) {
				sb.append("java.net.URLDecoder.decode(uriArray[").append(pathIndex).append("],\"UTF-8\");");
			} else {
				sb.append("uriArray[").append(pathIndex).append("]");
			}
		} else if (lTypeName.equals(java.math.BigInteger.class.getName())) {
			sb.append("java.math.BigInteger.valueof(uriArray[").append(pathIndex).append("]);");
		} else if (lTypeName.equals(java.math.BigDecimal.class.getName())) {
			sb.append("java.math.BigDecimal.valueof(uriArray[").append(pathIndex).append("]);");
		} else {
			throw new AptException(mpe.getRef(),"UnSupportedType on paramter with @PathVar");
		}
	}

}
