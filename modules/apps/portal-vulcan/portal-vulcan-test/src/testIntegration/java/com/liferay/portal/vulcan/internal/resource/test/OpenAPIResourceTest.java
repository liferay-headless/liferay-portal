/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.resource.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.PropsValuesTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.internal.test.util.URLConnectionUtil;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import java.io.InputStream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author Carlos Correa
 */
@RunWith(Arquillian.class)
public class OpenAPIResourceTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testGetOpenAPIFilterableFields() throws Exception {
		Map<String, EntityField> entityFieldsMap =
			_getStructuredContentEntityFieldsMap();

		JSONObject jsonObject = HTTPTestUtil.invokeToJSONObject(
			StringPool.BLANK, "/headless-delivery/v1.0/openapi.json",
			Http.Method.GET);

		Set<String> expectedFilterableFields = entityFieldsMap.keySet();

		expectedFilterableFields.removeAll(
			Arrays.asList("creatorId", "assetLibraryId"));

		Set<String> actualFilterableFields = _getFilterableFieldsFromOpenAPI(
			jsonObject);

		Assert.assertEquals(
			"Mismatch between entity model filterable fields and OpenAPI " +
				"x-filterable fields",
			expectedFilterableFields, actualFilterableFields);
	}

	@Test
	public void testGetOpenAPIServerURL() throws Exception {
		InputStream inputStream = URLConnectionUtil.getInputStream(
			"http://localhost:8080/o/headless-delivery/v1.0/openapi.json");

		String path = _getPath(inputStream, "/servers/0/url");

		Assert.assertTrue(path.startsWith("http://localhost:8080/"));

		try (SafeCloseable safeCloseable =
				PropsValuesTestUtil.swapWithSafeCloseable(
					"WEB_SERVER_PROTOCOL", "https")) {

			inputStream = URLConnectionUtil.getInputStream(
				"http://localhost:8080/o/headless-delivery/v1.0/openapi.json");

			path = _getPath(inputStream, "/servers/0/url");

			Assert.assertTrue(path.startsWith("https://localhost:8080/"));
		}
	}

	private Set<String> _getFilterableFieldsFromOpenAPI(
		JSONObject openAPIJSONObject) {

		Set<String> filterableFields = new HashSet<>();

		JSONObject propertiesJSONObject = openAPIJSONObject.getJSONObject(
			"components"
		).getJSONObject(
			"schemas"
		).getJSONObject(
			"StructuredContent"
		).getJSONObject(
			"properties"
		);

		for (String propertyName : propertiesJSONObject.keySet()) {
			JSONObject propertyJSONObject = propertiesJSONObject.getJSONObject(
				propertyName);

			if ((propertyJSONObject != null) &&
				propertyJSONObject.has("x-filterable")) {

				boolean filterable = propertyJSONObject.getBoolean(
					"x-filterable");

				if (filterable) {
					filterableFields.add(propertyName);
				}
			}
		}

		return filterableFields;
	}

	private String _getPath(InputStream inputStream, String path)
		throws Exception {

		JsonNode jsonNode = _objectMapper.readTree(inputStream);

		jsonNode = jsonNode.at(path);

		return jsonNode.asText();
	}

	private Map<String, EntityField> _getStructuredContentEntityFieldsMap()
		throws Exception {

		Bundle bundle = FrameworkUtil.getBundle(OpenAPIResourceTest.class);

		BundleContext bundleContext = bundle.getBundleContext();

		String applicationName = "Liferay.Headless.Delivery";

		String resourceFilter = StringBundler.concat(
			"(&(entity.class.name=com.liferay.headless.delivery.dto.v1_0.",
			"StructuredContent)",
			"(osgi.jaxrs.application.select=\\(osgi.jaxrs.name=",
			applicationName, "\\)))");

		ServiceReference<?>[] resourceServiceReferences =
			bundleContext.getServiceReferences((String)null, resourceFilter);

		ServiceReference<?> serviceReference = resourceServiceReferences[0];

		EntityModelResource structuredContentResource =
			(EntityModelResource)bundleContext.getService(serviceReference);

		long companyId = TestPropsValues.getCompanyId();

		MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();

		params.putSingle("companyId", String.valueOf(companyId));

		EntityModel entityModel = structuredContentResource.getEntityModel(
			params);

		return entityModel.getEntityFieldsMap();
	}

	private final ObjectMapper _objectMapper = new ObjectMapper();

}