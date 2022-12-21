package com.liferay.api.builder.registry;

import com.liferay.portal.vulcan.yaml.openapi.PathItem;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Matija Petanjek
 */
public interface APIBuilderOpenAPIRegistry {

	public Map.Entry<Pattern, Map.Entry<String, PathItem>> getPathItem(
		String path);

}