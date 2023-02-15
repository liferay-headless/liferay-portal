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

import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.related.models.NestedEntityRelatedModelsProvider;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.util.StringUtil;

import java.util.List;

import javax.ws.rs.BadRequestException;

/**
 * @author Sergio Jiménez del Coso
 */
public class NestedEntityObjectEntryMtoMRelatedModelsProviderImpl
	implements NestedEntityRelatedModelsProvider {

	public NestedEntityObjectEntryMtoMRelatedModelsProviderImpl(
		ObjectDefinition objectDefinition,
		ObjectFieldLocalService objectFieldLocalService) {

		_objectDefinition = objectDefinition;
		_objectFieldLocalService = objectFieldLocalService;
	}

	@Override
	public String getClassName() {
		return _objectDefinition.getClassName();
	}

	@Override
	public String getObjectRelationshipType() {
		return ObjectRelationshipConstants.TYPE_MANY_TO_MANY;
	}

	public void validate(
		Object propertyValue, ObjectEntry serviceBuilderObjectEntry,
		ObjectRelationship objectRelationship) {

		if (!(propertyValue instanceof List) ||
			!StringUtil.equals(
				objectRelationship.getType(), getObjectRelationshipType())) {

			throw new BadRequestException(
				"Unable to create nested object entries for object entry " +
					serviceBuilderObjectEntry.getObjectEntryId());
		}
	}

	private final ObjectDefinition _objectDefinition;
	private final ObjectFieldLocalService _objectFieldLocalService;

}