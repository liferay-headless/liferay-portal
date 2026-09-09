/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.portal.instances.internal.resource.v1_0;

import com.liferay.headless.portal.instances.resource.v1_0.PortalInstanceOperationResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Alberto Chaparro
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/portal-instance-operation.properties",
	scope = ServiceScope.PROTOTYPE,
	service = PortalInstanceOperationResource.class
)
public class PortalInstanceOperationResourceImpl
	extends BasePortalInstanceOperationResourceImpl {
}