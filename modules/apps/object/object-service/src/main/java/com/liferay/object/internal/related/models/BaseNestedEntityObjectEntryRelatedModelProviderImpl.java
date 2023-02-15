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

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.related.models.NestedEntityObjectRelatedModelsProvider;

import java.util.List;
import java.util.Map;

/**
 * @author Sergio Jiménez del Coso
 */
public abstract class BaseNestedEntityObjectEntryRelatedModelProviderImpl
	implements NestedEntityObjectRelatedModelsProvider {

	public BaseNestedEntityObjectEntryRelatedModelProviderImpl(
		ObjectDefinition objectDefinition) {

		this.objectDefinition = objectDefinition;
	}

	@Override
	public String getClassName() {
		return objectDefinition.getClassName();
	}

	protected boolean isMap(List<Object> nestedObjectEntryPropertiesList) {
		for (Object nestedObjectEntryProperties :
				nestedObjectEntryPropertiesList) {

			if (!(nestedObjectEntryProperties instanceof Map)) {
				return false;
			}
		}

		return true;
	}

	protected final ObjectDefinition objectDefinition;

}