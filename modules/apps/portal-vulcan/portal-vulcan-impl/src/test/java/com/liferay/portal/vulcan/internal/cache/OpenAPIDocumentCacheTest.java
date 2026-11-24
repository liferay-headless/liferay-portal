/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.SingleVMPool;
import com.liferay.portal.kernel.model.VirtualHost;
import com.liferay.portal.kernel.service.VirtualHostLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Petteri Karttunen
 */
public class OpenAPIDocumentCacheTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		Mockito.when(
			_virtualHostLocalService.fetchVirtualHost(Mockito.anyString())
		).thenReturn(
			Mockito.mock(VirtualHost.class)
		);

		_contentPortalCache = _createPortalCache();

		Mockito.doReturn(
			_contentPortalCache
		).when(
			_singleVMPool
		).getPortalCache(
			Mockito.endsWith(".content")
		);

		_openAPIDocumentCache = new OpenAPIDocumentCache();

		ReflectionTestUtil.setFieldValue(
			_openAPIDocumentCache, "_singleVMPool", _singleVMPool);
		ReflectionTestUtil.setFieldValue(
			_openAPIDocumentCache, "_virtualHostLocalService",
			_virtualHostLocalService);

		_openAPIDocumentCache.activate();
	}

	@Test
	public void testClear() {
		_requestContent(_BASE_PATH, _COMPANY_ID, _SERVER_URL);
		_requestContent(_BASE_PATH, _COMPANY_ID_OTHER, _SERVER_URL);

		_openAPIDocumentCache.clear();

		Assert.assertEquals(
			1, _getGenerationsCount(_BASE_PATH, _COMPANY_ID, _SERVER_URL));
		Assert.assertEquals(
			1,
			_getGenerationsCount(_BASE_PATH, _COMPANY_ID_OTHER, _SERVER_URL));
	}

	@Test
	public void testClearRemovesEntriesFromPortalCaches() {
		_openAPIDocumentCache.clear();

		Mockito.verify(
			_contentPortalCache
		).removeAll();
	}

	@Test
	public void testClearWhenBasePathIsCleared() {
		_requestContent(_BASE_PATH, _COMPANY_ID, _SERVER_URL);
		_requestContent(_BASE_PATH_OTHER, _COMPANY_ID, _SERVER_URL);

		_openAPIDocumentCache.clear(_BASE_PATH);

		Assert.assertEquals(
			1, _getGenerationsCount(_BASE_PATH, _COMPANY_ID, _SERVER_URL));
		Assert.assertEquals(
			0,
			_getGenerationsCount(_BASE_PATH_OTHER, _COMPANY_ID, _SERVER_URL));
	}

	@Test
	public void testClearWhenCompanyIsCleared() {
		_requestContent(_BASE_PATH, _COMPANY_ID, _SERVER_URL);
		_requestContent(_BASE_PATH, _COMPANY_ID_OTHER, _SERVER_URL);

		_openAPIDocumentCache.clear(_COMPANY_ID);

		Assert.assertEquals(
			1, _getGenerationsCount(_BASE_PATH, _COMPANY_ID, _SERVER_URL));
		Assert.assertEquals(
			0,
			_getGenerationsCount(_BASE_PATH, _COMPANY_ID_OTHER, _SERVER_URL));
	}

	@Test
	public void testClearWhenRESTContextPathIsCleared() {
		String childBasePath =
			_BASE_PATH + StringPool.SLASH + RandomTestUtil.randomString();
		String siblingBasePath = _BASE_PATH + RandomTestUtil.randomString();

		_requestContent(_BASE_PATH, _COMPANY_ID, _SERVER_URL);
		_requestContent(_BASE_PATH, _COMPANY_ID_OTHER, _SERVER_URL);
		_requestContent(_BASE_PATH_OTHER, _COMPANY_ID, _SERVER_URL);
		_requestContent(childBasePath, _COMPANY_ID, _SERVER_URL);
		_requestContent(siblingBasePath, _COMPANY_ID, _SERVER_URL);

		_openAPIDocumentCache.clear(_COMPANY_ID, childBasePath);

		Assert.assertEquals(
			1, _getGenerationsCount(_BASE_PATH, _COMPANY_ID, _SERVER_URL));
		Assert.assertEquals(
			0,
			_getGenerationsCount(_BASE_PATH, _COMPANY_ID_OTHER, _SERVER_URL));
		Assert.assertEquals(
			0,
			_getGenerationsCount(_BASE_PATH_OTHER, _COMPANY_ID, _SERVER_URL));
		Assert.assertEquals(
			1, _getGenerationsCount(childBasePath, _COMPANY_ID, _SERVER_URL));
		Assert.assertEquals(
			0, _getGenerationsCount(siblingBasePath, _COMPANY_ID, _SERVER_URL));
	}

	@Test
	public void testGetContentWhenClearedDuringGeneration() {
		_openAPIDocumentCache.getContent(
			_BASE_PATH, _COMPANY_ID,
			() -> {
				_openAPIDocumentCache.clear();

				return _CONTENT;
			},
			_PATH, _SERVER_URL);

		_requestContent(_BASE_PATH, _COMPANY_ID, _SERVER_URL);

		Assert.assertEquals(1, _generationsCount.get());
	}

	@Test
	public void testGetContentWhenServerURLIsCached() {
		_requestContent(_BASE_PATH, _COMPANY_ID, _SERVER_URL);
		_requestContent(_BASE_PATH, _COMPANY_ID, _SERVER_URL);

		Assert.assertEquals(1, _generationsCount.get());

		Mockito.verify(
			_virtualHostLocalService, Mockito.times(1)
		).fetchVirtualHost(
			_HOST
		);
	}

	@Test
	public void testGetContentWhenServerURLIsNotTrusted() {
		String otherHost = StringUtil.toLowerCase(
			RandomTestUtil.randomString());

		Mockito.when(
			_virtualHostLocalService.fetchVirtualHost(otherHost)
		).thenReturn(
			null
		);

		String otherServerURL = _getServerURL(otherHost);

		_requestContent(_BASE_PATH, _COMPANY_ID, otherServerURL);
		_requestContent(_BASE_PATH, _COMPANY_ID, otherServerURL);

		Assert.assertEquals(2, _generationsCount.get());
	}

	private static String _getServerURL(String host) {
		return "http://" + host;
	}

	private <K extends Serializable, V> PortalCache<K, V> _createPortalCache() {
		Map<K, V> map = new HashMap<>();

		PortalCache<K, V> portalCache = Mockito.mock(PortalCache.class);

		Mockito.when(
			portalCache.get(Mockito.any())
		).thenAnswer(
			invocation -> map.get(invocation.getArgument(0))
		);

		Mockito.doAnswer(
			invocation -> {
				map.put(invocation.getArgument(0), invocation.getArgument(1));

				return null;
			}
		).when(
			portalCache
		).put(
			Mockito.any(), Mockito.any()
		);

		return portalCache;
	}

	private int _getGenerationsCount(
		String basePath, long companyId, String serverURL) {

		_generationsCount.set(0);

		_requestContent(basePath, companyId, serverURL);

		return _generationsCount.get();
	}

	private void _requestContent(
		String basePath, long companyId, String serverURL) {

		_openAPIDocumentCache.getContent(
			basePath, companyId,
			() -> {
				_generationsCount.incrementAndGet();

				return _CONTENT;
			},
			_PATH, serverURL);
	}

	private static final String _BASE_PATH =
		"/c/" + RandomTestUtil.randomString();

	private static final String _BASE_PATH_OTHER =
		"/o/" + RandomTestUtil.randomString();

	private static final long _COMPANY_ID = RandomTestUtil.randomLong();

	private static final long _COMPANY_ID_OTHER = RandomTestUtil.randomLong();

	private static final String _CONTENT = RandomTestUtil.randomString();

	private static final String _HOST = StringUtil.toLowerCase(
		RandomTestUtil.randomString());

	private static final String _PATH =
		StringPool.SLASH + RandomTestUtil.randomString();

	private static final String _SERVER_URL = _getServerURL(_HOST);

	private PortalCache<String, String> _contentPortalCache;
	private final AtomicInteger _generationsCount = new AtomicInteger();
	private OpenAPIDocumentCache _openAPIDocumentCache;
	private final SingleVMPool _singleVMPool = Mockito.mock(SingleVMPool.class);
	private final VirtualHostLocalService _virtualHostLocalService =
		Mockito.mock(VirtualHostLocalService.class);

}