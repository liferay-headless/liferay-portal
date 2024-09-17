/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.openapi.contributor;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.openapi.OpenAPIContext;
import com.liferay.portal.vulcan.openapi.contributor.OpenAPIContributor;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Magdalena Jedraszak
 */
@Component(service = OpenAPIContributor.class)
public class ExtensionsOpenAPIContributor implements OpenAPIContributor {

	@Override
	public void contribute(OpenAPI openAPI, OpenAPIContext openAPIContext)
		throws Exception {

		Map<String, Schema> schemas = openAPI.getComponents(
		).getSchemas();

		if (schemas.isEmpty()) {
			return;
		}

		Map<String, EntityField> entityFieldsMap = getEntityModelFromJaxrs(
			openAPIContext.getPath());

		if ((entityFieldsMap == null) || entityFieldsMap.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Schema> schemaEntry : schemas.entrySet()) {
			Schema schema = schemaEntry.getValue();

			if (schema.getProperties() != null) {
				_processSchemaProperties(schema, entityFieldsMap);
			}
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext)
		throws InvalidSyntaxException {

		_bundleContext = bundleContext;

		_serviceTrackerMap = ServiceTrackerMapFactory.openMultiValueMap(
			bundleContext, null, "osgi.jaxrs.application.select");
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	protected Map<String, EntityField> getEntityModelFromJaxrs(String path)
		throws Exception {

		String applicationName = _findApplicationName(path);

		if (applicationName != null) {
			String applicationSelectFilter =
				"(osgi.jaxrs.name=" + applicationName + ")";

			Object resourcesObject = _serviceTrackerMap.getService(
				applicationSelectFilter);

			if (resourcesObject instanceof List<?>) {
				List<?> resources = (List<?>)resourcesObject;

				for (Object resource : resources) {
					if (resource instanceof EntityModelResource) {
						EntityModelResource entityModelResource =
							(EntityModelResource)resource;

						EntityModel entityModel =
							entityModelResource.getEntityModel(
								new MultivaluedHashMap<>());

						if (entityModel != null) {
							return entityModel.getEntityFieldsMap();
						}
					}
				}
			}
		}

		return null;
	}

	private String _findApplicationName(String path) {
		try {
			String applicationBase = StringUtil.removeFirst(path, "/o");

			applicationBase = StringUtil.replaceLast(applicationBase, '/', "");

			String filter =
				"(osgi.jaxrs.application.base=" + applicationBase + ")";

			Collection<ServiceReference<Application>> serviceReferences =
				_bundleContext.getServiceReferences(Application.class, filter);

			if ((serviceReferences != null) && !serviceReferences.isEmpty()) {
				for (ServiceReference<Application> serviceReference :
						serviceReferences) {

					return (String)serviceReference.getProperty(
						"osgi.jaxrs.name");
				}
			}
		}
		catch (InvalidSyntaxException invalidSyntaxException) {
			if (_log.isDebugEnabled()) {
				_log.debug(invalidSyntaxException);
			}

			return null;
		}

		return null;
	}

	private void _processSchemaProperties(
		Schema schema, Map<String, EntityField> entityFieldsMap) {

		Map<?, ?> properties = schema.getProperties();

		for (Map.Entry<?, ?> propertyEntry : properties.entrySet()) {
			String propertyName = (String)propertyEntry.getKey();

			if (entityFieldsMap.containsKey(propertyName)) {
				Schema propertySchema = (Schema)propertyEntry.getValue();

				_updateSchemaExtensions(propertySchema);
			}
		}
	}

	private void _updateSchemaExtensions(Schema propertySchema) {
		Map<String, Object> extensions = propertySchema.getExtensions();

		if (extensions == null) {
			extensions = new HashMap<>();

			propertySchema.setExtensions(extensions);
		}

		extensions.put("x-filterable", true);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ExtensionsOpenAPIContributor.class);

	private BundleContext _bundleContext;
	private ServiceTrackerMap<String, ?> _serviceTrackerMap;

}