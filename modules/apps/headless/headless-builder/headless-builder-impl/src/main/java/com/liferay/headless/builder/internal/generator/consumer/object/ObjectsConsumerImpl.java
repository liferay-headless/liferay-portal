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

package com.liferay.headless.builder.internal.generator.consumer.object;

import com.liferay.headless.builder.internal.generator.application.ApiApplication;
import com.liferay.headless.builder.internal.generator.consumer.Consumer;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.util.GroupThreadLocal;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luis Miguel Barcos
 */
@Component(service = Consumer.class)
public class ObjectsConsumerImpl implements Consumer<String> {

	@Override
	public ApiApplication getApplicationInformation(String apiApplicationERC)
		throws Exception {

		ObjectEntry apiApplicationObjectEntry =
			_objectEntryLocalService.getObjectEntry(
				apiApplicationERC, CompanyThreadLocal.getCompanyId(),
				GroupThreadLocal.getGroupId());

		Map<String, Serializable> apiApplicationObjectEntryProperties =
			apiApplicationObjectEntry.getValues();

		ApiApplication.Builder builder = new ApiApplication.Builder();

		return builder.setBaseURL(
			_getBaseURL(apiApplicationObjectEntryProperties)
		).setCompanyId(
			apiApplicationObjectEntry.getCompanyId()
		).setOsgiJaxRsName(
			_buildOsgiJaxRsName(
				apiApplicationObjectEntryProperties,
				apiApplicationObjectEntry.getCompanyId())
		).build();
	}

	private String _buildOsgiJaxRsName(
		Map<String, Serializable> apiApplicationObjectEntryProperties,
		long companyId) {

		return _getTitle(apiApplicationObjectEntryProperties) + companyId;
	}

	private String _getBaseURL(
		Map<String, Serializable> apiApplicationObjectEntryProperties) {

		return (String)_getPropertyValue(
			ApiApplicationObjectProperties.BASE_URL,
			apiApplicationObjectEntryProperties);
	}

	private Object _getPropertyValue(
		ApiApplicationObjectProperties apiApplicationObjectProperty,
		Map<String, Serializable> apiApplicationObjectEntryProperties) {

		return apiApplicationObjectEntryProperties.get(
			apiApplicationObjectProperty.getPropertyName());
	}

	private String _getTitle(
		Map<String, Serializable> apiApplicationObjectEntryProperties) {

		return (String)_getPropertyValue(
			ApiApplicationObjectProperties.TITLE,
			apiApplicationObjectEntryProperties);
	}

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}