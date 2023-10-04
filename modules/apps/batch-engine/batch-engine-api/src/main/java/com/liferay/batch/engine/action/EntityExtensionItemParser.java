/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.action;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * @author Carlos Correa
 */
public interface EntityExtensionItemParser<T> {

	public EntityExtensionItem<T> parse(Class<T> clazz, Map<String, Object> fieldNameValueMap, ObjectMapper objectMapper) throws Exception;

}