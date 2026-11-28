/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.SingleVMPool;
import com.liferay.portal.kernel.service.VirtualHostLocalService;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = OpenAPIDocumentCache.class)
public class OpenAPIDocumentCache {

	public void clear() {
		_globalGeneration.incrementAndGet();

		_contentPortalCache.removeAll();
	}

	public void clear(long companyId) {
		AtomicLong companyGeneration = _getCompanyGeneration(companyId);

		companyGeneration.incrementAndGet();
	}

	public void clear(long companyId, String restContextPath) {
		_incrementCompanyPathGeneration(companyId, restContextPath);

		int index = restContextPath.lastIndexOf(StringPool.SLASH);

		while (index > 0) {
			_incrementCompanyPathGeneration(
				companyId, restContextPath.substring(0, index));

			index = restContextPath.lastIndexOf(StringPool.SLASH, index - 1);
		}
	}

	public void clear(String basePath) {
		AtomicLong basePathGeneration = _getBasePathGeneration(basePath);

		basePathGeneration.incrementAndGet();
	}

	public String getContent(
		String basePath, long companyId, Supplier<String> contentSupplier,
		String path, String serverURL) {

		return _getContent(
			contentSupplier, _getKey(basePath, companyId, path, serverURL),
			serverURL);
	}

	@Activate
	protected void activate() {
		_contentPortalCache =
			(PortalCache<String, String>)_singleVMPool.getPortalCache(
				OpenAPIDocumentCache.class.getName() + ".content");

		Set<String> trustedHosts = new HashSet<>();

		for (String ignoredHost :
				PropsUtil.getArray("virtual.hosts.ignore.hosts")) {

			trustedHosts.add(StringUtil.toLowerCase(ignoredHost.trim()));
		}

		String webServerHost = PropsUtil.get("web.server.host");

		if (Validator.isNotNull(webServerHost)) {
			trustedHosts.add(StringUtil.toLowerCase(webServerHost.trim()));
		}

		_serverURLCacheabilityChecker = new ServerURLCacheabilityChecker(
			trustedHosts, _virtualHostLocalService);
	}

	private AtomicLong _getBasePathGeneration(String basePath) {
		return _basePathGenerations.computeIfAbsent(
			basePath, curBasePath -> new AtomicLong());
	}

	private AtomicLong _getCompanyGeneration(long companyId) {
		return _companyGenerations.computeIfAbsent(
			companyId, curCompanyId -> new AtomicLong());
	}

	private AtomicLong _getCompanyPathGeneration(long companyId, String path) {
		return _companyPathGenerations.computeIfAbsent(
			StringBundler.concat(companyId, StringPool.POUND, path),
			curCompanyPathKey -> new AtomicLong());
	}

	private String _getContent(
		Supplier<String> contentSupplier, String key, String serverURL) {

		String content = _contentPortalCache.get(key);

		if (content != null) {
			return content;
		}

		content = contentSupplier.get();

		if ((content != null) &&
			_serverURLCacheabilityChecker.isCacheable(serverURL)) {

			_contentPortalCache.put(key, content);
		}

		return content;
	}

	private String _getKey(
		String basePath, long companyId, String path, String serverURL) {

		AtomicLong basePathGeneration = _getBasePathGeneration(basePath);
		AtomicLong companyGeneration = _getCompanyGeneration(companyId);
		AtomicLong companyPathGeneration = _getCompanyPathGeneration(
			companyId, basePath);

		return StringBundler.concat(
			_globalGeneration.get(), StringPool.POUND, companyGeneration.get(),
			StringPool.POUND, basePathGeneration.get(), StringPool.POUND,
			companyPathGeneration.get(), StringPool.POUND, companyId,
			StringPool.POUND, basePath, StringPool.POUND, path,
			StringPool.POUND, serverURL);
	}

	private void _incrementCompanyPathGeneration(long companyId, String path) {
		AtomicLong companyPathGeneration = _getCompanyPathGeneration(
			companyId, path);

		companyPathGeneration.incrementAndGet();
	}

	private final Map<String, AtomicLong> _basePathGenerations =
		new ConcurrentHashMap<>();
	private final Map<Long, AtomicLong> _companyGenerations =
		new ConcurrentHashMap<>();
	private final Map<String, AtomicLong> _companyPathGenerations =
		new ConcurrentHashMap<>();
	private PortalCache<String, String> _contentPortalCache;
	private final AtomicLong _globalGeneration = new AtomicLong();
	private volatile ServerURLCacheabilityChecker _serverURLCacheabilityChecker;

	@Reference
	private SingleVMPool _singleVMPool;

	@Reference
	private VirtualHostLocalService _virtualHostLocalService;

}