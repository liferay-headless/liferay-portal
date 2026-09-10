/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.mcp.server.rest.client.dto.v1_0.ToolSet;
import com.liferay.mcp.server.rest.client.pagination.Page;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.field.builder.TextObjectFieldBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.test.util.ObjectDefinitionTestUtil;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Alejandro Tardín
 */
@FeatureFlag("LPD-63311")
@RunWith(Arquillian.class)
public class ToolSetResourceTest extends BaseToolSetResourceTestCase {

	@Override
	@Test
	public void testGetToolSetsPage() throws Exception {
		_assertToolSet(
			toolSet ->
				Objects.equals(toolSet.getName(), "mcp-server-v1.0") &&
				Validator.isNotNull(toolSet.getDescription()));

		ObjectDefinition objectDefinition =
			ObjectDefinitionTestUtil.publishObjectDefinition();

		String restContextPath = objectDefinition.getRESTContextPath();

		_assertToolSet(
			toolSet -> Objects.equals(
				"c-" + restContextPath.substring(3), toolSet.getName()));

		_assertToolSet(
			toolSet -> Objects.equals(
				toolSet.getName(), "headless-commerce-admin-pricing-v1.0"));
		_assertToolSet(
			toolSet -> Objects.equals(
				toolSet.getName(), "headless-commerce-admin-pricing-v2.0"));

		_testGetToolSetsPageWithAnotherCompany();
	}

	private void _assertToolSet(Predicate<ToolSet> predicate) throws Exception {
		Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

		Assert.assertTrue(
			ListUtil.exists(
				new ArrayList<>(toolSetsPage.getItems()), predicate));
	}

	private void _testGetToolSetsPageWithAnotherCompany() throws Exception {
		Company company = CompanyTestUtil.addCompany();

		try {
			User user = UserTestUtil.addCompanyAdminUser(company);

			String name = "ToolSetScopeProbe";

			try (SafeCloseable safeCloseable =
					CompanyThreadLocal.setCompanyIdWithSafeCloseable(
						company.getCompanyId())) {

				ObjectDefinitionTestUtil.publishObjectDefinition(
					false, false, false, name,
					Arrays.asList(
						new TextObjectFieldBuilder(
						).userId(
							user.getUserId()
						).labelMap(
							LocalizedMapUtil.getLocalizedMap("Able")
						).name(
							"able"
						).build()),
					0, ObjectDefinitionConstants.SCOPE_COMPANY,
					user.getUserId());
			}

			String toolSetName = "c-" + StringUtil.toLowerCase(name);

			Page<ToolSet> toolSetsPage = toolSetResource.getToolSetsPage();

			List<ToolSet> toolSets = new ArrayList<>(toolSetsPage.getItems());

			Assert.assertFalse(
				toolSets.toString(),
				ListUtil.exists(
					toolSets,
					toolSet -> StringUtil.startsWith(
						toolSet.getName(), toolSetName)));
		}
		finally {
			CompanyLocalServiceUtil.deleteCompany(company.getCompanyId());
		}
	}

}