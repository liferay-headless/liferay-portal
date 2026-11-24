/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.model.listener;

import com.liferay.object.model.ObjectRelationship;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = ModelListener.class)
public class ObjectRelationshipModelListener
	extends BaseModelListener<ObjectRelationship> {

	@Override
	public void onAfterCreate(ObjectRelationship objectRelationship) {
		_invalidate(objectRelationship);
	}

	@Override
	public void onAfterRemove(ObjectRelationship objectRelationship) {
		_invalidate(objectRelationship);
	}

	@Override
	public void onAfterUpdate(
		ObjectRelationship originalObjectRelationship,
		ObjectRelationship objectRelationship) {

		_invalidate(objectRelationship);
	}

	private void _invalidate(ObjectRelationship objectRelationship) {
		_objectDefinitionOpenAPIDocumentInvalidator.invalidate(
			objectRelationship.getObjectDefinitionId1());
		_objectDefinitionOpenAPIDocumentInvalidator.invalidate(
			objectRelationship.getObjectDefinitionId2());
	}

	@Reference
	private ObjectDefinitionOpenAPIDocumentInvalidator
		_objectDefinitionOpenAPIDocumentInvalidator;

}