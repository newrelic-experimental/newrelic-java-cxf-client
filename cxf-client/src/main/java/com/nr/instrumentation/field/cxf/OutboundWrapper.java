package com.nr.instrumentation.field.cxf;

import java.util.Map;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.OutboundHeaders;

public class OutboundWrapper implements OutboundHeaders {

	private Map<String, Object> request;
	
	public OutboundWrapper(Map<String, Object> map) {
		request = map;
	}
	
	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public void setHeader(String name, String value) {
		request.put(name, value);
	}

}
