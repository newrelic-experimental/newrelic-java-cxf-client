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

import com.newrelic.api.agent.ExternalParameters;
import com.newrelic.api.agent.HttpParameters;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TracedMethod;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.labs.cxf.CXFHeaders;

@Weave
public abstract class ClientImpl {

	@Trace
	private Object[] doInvoke(ClientCallback callback, BindingOperationInfo oi, Object[] params, Map<String, Object> context, Exchange exchange) {
		if(callback != null) {
			callback.token = NewRelic.getAgent().getTransaction().getToken();
		}
		
		CXFHeaders outWrapper = null;
		if(context != null) {
			outWrapper = new CXFHeaders(context);
		} else {
			outWrapper = new CXFHeaders(exchange);
		}
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(outWrapper);
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
								NewRelic.getAgent().getLogger().log(Level.FINER,e,"Exception getting URI from {0}",new Object[] { uriStr });
							}
						}
					}
				}
			}
			if (theURI != null) {
				ExternalParameters extParams = HttpParameters.library("CXF-Client").uri(theURI).procedure("invoke").noInboundHeaders().build();
				NewRelic.getAgent().getTracedMethod().reportAsExternal(extParams);
			}
		} else {
			NewRelic.getAgent().getLogger().log(Level.FINE, "input exchange object is null");
		}
		return Weaver.callOriginal();
	}
	
	@Trace
	protected Object[] processResult(Message message, Exchange exchange, BindingOperationInfo oi, Map<String, Object> resContext) {
		CXFHeaders wrapper = new CXFHeaders(resContext);
		TracedMethod traced = NewRelic.getAgent().getTracedMethod();
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
							NewRelic.getAgent().getLogger().log(Level.FINER, e, "Exception getting URI from {0}",  new Object[] {uriStr});
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
		traced.addCustomAttribute("Host", host);
		traced.addCustomAttribute("URI", uri);
		NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.HTTP, wrapper);
		return Weaver.callOriginal();
	}
}
