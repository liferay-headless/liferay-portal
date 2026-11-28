/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.crud;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegate;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegateBuilder;
import com.liferay.portal.vulcan.crud.VulcanCRUDItemDelegateBuilderRegistry;
import com.liferay.portal.vulcan.jaxrs.context.ContextDataInjectorBuilderFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Carlos Correa
 */
@Component(service = VulcanCRUDItemDelegateBuilderRegistry.class)
public class VulcanCRUDItemDelegateBuilderRegistryImpl
	implements VulcanCRUDItemDelegateBuilderRegistry {

	@Override
	public VulcanCRUDItemDelegateBuilder builder(
		Company company, String entityClassName) {

		ServiceObjects<VulcanCRUDItemDelegate<?>> serviceObjects =
			_getServiceObjects(company.getCompanyId(), entityClassName);

		if (serviceObjects == null) {
			return null;
		}

		return new VulcanCRUDItemDelegateBuilderImpl(
			company, _contextDataInjectorBuilderFactory.builder(),
			serviceObjects);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, null, "(crud.item.delegate=true)",
			(serviceReference, emitter) -> emitter.emit(
				_encodeKey(
					GetterUtil.getLong(
						serviceReference.getProperty("companyId")),
					(String)serviceReference.getProperty(
						"crud.entity.class.name"))),
			new ServiceTrackerCustomizer
				<VulcanCRUDItemDelegate<?>,
				 ServiceObjects<VulcanCRUDItemDelegate<?>>>() {

				@Override
				public ServiceObjects<VulcanCRUDItemDelegate<?>> addingService(
					ServiceReference<VulcanCRUDItemDelegate<?>>
						serviceReference) {

					return bundleContext.getServiceObjects(serviceReference);
				}

				@Override
				public void modifiedService(
					ServiceReference<VulcanCRUDItemDelegate<?>>
						serviceReference,
					ServiceObjects<VulcanCRUDItemDelegate<?>> serviceObjects) {
				}

				@Override
				public void removedService(
					ServiceReference<VulcanCRUDItemDelegate<?>>
						serviceReference,
					ServiceObjects<VulcanCRUDItemDelegate<?>> serviceObjects) {
				}

			});
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private String _encodeKey(Long companyId, String entityClassName) {
		if (Validator.isNull(companyId)) {
			return entityClassName;
		}

		return StringBundler.concat(
			entityClassName, StringPool.POUND, companyId);
	}

	private ServiceObjects<VulcanCRUDItemDelegate<?>> _getServiceObjects(
		long companyId, String entityClassName) {

		ServiceObjects<VulcanCRUDItemDelegate<?>> serviceObjects =
			_serviceTrackerMap.getService(
				_encodeKey(companyId, entityClassName));

		if (serviceObjects != null) {
			return serviceObjects;
		}

		return _serviceTrackerMap.getService(_encodeKey(null, entityClassName));
	}

	@Reference
	private ContextDataInjectorBuilderFactory
		_contextDataInjectorBuilderFactory;

	private ServiceTrackerMap<String, ServiceObjects<VulcanCRUDItemDelegate<?>>>
		_serviceTrackerMap;

}