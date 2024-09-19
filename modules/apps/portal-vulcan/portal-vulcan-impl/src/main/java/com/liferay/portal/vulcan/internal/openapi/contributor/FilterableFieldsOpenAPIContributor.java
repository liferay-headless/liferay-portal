/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.openapi.contributor;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.openapi.OpenAPIContext;
import com.liferay.portal.vulcan.openapi.contributor.OpenAPIContributor;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.cxf.common.util.CollectionUtils;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * @author Magdalena Jedraszak
 */
@Component(service = OpenAPIContributor.class)
public class FilterableFieldsOpenAPIContributor implements OpenAPIContributor {

	@Override
	public void contribute(OpenAPI openAPI, OpenAPIContext openAPIContext)
		throws Exception {

		if (openAPIContext == null) {
			return;
		}

		Map<String, Schema> schemas = openAPI.getComponents(
		).getSchemas();

		if (schemas.isEmpty()) {
			return;
		}

		Map<String, EntityField> entityFieldsMap = _fetchEntityFieldsMap(
			openAPIContext.getPath());

		if (MapUtil.isEmpty(entityFieldsMap)) {
			return;
		}

		for (Schema schema : schemas.values()) {
			_processSchemaProperties(schema, entityFieldsMap);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext)
		throws InvalidSyntaxException {

		_bundleContext = bundleContext;
	}

	private Map<String, EntityField> _fetchEntityFieldsMap(String path)
		throws Exception {

		String trimmedPath = StringUtil.removeFirst(path, "/o");

		String applicationBase = StringUtil.replaceLast(trimmedPath, '/', "");

		String applicationFilter = String.format(
			"(osgi.jaxrs.application.base=%s)", applicationBase);

		Collection<ServiceReference<Application>>
			bundleContextServiceReferences =
			_bundleContext.getServiceReferences(
				Application.class, applicationFilter);

		if (CollectionUtils.isEmpty(bundleContextServiceReferences)) {
			return Collections.emptyMap();
		}

		boolean objects = false;

		List<EntityModelResource> resources = new ArrayList<>();

		for (ServiceReference<Application> applicationServiceReference :
			bundleContextServiceReferences) {

			String applicationName =
				(String)applicationServiceReference.getProperty(
					"osgi.jaxrs.name");

			if (applicationName.isEmpty()) {
				continue;
			}

			Object objectsObject = applicationServiceReference.getProperty(
				"liferay.objects");

			if (objectsObject != null) {
				objects = (boolean)objectsObject;
			}

			String resourceFilter = StringBundler.concat(
				"(&(osgi.jaxrs.resource=true)(osgi.jaxrs.application.select=\\",
				"(osgi.jaxrs.name=", applicationName, "\\)))");

			ServiceReference<?>[] resourceServiceReferences =
				_bundleContext.getServiceReferences(
					(String)null, resourceFilter);

			List<EntityModelResource> entityModelResources = new ArrayList<>();

			for (ServiceReference<?> serviceReference :
				resourceServiceReferences) {

				Object service = _bundleContext.getService(serviceReference);

				if (!(service instanceof EntityModelResource)) {
					continue;
				}

				entityModelResources.add((EntityModelResource)service);
			}

			resources.addAll(entityModelResources);
		}

		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();

		for (EntityModelResource resource : resources) {
			if (objects) {
				HttpServletRequest httpServletRequest =
					_getHttpServletRequestFromServiceContext();

				if (httpServletRequest != null) {
					long companyId = (long)httpServletRequest.getAttribute(
						WebKeys.COMPANY_ID);

					params.putSingle("companyId", String.valueOf(companyId));
				}
			}

			EntityModel entityModel = resource.getEntityModel(params);

			if (entityModel == null) {
				continue;
			}

			return entityModel.getEntityFieldsMap();
		}

		return Collections.emptyMap();
	}

	private HttpServletRequest _getHttpServletRequestFromServiceContext() {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext != null) {
			HttpServletRequest httpServletRequest = serviceContext.getRequest();

			if (httpServletRequest != null) {
				return httpServletRequest;
			}
		}

		return null;
	}

	private void _processSchemaProperties(
		Schema schema, Map<String, EntityField> entityFieldsMap) {

		Map<String, Schema> properties = schema.getProperties();

		if (properties == null) {
			return;
		}

		for (Map.Entry<String, Schema> propertyEntry : properties.entrySet()) {
			String propertyName = propertyEntry.getKey();

			if (entityFieldsMap.containsKey(propertyName)) {
				Schema propertySchema = propertyEntry.getValue();

				propertySchema.addExtension("x-filterable", true);
			}
		}
	}

	private BundleContext _bundleContext;

}