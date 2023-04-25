package org.apache.cxf.endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.agent.bridge.TracedMethod;
import com.newrelic.agent.bridge.external.ExternalParameters;
import com.newrelic.agent.bridge.external.ExternalParametersFactory;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.nr.instrumentation.field.cxf.InboundWrapper;
import com.nr.instrumentation.field.cxf.OutboundWrapper;

@Weave
public abstract class ClientImpl {

	@Trace
	private Object[] doInvoke(ClientCallback callback, BindingOperationInfo oi, Object[] params, Map<String, Object> context, Exchange exchange) {
		if(callback != null) {
			callback.token = AgentBridge.getAgent().getTransaction().getToken();
		}
		
		OutboundWrapper outWrapper = null;
		if(context != null) {
			outWrapper = new OutboundWrapper(context);
		} else {
			outWrapper = new OutboundWrapper(exchange);
		}
		AgentBridge.getAgent().getTransaction().getCrossProcessState().processOutboundRequestHeaders(outWrapper);
		if (exchange != null) {
			Destination dest = exchange.getDestination();
			URI theURI = null;
			if (dest != null) {
				EndpointReferenceType address = dest.getAddress();
				if (address != null) {
					AttributedURIType attruri = address.getAddress();
					if (attruri != null) {
						String uriStr = attruri.getValue();
						if (uriStr != null) {
							try {
								theURI = new URI(uriStr);
							} catch (URISyntaxException e) {
								AgentBridge.getAgent().getLogger().log(Level.FINER,e,"Exception getting URI from {0}",new Object[] { uriStr });
							}
						}
					}
				}
			}
			if (theURI != null) {
				ExternalParameters extParams = ExternalParametersFactory.createForHttp("CXF-Client", theURI, "invoke");
				AgentBridge.getAgent().getTracedMethod().reportAsExternal(extParams);
			}
		} else {
			AgentBridge.getAgent().getLogger().log(Level.FINE, "input exchange object is null");
		}
		return Weaver.callOriginal();
	}
	
	@Trace
	protected Object[] processResult(Message message, Exchange exchange, BindingOperationInfo oi, Map<String, Object> resContext) {
		InboundWrapper wrapper = new InboundWrapper(resContext);
		TracedMethod traced = AgentBridge.getAgent().getTracedMethod();
		Destination dest = exchange.getDestination();
		String uri = null;
		String host = null;
		if(dest != null) {
			EndpointReferenceType address = dest.getAddress();
			if(address != null) {
				AttributedURIType attruri = address.getAddress();
				if(attruri != null) {
					String uriStr = attruri.getValue();
					if(uriStr != null) {
						try {
							URI theURI = new URI(uriStr);
							uri = theURI.getPath();
							host = theURI.getHost();
						} catch (URISyntaxException e) {
							AgentBridge.getAgent().getLogger().log(Level.FINER, e, "Exception getting URI from {0}",  new Object[] {uriStr});
						}
					}
				}
			}
		}
		if(host == null) {
			host = "UnknownHost";
		}
		if(uri == null) {
			uri = "UnknownPath";
		}
		AgentBridge.getAgent().getTransaction().getCrossProcessState().processInboundResponseHeaders(wrapper, traced, host, uri, false);
		return Weaver.callOriginal();
	}
}
