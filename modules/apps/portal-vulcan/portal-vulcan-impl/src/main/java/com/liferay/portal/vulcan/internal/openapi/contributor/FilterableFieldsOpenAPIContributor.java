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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedHashMap;

import org.apache.cxf.common.util.CollectionUtils;
import org.apache.cxf.common.util.StringUtils;

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

		for (Schema schema : schemas.values()) {
			Map<String, EntityField> entityFieldsMap = _fetchEntityFieldsMap(
				openAPIContext, schema);

			if (MapUtil.isEmpty(entityFieldsMap)) {
				continue;
			}

			_processSchemaProperties(schema, entityFieldsMap);
		}
	}

	@Activate
	protected void activate(BundleContext bundleContext)
		throws InvalidSyntaxException {

		_bundleContext = bundleContext;
	}

	private Map<String, EntityField> _buildEntityFieldsMap(
			List<EntityModelResource> resources, OpenAPIContext openAPIContext)
		throws Exception {

		Map<String, EntityField> entityFieldsMap = new HashMap<>();
		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();

		for (EntityModelResource resource : resources) {
			params.putSingle(
				"companyId", String.valueOf(_getCompanyId(openAPIContext)));

			EntityModel entityModel = resource.getEntityModel(params);

			if (entityModel == null) {
				continue;
			}

			Map<String, EntityField> currentEntityFieldsMap =
				entityModel.getEntityFieldsMap();

			if (currentEntityFieldsMap != null) {
				entityFieldsMap.putAll(currentEntityFieldsMap);
			}
		}

		return entityFieldsMap;
	}

	private String _createResourceFilter(
		String applicationName, String xClassNameDefault,
		OpenAPIContext openAPIContext) {

		String resourceFilter = StringBundler.concat(
			"(&(osgi.jaxrs.resource=true)(osgi.jaxrs.application.select=\\",
			"(osgi.jaxrs.name=", applicationName, "\\))(entity.class.name=",
			xClassNameDefault, ")");

		String apiVersion = openAPIContext.getVersion();

		if (apiVersion != null) {
			resourceFilter = StringBundler.concat(
				resourceFilter, "(api.version=", apiVersion, "))");
		}
		else {
			resourceFilter += ")";
		}

		return resourceFilter;
	}

	private Map<String, EntityField> _fetchEntityFieldsMap(
			OpenAPIContext openAPIContext, Schema schema)
		throws Exception {

		Schema xClassNameSchema = (Schema)schema.getProperties(
		).get(
			"x-class-name"
		);

		if (xClassNameSchema == null) {
			return Collections.emptyMap();
		}

		String xClassNameDefault = (String)xClassNameSchema.getDefault();

		if ((xClassNameDefault == null) || xClassNameDefault.isEmpty()) {
			return Collections.emptyMap();
		}

		Collection<ServiceReference<Application>>
			bundleContextServiceReferences = _getApplicationServiceReferences(
				openAPIContext);

		if (CollectionUtils.isEmpty(bundleContextServiceReferences)) {
			return Collections.emptyMap();
		}

		List<EntityModelResource> resources = _getEntityModelResources(
			bundleContextServiceReferences, xClassNameDefault, openAPIContext);

		if (resources.isEmpty()) {
			return Collections.emptyMap();
		}

		return _buildEntityFieldsMap(resources, openAPIContext);
	}

	private Collection<ServiceReference<Application>>
			_getApplicationServiceReferences(OpenAPIContext openAPIContext)
		throws Exception {

		String trimmedPath = StringUtil.removeFirst(
			openAPIContext.getPath(), "/o");

		String applicationBase = StringUtil.replaceLast(trimmedPath, '/', "");

		String applicationFilter = String.format(
			"(osgi.jaxrs.application.base=%s)", applicationBase);

		return _bundleContext.getServiceReferences(
			Application.class, applicationFilter);
	}

	private long _getCompanyId(OpenAPIContext openAPIContext) throws Exception {
		long companyId = openAPIContext.getCompanyId();

		if (companyId == 0) {
			HttpServletRequest httpServletRequest =
				_getHttpServletRequestFromServiceContext();

			if (httpServletRequest != null) {
				companyId = (long)httpServletRequest.getAttribute(
					WebKeys.COMPANY_ID);
			}
		}

		return companyId;
	}

	private List<EntityModelResource> _getEntityModelResources(
			Collection<ServiceReference<Application>>
				bundleContextServiceReferences,
			String xClassNameDefault, OpenAPIContext openAPIContext)
		throws Exception {

		List<EntityModelResource> resources = new ArrayList<>();

		for (ServiceReference<Application> applicationServiceReference :
				bundleContextServiceReferences) {

			String applicationName =
				(String)applicationServiceReference.getProperty(
					"osgi.jaxrs.name");

			if (StringUtils.isEmpty(applicationName)) {
				continue;
			}

			String resourceFilter = _createResourceFilter(
				applicationName, xClassNameDefault, openAPIContext);

			ServiceReference<?>[] resourceServiceReferences =
				_bundleContext.getServiceReferences(
					(String)null, resourceFilter);

			if (resourceServiceReferences == null) {
				continue;
			}

			for (ServiceReference<?> serviceReference :
					resourceServiceReferences) {

				Object service = _bundleContext.getService(serviceReference);

				if (service instanceof EntityModelResource) {
					resources.add((EntityModelResource)service);
				}
			}
		}

		return resources;
	}

	private HttpServletRequest _getHttpServletRequestFromServiceContext() {
		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		if (serviceContext != null) {
			return serviceContext.getRequest();
		}

		return null;
	}

	private void _processSchemaProperties(
		Schema schema, Map<String, EntityField> entityFieldsMap) {

		List<String> filterableFields = new ArrayList<>(
			entityFieldsMap.keySet());

		if (!filterableFields.isEmpty()) {
			schema.addExtension("x-filterable", filterableFields);
		}
	}

	private BundleContext _bundleContext;

}