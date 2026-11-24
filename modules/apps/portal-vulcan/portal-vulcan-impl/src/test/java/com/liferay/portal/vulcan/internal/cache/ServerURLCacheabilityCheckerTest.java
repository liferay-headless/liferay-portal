/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.cache;

import com.liferay.portal.kernel.model.VirtualHost;
import com.liferay.portal.kernel.service.VirtualHostLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Petteri Karttunen
 */
public class ServerURLCacheabilityCheckerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testIsCacheableWhenHostIsIgnored() {
		String host = StringUtil.toLowerCase(RandomTestUtil.randomString());

		ServerURLCacheabilityChecker serverURLCacheabilityChecker =
			_createChecker(Collections.singleton(host));

		Assert.assertTrue(
			serverURLCacheabilityChecker.isCacheable(
				"http://" + host + ":8080/o"));

		Mockito.verify(
			_virtualHostLocalService, Mockito.never()
		).fetchVirtualHost(
			Mockito.anyString()
		);
	}

	@Test
	public void testIsCacheableWhenHostIsUnknown() {
		String host = StringUtil.toLowerCase(RandomTestUtil.randomString());

		ServerURLCacheabilityChecker serverURLCacheabilityChecker =
			_createChecker(Collections.emptySet());

		Assert.assertFalse(
			serverURLCacheabilityChecker.isCacheable(
				"http://" + host + ":8080/o"));
	}

	@Test
	public void testIsCacheableWhenHostIsVirtualHost() {
		String host = StringUtil.toLowerCase(RandomTestUtil.randomString());

		Mockito.when(
			_virtualHostLocalService.fetchVirtualHost(host)
		).thenReturn(
			Mockito.mock(VirtualHost.class)
		);

		ServerURLCacheabilityChecker serverURLCacheabilityChecker =
			_createChecker(Collections.emptySet());

		Assert.assertTrue(
			serverURLCacheabilityChecker.isCacheable(
				"http://" + host + ":8080/o"));
	}

	@Test
	public void testIsCacheableWhenServerURLHostCaseIsDifferent() {
		ServerURLCacheabilityChecker serverURLCacheabilityChecker =
			_createChecker(Collections.singleton("localhost"));

		Assert.assertTrue(
			serverURLCacheabilityChecker.isCacheable(
				"http://LocalHost:8080/o"));
	}

	@Test
	public void testIsCacheableWhenServerURLHostIsNull() {
		ServerURLCacheabilityChecker serverURLCacheabilityChecker =
			_createChecker(Collections.emptySet());

		Assert.assertFalse(
			serverURLCacheabilityChecker.isCacheable(
				"/" + RandomTestUtil.randomString()));
	}

	@Test
	public void testIsCacheableWhenServerURLIsMalformed() {
		ServerURLCacheabilityChecker serverURLCacheabilityChecker =
			_createChecker(Collections.emptySet());

		Assert.assertFalse(
			serverURLCacheabilityChecker.isCacheable("http://[malformed"));
	}

	private ServerURLCacheabilityChecker _createChecker(
		Set<String> trustedHosts) {

		return new ServerURLCacheabilityChecker(
			trustedHosts, _virtualHostLocalService);
	}

	private final VirtualHostLocalService _virtualHostLocalService =
		Mockito.mock(VirtualHostLocalService.class);

}