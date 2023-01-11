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

package com.liferay.api.builder.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matija Petanjek
 */
public class URIUtil {

	public static Map<String, String> getPathParams(
		String requestURI, Pattern pathPattern, String path) {

		List<String> pathParamValues = _getPathParamValues(
			requestURI, pathPattern);
		List<String> pathParamNames = _getPathParamNames(path, pathPattern);

		if (pathParamNames.size() != pathParamValues.size()) {
			throw new IllegalStateException();
		}

		Map<String, String> pathParams = new HashMap<>();

		for (int i = 0; i < pathParamNames.size(); i++) {
			pathParams.put(pathParamNames.get(i), pathParamValues.get(i));
		}

		return pathParams;
	}

	private static List<String> _getPathParamNames(
		String path, Pattern pathPattern) {

		List<String> matchedValues = new ArrayList<>();

		Matcher matcher = pathPattern.matcher(path);

		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String value = matcher.group(i);

				matchedValues.add(value.substring(1, value.length() - 1));
			}
		}

		return matchedValues;
	}

	private static List<String> _getPathParamValues(
		String requestURI, Pattern pathPattern) {

		List<String> pathParamValues = new ArrayList<>();

		Matcher matcher = pathPattern.matcher(requestURI);

		while (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				pathParamValues.add(matcher.group(i));
			}
		}

		return pathParamValues;
	}

}