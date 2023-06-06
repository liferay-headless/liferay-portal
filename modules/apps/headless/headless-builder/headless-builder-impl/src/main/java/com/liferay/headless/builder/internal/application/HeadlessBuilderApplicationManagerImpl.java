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

package com.liferay.headless.builder.internal.application;

import com.liferay.headless.builder.application.HeadlessBuilderApplicationManager;
import com.liferay.headless.builder.internal.generator.application.ApiApplication;
import com.liferay.headless.builder.internal.generator.publisher.ApplicationPublisher;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = HeadlessBuilderApplicationManager.class)
public class HeadlessBuilderApplicationManagerImpl
	implements HeadlessBuilderApplicationManager {

	@Override
	public void publishApplication(String externalReferenceCode)
		throws Exception {

		// TODO Implement the extraction of the information from the @Consumer

		// TODO Implement the publication of the REST Application
		//  with the @Publisher

		ApiApplication.Builder builder = new ApiApplication.Builder();

		_applicationPublisher.publish(
			builder.setBaseURL(
				"/my-path"
			).setCompanyId(
				CompanyThreadLocal.getCompanyId()
			).setOsgiJaxRsName(
				"myOSGiJaxRsName"
			).build());
	}

	@Override
	public void unpublishApplication() throws Exception {
		_applicationPublisher.undeploy();
	}

	@Reference
	private ApplicationPublisher _applicationPublisher;

}