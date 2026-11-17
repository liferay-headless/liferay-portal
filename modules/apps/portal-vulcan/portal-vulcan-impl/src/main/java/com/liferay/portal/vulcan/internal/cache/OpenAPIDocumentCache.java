/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = OpenAPIDocumentCache.class)
public class OpenAPIDocumentCache {

	public void clear() {
		_clearCount.incrementAndGet();

		_entries.clear();
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

		Entry entry = _getEntry(
			basePath, companyId, contentSupplier, path, serverURL);

		if (entry == null) {
			return null;
		}

		return entry._content;
	}

	public JSONObject getContentJSONObject(
			String basePath, long companyId, Supplier<String> contentSupplier,
			String path, String serverURL)
		throws JSONException {

		Entry entry = _getEntry(
			basePath, companyId, contentSupplier, path, serverURL);

		if (entry == null) {
			return null;
		}

		JSONObject contentJSONObject = entry._contentJSONObject;

		if (contentJSONObject == null) {
			contentJSONObject = _jsonFactory.createJSONObject(entry._content);

			entry._contentJSONObject = contentJSONObject;
		}

		return contentJSONObject;
	}

	private void _clear(Predicate<String[]> predicate) {
		_clearCount.incrementAndGet();

		Set<String> keys = _entries.keySet();

		keys.removeIf(
			key -> predicate.test(StringUtil.split(key, CharPool.POUND)));
	}

	private Entry _getEntry(
		String basePath, long companyId, Supplier<String> contentSupplier,
		String path, String serverURL) {

		String key = StringBundler.concat(
			companyId, StringPool.POUND, basePath, StringPool.POUND, path,
			StringPool.POUND, serverURL);

		Entry entry = _entries.get(key);

		if (entry != null) {
			return entry;
		}

		long clearCount = _clearCount.get();

		String content = contentSupplier.get();

		if (content != null) {
			entry = new Entry(content);
		}

		if ((entry != null) && (clearCount == _clearCount.get())) {
			_entries.put(key, entry);
		}

		return entry;
	}

	private final AtomicLong _clearCount = new AtomicLong();
	private final Map<String, Entry> _entries = new ConcurrentHashMap<>();

	@Reference
	private JSONFactory _jsonFactory;

	private static class Entry {

		private Entry(String content) {
			_content = content;
		}

		private final String _content;
		private volatile JSONObject _contentJSONObject;

	}

}