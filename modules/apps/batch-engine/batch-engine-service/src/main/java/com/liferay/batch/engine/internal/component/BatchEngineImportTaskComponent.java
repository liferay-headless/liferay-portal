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

package com.liferay.batch.engine.internal.component;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import com.liferay.batch.engine.BatchEngineImportTaskExecutor;
import com.liferay.batch.engine.internal.unit.BatchEngineUnitProcessorImpl;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.unit.BatchEngineUnit;
import com.liferay.petra.executor.PortalExecutorManager;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.vulcan.resource.EntityModelResource;

/**
 * @author Raymond Augé
 */
@Component(
	factory = "batch.engine.import.task.component",
	scope = ServiceScope.PROTOTYPE, service = {}
)
public class BatchEngineImportTaskComponent {

	@Activate
	protected void activate(
		ComponentContext componentContext, Map<String, Object> properties) {

		ComponentInstance<BatchEngineImportTaskComponent> componentInstance =
			componentContext.getComponentInstance();

		BatchEngineImportTask batchEngineImportTask =
			(BatchEngineImportTask)properties.get("batchEngineImportTask");

		BatchEngineUnit batchEngineUnit = (BatchEngineUnit)properties.get(
			"batchEngineUnit");

		ExecutorService executorService =
			_portalExecutorManager.getPortalExecutor(
				BatchEngineUnitProcessorImpl.class.getName());

		executorService.submit(
			() -> {
				try {
					_batchEngineImportTaskExecutor.execute(
						batchEngineImportTask);

					if (_log.isInfoEnabled()) {
						_log.info(
							StringBundler.concat(
								"Successfully deployed batch engine file ",
								batchEngineUnit.getFileName(), " ",
								batchEngineUnit.getDataFileName()));
					}
				}
				finally {
					componentInstance.dispose();
				}
			});
	}

	private static final Log _log = LogFactoryUtil.getLog(
		BatchEngineImportTaskComponent.class);

	@Reference
	private BatchEngineImportTaskExecutor _batchEngineImportTaskExecutor;

	@Reference()
	private EntityModelResource _entityModelResource;

	@Reference
	private PortalExecutorManager _portalExecutorManager;

}