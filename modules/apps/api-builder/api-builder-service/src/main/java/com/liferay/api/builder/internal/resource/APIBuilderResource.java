package com.liferay.api.builder.internal.resource;

import com.liferay.api.builder.internal.handler.GETHttpServletRequestHandler;
import com.liferay.api.builder.registry.APIBuilderOpenAPIRegistry;
import com.liferay.portal.vulcan.yaml.openapi.PathItem;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matija Petanjek
 */

@Component(
	properties = {
		"osgi.jaxrs.application.select=(osgi.jaxrs.name=Liferay.API.Builder)",
		"osgi.jaxrs.resource=true"
	},
	scope = ServiceScope.PROTOTYPE,
	service = APIBuilderResource.class
)
public class APIBuilderResource {

	@GET
	public Response get() {

		Map.Entry<Pattern, Map.Entry<String, PathItem>> pathItem =
			_apiBuilderOpenAPIRegistry.getPathItem(
				_httpServletRequest.getRequestURI());

		GETHttpServletRequestHandler getHttpServletRequestHandler =
			new GETHttpServletRequestHandler();

		getHttpServletRequestHandler.handle(
				_httpServletRequest,
				_getPathParams(
					pathItem.getKey(), _httpServletRequest.getRequestURI(),
					pathItem.getValue().getKey()),
				pathItem.getValue().getValue().getGet().getMappingsDefinition());

		return Response.ok("HELLO").build();
	}

	private List<String> _getPathParamNames(
		String path, Pattern pathPattern) {

		List<String> pathParamNames = new ArrayList<>();

		Matcher matcher = pathPattern.matcher(path);

		while(matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				pathParamNames.add(matcher.group(i));
			}
		}

		return pathParamNames;
	}

	private List<String> _getPathParamValues(
		String requestURI, Pattern pathPattern) {

		List<String> pathParamValues = new ArrayList<>();

		Matcher matcher = pathPattern.matcher(requestURI);

		while(matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				pathParamValues.add(matcher.group(i));
			}
		}

		return pathParamValues;
	}

	private Map<String, String> _getPathParams(Pattern pathPattern, String  requestUri, String path) {
		List<String> pathParamValues = _getPathParamValues(requestUri, pathPattern);
		List<String> pathParamNames = _getPathParamNames(path, pathPattern);

		return new HashMap<>();
	}

	@Reference
	private APIBuilderOpenAPIRegistry _apiBuilderOpenAPIRegistry;

	@Context
	private HttpServletRequest _httpServletRequest;
}