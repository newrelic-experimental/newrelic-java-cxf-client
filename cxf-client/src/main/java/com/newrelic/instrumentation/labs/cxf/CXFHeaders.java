package com.newrelic.instrumentation.labs.cxf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;

public class CXFHeaders implements Headers {
	
	private Map<String, Object> httpobject;

	public CXFHeaders(Map<String, Object> req) {
		httpobject = req;
	}

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.HTTP;
	}

	@Override
	public String getHeader(String name) {
		Object value = httpobject.get(name);
		
		return value != null ? value.toString() : null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		List<String> list = new ArrayList<>();
		String value = getHeader(name);
		if(value != null && !value.isEmpty()) {
			list.add(value);
		}
		return list;
	}

	@Override
	public void setHeader(String name, String value) {
		httpobject.put(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		httpobject.put(name, value);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return httpobject.keySet();
	}

	@Override
	public boolean containsHeader(String name) {
		return getHeaderNames().contains(name);
	}

}
