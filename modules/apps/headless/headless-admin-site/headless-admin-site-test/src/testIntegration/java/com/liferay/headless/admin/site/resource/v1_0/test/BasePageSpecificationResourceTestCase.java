/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.site.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.headless.admin.site.client.dto.v1_0.ContentPageSpecification;
import com.liferay.headless.admin.site.client.dto.v1_0.PageSpecification;
import com.liferay.headless.admin.site.client.dto.v1_0.WidgetPageSpecification;
import com.liferay.headless.admin.site.client.http.HttpInvoker;
import com.liferay.headless.admin.site.client.pagination.Page;
import com.liferay.headless.admin.site.client.resource.v1_0.PageSpecificationResource;
import com.liferay.headless.admin.site.client.serdes.v1_0.PageSpecificationSerDes;
import com.liferay.petra.function.transform.TransformUtil;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.util.PropsValues;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import java.lang.reflect.Method;

import java.text.Format;

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
import java.util.function.Supplier;

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
public abstract class BasePageSpecificationResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_format = FastDateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_pageSpecificationResource.setContextCompany(testCompany);

		_testCompanyAdminUser = UserTestUtil.getAdminUser(
			testCompany.getCompanyId());

		pageSpecificationResource = PageSpecificationResource.builder(
		).authentication(
			_testCompanyAdminUser.getEmailAddress(),
			PropsValues.DEFAULT_ADMIN_PASSWORD
		).endpoint(
			testCompany.getVirtualHostname(), 8080, "http"
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
	public void testClientSerDesToDTO() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		PageSpecification pageSpecification1 = randomPageSpecification();

		String json = objectMapper.writeValueAsString(pageSpecification1);

		PageSpecification pageSpecification2 = PageSpecificationSerDes.toDTO(
			json);

		Assert.assertTrue(equals(pageSpecification1, pageSpecification2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = getClientSerDesObjectMapper();

		PageSpecification pageSpecification = randomPageSpecification();

		String json1 = objectMapper.writeValueAsString(pageSpecification);
		String json2 = PageSpecificationSerDes.toJSON(pageSpecification);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	protected ObjectMapper getClientSerDesObjectMapper() {
		return new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		PageSpecification pageSpecification = randomPageSpecification();

		pageSpecification.setExternalReferenceCode(regex);

		String json = PageSpecificationSerDes.toJSON(pageSpecification);

		Assert.assertFalse(json.contains(regex));

		pageSpecification = PageSpecificationSerDes.toDTO(json);

		Assert.assertEquals(
			regex, pageSpecification.getExternalReferenceCode());
	}

	@Test
	public void testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage()
		throws Exception {

		String siteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode();
		String irrelevantSiteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode();
		String displayPageTemplateExternalReferenceCode =
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getDisplayPageTemplateExternalReferenceCode();
		String irrelevantDisplayPageTemplateExternalReferenceCode =
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantDisplayPageTemplateExternalReferenceCode();

		Page<PageSpecification> page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode,
					displayPageTemplateExternalReferenceCode);

		long totalCount = page.getTotalCount();

		if ((irrelevantSiteExternalReferenceCode != null) &&
			(irrelevantDisplayPageTemplateExternalReferenceCode != null)) {

			PageSpecification irrelevantPageSpecification =
				testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
					irrelevantSiteExternalReferenceCode,
					irrelevantDisplayPageTemplateExternalReferenceCode,
					randomIrrelevantPageSpecification());

			page =
				pageSpecificationResource.
					getSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage(
						irrelevantSiteExternalReferenceCode,
						irrelevantDisplayPageTemplateExternalReferenceCode);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantPageSpecification,
				(List<PageSpecification>)page.getItems());
			assertValid(
				page,
				testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
					irrelevantSiteExternalReferenceCode,
					irrelevantDisplayPageTemplateExternalReferenceCode));
		}

		PageSpecification pageSpecification1 =
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode,
				displayPageTemplateExternalReferenceCode,
				randomPageSpecification());

		PageSpecification pageSpecification2 =
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode,
				displayPageTemplateExternalReferenceCode,
				randomPageSpecification());

		page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode,
					displayPageTemplateExternalReferenceCode);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			pageSpecification1, (List<PageSpecification>)page.getItems());
		assertContains(
			pageSpecification2, (List<PageSpecification>)page.getItems());
		assertValid(
			page,
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				siteExternalReferenceCode,
				displayPageTemplateExternalReferenceCode));
	}

	protected Map<String, Map<String, String>>
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				String siteExternalReferenceCode,
				String displayPageTemplateExternalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected PageSpecification
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				String siteExternalReferenceCode,
				String displayPageTemplateExternalReferenceCode,
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected String
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getDisplayPageTemplateExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeDisplayPageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantDisplayPageTemplateExternalReferenceCode()
		throws Exception {

		return null;
	}

	@Test
	public void testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage()
		throws Exception {

		String siteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode();
		String irrelevantSiteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode();
		String masterPageExternalReferenceCode =
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getMasterPageExternalReferenceCode();
		String irrelevantMasterPageExternalReferenceCode =
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantMasterPageExternalReferenceCode();

		Page<PageSpecification> page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode, masterPageExternalReferenceCode);

		long totalCount = page.getTotalCount();

		if ((irrelevantSiteExternalReferenceCode != null) &&
			(irrelevantMasterPageExternalReferenceCode != null)) {

			PageSpecification irrelevantPageSpecification =
				testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
					irrelevantSiteExternalReferenceCode,
					irrelevantMasterPageExternalReferenceCode,
					randomIrrelevantPageSpecification());

			page =
				pageSpecificationResource.
					getSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage(
						irrelevantSiteExternalReferenceCode,
						irrelevantMasterPageExternalReferenceCode);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantPageSpecification,
				(List<PageSpecification>)page.getItems());
			assertValid(
				page,
				testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
					irrelevantSiteExternalReferenceCode,
					irrelevantMasterPageExternalReferenceCode));
		}

		PageSpecification pageSpecification1 =
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, masterPageExternalReferenceCode,
				randomPageSpecification());

		PageSpecification pageSpecification2 =
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, masterPageExternalReferenceCode,
				randomPageSpecification());

		page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode, masterPageExternalReferenceCode);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			pageSpecification1, (List<PageSpecification>)page.getItems());
		assertContains(
			pageSpecification2, (List<PageSpecification>)page.getItems());
		assertValid(
			page,
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				siteExternalReferenceCode, masterPageExternalReferenceCode));
	}

	protected Map<String, Map<String, String>>
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				String siteExternalReferenceCode,
				String masterPageExternalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected PageSpecification
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				String siteExternalReferenceCode,
				String masterPageExternalReferenceCode,
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected String
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getMasterPageExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeMasterPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantMasterPageExternalReferenceCode()
		throws Exception {

		return null;
	}

	@Test
	public void testDeleteSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Test
	public void testGetSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Test
	public void testGraphQLGetSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode()
		throws Exception {

		Assert.assertTrue(true);
	}

	@Test
	public void testGraphQLGetSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodeNotFound()
		throws Exception {

		Assert.assertTrue(true);
	}

	@Test
	public void testPatchSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode()
		throws Exception {

		Assert.assertTrue(false);
	}

	@Test
	public void testPutSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode()
		throws Exception {

		Assert.assertTrue(false);
	}

	protected PageSpecification
			testPutSiteByExternalReferenceCodePageSpecificationByExternalReferenceCode_createPageSpecification()
		throws Exception {

		return randomPageSpecification();
	}

	@Test
	public void testPostSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish()
		throws Exception {

		PageSpecification randomPageSpecification = randomPageSpecification();

		PageSpecification postPageSpecification =
			testPostSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish_addPageSpecification(
				randomPageSpecification);

		assertEquals(randomPageSpecification, postPageSpecification);
		assertValid(postPageSpecification);

		ContentPageSpecification contentPageSpecification =
			new ContentPageSpecification() {
				{
					externalReferenceCode = StringUtil.toLowerCase(
						RandomTestUtil.randomString());
					draftContentPageSpecificationExternalReferenceCode =
						StringUtil.toLowerCase(RandomTestUtil.randomString());

					type = Type.create("ContentPageSpecification");
				}
			};

		assertEquals(
			contentPageSpecification,
			testPostSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish_addPageSpecification(
				contentPageSpecification));

		WidgetPageSpecification widgetPageSpecification =
			new WidgetPageSpecification() {
				{
					externalReferenceCode = StringUtil.toLowerCase(
						RandomTestUtil.randomString());

					type = Type.create("WidgetPageSpecification");
				}
			};

		assertEquals(
			widgetPageSpecification,
			testPostSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish_addPageSpecification(
				widgetPageSpecification));
	}

	protected PageSpecification
			testPostSiteByExternalReferenceCodePageSpecificationByExternalReferenceCodePublish_addPageSpecification(
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage()
		throws Exception {

		String siteExternalReferenceCode =
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode();
		String irrelevantSiteExternalReferenceCode =
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode();
		String pageTemplateExternalReferenceCode =
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getPageTemplateExternalReferenceCode();
		String irrelevantPageTemplateExternalReferenceCode =
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantPageTemplateExternalReferenceCode();

		Page<PageSpecification> page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode,
					pageTemplateExternalReferenceCode);

		long totalCount = page.getTotalCount();

		if ((irrelevantSiteExternalReferenceCode != null) &&
			(irrelevantPageTemplateExternalReferenceCode != null)) {

			PageSpecification irrelevantPageSpecification =
				testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
					irrelevantSiteExternalReferenceCode,
					irrelevantPageTemplateExternalReferenceCode,
					randomIrrelevantPageSpecification());

			page =
				pageSpecificationResource.
					getSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage(
						irrelevantSiteExternalReferenceCode,
						irrelevantPageTemplateExternalReferenceCode);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantPageSpecification,
				(List<PageSpecification>)page.getItems());
			assertValid(
				page,
				testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
					irrelevantSiteExternalReferenceCode,
					irrelevantPageTemplateExternalReferenceCode));
		}

		PageSpecification pageSpecification1 =
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, pageTemplateExternalReferenceCode,
				randomPageSpecification());

		PageSpecification pageSpecification2 =
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, pageTemplateExternalReferenceCode,
				randomPageSpecification());

		page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode,
					pageTemplateExternalReferenceCode);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			pageSpecification1, (List<PageSpecification>)page.getItems());
		assertContains(
			pageSpecification2, (List<PageSpecification>)page.getItems());
		assertValid(
			page,
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				siteExternalReferenceCode, pageTemplateExternalReferenceCode));
	}

	protected Map<String, Map<String, String>>
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				String siteExternalReferenceCode,
				String pageTemplateExternalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected PageSpecification
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				String siteExternalReferenceCode,
				String pageTemplateExternalReferenceCode,
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected String
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getPageTemplateExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodePageTemplateByExternalReferenceCodePageSpecificationsPage_getIrrelevantPageTemplateExternalReferenceCode()
		throws Exception {

		return null;
	}

	@Test
	public void testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage()
		throws Exception {

		String siteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode();
		String irrelevantSiteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode();
		String sitePageExternalReferenceCode =
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getSitePageExternalReferenceCode();
		String irrelevantSitePageExternalReferenceCode =
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSitePageExternalReferenceCode();

		Page<PageSpecification> page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode, sitePageExternalReferenceCode);

		long totalCount = page.getTotalCount();

		if ((irrelevantSiteExternalReferenceCode != null) &&
			(irrelevantSitePageExternalReferenceCode != null)) {

			PageSpecification irrelevantPageSpecification =
				testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
					irrelevantSiteExternalReferenceCode,
					irrelevantSitePageExternalReferenceCode,
					randomIrrelevantPageSpecification());

			page =
				pageSpecificationResource.
					getSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage(
						irrelevantSiteExternalReferenceCode,
						irrelevantSitePageExternalReferenceCode);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantPageSpecification,
				(List<PageSpecification>)page.getItems());
			assertValid(
				page,
				testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
					irrelevantSiteExternalReferenceCode,
					irrelevantSitePageExternalReferenceCode));
		}

		PageSpecification pageSpecification1 =
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, sitePageExternalReferenceCode,
				randomPageSpecification());

		PageSpecification pageSpecification2 =
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, sitePageExternalReferenceCode,
				randomPageSpecification());

		page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode, sitePageExternalReferenceCode);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			pageSpecification1, (List<PageSpecification>)page.getItems());
		assertContains(
			pageSpecification2, (List<PageSpecification>)page.getItems());
		assertValid(
			page,
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				siteExternalReferenceCode, sitePageExternalReferenceCode));
	}

	protected Map<String, Map<String, String>>
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				String siteExternalReferenceCode,
				String sitePageExternalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected PageSpecification
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				String siteExternalReferenceCode,
				String sitePageExternalReferenceCode,
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected String
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getSitePageExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeSitePageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSitePageExternalReferenceCode()
		throws Exception {

		return null;
	}

	@Test
	public void testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage()
		throws Exception {

		String siteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode();
		String irrelevantSiteExternalReferenceCode =
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode();
		String utilityPageExternalReferenceCode =
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getUtilityPageExternalReferenceCode();
		String irrelevantUtilityPageExternalReferenceCode =
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantUtilityPageExternalReferenceCode();

		Page<PageSpecification> page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode,
					utilityPageExternalReferenceCode);

		long totalCount = page.getTotalCount();

		if ((irrelevantSiteExternalReferenceCode != null) &&
			(irrelevantUtilityPageExternalReferenceCode != null)) {

			PageSpecification irrelevantPageSpecification =
				testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
					irrelevantSiteExternalReferenceCode,
					irrelevantUtilityPageExternalReferenceCode,
					randomIrrelevantPageSpecification());

			page =
				pageSpecificationResource.
					getSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage(
						irrelevantSiteExternalReferenceCode,
						irrelevantUtilityPageExternalReferenceCode);

			Assert.assertEquals(totalCount + 1, page.getTotalCount());

			assertContains(
				irrelevantPageSpecification,
				(List<PageSpecification>)page.getItems());
			assertValid(
				page,
				testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
					irrelevantSiteExternalReferenceCode,
					irrelevantUtilityPageExternalReferenceCode));
		}

		PageSpecification pageSpecification1 =
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, utilityPageExternalReferenceCode,
				randomPageSpecification());

		PageSpecification pageSpecification2 =
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				siteExternalReferenceCode, utilityPageExternalReferenceCode,
				randomPageSpecification());

		page =
			pageSpecificationResource.
				getSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage(
					siteExternalReferenceCode,
					utilityPageExternalReferenceCode);

		Assert.assertEquals(totalCount + 2, page.getTotalCount());

		assertContains(
			pageSpecification1, (List<PageSpecification>)page.getItems());
		assertContains(
			pageSpecification2, (List<PageSpecification>)page.getItems());
		assertValid(
			page,
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				siteExternalReferenceCode, utilityPageExternalReferenceCode));
	}

	protected Map<String, Map<String, String>>
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getExpectedActions(
				String siteExternalReferenceCode,
				String utilityPageExternalReferenceCode)
		throws Exception {

		Map<String, Map<String, String>> expectedActions = new HashMap<>();

		return expectedActions;
	}

	protected PageSpecification
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_addPageSpecification(
				String siteExternalReferenceCode,
				String utilityPageExternalReferenceCode,
				PageSpecification pageSpecification)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getSiteExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantSiteExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected String
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getUtilityPageExternalReferenceCode()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected String
			testGetSiteByExternalReferenceCodeUtilityPageByExternalReferenceCodePageSpecificationsPage_getIrrelevantUtilityPageExternalReferenceCode()
		throws Exception {

		return null;
	}

	protected void assertContains(
		PageSpecification pageSpecification,
		List<PageSpecification> pageSpecifications) {

		boolean contains = false;

		for (PageSpecification item : pageSpecifications) {
			if (equals(pageSpecification, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			pageSpecifications + " does not contain " + pageSpecification,
			contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		PageSpecification pageSpecification1,
		PageSpecification pageSpecification2) {

		Assert.assertTrue(
			pageSpecification1 + " does not equal " + pageSpecification2,
			equals(pageSpecification1, pageSpecification2));
	}

	protected void assertEquals(
		List<PageSpecification> pageSpecifications1,
		List<PageSpecification> pageSpecifications2) {

		Assert.assertEquals(
			pageSpecifications1.size(), pageSpecifications2.size());

		for (int i = 0; i < pageSpecifications1.size(); i++) {
			PageSpecification pageSpecification1 = pageSpecifications1.get(i);
			PageSpecification pageSpecification2 = pageSpecifications2.get(i);

			assertEquals(pageSpecification1, pageSpecification2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<PageSpecification> pageSpecifications1,
		List<PageSpecification> pageSpecifications2) {

		Assert.assertEquals(
			pageSpecifications1.size(), pageSpecifications2.size());

		for (PageSpecification pageSpecification1 : pageSpecifications1) {
			boolean contains = false;

			for (PageSpecification pageSpecification2 : pageSpecifications2) {
				if (equals(pageSpecification1, pageSpecification2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				pageSpecifications2 + " does not contain " + pageSpecification1,
				contains);
		}
	}

	protected void assertValid(PageSpecification pageSpecification)
		throws Exception {

		boolean valid = true;

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (pageSpecification.getExternalReferenceCode() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("settings", additionalAssertFieldName)) {
				if (pageSpecification.getSettings() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("status", additionalAssertFieldName)) {
				if (pageSpecification.getStatus() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("type", additionalAssertFieldName)) {
				if (pageSpecification.getType() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"draftContentPageSpecificationExternalReferenceCode",
					additionalAssertFieldName)) {

				if (!(pageSpecification instanceof ContentPageSpecification)) {
					continue;
				}

				if (((ContentPageSpecification)pageSpecification).
						getDraftContentPageSpecificationExternalReferenceCode() ==
							null) {

					valid = false;
				}

				continue;
			}

			if (Objects.equals("pageExperiences", additionalAssertFieldName)) {
				if (!(pageSpecification instanceof ContentPageSpecification)) {
					continue;
				}

				if (((ContentPageSpecification)pageSpecification).
						getPageExperiences() == null) {

					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"widgetPageSections", additionalAssertFieldName)) {

				if (!(pageSpecification instanceof WidgetPageSpecification)) {
					continue;
				}

				if (((WidgetPageSpecification)pageSpecification).
						getWidgetPageSections() == null) {

					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<PageSpecification> page) {
		assertValid(page, Collections.emptyMap());
	}

	protected void assertValid(
		Page<PageSpecification> page,
		Map<String, Map<String, String>> expectedActions) {

		boolean valid = false;

		java.util.Collection<PageSpecification> pageSpecifications =
			page.getItems();

		int size = pageSpecifications.size();

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

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.headless.admin.site.dto.v1_0.PageSpecification.
						class)) {

			if (!ArrayUtil.contains(
					getAdditionalAssertFieldNames(), field.getName())) {

				continue;
			}

			graphQLFields.addAll(getGraphQLFields(field));
		}

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

	protected boolean equals(
		PageSpecification pageSpecification1,
		PageSpecification pageSpecification2) {

		if (pageSpecification1 == pageSpecification2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals(
					"externalReferenceCode", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						pageSpecification1.getExternalReferenceCode(),
						pageSpecification2.getExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("settings", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageSpecification1.getSettings(),
						pageSpecification2.getSettings())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("status", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageSpecification1.getStatus(),
						pageSpecification2.getStatus())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("type", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						pageSpecification1.getType(),
						pageSpecification2.getType())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"draftContentPageSpecificationExternalReferenceCode",
					additionalAssertFieldName)) {

				if (!(pageSpecification1 instanceof ContentPageSpecification) ||
					!(pageSpecification2 instanceof ContentPageSpecification)) {

					continue;
				}

				if (!Objects.deepEquals(
						((ContentPageSpecification)pageSpecification1).
							getDraftContentPageSpecificationExternalReferenceCode(),
						((ContentPageSpecification)pageSpecification2).
							getDraftContentPageSpecificationExternalReferenceCode())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("pageExperiences", additionalAssertFieldName)) {
				if (!(pageSpecification1 instanceof ContentPageSpecification) ||
					!(pageSpecification2 instanceof ContentPageSpecification)) {

					continue;
				}

				if (!Objects.deepEquals(
						((ContentPageSpecification)pageSpecification1).
							getPageExperiences(),
						((ContentPageSpecification)pageSpecification2).
							getPageExperiences())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"widgetPageSections", additionalAssertFieldName)) {

				if (!(pageSpecification1 instanceof WidgetPageSpecification) ||
					!(pageSpecification2 instanceof WidgetPageSpecification)) {

					continue;
				}

				if (!Objects.deepEquals(
						((WidgetPageSpecification)pageSpecification1).
							getWidgetPageSections(),
						((WidgetPageSpecification)pageSpecification2).
							getWidgetPageSections())) {

					return false;
				}

				continue;
			}

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

		if (!(_pageSpecificationResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_pageSpecificationResource;

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
		EntityField entityField, String operator,
		PageSpecification pageSpecification) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("externalReferenceCode")) {
			Object object = pageSpecification.getExternalReferenceCode();

			String value = String.valueOf(object);

			if (operator.equals("contains")) {
				sb = new StringBundler();

				sb.append("contains(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 2)) {
					sb.append(value.substring(1, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else if (operator.equals("startswith")) {
				sb = new StringBundler();

				sb.append("startswith(");
				sb.append(entityFieldName);
				sb.append(",'");

				if ((object != null) && (value.length() > 1)) {
					sb.append(value.substring(0, value.length() - 1));
				}
				else {
					sb.append(value);
				}

				sb.append("')");
			}
			else {
				sb.append("'");
				sb.append(value);
				sb.append("'");
			}

			return sb.toString();
		}

		if (entityFieldName.equals("settings")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("status")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("type")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

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

	protected PageSpecification randomPageSpecification() throws Exception {
		List<Supplier<PageSpecification>> suppliers = Arrays.asList(
			() -> {
				ContentPageSpecification pageSpecification =
					new ContentPageSpecification();

				pageSpecification.setExternalReferenceCode(
					StringUtil.toLowerCase(RandomTestUtil.randomString()));

				pageSpecification.
					setDraftContentPageSpecificationExternalReferenceCode(
						StringUtil.toLowerCase(RandomTestUtil.randomString()));

				pageSpecification.setType(
					PageSpecification.Type.create("ContentPageSpecification"));

				return pageSpecification;
			},
			() -> {
				WidgetPageSpecification pageSpecification =
					new WidgetPageSpecification();

				pageSpecification.setExternalReferenceCode(
					StringUtil.toLowerCase(RandomTestUtil.randomString()));

				pageSpecification.setType(
					PageSpecification.Type.create("WidgetPageSpecification"));

				return pageSpecification;
			});

		Supplier<PageSpecification> supplier = suppliers.get(
			RandomTestUtil.randomInt(0, suppliers.size() - 1));

		return supplier.get();
	}

	protected PageSpecification randomIrrelevantPageSpecification()
		throws Exception {

		PageSpecification randomIrrelevantPageSpecification =
			randomPageSpecification();

		return randomIrrelevantPageSpecification;
	}

	protected PageSpecification randomPatchPageSpecification()
		throws Exception {

		return randomPageSpecification();
	}

	protected PageSpecificationResource pageSpecificationResource;
	protected com.liferay.portal.kernel.model.Group irrelevantGroup;
	protected com.liferay.portal.kernel.model.Company testCompany;
	protected com.liferay.portal.kernel.model.Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = source.getClass();

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					_getAllDeclaredFields(sourceClass)) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				try {
					Method setMethod = _getMethod(
						targetClass, field.getName(), "set",
						getMethod.getReturnType());

					setMethod.invoke(target, getMethod.invoke(source));
				}
				catch (Exception e) {
					continue;
				}
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

		private static List<java.lang.reflect.Field> _getAllDeclaredFields(
			Class<?> clazz) {

			List<java.lang.reflect.Field> fields = new ArrayList<>();

			while ((clazz != null) && (clazz != Object.class)) {
				for (java.lang.reflect.Field field :
						clazz.getDeclaredFields()) {

					fields.add(field);
				}

				clazz = clazz.getSuperclass();
			}

			return fields;
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
		LogFactoryUtil.getLog(BasePageSpecificationResourceTestCase.class);

	private static Format _format;

	private com.liferay.portal.kernel.model.User _testCompanyAdminUser;

	@Inject
	private
		com.liferay.headless.admin.site.resource.v1_0.PageSpecificationResource
			_pageSpecificationResource;

}