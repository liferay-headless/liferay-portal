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

package com.liferay.api.builder.registry;

import com.liferay.portal.vulcan.yaml.openapi.PathItem;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Matija Petanjek
 */
public interface APIBuilderOpenAPIRegistry {

	public Map.Entry<Pattern, Map.Entry<String, PathItem>> getPathItemEntry(
		String path);

}