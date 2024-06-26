package org.apache.cxf.endpoint;

import java.util.Map;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.cxf.CXFHeaders;

@Weave(originalName="org.apache.cxf.endpoint.ClientCallback")
public abstract class ClientCallback {
	
	@NewField
	public Token token;

	@Trace(async=true)
	public void handleResponse(Map<String, Object> ctx, Object[] res) {
		if(token != null) {
			token.linkAndExpire();
		}
		CXFHeaders wrapper = new CXFHeaders(ctx);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.HTTP, wrapper);
		Weaver.callOriginal();
	}
}
