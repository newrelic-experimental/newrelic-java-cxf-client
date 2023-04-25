package org.apache.cxf.endpoint;

import java.util.Map;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.Token;
import com.newrelic.agent.bridge.TracedMethod;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.field.cxf.InboundWrapper;

@Weave(originalName="org.apache.cxf.endpoint.ClientCallback")
public abstract class ClientCallback {
	
	@NewField
	public Token token;

	@Trace(async=true)
	public void handleResponse(Map<String, Object> ctx, Object[] res) {
		if(token != null) {
			token.linkAndExpire();
		}
		InboundWrapper wrapper = new InboundWrapper(ctx);
		TracedMethod traced = AgentBridge.getAgent().getTracedMethod();
		String host = "UnknownHost";
		String uri = "/Unknown";
		AgentBridge.getAgent().getTransaction().getCrossProcessState().processInboundResponseHeaders(wrapper, traced, host, uri, false);
		Weaver.callOriginal();
	}
}
