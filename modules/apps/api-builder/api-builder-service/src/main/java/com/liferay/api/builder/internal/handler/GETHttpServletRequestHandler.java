package com.liferay.api.builder.internal.handler;

import com.liferay.info.item.InfoItemServiceRegistry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.service.PersistedModelLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.util.SearchUtil;
import com.liferay.portal.vulcan.yaml.openapi.Get;
import com.liferay.portal.vulcan.yaml.openapi.MappingsDefinition;
import com.liferay.portal.vulcan.yaml.openapi.OpenAPIYAML;
import com.liferay.portal.vulcan.yaml.openapi.PathItem;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matija Petanjek
 */
public class GETHttpServletRequestHandler {

	private String _getId(Pattern pathPattern, String  path) {
		Matcher matcher = pathPattern.matcher(path);

		if (matcher.find()) {
			return matcher.group(1);
		}

		return StringPool.BLANK;
	}

	public String handle(
			HttpServletRequest httpServletRequest,
			Map<String, String> pathParams,
			MappingsDefinition mappingsDefinition) {


		//TODO USE ELASTICSEARCH TO FETCH OBJECT -> fetch BlogPosting by Id
		//TODO 	_persistedModelLocalService.dslQuery() -> fetch DB entity -> NOT SURE IF THIS IS NEEDED, MAYBE WE CAN USE DIRECTLY INFO FRAMEWORK
		//TODO USE INFO FRAMEWORK TO -> parse Field values from DB entity
		//TODO use JSONWriter to create JSON based on #mappingsDefinition and INFO FRAMEWORK object

		return "HELLO";
	}

	private HttpServletRequest _httpServletRequest;
	private InfoItemServiceRegistry _infoItemServiceRegistry;
	private PersistedModelLocalService _persistedModelLocalService;
	private OpenAPIYAML _openAPIYAML;

}