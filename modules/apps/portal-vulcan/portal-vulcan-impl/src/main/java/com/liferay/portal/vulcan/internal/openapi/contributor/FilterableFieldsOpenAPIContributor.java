/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.openapi.contributor;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.StringUtil;
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

	private String _createApplicationFilter(String applicationBase) {
		return String.format(
			"(osgi.jaxrs.application.base=%s)", applicationBase);
	}

	private Map<String, EntityField> _extractEntityFieldsFromResources(
			List<EntityModelResource> resources)
		throws Exception {

		for (EntityModelResource resource : resources) {
			EntityModel entityModel = resource.getEntityModel(
				new MultivaluedHashMap<>());

			if (entityModel == null) {
				continue;
			}

			return entityModel.getEntityFieldsMap();
		}

		return Collections.emptyMap();
	}

	private Map<String, EntityField> _fetchEntityFieldsMap(String path)
		throws Exception {

		List<EntityModelResource> resources = _findEntityModelResources(path);

		return _extractEntityFieldsFromResources(resources);
	}

	private List<EntityModelResource> _findEntityModelResources(String path)
		throws Exception {

		String trimmedPath = StringUtil.removeFirst(path, "/o");

		String applicationBase = StringUtil.replaceLast(trimmedPath, '/', "");

		String applicationFilter = _createApplicationFilter(applicationBase);

		Collection<ServiceReference<Application>> serviceReferences =
			_bundleContext.getServiceReferences(
				Application.class, applicationFilter);

		if (CollectionUtils.isEmpty(serviceReferences)) {
			return Collections.emptyList();
		}

		return _retrieveResourcesFromApplications(serviceReferences);
	}

	private List<EntityModelResource> _getResourcesForApplication(
			String applicationName)
		throws Exception {

		String resourceFilter = StringBundler.concat(
			"(&(osgi.jaxrs.resource=true)(osgi.jaxrs.application.select=\\",
			"(osgi.jaxrs.name=", applicationName, "\\)))");

		ServiceReference<?>[] serviceReferences =
			_bundleContext.getServiceReferences((String)null, resourceFilter);

		List<EntityModelResource> resources = new ArrayList<>();

		for (ServiceReference<?> serviceReference : serviceReferences) {
			Object service = _bundleContext.getService(serviceReference);

			if (!(service instanceof EntityModelResource)) {
				continue;
			}

			resources.add((EntityModelResource)service);
		}

		return resources;
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

	private List<EntityModelResource> _retrieveResourcesFromApplications(
			Collection<ServiceReference<Application>> serviceReferences)
		throws Exception {

		List<EntityModelResource> resources = new ArrayList<>();

		for (ServiceReference<Application> applicationServiceReference :
				serviceReferences) {

			String applicationName =
				(String)applicationServiceReference.getProperty(
					"osgi.jaxrs.name");

			if (applicationName.isEmpty()) {
				continue;
			}

			List<EntityModelResource> applicationResources =
				_getResourcesForApplication(applicationName);

			resources.addAll(applicationResources);
		}

		return resources;
	}

	private BundleContext _bundleContext;

}