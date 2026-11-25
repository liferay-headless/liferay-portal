/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.crud.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.ProxyFactory;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegate;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegateBuilder.SafeCloseableVulcanCRUDItemDelegate;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegateBuilderRegistry;

import jakarta.ws.rs.core.UriInfo;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Alejandro Tardín
 */
@RunWith(Arquillian.class)
public class VulcanCRUDItemDelegateBuilderRegistryTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			VulcanCRUDItemDelegateBuilderRegistryTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceRegistration = bundleContext.registerService(
			VulcanCRUDItemDelegate.class.getName(),
			new PrototypeServiceFactory<TestVulcanCRUDItemDelegate>() {

				@Override
				public TestVulcanCRUDItemDelegate getService(
					Bundle bundle,
					ServiceRegistration<TestVulcanCRUDItemDelegate>
						serviceRegistration) {

					_instancesCount.incrementAndGet();

					return new TestVulcanCRUDItemDelegate();
				}

				@Override
				public void ungetService(
					Bundle bundle,
					ServiceRegistration<TestVulcanCRUDItemDelegate>
						serviceRegistration,
					TestVulcanCRUDItemDelegate testVulcanCRUDItemDelegate) {

					_releasedInstancesCount.incrementAndGet();
				}

			},
			HashMapDictionaryBuilder.<String, Object>put(
				"crud.entity.class.name", _ENTITY_CLASS_NAME
			).put(
				"crud.item.delegate", "true"
			).build());
	}

	@After
	public void tearDown() {
		_serviceRegistration.unregister();
	}

	@Test
	public void testBuild() throws Exception {
		UriInfo uriInfo1 = ProxyFactory.newDummyInstance(UriInfo.class);
		UriInfo uriInfo2 = ProxyFactory.newDummyInstance(UriInfo.class);

		try (SafeCloseableVulcanCRUDItemDelegate<?> vulcanCRUDItemDelegate1 =
				_build(uriInfo1);
			SafeCloseableVulcanCRUDItemDelegate<?> vulcanCRUDItemDelegate2 =
				_build(uriInfo2)) {

			long id = RandomTestUtil.randomLong();

			Assert.assertSame(uriInfo1, vulcanCRUDItemDelegate1.fetchItem(id));
			Assert.assertSame(uriInfo1, vulcanCRUDItemDelegate1.getItem(id));
			Assert.assertSame(uriInfo2, vulcanCRUDItemDelegate2.fetchItem(id));
			Assert.assertSame(uriInfo2, vulcanCRUDItemDelegate2.getItem(id));
		}

		Assert.assertEquals(2, _instancesCount.get());
		Assert.assertEquals(2, _releasedInstancesCount.get());
	}

	public class TestVulcanCRUDItemDelegate
		implements VulcanCRUDItemDelegate<UriInfo> {

		@Override
		public UriInfo getItem(Long id) {
			return contextUriInfo;
		}

		protected UriInfo contextUriInfo;

	}

	private SafeCloseableVulcanCRUDItemDelegate<?> _build(UriInfo uriInfo)
		throws Exception {

		return _vulcanCRUDItemDelegateBuilderRegistry.builder(
			_companyLocalService.getCompany(TestPropsValues.getCompanyId()),
			_ENTITY_CLASS_NAME
		).acceptLanguage(
			null
		).groupLocalService(
			null
		).httpServletRequest(
			null
		).httpServletResponse(
			null
		).resourceActionLocalService(
			null
		).resourcePermissionLocalService(
			null
		).roleLocalService(
			null
		).scopeChecker(
			null
		).uriInfo(
			uriInfo
		).user(
			TestPropsValues.getUser()
		).build();
	}

	private static final String _ENTITY_CLASS_NAME =
		VulcanCRUDItemDelegateBuilderRegistryTest.class.getName();

	@Inject
	private CompanyLocalService _companyLocalService;

	private final AtomicInteger _instancesCount = new AtomicInteger();
	private final AtomicInteger _releasedInstancesCount = new AtomicInteger();
	private ServiceRegistration<?> _serviceRegistration;

	@Inject
	private VulcanCRUDItemDelegateBuilderRegistry
		_vulcanCRUDItemDelegateBuilderRegistry;

}