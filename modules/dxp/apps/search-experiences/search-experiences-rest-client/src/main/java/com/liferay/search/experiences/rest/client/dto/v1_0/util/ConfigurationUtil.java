/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.search.experiences.rest.client.dto.v1_0.util;

import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.search.experiences.rest.client.dto.v1_0.Clause;
import com.liferay.search.experiences.rest.client.dto.v1_0.Configuration;
import com.liferay.search.experiences.rest.client.dto.v1_0.QueryConfiguration;

import java.util.Map;

/**
 * @author Bryan Engler
 */
public class ConfigurationUtil {

	protected static Configuration unpack(Configuration configuration) {
		if (configuration == null) {
			return null;
		}

		QueryConfiguration queryConfiguration =
			configuration.getQueryConfiguration();

		if (queryConfiguration != null) {
			ArrayUtil.isNotEmptyForEach(
				queryConfiguration.getQueryEntries(),
				queryEntry -> ArrayUtil.isNotEmptyForEach(
					queryEntry.getClauses(), ConfigurationUtil::_unpack));
		}

		return configuration;
	}

	private static void _unpack(Clause clause) {
		Object query = clause.getQuery();

		clause.setQuery(
			() -> {
				if (query instanceof Map) {
					return JSONFactoryUtil.createJSONObject((Map<?, ?>)query);
				}

				try {
					return JSONFactoryUtil.createJSONObject(
						String.valueOf(query));
				}
				catch (JSONException jsonException) {
					if (_log.isDebugEnabled()) {
						_log.debug(jsonException);
					}
				}

				return null;
			});
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ConfigurationUtil.class);

}