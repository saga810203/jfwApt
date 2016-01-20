package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.annotation.web.RequestParam;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;

public abstract class AbstractRequestParamTransfer implements RequestParamTransfer {
	protected StringBuilder sb;
	protected MethodParamEntry mpe;
	protected RequestMappingCodeGenerator rmcg;
	protected RequestParam annotation;
	protected RequestParamTransfer.FieldRequestParam frp;

	public abstract void bulidParam();

	public abstract void bulidBeanProterty();

	public void checkRequestParamName() {
		if (annotation.value() == null || annotation.value().trim().length() == 0) {
			throw new RuntimeException("@RequestParam not set value");
		}
	}

	public void checkRequestFieldParamName() {
		if (this.frp.getValue() == null || this.frp.getValue().trim().length() == 0) {
			throw new RuntimeException("@RequestParam.fields no set value");
		}
	}

	public void raiseNoFoundError(String paramName) {
		this.sb.append("throw new IllegalArgumentException(\"not found parameter:" + paramName + "\");");
	}

	@Override
	public void transfer(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg,
			RequestParam annotation) {
		this.sb = sb;
		this.mpe = mpe;
		this.annotation = annotation;
		this.frp = null;
		this.bulidParam();
	}

	@Override
	public void transferBeanProperty(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg,
			RequestParamTransfer.FieldRequestParam frp) {
		this.sb = sb;
		this.mpe = mpe;
		this.frp = frp;
		this.bulidBeanProterty();
	}

}
