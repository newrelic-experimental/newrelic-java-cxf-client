package com.nr.instrumentation.field.cxf;

import java.util.Map;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.InboundHeaders;

public class InboundWrapper implements InboundHeaders {

	private Map<String, Object> response;

	public InboundWrapper(Map<String, Object> response) {
		super();
		this.response = response;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public String getHeader(String name) {
		return (String) response.get(name);
	}

}
