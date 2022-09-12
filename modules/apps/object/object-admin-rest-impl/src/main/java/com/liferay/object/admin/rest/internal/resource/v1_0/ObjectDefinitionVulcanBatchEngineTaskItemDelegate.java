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

package com.liferay.object.admin.rest.internal.resource.v1_0;

import com.liferay.object.admin.rest.dto.v1_0.ObjectDefinition;
import com.liferay.object.admin.rest.dto.v1_0.Status;
import com.liferay.object.admin.rest.internal.dto.v1_0.util.ObjectFieldUtil;
import com.liferay.object.admin.rest.internal.odata.entity.v1_0.ObjectDefinitionEntityModel;
import com.liferay.object.exception.ObjectDefinitionStorageTypeException;
import com.liferay.object.service.ObjectDefinitionService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFieldSettingLocalService;
import com.liferay.object.service.ObjectFilterLocalService;
import com.liferay.object.util.LocalizedMapUtil;
import com.liferay.petra.function.UnsafeBiConsumer;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.function.UnsafeFunction;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;
import com.liferay.portal.vulcan.batch.engine.VulcanBatchEngineTaskItemDelegate;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.TransformUtil;

import java.io.Serializable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Igor Beslic
 */
@Component(
	property = "batch.engine.task.item.delegate.name=BATCH",
	service = VulcanBatchEngineTaskItemDelegate.class
)
public class ObjectDefinitionVulcanBatchEngineTaskItemDelegate
	implements VulcanBatchEngineTaskItemDelegate<ObjectDefinition> {

	@Override
	public void create(
			Collection<ObjectDefinition> objectDefinitions,
			Map<String, Serializable> parameters)
		throws Exception {

		UnsafeConsumer<ObjectDefinition, Exception> unsafeConsumer =
			_getUnsafeConsumer(parameters);

		for (ObjectDefinition objectDefinition : objectDefinitions) {
			if (!Validator.isBlank(objectDefinition.getStorageType()) &&
				!GetterUtil.getBoolean(
					PropsUtil.get("feature.flag.LPS-135430"))) {

				throw new ObjectDefinitionStorageTypeException();
			}

			unsafeConsumer.accept(objectDefinition);
		}
	}

	@Override
	public void delete(
			Collection<ObjectDefinition> items,
			Map<String, Serializable> parameters)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	@Override
	public EntityModel getEntityModel(Map<String, List<String>> multivaluedMap)
		throws Exception {

		return _entityModel;
	}

	@Override
	public Page<ObjectDefinition> read(
		Filter filter, Pagination pagination, Sort[] sorts,
		Map<String, Serializable> parameters, String search) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void setContextBatchUnsafeConsumer(
		UnsafeBiConsumer
			<Collection<ObjectDefinition>,
			 UnsafeConsumer<ObjectDefinition, Exception>, Exception>
				contextBatchUnsafeConsumer) {

		throw new UnsupportedOperationException();
	}

	@Override
	public void setContextCompany(Company contextCompany) {
		if (_log.isWarnEnabled()) {
			_log.warn(
				"Provided company is ignored in BATCH. Current user's " +
					"company would be used instead.");
		}
	}

	@Override
	public void setContextUser(User contextUser) {
		_contextUser = contextUser;
	}

	@Override
	public void setLanguageId(String languageId) {
		_contextAcceptLanguage = new AcceptLanguage() {

			@Override
			public List<Locale> getLocales() {
				return null;
			}

			@Override
			public String getPreferredLanguageId() {
				return languageId;
			}

			@Override
			public Locale getPreferredLocale() {
				return LocaleUtil.fromLanguageId(languageId);
			}

		};
	}

	@Override
	public void update(
			Collection<ObjectDefinition> items,
			Map<String, Serializable> parameters)
		throws Exception {

		throw new UnsupportedOperationException();
	}

	protected <T, R, E extends Throwable> List<R> transformToList(
		T[] array, UnsafeFunction<T, R, E> unsafeFunction) {

		return TransformUtil.transformToList(array, unsafeFunction);
	}

	private com.liferay.object.model.ObjectDefinition _create(
			ObjectDefinition objectDefinition)
		throws Exception {

		return _objectDefinitionService.addCustomObjectDefinition(
			LocalizedMapUtil.getLocalizedMap(objectDefinition.getLabel()),
			objectDefinition.getName(), objectDefinition.getPanelAppOrder(),
			objectDefinition.getPanelCategoryKey(),
			LocalizedMapUtil.getLocalizedMap(objectDefinition.getPluralLabel()),
			objectDefinition.getScope(), objectDefinition.getStorageType(),
			transformToList(
				objectDefinition.getObjectFields(),
				objectField -> ObjectFieldUtil.toObjectField(
					objectField, _objectFieldLocalService,
					_objectFieldSettingLocalService,
					_objectFilterLocalService)));
	}

	private void _createWithWorkflow(ObjectDefinition objectDefinition)
		throws Exception {

		com.liferay.object.model.ObjectDefinition objectDefinition1 = _create(
			objectDefinition);

		Status status = objectDefinition.getStatus();

		if (status.getCode() == WorkflowConstants.STATUS_APPROVED) {
			_objectDefinitionService.publishCustomObjectDefinition(
				objectDefinition1.getObjectDefinitionId());
		}
	}

	private UnsafeConsumer<ObjectDefinition, Exception> _getUnsafeConsumer(
		Map<String, Serializable> parameters) {

		Boolean applyWorkflowStatus = (Boolean)parameters.getOrDefault(
			"applyWorkflowStatus", Boolean.TRUE);

		String createStrategy = (String)parameters.getOrDefault(
			"createStrategy", "INSERT");

		if ("INSERT".equalsIgnoreCase(createStrategy)) {
			if (!applyWorkflowStatus) {
				return objectDefinition -> _create(objectDefinition);
			}

			return objectDefinition -> _createWithWorkflow(objectDefinition);
		}

		if (!applyWorkflowStatus) {
			return objectDefinition -> _upsert(objectDefinition);
		}

		return objectDefinition -> _upsertWithWorkflow(objectDefinition);
	}

	private void _upsert(ObjectDefinition objectDefinition) {

		// https://issues.liferay.com/browse/LPS-162113

		throw new UnsupportedOperationException();
	}

	private void _upsertWithWorkflow(ObjectDefinition objectDefinition) {

		// https://issues.liferay.com/browse/LPS-162113

		throw new UnsupportedOperationException();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectDefinitionVulcanBatchEngineTaskItemDelegate.class);

	private static final EntityModel _entityModel =
		new ObjectDefinitionEntityModel();

	private AcceptLanguage _contextAcceptLanguage;
	private User _contextUser;

	@Reference
	private ObjectDefinitionService _objectDefinitionService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectFieldSettingLocalService _objectFieldSettingLocalService;

	@Reference
	private ObjectFilterLocalService _objectFilterLocalService;

}