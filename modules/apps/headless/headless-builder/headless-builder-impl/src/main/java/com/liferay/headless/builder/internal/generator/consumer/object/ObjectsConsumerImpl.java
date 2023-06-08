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

package com.liferay.headless.builder.internal.generator.consumer.object;

import com.liferay.headless.builder.internal.generator.application.ApiApplication;
import com.liferay.headless.builder.internal.generator.application.Operation;
import com.liferay.headless.builder.internal.generator.consumer.Consumer;
import com.liferay.headless.builder.internal.generator.consumer.object.model.ApiApplicationObjectModel;
import com.liferay.headless.builder.internal.generator.consumer.object.model.ApiEndpointsObjectModel;
import com.liferay.headless.builder.internal.generator.consumer.object.model.ObjectModelsFactory;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = Consumer.class)
public class ObjectsConsumerImpl implements Consumer<String> {

	@Override
	public ApiApplication getApiApplication(String apiApplicationERC)
		throws Exception {

		ApiApplicationObjectModel apiApplicationObjectModel =
			_objectModelsFactory.getObjectModel(
				apiApplicationERC, ApiApplicationObjectModel.class);

		ApiApplication.Builder builder = new ApiApplication.Builder();

		return builder.setBaseURL(
			apiApplicationObjectModel.getBaseURL()
		).setCompanyId(
			apiApplicationObjectModel.getCompanyId()
		).setOsgiJaxRsName(
			apiApplicationObjectModel.getOsgiJaxRsName()
		).setOperations(
			_getOperations(apiApplicationERC)
		).build();
	}

	private List<Operation> _getOperations(String apiApplicationERC)
		throws Exception {

		ApiEndpointsObjectModel apiEndpointsObjectModel =
			_objectModelsFactory.getObjectModel(
				apiApplicationERC, ApiEndpointsObjectModel.class);

		List<Operation> operations = new ArrayList<>();

		Operation.Builder operationBuilder = new Operation.Builder();

		for (ApiEndpointsObjectModel.ApiEndpoint apiEndpoint :
				apiEndpointsObjectModel.getApiEndpoints()) {

			operations.add(
				operationBuilder.setMethod(
					apiEndpoint.getMethod()
				).setPath(
					apiEndpoint.getPath()
				).setScope(
					apiEndpoint.getScope()
				).build());
		}

		return operations;
	}

	@Reference
	private ObjectModelsFactory _objectModelsFactory;

}