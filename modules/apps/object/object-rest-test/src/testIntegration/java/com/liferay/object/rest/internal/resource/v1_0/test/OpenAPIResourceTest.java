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

package com.liferay.object.rest.internal.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.rest.internal.resource.v1_0.test.util.HTTPTestUtil;
import com.liferay.object.rest.internal.resource.v1_0.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.rest.internal.resource.v1_0.test.util.ObjectRelationshipTestUtil;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsUtil;

import java.util.Collections;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Carlos Correa
 */
@RunWith(Arquillian.class)
public class OpenAPIResourceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_company = CompanyTestUtil.addCompany();
	}

	@Before
	public void setUp() throws Exception {
		_objectDefinition1 = ObjectDefinitionTestUtil.publishObjectDefinition(
			Collections.singletonList(
				ObjectFieldUtil.createObjectField(
					"Text", "String", true, true, null,
					RandomTestUtil.randomString(), _OBJECT_FIELD_NAME, false)));
	}

	@Test
	public void testGetNestedEntitiesInObjectRelationship() throws Exception {
		PropsUtil.addProperties(
			UnicodePropertiesBuilder.setProperty(
				"feature.flag.LPS-168462", "true"
			).build());

		_testGetNestedEntityInManyToManyObjectRelationship();

		_testGetNestedEntityInOneToManyObjectRelationship();

		PropsUtil.addProperties(
			UnicodePropertiesBuilder.setProperty(
				"feature.flag.LPS-168462", "false"
			).build());
	}

	@Test
	public void testGetOpenAPI() throws Exception {
		_user = UserTestUtil.addUser(_company);

		_objectDefinition2 = ObjectDefinitionTestUtil.publishObjectDefinition(
			Collections.singletonList(
				ObjectFieldUtil.createObjectField(
					"Text", "String", true, true, null,
					RandomTestUtil.randomString(), _OBJECT_FIELD_NAME, false)),
			_user.getUserId());

		JSONObject jsonObject = HTTPTestUtil.invoke(
			null, "/openapi", Http.Method.GET);

		JSONArray jsonArray = jsonObject.getJSONArray(
			_objectDefinition1.getRESTContextPath());

		Assert.assertEquals(1, jsonArray.length());
		Assert.assertEquals(
			"http://localhost:8080/o" +
				_objectDefinition1.getRESTContextPath() + "/openapi.yaml",
			jsonArray.get(0));

		jsonObject = HTTPTestUtil.invoke(
			null, _objectDefinition1.getRESTContextPath() + "/openapi.json",
			Http.Method.GET);

		Assert.assertNotNull(jsonObject.getString("openapi"));
		Assert.assertNull(
			jsonObject.getJSONArray(_objectDefinition2.getRESTContextPath()));

		jsonObject = HTTPTestUtil.invoke(
			null, _objectDefinition2.getRESTContextPath() + "/openapi.json",
			Http.Method.GET);

		Assert.assertEquals("NOT_FOUND", jsonObject.getString("status"));
	}

	private String _getNestedEntitySchema(
		JSONObject jsonObject, ObjectRelationship objectRelationship,
		ObjectDefinition objectDefinition) {

		String nestedEntitySchema;

		if (Objects.equals(
				objectRelationship.getType(),
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY) &&
			(objectDefinition.getObjectDefinitionId() ==
				_objectDefinition2.getObjectDefinitionId())) {

			nestedEntitySchema = (String)jsonObject.getJSONObject(
				"components"
			).getJSONObject(
				"schemas"
			).getJSONObject(
				objectDefinition.getShortName()
			).getJSONObject(
				"properties"
			).getJSONObject(
				objectRelationship.getName()
			).get(
				"$ref"
			);
		}
		else {
			nestedEntitySchema = (String)jsonObject.getJSONObject(
				"components"
			).getJSONObject(
				"schemas"
			).getJSONObject(
				objectDefinition.getShortName()
			).getJSONObject(
				"properties"
			).getJSONObject(
				objectRelationship.getName()
			).getJSONObject(
				"items"
			).get(
				"$ref"
			);
		}

		String[] nestedEntitySchemaSplit = nestedEntitySchema.split("/");

		return nestedEntitySchemaSplit[nestedEntitySchemaSplit.length - 1];
	}

	private void _testGetNestedEntityInManyToManyObjectRelationship()
		throws Exception {

		_objectDefinition2 = ObjectDefinitionTestUtil.publishObjectDefinition(
			Collections.singletonList(
				ObjectFieldUtil.createObjectField(
					"Text", "String", true, true, null,
					RandomTestUtil.randomString(), _OBJECT_FIELD_NAME, false)));

		ObjectRelationship objectRelationship =
			ObjectRelationshipTestUtil.addObjectRelationship(
				_objectDefinition1, _objectDefinition2,
				TestPropsValues.getUserId(),
				ObjectRelationshipConstants.TYPE_MANY_TO_MANY);

		JSONObject jsonObject = HTTPTestUtil.invoke(
			null, "/openapi", Http.Method.GET);

		JSONArray jsonArray = jsonObject.getJSONArray(
			_objectDefinition1.getRESTContextPath());

		Assert.assertEquals(1, jsonArray.length());
		Assert.assertEquals(
			"http://localhost:8080/o" +
				_objectDefinition1.getRESTContextPath() + "/openapi.yaml",
			jsonArray.get(0));

		jsonObject = HTTPTestUtil.invoke(
			null, _objectDefinition1.getRESTContextPath() + "/openapi.json",
			Http.Method.GET);

		Assert.assertNotNull(jsonObject.getString("openapi"));

		Assert.assertEquals(
			_getNestedEntitySchema(
				jsonObject, objectRelationship, _objectDefinition1),
			_objectDefinition2.getShortName());

		Assert.assertNull(
			jsonObject.getJSONArray(_objectDefinition2.getRESTContextPath()));

		jsonObject = HTTPTestUtil.invoke(
			null, _objectDefinition2.getRESTContextPath() + "/openapi.json",
			Http.Method.GET);

		Assert.assertNotNull(jsonObject.getString("openapi"));

		Assert.assertEquals(
			_getNestedEntitySchema(
				jsonObject, objectRelationship, _objectDefinition2),
			_objectDefinition1.getShortName());
	}

	private void _testGetNestedEntityInOneToManyObjectRelationship()
		throws Exception {

		_objectDefinition2 = ObjectDefinitionTestUtil.publishObjectDefinition(
			Collections.singletonList(
				ObjectFieldUtil.createObjectField(
					"Text", "String", true, true, null,
					RandomTestUtil.randomString(), _OBJECT_FIELD_NAME, false)));

		ObjectRelationship objectRelationship =
			ObjectRelationshipTestUtil.addObjectRelationship(
				_objectDefinition1, _objectDefinition2,
				TestPropsValues.getUserId(),
				ObjectRelationshipConstants.TYPE_ONE_TO_MANY);

		JSONObject jsonObject = HTTPTestUtil.invoke(
			null, "/openapi", Http.Method.GET);

		JSONArray jsonArray = jsonObject.getJSONArray(
			_objectDefinition1.getRESTContextPath());

		Assert.assertEquals(1, jsonArray.length());
		Assert.assertEquals(
			"http://localhost:8080/o" +
				_objectDefinition1.getRESTContextPath() + "/openapi.yaml",
			jsonArray.get(0));

		jsonObject = HTTPTestUtil.invoke(
			null, _objectDefinition1.getRESTContextPath() + "/openapi.json",
			Http.Method.GET);

		Assert.assertNotNull(jsonObject.getString("openapi"));

		Assert.assertEquals(
			_getNestedEntitySchema(
				jsonObject, objectRelationship, _objectDefinition1),
			_objectDefinition2.getShortName());

		Assert.assertNull(
			jsonObject.getJSONArray(_objectDefinition2.getRESTContextPath()));

		jsonObject = HTTPTestUtil.invoke(
			null, _objectDefinition2.getRESTContextPath() + "/openapi.json",
			Http.Method.GET);

		Assert.assertNotNull(jsonObject.getString("openapi"));

		Assert.assertEquals(
			_getNestedEntitySchema(
				jsonObject, objectRelationship, _objectDefinition2),
			_objectDefinition1.getShortName());
	}

	private static final String _OBJECT_FIELD_NAME =
		"x" + RandomTestUtil.randomString();

	private static Company _company;

	@DeleteAfterTestRun
	private ObjectDefinition _objectDefinition1;

	@DeleteAfterTestRun
	private ObjectDefinition _objectDefinition2;

	@DeleteAfterTestRun
	private User _user;

}