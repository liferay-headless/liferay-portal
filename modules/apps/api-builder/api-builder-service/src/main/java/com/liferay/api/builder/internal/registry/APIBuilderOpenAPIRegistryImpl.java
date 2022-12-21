/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.api.builder.internal.registry;

import com.liferay.api.builder.constants.APIBuilderConstants;
import com.liferay.api.builder.registry.APIBuilderOpenAPIRegistry;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.vulcan.yaml.YAMLUtil;
import com.liferay.portal.vulcan.yaml.openapi.OpenAPIYAML;
import com.liferay.portal.vulcan.yaml.openapi.PathItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Matija Petanjek
 *
 * This should be a REST service where user will be able to submit an openAPI spec.
 */
@Component(immediate = true, service = APIBuilderOpenAPIRegistry.class)
public class APIBuilderOpenAPIRegistryImpl
	implements APIBuilderOpenAPIRegistry {

	public Map.Entry<Pattern, Map.Entry<String, PathItem>> getPathItem(String path) {
		for (Map.Entry<Pattern, Map.Entry<String, PathItem>> entry :
				_pathItemsMap.entrySet()) {

			Pattern pattern = entry.getKey();

			Matcher matcher = pattern.matcher(path);

			if (matcher.matches()) {
				return entry;
			}
		}

		throw new IllegalArgumentException(
			"Endpoint is not registered with API Builder");
	}

	@Activate
	protected void activate(BundleContext bundleContext) throws Exception {
		OpenAPIYAML openAPIYAML = null;

		try (InputStream inputStream = getClass().getResourceAsStream(
				"/builder-openapi.yaml");
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream))) {

			Stream<String> stream = reader.lines();

			openAPIYAML = YAMLUtil.loadOpenAPIYAML(
				stream.collect(Collectors.joining(System.lineSeparator())));
		}

		_registerPathItems(openAPIYAML.getPathItems(), openAPIYAML);
	}

	private void _registerPathItems(
		Map<String, PathItem> pathItems, OpenAPIYAML openAPIYAML) {

		for (Map.Entry<String, PathItem> entry : pathItems.entrySet()) {
			String path = entry.getKey();

			path = Portal.PATH_MODULE + APIBuilderConstants.BASE_PATH + path;

			String pathRegex = path.replaceAll("\\{.+\\}", "(.+)");

			_pathItemsMap.put(Pattern.compile(pathRegex), entry);
		}
	}

	private final Map<Pattern, Map.Entry<String, PathItem>> _pathItemsMap = new HashMap<>();

}