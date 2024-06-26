/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.resource.v1_0.test;

import com.liferay.headless.admin.site.client.http.HttpInvoker;
import com.liferay.headless.admin.site.client.pagination.Page;
import com.liferay.headless.admin.site.client.resource.v1_0.WidgetInstanceResource;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import java.lang.reflect.Method;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Generated;

import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Rubén Pulido
 * @generated
 */
@Generated("")
public abstract class BaseWidgetInstanceResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_widgetInstanceResource.setContextCompany(testCompany);

		WidgetInstanceResource.Builder builder =
			WidgetInstanceResource.builder();

		widgetInstanceResource = builder.authentication(
			"test@liferay.com", PropsValues.DEFAULT_ADMIN_PASSWORD
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage()
		throws Exception {

		String siteExternalReferenceCode =
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getSiteExternalReferenceCode();
		String irrelevantSiteExternalReferenceCode =
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getIrrelevantSiteExternalReferenceCode();
		String widgetInstanceExternalReferenceCode =
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getWidgetInstanceExternalReferenceCode();
		String irrelevantWidgetInstanceExternalReferenceCode =
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getIrrelevantWidgetInstanceExternalReferenceCode();

		Page<WidgetInstance> page =
			widgetInstanceResource.
				getSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage(
					siteExternalReferenceCode,
					widgetInstanceExternalReferenceCode);

		long totalCount = page.getTotalCount();

		if ((irrelevantSiteExternalReferenceCode != null) &&
			(irrelevantWidgetInstanceExternalReferenceCode != null)) {

			WidgetInstance irrelevantWidgetInstance =
				testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_addWidgetInstance(
					irrelevantSiteExternalReferenceCode,
					irrelevantWidgetInstanceExternalReferenceCode,
					randomIrrelevantWidgetInstance());

			page =
				widgetInstanceResource.
					getSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage(
						irrelevantSiteExternalReferenceCode,
						irrelevantWidgetInstanceExternalReferenceCode);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantWidgetInstance,
				(List<WidgetInstance>)page.getItems());
			assertValid(
				page,
				testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getExpectedActions(
					irrelevantSiteExternalReferenceCode,
					irrelevantWidgetInstanceExternalReferenceCode));
		}

		WidgetInstance widgetInstance1 =
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_addWidgetInstance(
				siteExternalReferenceCode, widgetInstanceExternalReferenceCode,
				randomWidgetInstance());

		WidgetInstance widgetInstance2 =
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_addWidgetInstance(
				siteExternalReferenceCode, widgetInstanceExternalReferenceCode,
				randomWidgetInstance());

		page =
			widgetInstanceResource.
				getSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage(
					siteExternalReferenceCode,
					widgetInstanceExternalReferenceCode);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(widgetInstance1, (List<WidgetInstance>)page.getItems());
		assertContains(widgetInstance2, (List<WidgetInstance>)page.getItems());
		assertValid(
			page,
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getExpectedActions(
				siteExternalReferenceCode,
				widgetInstanceExternalReferenceCode));
	}

	protected Map<String, Map<String, String>>
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getExpectedActions(
				String siteExternalReferenceCode,
				String widgetInstanceExternalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected WidgetInstance
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_addWidgetInstance(
				String siteExternalReferenceCode,
				String widgetInstanceExternalReferenceCode,
				WidgetInstance widgetInstance)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getSiteExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getIrrelevantSiteExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected String
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getWidgetInstanceExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteSiteExternalReferenceCodeWidgetInstanceWidgetInstanceExternalReferenceCodePage_getIrrelevantWidgetInstanceExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected void assertContains(
		Object widgetInstance, List<Object> widgetInstances) {

		boolean contains = false;

		for (Object item : widgetInstances) {
			if (equals(widgetInstance, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			widgetInstances + " does not contain " + widgetInstance, contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		Object widgetInstance1, Object widgetInstance2) {

		Assert.assertTrue(
			widgetInstance1 + " does not equal " + widgetInstance2,
			equals(widgetInstance1, widgetInstance2));
	}

	protected void assertEquals(
		List<Object> widgetInstances1, List<Object> widgetInstances2) {

		Assert.assertEquals(widgetInstances1.size(), widgetInstances2.size());

		for (int i = 0; i < widgetInstances1.size(); i++) {
			Object widgetInstance1 = widgetInstances1.get(i);
			Object widgetInstance2 = widgetInstances2.get(i);

			assertEquals(widgetInstance1, widgetInstance2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<Object> widgetInstances1, List<Object> widgetInstances2) {

		Assert.assertEquals(widgetInstances1.size(), widgetInstances2.size());

		for (Object widgetInstance1 : widgetInstances1) {
			boolean contains = false;

			for (Object widgetInstance2 : widgetInstances2) {
				if (equals(widgetInstance1, widgetInstance2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				widgetInstances2 + " does not contain " + widgetInstance1,
				contains);
		}
	}

	protected void assertValid(Object widgetInstance) throws Exception {
		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<Object> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<Object> page, Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<Object> widgetInstances = page.getItems();

		int size = widgetInstances.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);

		assertValid(page.getActions(), expectedActions);
	}

	protected void assertValid(
		Map<String, Map<String, String>> actions1,
		Map<String, Map<String, String>> actions2) {

		for (String key : actions2.keySet()) {
			Map action = actions1.get(key);

			Assert.assertNotNull(key + " does not contain an action", action);

			Map<String, String> expectedAction = actions2.get(key);

			Assert.assertEquals(
				expectedAction.get("method"), action.get("method"));
			Assert.assertEquals(expectedAction.get("href"), action.get("href"));
		}
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(Object widgetInstance1, Object widgetInstance2) {
		if (widgetInstance1 == widgetInstance2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		if (clazz.getClassLoader() == null) {
			return new java.lang.reflect.Field[0];
		}

		return TransformUtil.transform(
			ReflectionUtil.getDeclaredFields(clazz),
			field -> {
				if (field.isSynthetic()) {
					return null;
				}

				return field;
			},
			java.lang.reflect.Field.class);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_widgetInstanceResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_widgetInstanceResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		if (entityModel == null) {
			return Collections.emptyList();
		}

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		return TransformUtil.transform(
			getEntityFields(),
			entityField -> {
				if (!Objects.equals(entityField.getType(), type) ||
					ArrayUtil.contains(
						getIgnoredEntityFieldNames(), entityField.getName())) {

					return null;
				}

				return entityField;
			});
	}

	protected String getFilterString(
		EntityField entityField, String operator, Object widgetInstance) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path("http://localhost:8080/o/graphql");
		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected WidgetInstanceResource widgetInstanceResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = _getSuperClass(source.getClass());

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					sourceClass.getDeclaredFields()) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				Method setMethod = _getMethod(
					targetClass, field.getName(), "set",
					getMethod.getReturnType());

				setMethod.invoke(target, getMethod.invoke(source));
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Class<?> _getSuperClass(Class<?> clazz) {
			Class<?> superClass = clazz.getSuperclass();

			if ((superClass == null) || (superClass == Object.class)) {
				return clazz;
			}

			return superClass;
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseWidgetInstanceResourceTestCase.class);

	private static DateFormat _dateFormat;

	@Inject
	private com.liferay.headless.admin.site.resource.v1_0.WidgetInstanceResource
		_widgetInstanceResource;

}