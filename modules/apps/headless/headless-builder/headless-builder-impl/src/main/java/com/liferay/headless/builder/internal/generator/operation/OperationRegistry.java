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

package com.liferay.headless.builder.internal.generator.operation;

import com.liferay.headless.builder.internal.generator.application.ApiApplication;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Carlos Correa
 * @author Luis Miguel Barcos
 */
@Component(service = OperationRegistry.class)
public class OperationRegistry {

	public void registerApiApplicationOperations(
		ApiApplication apiApplication) {

		for (Operation operation : apiApplication.getOperations()) {
			_register(operation);
		}
	}

	public void unregisterApiApplicationOperations(
		ApiApplication apiApplication) {

		for (Operation operation : apiApplication.getOperations()) {
			_unregister(operation);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, Operation.class);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();
	}

	private boolean _exists(Operation operation) {
		for (Operation existingOperation : _serviceTrackerList) {
			if (Objects.equals(operation, existingOperation)) {
				return true;
			}
		}

		return false;
	}

	private void _register(Operation operation) {
		if (_exists(operation)) {
			throw new IllegalStateException(
				StringBundler.concat(
					"There is already an operation for the company ",
					operation.getCompanyId(), " and the endpoint ",
					operation.getMethod(
					).name(),
					" ", operation.getPath()));
		}

		_serviceRegistrations.put(
			operation.getKey(),
			_bundleContext.registerService(
				Operation.class, operation,
				HashMapDictionaryBuilder.<String, Object>put(
					"companyId", operation::getCompanyId
				).put(
					"operation.key", operation.getKey()
				).build()));
	}

	private void _unregister(Operation operation) {
		ServiceRegistration<Operation> serviceRegistration =
			_serviceRegistrations.remove(operation.getKey());

		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}
	}

	private BundleContext _bundleContext;
	private final Map<String, ServiceRegistration<Operation>>
		_serviceRegistrations = new HashMap<>();
	private ServiceTrackerList<Operation> _serviceTrackerList;

}