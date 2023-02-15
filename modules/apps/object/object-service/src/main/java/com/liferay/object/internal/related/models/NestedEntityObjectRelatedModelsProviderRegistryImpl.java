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

package com.liferay.object.internal.related.models;

import com.liferay.object.related.models.NestedEntityObjectRelatedModelsProvider;
import com.liferay.object.related.models.NestedEntityObjectRelatedModelsProviderRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.string.StringPool;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Sergio Jiménez del Coso
 */
@Component(service = NestedEntityObjectRelatedModelsProviderRegistry.class)
public class NestedEntityObjectRelatedModelsProviderRegistryImpl
	implements NestedEntityObjectRelatedModelsProviderRegistry {

	@Override
	public NestedEntityObjectRelatedModelsProvider
		getNestedEntityRelatedModelsProvider(String className, String type) {

		String key = _getKey(className, type);

		NestedEntityObjectRelatedModelsProvider
			nestedEntityRelatedModelsProvider = _serviceTrackerMap.getService(
				key);

		if (nestedEntityRelatedModelsProvider == null) {
			throw new IllegalArgumentException(
				"No nested entity related models provider found with key " +
					key);
		}

		return nestedEntityRelatedModelsProvider;
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, NestedEntityObjectRelatedModelsProvider.class, null,
			(serviceReference, emitter) -> {
				NestedEntityObjectRelatedModelsProvider
					nestedEntityRelatedModelsProvider =
						bundleContext.getService(serviceReference);

				emitter.emit(
					_getKey(
						nestedEntityRelatedModelsProvider.getClassName(),
						nestedEntityRelatedModelsProvider.
							getObjectRelationshipType()));
			});
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private String _getKey(String className, String type) {
		return className + StringPool.POUND + type;
	}

	private ServiceTrackerMap<String, NestedEntityObjectRelatedModelsProvider>
		_serviceTrackerMap;

}