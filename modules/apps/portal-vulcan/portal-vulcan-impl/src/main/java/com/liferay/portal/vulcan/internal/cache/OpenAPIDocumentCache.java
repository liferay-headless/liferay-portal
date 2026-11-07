/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;

/**
 * @author Petteri Karttunen
 */
@Component(service = OpenAPIDocumentCache.class)
public class OpenAPIDocumentCache {

	public void clear() {
		_clearCount.incrementAndGet();

		_contents.clear();
	}

	public String getContent(
		String basePath, long companyId, Supplier<String> contentSupplier,
		String path, String serverURL) {

		String key = StringBundler.concat(
			companyId, StringPool.POUND, basePath, StringPool.POUND, path,
			StringPool.POUND, serverURL);

		String content = _contents.get(key);

		if (content != null) {
			return content;
		}

		long clearCount = _clearCount.get();

		content = contentSupplier.get();

		if ((content != null) && (clearCount == _clearCount.get())) {
			_contents.put(key, content);
		}

		return content;
	}

	private final AtomicLong _clearCount = new AtomicLong();
	private final Map<String, String> _contents = new ConcurrentHashMap<>();

}