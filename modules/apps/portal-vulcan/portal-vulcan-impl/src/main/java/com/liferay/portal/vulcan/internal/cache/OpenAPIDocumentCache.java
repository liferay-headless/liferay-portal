/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
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

	public void clear(long companyId) {
		_clear(
			keyParts -> Objects.equals(keyParts[0], String.valueOf(companyId)));
	}

	public void clear(long companyId, String restContextPath) {
		_clear(
			keyParts -> {
				if (!Objects.equals(keyParts[0], String.valueOf(companyId))) {
					return false;
				}

				String basePath = keyParts[1];

				if (restContextPath.equals(basePath) ||
					restContextPath.startsWith(basePath + StringPool.SLASH)) {

					return true;
				}

				return false;
			});
	}

	public void clear(String basePath) {
		_clear(keyParts -> Objects.equals(keyParts[1], basePath));
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

	private void _clear(Predicate<String[]> predicate) {
		_clearCount.incrementAndGet();

		Set<String> keys = _contents.keySet();

		keys.removeIf(
			key -> predicate.test(StringUtil.split(key, CharPool.POUND)));
	}

	private final AtomicLong _clearCount = new AtomicLong();
	private final Map<String, String> _contents = new ConcurrentHashMap<>();

}