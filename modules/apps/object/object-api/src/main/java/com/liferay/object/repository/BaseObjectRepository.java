/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.repository;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.petra.sql.dsl.DynamicObjectDefinitionTable;
import com.liferay.object.repository.entity.ObjectEntity;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.sql.dsl.query.sort.OrderByExpression;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.ObjectValuePair;

import java.io.Serializable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Reference;

/**
 * @author Riccardo Alberti
 */
public abstract class BaseObjectRepository<T extends ObjectEntity>
	implements ObjectRepository<T> {

	public BaseObjectRepository() {
		objectMapper.configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@Override
	public T deleteObjectEntity(long objectEntryId) throws PortalException {
		T objectEntity = getObjectEntity(objectEntryId);

		objectEntryLocalService.deleteObjectEntry(objectEntryId);

		return objectEntity;
	}

	@Override
	public T fetchObjectEntity(long objectEntryId) throws PortalException {
		ObjectEntry objectEntry = objectEntryLocalService.fetchObjectEntry(
			objectEntryId);

		if (objectEntry == null) {
			return null;
		}

		return _toObjectEntity(objectEntry.getValues());
	}

	@Override
	public List<T> getObjectEntities(
			long groupId, long companyId, long userId, String search, int start,
			int end, String predicateName, Object... parameters)
		throws PortalException {

		long objectDefinitionId = _getObjectDefinitionId(companyId);

		ObjectValuePair<Predicate, OrderByExpression[]> objectValuePair =
			getPredicateAndOrderByExpression(
				predicateName,
				_getDynamicObjectDefinitionTable(companyId, objectDefinitionId),
				parameters);

		return _toObjectEntities(
			objectEntryLocalService.getValuesList(
				groupId, companyId, userId, objectDefinitionId,
				objectValuePair.getKey(), search, start, end,
				objectValuePair.getValue()));
	}

	@Override
	public int getObjectEntitiesCount(
			long groupId, long companyId, long userId, String search,
			String predicateName, Object... parameters)
		throws PortalException {

		long objectDefinitionId = _getObjectDefinitionId(companyId);

		ObjectValuePair<Predicate, OrderByExpression[]> objectValuePair =
			getPredicateAndOrderByExpression(
				predicateName,
				_getDynamicObjectDefinitionTable(companyId, objectDefinitionId),
				parameters);

		return objectEntryLocalService.getValuesListCount(
			groupId, companyId, userId, objectDefinitionId,
			objectValuePair.getKey(), search);
	}

	@Override
	public T getObjectEntity(long objectEntryId) throws PortalException {
		return _toObjectEntity(
			objectEntryLocalService.getValues(objectEntryId));
	}

	@Override
	public T saveObjectEntity(
			long groupId, long companyId, long userId, T entity,
			ServiceContext serviceContext)
		throws PortalException {

		ObjectEntry objectEntry = objectEntryLocalService.addObjectEntry(
			userId, groupId, _getObjectDefinitionId(companyId),
			objectMapper.convertValue(
				entity,
				new TypeReference<Map<String, Serializable>>() {
				}),
			serviceContext);

		return getObjectEntity(objectEntry.getObjectEntryId());
	}

	@Override
	public T updateObjectEntity(
			long userId, long objectEntryId, T entity,
			ServiceContext serviceContext)
		throws PortalException {

		ObjectEntry objectEntry = objectEntryLocalService.updateObjectEntry(
			userId, objectEntryId,
			objectMapper.convertValue(
				entity,
				new TypeReference<Map<String, Serializable>>() {
				}),
			serviceContext);

		return getObjectEntity(objectEntry.getObjectEntryId());
	}

	protected abstract ObjectValuePair<Predicate, OrderByExpression[]>
		getPredicateAndOrderByExpression(
			String predicateName,
			DynamicObjectDefinitionTable dynamicObjectDefinitionTable,
			Object... parameters);

	protected static final ObjectMapper objectMapper = new ObjectMapper();

	@Reference
	protected ObjectDefinitionLocalService objectDefinitionLocalService;

	@Reference
	protected ObjectEntryLocalService objectEntryLocalService;

	@Reference
	protected ObjectFieldLocalService objectFieldLocalService;

	private DynamicObjectDefinitionTable _getDynamicObjectDefinitionTable(
			long companyId, long objectDefinitionId)
		throws PortalException {

		ObjectDefinition objectDefinition =
			objectDefinitionLocalService.fetchObjectDefinition(
				companyId, _getObjectDefinitionName());

		return new DynamicObjectDefinitionTable(
			objectDefinition,
			objectFieldLocalService.getObjectFields(
				objectDefinitionId, objectDefinition.getDBTableName()),
			objectDefinition.getDBTableName());
	}

	private long _getObjectDefinitionId(long companyId) throws PortalException {
		ObjectDefinition objectDefinition =
			objectDefinitionLocalService.fetchObjectDefinition(
				companyId, _getObjectDefinitionName());

		if (objectDefinition == null) {
			return 0;
		}

		return objectDefinition.getObjectDefinitionId();
	}

	private String _getObjectDefinitionName() throws PortalException {
		Class<T> clazz = _getObjectEntityClass();

		JsonTypeName annotation = clazz.getAnnotation(JsonTypeName.class);

		if (annotation == null) {
			throw new PortalException(
				clazz.getSimpleName() + " is not a valid ObjectEntity");
		}

		return annotation.value();
	}

	private Class<T> _getObjectEntityClass() {
		Class<?> clazz = getClass();

		ParameterizedType parameterizedType =
			(ParameterizedType)clazz.getGenericSuperclass();

		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

		return (Class<T>)actualTypeArguments[0];
	}

	private List<T> _toObjectEntities(
		List<Map<String, Serializable>> entityMaps) {

		return TransformUtil.transform(entityMaps, this::_toObjectEntity);
	}

	private T _toObjectEntity(Map<String, Serializable> entityMap) {
		return objectMapper.convertValue(entityMap, _getObjectEntityClass());
	}

}