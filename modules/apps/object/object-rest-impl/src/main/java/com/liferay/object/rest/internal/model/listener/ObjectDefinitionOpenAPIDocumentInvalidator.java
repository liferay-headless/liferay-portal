/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.model.listener;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.system.JaxRsApplicationDescriptor;
import com.liferay.object.system.SystemObjectDefinitionManager;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.vulcan.application.OpenAPIDocumentCacheInvalidator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Petteri Karttunen
 */
@Component(service = ObjectDefinitionOpenAPIDocumentInvalidator.class)
public class ObjectDefinitionOpenAPIDocumentInvalidator {

	public void invalidate(long objectDefinitionId) {
		invalidate(
			_objectDefinitionLocalService.fetchObjectDefinition(
				objectDefinitionId));
	}

	public void invalidate(ObjectDefinition objectDefinition) {
		if ((objectDefinition == null) || !objectDefinition.isApproved()) {
			return;
		}

		String restContextPath = _getRESTContextPath(objectDefinition);

		if (restContextPath == null) {
			return;
		}

		_openAPIDocumentCacheInvalidator.invalidate(
			objectDefinition.getCompanyId(), restContextPath);
	}

	private String _getRESTContextPath(ObjectDefinition objectDefinition) {
		if (!objectDefinition.isUnmodifiableSystemObject()) {
			return objectDefinition.getRESTContextPath();
		}

		SystemObjectDefinitionManager systemObjectDefinitionManager =
			_systemObjectDefinitionManagerRegistry.
				getSystemObjectDefinitionManager(objectDefinition.getName());

		if (systemObjectDefinitionManager == null) {
			return null;
		}

		JaxRsApplicationDescriptor jaxRsApplicationDescriptor =
			systemObjectDefinitionManager.getJaxRsApplicationDescriptor();

		if (jaxRsApplicationDescriptor == null) {
			return null;
		}

		String restContextPath =
			jaxRsApplicationDescriptor.getRESTContextPath();

		if (Validator.isNull(restContextPath)) {
			return null;
		}

		if (!StringUtil.startsWith(restContextPath, StringPool.SLASH)) {
			return StringPool.SLASH + restContextPath;
		}

		return restContextPath;
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private OpenAPIDocumentCacheInvalidator _openAPIDocumentCacheInvalidator;

	@Reference
	private SystemObjectDefinitionManagerRegistry
		_systemObjectDefinitionManagerRegistry;

}