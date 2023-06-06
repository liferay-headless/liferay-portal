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

import javax.ws.rs.core.Application;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = ApplicationPublisher.class)
public class ApplicationPublisherImpl implements ApplicationPublisher {

	@Override
	public void publish(ApiApplication application) throws Exception {
		if ((_applicationServiceRegistration != null) ||
			(_resourceServiceRegistration != null)) {

			return;
		}

		String osgiJaxRsName = application.getOsgiJaxRsName();

		_applicationServiceRegistration = _bundleContext.registerService(
			Application.class, new HeadlessBuilderApplication(),
			HashMapDictionaryBuilder.<String, Object>put(
				"companyId", application.getCompanyId()
			).put(
				"liferay.filter.disabled", true
			).put(
				"liferay.jackson", false
			).put(
				"osgi.jaxrs.application.base", application.getBaseURL()
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

					return new HeadlessBuilderResource();
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
	}

	@Deactivate
	protected void deactivate() {
		_undeploy();
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

}