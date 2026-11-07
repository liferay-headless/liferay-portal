/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.rest.internal.model.listener.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.field.util.ObjectFieldUtil;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.object.test.util.OpenAPIDocumentTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.application.HeadlessApplicationProvider;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Petteri Karttunen
 */
@RunWith(Arquillian.class)
public class ObjectFieldModelListenerTest {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Test
	public void testOnAfterCreate() throws Exception {
		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		_testOnAfterCreate(
			objectDefinition.getRESTContextPath(), objectDefinition);
	}

	@Test
	public void testOnAfterCreateWithSystemObjectDefinition() throws Exception {
		_testOnAfterCreate(
			"/headless-admin-user",
			_objectDefinitionLocalService.fetchSystemObjectDefinition(
				TestPropsValues.getCompanyId(), "User"));
	}

	private void _testOnAfterCreate(
			String basePath, ObjectDefinition objectDefinition)
		throws Exception {

		Assert.assertNotNull(
			OpenAPIDocumentTestUtil.getContent(
				basePath, _headlessApplicationProvider));

		String name = "a" + RandomTestUtil.randomString();

		ObjectFieldUtil.addCustomObjectField(
			new TextObjectFieldBuilder(
			).labelMap(
				RandomTestUtil.randomLocaleStringMap()
			).name(
				name
			).objectDefinitionId(
				objectDefinition.getObjectDefinitionId()
			).userId(
				TestPropsValues.getUserId()
			).build());

		String content = OpenAPIDocumentTestUtil.getContent(
			basePath, _headlessApplicationProvider);

		Assert.assertTrue(content.contains(name));
	}

	@Inject
	private HeadlessApplicationProvider _headlessApplicationProvider;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

}