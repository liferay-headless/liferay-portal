/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.batch.engine.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.headless.batch.engine.client.dto.v1_0.ExportTask;
import com.liferay.headless.batch.engine.client.http.HttpInvoker;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.util.PropsValues;

import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

/**
 * @author Mauricio Valdivia
 */
@RunWith(Arquillian.class)
public class HeadlessDeliveryBatchModeTest {

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();
	}

	@After
	public void tearDown() throws PortalException {
		BlogsEntryLocalServiceUtil.deleteEntries(_group.getGroupId());
	}

	@Test
	public void testCannotCreateTwoSameBlogPostingsWithBatchPostCreateStrategyInsertWhenOneOfThemAlreadyExisting()
		throws Exception {

		String body = _createBlogPostingsBody(1, "headline");

		int taskId1 = _blogPostingsBatch(
			body, _group.getGroupId(), "INSERT", "ON_ERROR_FAIL");

		_waitImportCompletion(
			taskId1, ExportTask.ExecuteStatus.COMPLETED.toString());

		int taskId2 = _blogPostingsBatch(
			body, _group.getGroupId(), "INSERT", "ON_ERROR_FAIL");

		_waitImportCompletion(
			taskId2, ExportTask.ExecuteStatus.FAILED.toString());
	}

	@Test
	public void testCanUpdateBlogPostingWithBatchPutCreateStrategyUpdateWithOnErrorContinue()
		throws Exception {

		String createBody = _createBlogPostingsBody(1, "headlineFirst");

		int taskId1 = _blogPostingsBatch(
			createBody, _group.getGroupId(), "INSERT", "ON_ERROR_FAIL");

		_waitImportCompletion(
			taskId1, ExportTask.ExecuteStatus.COMPLETED.toString());

		BlogsEntry blog =
			BlogsEntryLocalServiceUtil.getBlogsEntryByExternalReferenceCode(
				"erc0", _group.getGroupId());

		String updateBody = StringBundler.concat(
			"[",
			JSONUtil.put(
				"articleBody", "updatedArticleBody"
			).put(
				"externalReferenceCode", blog.getExternalReferenceCode()
			).put(
				"headline", "updatedHeadline"
			).put(
				"id", blog.getEntryId()
			),
			",", JSONUtil.put("externalReferenceCode", "no exist"), "]");

		int taskId2 = _blogPostingsBatch(
			updateBody, _group.getGroupId(), "UPDATE", "ON_ERROR_CONTINUE");

		_waitImportCompletion(
			taskId2, ExportTask.ExecuteStatus.COMPLETED.toString());

		BlogsEntry updatedBlog = BlogsEntryLocalServiceUtil.getEntry(
			blog.getEntryId());

		JSONAssert.assertEquals(
			JSONUtil.put(
				"content", "updatedArticleBody"
			).put(
				"externalReferenceCode", blog.getExternalReferenceCode()
			).put(
				"title", "updatedHeadline"
			).toString(),
			updatedBlog.toString(), JSONCompareMode.LENIENT);
	}

	private int _blogPostingsBatch(
			String body, long groupId, String strategy, String importStrategy)
		throws Exception {

		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		if (Objects.equals(strategy, "UPDATE")) {
			httpInvoker.httpMethod(HttpInvoker.HttpMethod.PUT);
			httpInvoker.path(
				StringBundler.concat(
					"http://localhost:8080/o/headless-delivery/v1.0",
					"/blog-postings/batch?updateStrategy=", strategy,
					"&importStrategy=", importStrategy));
		}
		else {
			httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
			httpInvoker.path(
				StringBundler.concat(
					"http://localhost:8080/o/headless-delivery/v1.0/sites/",
					groupId, "/blog-postings/batch?createStrategy=", strategy,
					"&importStrategy=", importStrategy));
		}

		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		httpInvoker.body(body, "application/json");

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
			httpResponse.getContent());

		return (int)jsonObject.get("id");
	}

	private String _createBlogPostingsBody(
		int numberOfBlogPostings, String headline) {

		String body = "[";
		String articleBody = "articleBody";
		String externalReferenceCode = "erc";

		StringBuilder bodyBuilder = new StringBuilder(body);
		int i = 0;

		while (i != numberOfBlogPostings) {
			String blogPostingBody = JSONUtil.put(
				"articleBody", articleBody + i
			).put(
				"externalReferenceCode", externalReferenceCode + i
			).put(
				"headline", headline + i
			).toString();

			int j = i + 1;

			if (j == numberOfBlogPostings) {
				bodyBuilder.append(
					blogPostingBody
				).append(
					"]"
				);
			}
			else {
				bodyBuilder.append(
					blogPostingBody
				).append(
					","
				);
			}

			i++;
		}

		return bodyBuilder.toString();
	}

	private void _waitImportCompletion(int taskId, String expectedExecuteStatus)
		throws Exception {

		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.httpMethod(HttpInvoker.HttpMethod.GET);

		httpInvoker.userNameAndPassword(
			"test@liferay.com:" + PropsValues.DEFAULT_ADMIN_PASSWORD);

		httpInvoker.path(
			"http://localhost:8080/o/headless-batch-engine/v1.0/import-task/" +
				taskId);

		while (true) {
			HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(
				httpResponse.getContent());

			String executeStatus = (String)jsonObject.get("executeStatus");

			if (Objects.equals(
					executeStatus,
					ExportTask.ExecuteStatus.COMPLETED.toString()) ||
				Objects.equals(
					executeStatus,
					ExportTask.ExecuteStatus.FAILED.getValue())) {

				if (!Objects.equals(expectedExecuteStatus, executeStatus)) {
					throw new AssertionError("Not expected executeStatus");
				}

				break;
			}
		}
	}

	private Group _group;

}