/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.VirtualHostLocalService;
import com.liferay.portal.kernel.util.StringUtil;

import java.net.URI;

import java.util.Set;

/**
 * @author Petteri Karttunen
 */
public class ServerURLCacheabilityChecker {

	public ServerURLCacheabilityChecker(
		Set<String> trustedHosts,
		VirtualHostLocalService virtualHostLocalService) {

		_trustedHosts = trustedHosts;
		_virtualHostLocalService = virtualHostLocalService;
	}

	public boolean isCacheable(String serverURL) {
		String host;

		try {
			URI uri = URI.create(serverURL);

			host = uri.getHost();
		}
		catch (IllegalArgumentException illegalArgumentException) {
			if (_log.isDebugEnabled()) {
				_log.debug(illegalArgumentException);
			}

			return false;
		}

		if (host == null) {
			return false;
		}

		host = StringUtil.toLowerCase(host);

		if (_trustedHosts.contains(host) ||
			(_virtualHostLocalService.fetchVirtualHost(host) != null)) {

			return true;
		}

		return false;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ServerURLCacheabilityChecker.class);

	private final Set<String> _trustedHosts;
	private final VirtualHostLocalService _virtualHostLocalService;

}