/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.repository;

import com.liferay.object.repository.entity.ObjectEntity;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;

import java.util.List;

/**
 * @author Riccardo Alberti
 */
public interface ObjectRepository<T extends ObjectEntity> {

	public T deleteObjectEntity(long objectEntryId) throws PortalException;

	public T fetchObjectEntity(long objectEntryId) throws PortalException;

	public List<T> getObjectEntities(
			long groupId, long companyId, long userId, String search, int start,
			int end, String predicateName, Object... parameters)
		throws PortalException;

	public int getObjectEntitiesCount(
			long groupId, long companyId, long userId, String search,
			String predicateName, Object... parameters)
		throws PortalException;

	public T getObjectEntity(long objectEntryId) throws PortalException;

	public T saveObjectEntity(
			long groupId, long companyId, long userId, T entity,
			ServiceContext serviceContext)
		throws PortalException;

	public T updateObjectEntity(
			long userId, long objectEntryId, T entity,
			ServiceContext serviceContext)
		throws PortalException;

}