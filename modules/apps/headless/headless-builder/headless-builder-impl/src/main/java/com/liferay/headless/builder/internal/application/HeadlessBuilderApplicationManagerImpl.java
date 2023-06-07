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
import com.liferay.headless.builder.internal.generator.consumer.Consumer;
import com.liferay.headless.builder.internal.generator.publisher.ApplicationPublisher;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = HeadlessBuilderApplicationManager.class)
public class HeadlessBuilderApplicationManagerImpl<T>
	implements HeadlessBuilderApplicationManager<T> {

	@Override
	public void publishApplication(T applicationIdentifier) throws Exception {

		// TODO Implement the extraction of the information from the @Consumer

		ApiApplication apiApplication = _consumer.getApiApplication(
			applicationIdentifier);

		// TODO Implement the publication of the REST Application
		//  with the @Publisher

		_applicationPublisher.publish(apiApplication);
	}

	@Override
	public void unpublishApplication() throws Exception {
		_applicationPublisher.undeploy();
	}

	@Reference
	private ApplicationPublisher _applicationPublisher;

	@Reference
	private Consumer<T> _consumer;

}