/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.system.JaxRsApplicationDescriptor;
import com.liferay.object.system.SystemObjectDefinitionManager;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author Petteri Karttunen
 */
public class ObjectRESTPathUtil {

	public static String getRESTContextPath(ObjectDefinition objectDefinition) {
		if (!objectDefinition.isUnmodifiableSystemObject()) {
			return objectDefinition.getRESTContextPath();
		}

		SystemObjectDefinitionManagerRegistry
			systemObjectDefinitionManagerRegistry =
				_systemObjectDefinitionManagerRegistrySnapshot.get();

		if (systemObjectDefinitionManagerRegistry == null) {
			return null;
		}

		SystemObjectDefinitionManager systemObjectDefinitionManager =
			systemObjectDefinitionManagerRegistry.
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

	private static final Snapshot<SystemObjectDefinitionManagerRegistry>
		_systemObjectDefinitionManagerRegistrySnapshot = new Snapshot<>(
			ObjectRESTPathUtil.class,
			SystemObjectDefinitionManagerRegistry.class);

}