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

package com.liferay.batch.engine.internal.action;

import com.liferay.batch.engine.action.ImportTaskPostAction;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.vulcan.extension.ExtensionProvider;

import java.io.Serializable;

import java.lang.reflect.Field;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Matija Petanjek
 */
@Component(service = ImportTaskPostAction.class)
public class ExtendedObjectImportTaskPostAction
	implements ImportTaskPostAction {

	@Override
	public void run(
		BatchEngineImportTask batchEngineImportTask, Object item,
		Object persistedItem) {

		try {
			Map<String, Serializable> extendedProperties =
				_getExtendedProperties(item);

			if (extendedProperties != null) {
				_objectEntryExtensionProvider.setExtendedProperties(
					batchEngineImportTask.getCompanyId(),
					batchEngineImportTask.getUserId(),
					batchEngineImportTask.getClassName(), persistedItem,
					extendedProperties);
			}
		}
		catch (Exception exception) {
			_log.error("Failed to process object extended fields", exception);
		}
	}

	private Map<String, Serializable> _getExtendedProperties(Object item) {
		try {
			Field field = ReflectionUtil.getDeclaredField(
				item.getClass(), "extendedProperties");

			return (Map<String, Serializable>)field.get(item);
		}
		catch (Exception exception) {
			_log.error(exception);

			return null;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ExtendedObjectImportTaskPostAction.class);

	@Reference(
		target = "(component.name=com.liferay.object.rest.internal.vulcan.extension.v1_0.ObjectEntryExtensionProvider)"
	)
	private ExtensionProvider _objectEntryExtensionProvider;

}