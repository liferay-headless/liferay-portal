/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.batch.engine.internal.exportimport.data.handler;

import com.liferay.document.library.exportimport.data.handler.DLExportableRepositoryPublisher;
import com.liferay.portal.kernel.model.Repository;
import com.liferay.portal.kernel.service.RepositoryLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.function.Consumer;

/**
 * @author Vendel Toreki
 */
public class BatchEngineAttachmentDLExportableRepositoryPublisher
	implements DLExportableRepositoryPublisher {

	public BatchEngineAttachmentDLExportableRepositoryPublisher(
		String portletId, RepositoryLocalService repositoryLocalService) {

		_portletId = portletId;
		_repositoryLocalService = repositoryLocalService;
	}

	@Override
	public void publish(
		long groupId, String portletId, Consumer<Long> repositoryIdConsumer) {

		if (Validator.isNull(portletId) ||
			!StringUtil.equals(portletId, _portletId)) {

			return;
		}

		Repository repository = _repositoryLocalService.fetchRepository(
			groupId, _portletId);

		if (repository != null) {
			repositoryIdConsumer.accept(repository.getRepositoryId());
		}
	}

	private final String _portletId;
	private final RepositoryLocalService _repositoryLocalService;

}