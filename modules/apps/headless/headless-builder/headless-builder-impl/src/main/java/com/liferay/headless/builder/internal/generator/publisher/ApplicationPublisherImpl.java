/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.builder.internal.generator.publisher;

import com.liferay.headless.builder.internal.generator.application.ApiApplication;
import com.liferay.headless.builder.internal.generator.jaxrs.application.HeadlessBuilderApplication;
import com.liferay.headless.builder.internal.generator.resource.BaseHeadlessBuilderResource;
import com.liferay.headless.builder.internal.generator.resource.HeadlessBuilderResource;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Portal;

import javax.ws.rs.core.Application;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = ApplicationPublisher.class)
public class ApplicationPublisherImpl implements ApplicationPublisher {

	@Override
	public void publish(ApiApplication apiApplication) throws Exception {
		if ((_applicationServiceRegistration != null) ||
			(_resourceServiceRegistration != null)) {

			return;
		}

		String osgiJaxRsName = apiApplication.getOsgiJaxRsName();

		_applicationServiceRegistration = _bundleContext.registerService(
			Application.class, new HeadlessBuilderApplication(apiApplication),
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", apiApplication.getCompanyId()
			).put(
				"liferay.filter.disabled", true
			).put(
				"liferay.headless.builder.application", true
			).put(
				"liferay.jackson", false
			).put(
				"osgi.jaxrs.application.base", apiApplication.getBaseURL()
			).put(
				"osgi.jaxrs.extension.select",
				"(osgi.jaxrs.name=Liferay.Vulcan)"
			).put(
				"osgi.jaxrs.name", osgiJaxRsName
			).build());

		_resourceServiceRegistration = _bundleContext.registerService(
			BaseHeadlessBuilderResource.class,
			new PrototypeServiceFactory<BaseHeadlessBuilderResource>() {

				@Override
				public BaseHeadlessBuilderResource getService(
					Bundle bundle,
					ServiceRegistration<BaseHeadlessBuilderResource>
						serviceRegistration) {

					return new HeadlessBuilderResource(
						_portal, _serviceTracker);
				}

				@Override
				public void ungetService(
					Bundle bundle,
					ServiceRegistration<BaseHeadlessBuilderResource>
						serviceRegistration,
					BaseHeadlessBuilderResource objectEntryResource) {
				}

			},
			HashMapDictionaryBuilder.<String, Object>put(
				"api.version", "v1.0"
			).put(
				"osgi.jaxrs.application.select",
				"(osgi.jaxrs.name=" + osgiJaxRsName + ")"
			).put(
				"osgi.jaxrs.resource", "true"
			).build());
	}

	@Override
	public void undeploy() throws Exception {
		_undeploy();
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;

		_serviceTracker = new ServiceTracker<Application, ApiApplication>(
			bundleContext, Application.class, null) {

			@Override
			public ApiApplication addingService(
				ServiceReference<Application> serviceReference) {

				Object property = serviceReference.getProperty(
					"liferay.headless.builder.application");

				if ((property != null) && (Boolean)property) {
					HeadlessBuilderApplication headlessBuilderApplication =
						(HeadlessBuilderApplication)bundleContext.getService(
							serviceReference);

					return headlessBuilderApplication.getApiApplication();
				}

				return null;
			}

		};

		_serviceTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		_undeploy();
		_serviceTracker.close();
	}

	private void _undeploy() {
		if (_applicationServiceRegistration != null) {
			_applicationServiceRegistration.unregister();
		}

		if (_resourceServiceRegistration != null) {
			_resourceServiceRegistration.unregister();
		}

		_applicationServiceRegistration = null;
		_resourceServiceRegistration = null;
	}

	private static ServiceRegistration<Application>
		_applicationServiceRegistration;
	private static BundleContext _bundleContext;
	private static ServiceRegistration<BaseHeadlessBuilderResource>
		_resourceServiceRegistration;

	@Reference
	private Portal _portal;

	private ServiceTracker<Application, ApiApplication> _serviceTracker;

}