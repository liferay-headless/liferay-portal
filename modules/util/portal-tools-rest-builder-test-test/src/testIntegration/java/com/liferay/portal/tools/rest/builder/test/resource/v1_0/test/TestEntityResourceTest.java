/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.tools.rest.builder.test.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.HTTPTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.log.LogCapture;
import com.liferay.portal.test.log.LoggerTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.tools.rest.builder.test.client.dto.v1_0.ChildTestEntity1;
import com.liferay.portal.tools.rest.builder.test.client.dto.v1_0.ChildTestEntity2;
import com.liferay.portal.tools.rest.builder.test.client.dto.v1_0.TestEntity;
import com.liferay.portal.tools.rest.builder.test.client.resource.v1_0.TestEntityResource;
import com.liferay.portal.util.PropsValues;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@RunWith(Arquillian.class)
public class TestEntityResourceTest extends BaseTestEntityResourceTestCase {

	public TestEntityResource createTestEntityResourceWithParameters(
			String[] parameters)
		throws Exception {

		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_testEntityResource.setContextCompany(testCompany);

		User testCompanyAdminUser = UserTestUtil.getAdminUser(
			testCompany.getCompanyId());

		testEntityResource = TestEntityResource.builder(
		).authentication(
			testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(), 8080, "http"
		).locale(
			LocaleUtil.getDefault()
		).parameters(
			parameters
		).build();

		return testEntityResource;
	}

	@Override
	@Test
	public void testGetTestEntity() throws Exception {
		int initialCount = testEntityResource.getTestEntityCount();
		TestEntity testEntity = testEntityResource.postTestEntity(
			randomTestEntity());

		Assert.assertEquals(
			testEntity, testEntityResource.getTestEntity((long)initialCount));
	}

	@Override
	@Test
	public void testGetTestEntityCount() throws Exception {
		int initialCount = testEntityResource.getTestEntityCount();

		testEntityResource.postTestEntity(randomTestEntity());

		Assert.assertEquals(
			Integer.valueOf(initialCount + 1),
			testEntityResource.getTestEntityCount());
	}

	@Override
	@Test
	public void testPatchTestEntity() throws Exception {
		super.testPatchTestEntity();

		ChildTestEntity1 postChildTestEntity1 = new ChildTestEntity1();

		postChildTestEntity1.setProperty1(
			StringUtil.toLowerCase(RandomTestUtil.randomString()));
		postChildTestEntity1.setType(
			TestEntity.Type.create("ChildTestEntity1"));

		postChildTestEntity1 =
			(ChildTestEntity1)testEntityResource.postTestEntity(
				postChildTestEntity1);

		// Patch child test entity 1

		ChildTestEntity1 randomPatchChildTestEntity1 = new ChildTestEntity1();

		randomPatchChildTestEntity1.setProperty1(
			StringUtil.toLowerCase(RandomTestUtil.randomString()));
		randomPatchChildTestEntity1.setType(
			TestEntity.Type.create("ChildTestEntity1"));

		ChildTestEntity1 patchChildTestEntity1 =
			(ChildTestEntity1)testEntityResource.patchTestEntity(
				postChildTestEntity1.getId(),
				testPatchTestEntity_getOptionalParameter(),
				randomPatchChildTestEntity1);

		ChildTestEntity1 expectedPatchChildTestEntity1 =
			postChildTestEntity1.clone();

		BeanTestUtil.copyProperties(
			randomPatchChildTestEntity1, expectedPatchChildTestEntity1);

		ChildTestEntity1 getChildTestEntity1 =
			(ChildTestEntity1)testEntityResource.getTestEntity(
				patchChildTestEntity1.getId());

		assertEquals(expectedPatchChildTestEntity1, getChildTestEntity1);
		assertValid(getChildTestEntity1);

		// Patch child test entity 2

		ChildTestEntity2 randomPatchChildTestEntity2 = new ChildTestEntity2();

		randomPatchChildTestEntity2.setProperty2(
			StringUtil.toLowerCase(RandomTestUtil.randomString()));
		randomPatchChildTestEntity2.setType(
			TestEntity.Type.create("ChildTestEntity2"));

		ChildTestEntity2 patchChildTestEntity2 =
			(ChildTestEntity2)testEntityResource.patchTestEntity(
				postChildTestEntity1.getId(),
				testPatchTestEntity_getOptionalParameter(),
				randomPatchChildTestEntity2);

		ChildTestEntity2 expectedPatchChildTestEntity2 = new ChildTestEntity2();

		BeanTestUtil.copyProperties(
			postChildTestEntity1, expectedPatchChildTestEntity2);

		BeanTestUtil.copyProperties(
			randomPatchChildTestEntity2, expectedPatchChildTestEntity2);

		ChildTestEntity2 getChildTestEntity2 =
			(ChildTestEntity2)testEntityResource.getTestEntity(
				patchChildTestEntity2.getId());

		assertEquals(expectedPatchChildTestEntity2, getChildTestEntity2);
		assertValid(getChildTestEntity2);

		_testPatchTestEntityBatch();
	}

	@Override
	@Test
	public void testPostReservedWord() throws Exception {
		testEntityResource.postReservedWord(true);
	}

	@Override
	@Test
	public void testPostTestEntity() throws Exception {
		super.testPostTestEntity();

		_testPostTestEntityBatch();
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"property1", "property2"};
	}

	@Override
	protected TestEntity testGetTestEntitiesPage_addTestEntity(
			TestEntity testEntity)
		throws Exception {

		return testEntityResource.postTestEntity(testEntity);
	}

	@Override
	protected TestEntity testPatchTestEntity_addTestEntity() throws Exception {
		return testGetTestEntitiesPage_addTestEntity(randomTestEntity());
	}

	@Override
	protected Long testPatchTestEntity_getOptionalParameter() {
		return RandomTestUtil.nextLong();
	}

	@Override
	protected TestEntity testPostTestEntity_addTestEntity(TestEntity testEntity)
		throws Exception {

		return testGetTestEntitiesPage_addTestEntity(testEntity);
	}

	@Override
	protected TestEntity testPutTestEntity_addTestEntity() throws Exception {
		return testGetTestEntitiesPage_addTestEntity(randomTestEntity());
	}

	@Override
	protected Long testPutTestEntity_getOptionalParameter() {
		return RandomTestUtil.nextLong();
	}

	private void _testPatchTestEntityBatch() throws Exception {
		ChildTestEntity1 postChildTestEntity = new ChildTestEntity1();

		ChildTestEntity1 notExistingPostChildTestEntity =
			new ChildTestEntity1();

		postChildTestEntity.setProperty1(
			StringUtil.toLowerCase(RandomTestUtil.randomString()));
		postChildTestEntity.setType(TestEntity.Type.create("ChildTestEntity1"));

		ChildTestEntity1 childTestEntity =
			(ChildTestEntity1)testEntityResource.postTestEntity(
				postChildTestEntity);

		postChildTestEntity.setId(childTestEntity.getId());

		postChildTestEntity.setDescription(StringUtil.randomString());

		notExistingPostChildTestEntity.setProperty1(
			StringUtil.toLowerCase(RandomTestUtil.randomString()));
		notExistingPostChildTestEntity.setType(
			TestEntity.Type.create("ChildTestEntity1"));
		notExistingPostChildTestEntity.setId(childTestEntity.getId() + 1);

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.batch.engine.internal.strategy." +
					"OnErrorContinueBatchEngineImportStrategy",
				LoggerTestUtil.ERROR)) {

			_waitForFinish(
				"COMPLETED", true,
				JSONFactoryUtil.createJSONObject(
					createTestEntityResourceWithParameters(
						new String[] {
							"updateStrategy", "UPDATE", "importStrategy",
							"ON_ERROR_CONTINUE"
						}
					).putTestEntityBatchHttpResponse(
						null, null,
						JSONUtil.putAll(
							JSONFactoryUtil.createJSONObject(
								String.valueOf(postChildTestEntity)),
							JSONFactoryUtil.createJSONObject(
								String.valueOf(notExistingPostChildTestEntity)))
					).getContent()));
		}

		childTestEntity = (ChildTestEntity1)testEntityResource.getTestEntity(
			childTestEntity.getId());

		Assert.assertEquals(
			postChildTestEntity.getId(), childTestEntity.getId());
		Assert.assertEquals(
			postChildTestEntity.getDescription(),
			childTestEntity.getDescription());
	}

	private void _testPostTestEntityBatch() throws Exception {
		ChildTestEntity1 childTestEntity1 = new ChildTestEntity1() {
			{
				dateCreated = RandomTestUtil.nextDate();
				dateModified = RandomTestUtil.nextDate();
				description = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				documentId = RandomTestUtil.randomLong();
				externalReferenceCode = RandomTestUtil.randomString();
				id = RandomTestUtil.randomLong();
				jsonProperty = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				property1 = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				self = StringUtil.toLowerCase(RandomTestUtil.randomString());

				type = Type.create("ChildTestEntity1");
			}
		};

		testPostTestEntity_addTestEntity(childTestEntity1);

		ChildTestEntity2 childTestEntity2 = new ChildTestEntity2() {
			{
				dateCreated = RandomTestUtil.nextDate();
				dateModified = RandomTestUtil.nextDate();
				description = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				documentId = RandomTestUtil.randomLong();
				id = RandomTestUtil.randomLong();
				jsonProperty = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				property2 = StringUtil.toLowerCase(
					RandomTestUtil.randomString());
				self = StringUtil.toLowerCase(RandomTestUtil.randomString());

				type = Type.create("ChildTestEntity2");
			}
		};

		testPostTestEntity_addTestEntity(childTestEntity2);

		childTestEntity2.setId(RandomTestUtil.randomLong());

		try (LogCapture logCapture = LoggerTestUtil.configureLog4JLogger(
				"com.liferay.batch.engine.internal." +
					"BatchEngineImportTaskExecutorImpl",
				LoggerTestUtil.ERROR)) {

			_waitForFinish(
				"FAILED", true,
				JSONFactoryUtil.createJSONObject(
					testEntityResource.postTestEntityBatchHttpResponse(
						null,
						JSONUtil.putAll(
							JSONFactoryUtil.createJSONObject(
								String.valueOf(childTestEntity1)),
							JSONFactoryUtil.createJSONObject(
								String.valueOf(childTestEntity2)))
					).getContent()));
		}
	}

	private JSONObject _waitForFinish(
			String expectedExecuteStatus, boolean importTask,
			JSONObject jsonObject)
		throws Exception {

		String endpoint = StringBundler.concat(
			"headless-batch-engine/v1.0/",
			importTask ? "import-task" : "export-task",
			"/by-external-reference-code/");

		while (true) {
			jsonObject = HTTPTestUtil.invokeToJSONObject(
				null, endpoint + jsonObject.getString("externalReferenceCode"),
				Http.Method.GET);

			String executeStatus = jsonObject.getString("executeStatus");

			if (StringUtil.equals(executeStatus, "COMPLETED") ||
				StringUtil.equals(executeStatus, "FAILED")) {

				Assert.assertEquals(expectedExecuteStatus, executeStatus);

				return jsonObject;
			}
		}
	}

	@Inject
	private
		com.liferay.portal.tools.rest.builder.test.resource.v1_0.
			TestEntityResource _testEntityResource;

}