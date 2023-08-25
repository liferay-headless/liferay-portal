/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.builder.internal.application.registry;

import com.liferay.headless.builder.application.APIApplication;
import com.liferay.headless.builder.internal.jaxrs.application.ApiApplicationJaxrsApplication;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.Objects;

import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = ApiApplicationRegistry.class)
public class ApiApplicationRegistry {

	public APIApplication fetchApiApplication(String baseURL, long companyId) {
		for (Object service :
				ListUtil.fromArray(_serviceTracker.getServices())) {

			APIApplication apiApplication = (APIApplication)service;

			if (Objects.equals(apiApplication.getBaseURL(), baseURL) &&
				Objects.equals(apiApplication.getCompanyId(), companyId)) {

				return apiApplication;
			}
		}

		return null;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTracker = new ServiceTracker<Application, APIApplication>(
			bundleContext, Application.class, null) {

			@Override
			public APIApplication addingService(
				ServiceReference<Application> serviceReference) {

				if (GetterUtil.getBoolean(
						serviceReference.getProperty(
							"liferay.headless.builder.application"))) {

					ApiApplicationJaxrsApplication
						apiApplicationJaxrsApplication =
							(ApiApplicationJaxrsApplication)
								bundleContext.getService(serviceReference);

					return apiApplicationJaxrsApplication.getApiApplication();
				}

				return null;
			}

		};

		_serviceTracker.open();
	}

	@Deactivate
	protected void deactivate() {
		_serviceTracker.close();
	}

	private ServiceTracker<Application, APIApplication> _serviceTracker;

}