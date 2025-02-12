/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.headless.batch.engine.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.headless.batch.engine.client.dto.v1_0.ImportTask;
import com.liferay.headless.batch.engine.client.resource.v1_0.ImportTaskResource;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.rest.test.util.ObjectEntryTestUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.util.PropsValues;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Mauricio Valdivia
 */
@RunWith(Arquillian.class)
public class ImportTaskResourceTest extends BaseTaskResourceTestCase {

	@Test
	public void testDeleteImportTask() throws Exception {
		ObjectEntry objectEntry = ObjectEntryTestUtil.addObjectEntry(
			objectDefinition, OBJECT_FIELD_NAME_TEXT, "TestObject");

		int objectEntriesCount = ObjectEntryTestUtil.getObjectEntriesCount();

		ImportTaskResource importTaskResource = ImportTaskResource.builder(
		).authentication(
			"test@liferay.com", PropsValues.DEFAULT_ADMIN_PASSWORD
		).header(
			HttpHeaders.ACCEPT, ContentTypes.APPLICATION_JSON
		).header(
			HttpHeaders.CONTENT_TYPE, ContentTypes.APPLICATION_JSON
		).parameter(
			"siteId", String.valueOf(TestPropsValues.getGroupId())
		).build();

		String payload = JSONUtil.putAll(
			JSONUtil.put(
				"externalReferenceCode", objectEntry.getExternalReferenceCode())
		).toString();

		_executeDeleteImportTask(importTaskResource, payload);

		Assert.assertEquals(
			ObjectEntryTestUtil.getObjectEntriesCount(),
			objectEntriesCount - 1);
	}

	@Test
	public void testPostImportTask() throws Exception {

		// With "batchRestrictFields" query parameter

		Role role = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		ObjectEntry objectEntry = ObjectEntryTestUtil.addObjectEntry(
			objectDefinition, OBJECT_FIELD_NAME_TEXT, "TestObject");

		JSONObject beforeImportJSONObject = _getJSONObject(
			objectEntry.getExternalReferenceCode());

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject.put(
						"permissions",
						beforeImportJSONObject.getJSONArray(
							"permissions"
						).put(
							JSONUtil.put(
								"actionIds", JSONUtil.putAll("VIEW")
							).put(
								"roleName", role.getName()
							)
						))
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?batchRestrictFields=permissions,", OBJECT_FIELD_NAME_TEXT,
					"&createStrategy=UPSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.put(
				"permissions",
				JSONUtil.putAll(
					JSONUtil.put(
						"actionIds",
						JSONUtil.putAll(
							"DELETE", "PERMISSIONS", "UPDATE", "VIEW")
					).put(
						"roleName", "Owner"
					))
			).toString(),
			_getJSONObject(
				objectEntry.getExternalReferenceCode()
			).toString(),
			JSONCompareMode.LENIENT);

		// With "permissions" and "createStrategy" INSERT

		beforeImportJSONObject = JSONUtil.put(
			OBJECT_FIELD_NAME_TEXT, RandomTestUtil.randomString()
		).put(
			"externalReferenceCode", RandomTestUtil.randomString()
		).put(
			"permissions",
			JSONUtil.putAll(
				JSONUtil.put(
					"actionIds", JSONUtil.putAll("VIEW")
				).put(
					"roleName", role.getName()
				))
		);

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?createStrategy=INSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.merge(
				beforeImportJSONObject,
				JSONUtil.put(
					"permissions",
					JSONUtil.putAll(
						JSONUtil.put(
							"actionIds", JSONUtil.putAll("VIEW")
						).put(
							"roleName", role.getName()
						)))
			).toString(),
			_getJSONObject(
				beforeImportJSONObject.getString("externalReferenceCode")
			).toString(),
			JSONCompareMode.LENIENT);

		// With "permissions" and "createStrategy" UPSERT

		beforeImportJSONObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				OBJECT_FIELD_NAME_TEXT, RandomTestUtil.randomString()
			).put(
				"externalReferenceCode", RandomTestUtil.randomString()
			).toString(),
			StringBundler.concat(
				objectDefinition.getRESTContextPath(),
				"?nestedFields=permissions",
				"&restrictFields=dateCreated,dateModified"),
			Http.Method.POST);

		beforeImportJSONObject = beforeImportJSONObject.put(
			"permissions",
			beforeImportJSONObject.getJSONArray(
				"permissions"
			).put(
				JSONUtil.put(
					"actionIds", JSONUtil.putAll("VIEW")
				).put(
					"roleName", role.getName()
				)
			));

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?createStrategy=UPSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.merge(
				beforeImportJSONObject,
				JSONUtil.put(
					"permissions",
					JSONUtil.putAll(
						JSONUtil.put(
							"actionIds",
							JSONUtil.putAll(
								"DELETE", "PERMISSIONS", "UPDATE", "VIEW")
						).put(
							"roleName", "Owner"
						),
						JSONUtil.put(
							"actionIds", JSONUtil.putAll("VIEW")
						).put(
							"roleName", role.getName()
						)))
			).toString(),
			_getJSONObject(
				beforeImportJSONObject.getString("externalReferenceCode")
			).toString(),
			JSONCompareMode.LENIENT);

		// With empty "permissions" and "createStrategy" INSERT

		beforeImportJSONObject = JSONUtil.put(
			OBJECT_FIELD_NAME_TEXT, RandomTestUtil.randomString()
		).put(
			"externalReferenceCode", RandomTestUtil.randomString()
		).put(
			"permissions", JSONFactoryUtil.createJSONArray()
		);

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?createStrategy=INSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.merge(
				beforeImportJSONObject,
				JSONUtil.put("permissions", JSONFactoryUtil.createJSONArray())
			).toString(),
			_getJSONObject(
				beforeImportJSONObject.getString("externalReferenceCode")
			).toString(),
			JSONCompareMode.LENIENT);

		// With empty "permissions" and "createStrategy" UPSERT

		beforeImportJSONObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				OBJECT_FIELD_NAME_TEXT, RandomTestUtil.randomString()
			).put(
				"externalReferenceCode", RandomTestUtil.randomString()
			).toString(),
			StringBundler.concat(
				objectDefinition.getRESTContextPath(),
				"?nestedFields=permissions",
				"&restrictFields=dateCreated,dateModified"),
			Http.Method.POST
		).put(
			"permissions", JSONFactoryUtil.createJSONArray()
		);

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?createStrategy=UPSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.merge(
				beforeImportJSONObject,
				JSONUtil.put("permissions", JSONFactoryUtil.createJSONArray())
			).toString(),
			_getJSONObject(
				beforeImportJSONObject.getString("externalReferenceCode")
			).toString(),
			JSONCompareMode.LENIENT);

		// With no "permissions" and "createStrategy" INSERT

		beforeImportJSONObject = JSONUtil.put(
			OBJECT_FIELD_NAME_TEXT, RandomTestUtil.randomString()
		).put(
			"externalReferenceCode", RandomTestUtil.randomString()
		);

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?createStrategy=INSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.merge(
				beforeImportJSONObject,
				JSONUtil.put(
					"permissions",
					JSONUtil.putAll(
						JSONUtil.put(
							"actionIds",
							JSONUtil.putAll(
								"DELETE", "PERMISSIONS", "UPDATE", "VIEW")
						).put(
							"roleName", "Owner"
						)))
			).toString(),
			_getJSONObject(
				beforeImportJSONObject.getString("externalReferenceCode")
			).toString(),
			JSONCompareMode.LENIENT);

		// With no "permissions" and "createStrategy" UPSERT

		beforeImportJSONObject = HTTPTestUtil.invokeToJSONObject(
			JSONUtil.put(
				OBJECT_FIELD_NAME_TEXT, RandomTestUtil.randomString()
			).put(
				"externalReferenceCode", RandomTestUtil.randomString()
			).toString(),
			StringBundler.concat(
				objectDefinition.getRESTContextPath(),
				"?nestedFields=permissions",
				"&restrictFields=dateCreated,dateModified"),
			Http.Method.POST
		).put(
			"permissions", (JSONObject)null
		);

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?createStrategy=UPSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.merge(
				beforeImportJSONObject,
				JSONUtil.put(
					"permissions",
					JSONUtil.putAll(
						JSONUtil.put(
							"actionIds",
							JSONUtil.putAll(
								"DELETE", "PERMISSIONS", "UPDATE", "VIEW")
						).put(
							"roleName", "Owner"
						)))
			).toString(),
			_getJSONObject(
				beforeImportJSONObject.getString("externalReferenceCode")
			).toString(),
			JSONCompareMode.LENIENT);

		// Without "batchRestrictFields" query parameter

		objectEntry = ObjectEntryTestUtil.addObjectEntry(
			objectDefinition, OBJECT_FIELD_NAME_TEXT, "TestObject");

		beforeImportJSONObject = _getJSONObject(
			objectEntry.getExternalReferenceCode());

		waitForFinish(
			"COMPLETED", true,
			HTTPTestUtil.invokeToJSONObject(
				JSONUtil.putAll(
					beforeImportJSONObject.put(
						"permissions",
						beforeImportJSONObject.getJSONArray(
							"permissions"
						).put(
							JSONUtil.put(
								"actionIds", JSONUtil.putAll("VIEW")
							).put(
								"roleName", role.getName()
							)
						))
				).toString(),
				StringBundler.concat(
					"headless-batch-engine/v1.0/import-task",
					"/com.liferay.object.rest.dto.v1_0.ObjectEntry",
					"?createStrategy=UPSERT&taskItemDelegateName=",
					objectDefinition.getName()),
				Http.Method.POST));

		JSONAssert.assertEquals(
			JSONUtil.put(
				"permissions",
				JSONUtil.putAll(
					JSONUtil.put(
						"actionIds",
						JSONUtil.putAll(
							"DELETE", "PERMISSIONS", "UPDATE", "VIEW")
					).put(
						"roleName", "Owner"
					),
					JSONUtil.put(
						"actionIds", JSONUtil.putAll("VIEW")
					).put(
						"roleName", role.getName()
					))
			).toString(),
			_getJSONObject(
				objectEntry.getExternalReferenceCode()
			).toString(),
			JSONCompareMode.LENIENT);
	}

	private void _executeDeleteImportTask(
			ImportTaskResource importTaskResource, String payload)
		throws Exception {

		ImportTask importTask = importTaskResource.deleteImportTask(
			"com.liferay.object.rest.dto.v1_0.ObjectEntry", null, null, null,
			objectDefinition.getName(), payload);

		while (true) {
			importTask =
				importTaskResource.getImportTaskByExternalReferenceCode(
					importTask.getExternalReferenceCode());

			if (Objects.equals(
					importTask.getExecuteStatusAsString(), "COMPLETED")) {

				break;
			}
			else if (Objects.equals(
						importTask.getExecuteStatusAsString(), "FAILED")) {

				throw new AssertionError(importTask.getErrorMessage());
			}
		}
	}

	private JSONObject _getJSONObject(String externalReferenceCode)
		throws Exception {

		return HTTPTestUtil.invokeToJSONObject(
			null,
			StringBundler.concat(
				objectDefinition.getRESTContextPath(),
				"/by-external-reference-code/", externalReferenceCode,
				"?nestedFields=permissions"),
			Http.Method.GET);
	}

}