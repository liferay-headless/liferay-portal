/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.mcp.server.rest.internal.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.liferay.mcp.server.rest.dto.v1_0.Tool;
import com.liferay.mcp.server.rest.dto.v1_0.ToolSummary;
import com.liferay.mcp.server.rest.internal.exception.RestrictedFieldException;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.test.AssertUtils;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;
import com.liferay.portal.vulcan.http.VulcanRequestForwarder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author Alejandro Tardín
 */
public class OpenAPIUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_openAPIJSONObject = JSONFactoryUtil.createJSONObject(
			StringUtil.read(
				OpenAPIUtilTest.class.getResourceAsStream(
					"dependencies/openapi.json")));
	}

	@Test
	public void testGetRequest() throws Exception {

		// Path and query parameters

		_testGetRequest(
			null, null, "GET",
			"/v1.0/items/123?fields=name&restrictFields=actions",
			JSONUtil.put(
				"fields", "name"
			).put(
				"itemId", "123"
			),
			"getItem");
		_testGetRequest(
			null, null, "GET", "/v1.0/items/123?restrictFields=actions",
			JSONUtil.put("itemId", "123"), "getItem");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?fields=name%2Cinteger&restrictFields=actions",
			JSONUtil.put("fields", JSONUtil.putAll("name", "integer")),
			"getItems");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?filter=name+eq+%27John+Doe%27&restrictFields=actions",
			JSONUtil.put("filter", "name eq 'John Doe'"), "getItems");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?page=1&pageSize=20&fields=name&restrictFields=actions",
			JSONUtil.put(
				"fields", "name"
			).put(
				"page", "1"
			).put(
				"pageSize", "20"
			),
			"getItems");
		_testGetRequest(
			null, null, "GET", "/v1.0/items?restrictFields=actions",
			JSONFactoryUtil.createJSONObject(), "getItems");
		_testGetRequest(
			null, null, "GET", "/v1.0/items?restrictFields=actions",
			JSONUtil.put("fields", ""), "getItems");
		_testGetRequest(
			null, null, "GET", "/v1.0/items?restrictFields=actions",
			JSONUtil.put("restrictFields", "name"), "getItems");

		// Restricted fields

		_testGetRequest(
			null, null, "GET", "/v1.0/items?restrictFields=actions",
			JSONFactoryUtil.createJSONObject(), Collections.emptySet(),
			"getItems");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?restrictFields=actions%2Cname%2Cparent.name",
			JSONFactoryUtil.createJSONObject(),
			new LinkedHashSet<>(Arrays.asList("name", "parent.name")),
			"getItems");
		_testGetRequest(
			"{}", "application/json", "PATCH",
			"/v1.0/items/123?restrictFields=name",
			JSONUtil.put(
				"body", JSONUtil.put("name", "Test")
			).put(
				"itemId", "123"
			),
			Collections.singleton("name"), "patchItem");
		_testGetRequest(
			"{}", "application/json", "POST", "/v1.0/items?restrictFields=name",
			JSONUtil.put("body", JSONFactoryUtil.createJSONObject()),
			Collections.singleton("name"), "postItem");
		_testGetRequest(
			"{}", "application/json", "PUT",
			"/v1.0/items/123?restrictFields=name",
			JSONUtil.put(
				"body", JSONUtil.put("name", "Test")
			).put(
				"itemId", "123"
			),
			Collections.singleton("name"), "putItem");
		_testGetRequest(
			JSONUtil.put(
				"string", "Test"
			).toString(),
			"application/json", "POST", "/v1.0/items?restrictFields=boolean",
			JSONUtil.put(
				"body",
				JSONUtil.put(
					"boolean", true
				).put(
					"string", "Test"
				)),
			Collections.singleton("boolean"), "postItem");
		_testGetRequest(
			JSONUtil.put(
				"object1", JSONUtil.put("boolean", true)
			).toString(),
			"application/json", "POST",
			"/v1.0/items?restrictFields=object1.string",
			JSONUtil.put(
				"body",
				JSONUtil.put(
					"object1",
					JSONUtil.put(
						"boolean", true
					).put(
						"string", "Test"
					))),
			Collections.singleton("object1.string"), "postItem");
		_testGetRequest(
			JSONUtil.put(
				"string", "Keep"
			).toString(),
			"application/json", "POST", "/v1.0/items?restrictFields=object1",
			JSONUtil.put(
				"body",
				JSONUtil.put(
					"object1", JSONUtil.put("string", "Test")
				).put(
					"string", "Keep"
				)),
			Collections.singleton("object1"), "postItem");
		_testGetRequest(
			JSONUtil.putAll(
				JSONUtil.put("string", "Keep")
			).toString(),
			"application/json", "POST", "/v1.0/batches?restrictFields=boolean",
			JSONUtil.put(
				"body",
				JSONUtil.putAll(
					JSONUtil.put(
						"boolean", true
					).put(
						"string", "Keep"
					))),
			Collections.singleton("boolean"), "postBatch");

		// Restricted field queries

		_testGetRequestFailure(
			StringBundler.concat(
				"The \"filter\" parameter of the \"getItems\" tool references ",
				"the restricted fields [string]. Remove them and invoke the ",
				"tool again."),
			JSONUtil.put("filter", "string eq 'Test'"),
			Collections.singleton("string"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"filter\" parameter of the \"getItems\" tool references ",
				"the restricted fields [object1/string]. Remove them and ",
				"invoke the tool again."),
			JSONUtil.put("filter", "object1/string eq 'Test'"),
			Collections.singleton("object1.string"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"filter\" parameter of the \"getItems\" tool references ",
				"the restricted fields [object1.string]. Remove them and ",
				"invoke the tool again."),
			JSONUtil.put("filter", "object1.string eq 'Test'"),
			Collections.singleton("object1.string"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"filter\" parameter of the \"getItems\" tool references ",
				"the restricted fields [object1/object2/boolean]. Remove them ",
				"and invoke the tool again."),
			JSONUtil.put("filter", "object1/object2/boolean eq true"),
			Collections.singleton("object1"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"filter\" parameter of the \"getItems\" tool references ",
				"the restricted fields [boolean, string]. Remove them and ",
				"invoke the tool again."),
			JSONUtil.put("filter", "string eq 'Test' and boolean eq true"),
			new LinkedHashSet<>(Arrays.asList("string", "boolean")),
			"getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"sort\" parameter of the \"getItems\" tool references ",
				"the restricted fields [string]. Remove them and invoke the ",
				"tool again."),
			JSONUtil.put("sort", "boolean:asc,string:desc"),
			Collections.singleton("string"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"sort\" parameter of the \"getItems\" tool references ",
				"the restricted fields [object1/string]. Remove them and ",
				"invoke the tool again."),
			JSONUtil.put("sort", "object1/string"),
			Collections.singleton("object1"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"aggregationTerms\" parameter of the \"getItems\" tool ",
				"references the restricted fields [string]. Remove them and ",
				"invoke the tool again."),
			JSONUtil.put(
				"aggregationTerms", JSONUtil.putAll("boolean", "string")),
			Collections.singleton("string"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"aggregationTerms\" parameter of the \"getItems\" tool ",
				"references the restricted fields [object1/string]. Remove ",
				"them and invoke the tool again."),
			JSONUtil.put("aggregationTerms", "boolean,object1/string"),
			Collections.singleton("object1"), "getItems");
		_testGetRequestFailure(
			StringBundler.concat(
				"The \"search\" parameter cannot be used on the \"getItems\" ",
				"tool because a keyword match confirms the value of a ",
				"restricted field even when the field is absent from the ",
				"response. Narrow the invocation with \"filter\" instead."),
			JSONUtil.put("search", "Test"), Collections.singleton("boolean"),
			"getItems");

		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?filter=string+eq+%27phoneNumber%27&restrictFields=" +
				"actions%2CphoneNumber",
			JSONUtil.put("filter", "string eq 'phoneNumber'"),
			Collections.singleton("phoneNumber"), "getItems");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?filter=string+eq+%27boolean%27%27s%27&" +
				"restrictFields=actions%2Cboolean",
			JSONUtil.put("filter", "string eq 'boolean''s'"),
			Collections.singleton("boolean"), "getItems");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?filter=object1%2Fboolean+eq+true&restrictFields=" +
				"actions%2Cobject1.string",
			JSONUtil.put("filter", "object1/boolean eq true"),
			Collections.singleton("object1.string"), "getItems");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?sort=boolean%3Aasc&restrictFields=actions%2Cstring",
			JSONUtil.put("sort", "boolean:asc"),
			Collections.singleton("string"), "getItems");
		_testGetRequest(
			null, null, "GET", "/v1.0/items?search=Test&restrictFields=actions",
			JSONUtil.put("search", "Test"), Collections.emptySet(), "getItems");
		_testGetRequest(
			null, null, "GET", "/v1.0/items?search=Test&restrictFields=actions",
			JSONUtil.put("search", "Test"), "getItems");
		_testGetRequest(
			null, null, "GET",
			"/v1.0/items?sort=string%3Aasc&restrictFields=actions",
			JSONUtil.put("sort", "string:asc"), "getItems");

		// Request bodies

		_testGetRequest(
			JSONUtil.put(
				"name", "Test"
			).toString(),
			"application/json", "PATCH", "/v1.0/items/123",
			JSONUtil.put(
				"body", JSONUtil.put("name", "Test")
			).put(
				"itemId", "123"
			),
			"patchItem");
		_testGetRequest(
			"{}", "application/json", "POST", "/v1.0/items",
			JSONUtil.put("body", JSONFactoryUtil.createJSONObject()),
			"postItem");

		// Multipart requests

		String fileContent = RandomTestUtil.randomString();
		String fileName = RandomTestUtil.randomString();
		String name = RandomTestUtil.randomString();

		VulcanRequestForwarder.Request request = OpenAPIUtil.getRequest(
			StringPool.BLANK, null,
			JSONUtil.put(
				"data",
				JSONUtil.put(
					"contentType", "text/plain"
				).put(
					"data",
					() -> {
						Base64.Encoder encoder = Base64.getEncoder();

						return encoder.encodeToString(fileContent.getBytes());
					}
				).put(
					"filename", fileName
				)
			).put(
				"name", name
			),
			_openAPIJSONObject, null, "postBinary", null);

		Assert.assertEquals("POST", request.getMethod());
		Assert.assertEquals("/v1.0/binaries", request.getPath());
		_assertMultipartContentType(request);

		List<FileItem> fileItems = _getFileItems(request);

		Assert.assertEquals(fileItems.toString(), 2, fileItems.size());

		FileItem fileItem = _getFileItem(fileItems, "data");

		Assert.assertFalse(fileItem.isFormField());
		Assert.assertEquals("text/plain", fileItem.getContentType());
		Assert.assertEquals(fileName, fileItem.getName());
		Assert.assertArrayEquals(fileContent.getBytes(), fileItem.get());

		fileItem = _getFileItem(fileItems, "name");

		Assert.assertTrue(fileItem.isFormField());
		Assert.assertEquals(name, fileItem.getString());

		request = OpenAPIUtil.getRequest(
			StringPool.BLANK, null,
			JSONUtil.put(
				"data",
				JSONUtil.put(
					"contentType", "text/plain"
				).put(
					"data",
					() -> {
						Base64.Encoder encoder = Base64.getEncoder();

						return encoder.encodeToString(fileContent.getBytes());
					}
				).put(
					"filename", fileName
				)
			).put(
				"name", name
			),
			_openAPIJSONObject, Collections.singleton("name"), "postBinary",
			null);

		fileItems = _getFileItems(request);

		Assert.assertEquals(fileItems.toString(), 1, fileItems.size());

		fileItem = _getFileItem(fileItems, "data");

		Assert.assertFalse(fileItem.isFormField());

		request = OpenAPIUtil.getRequest(
			StringPool.BLANK, null,
			JSONUtil.put(
				"boolean", true
			).put(
				"integer", 1
			).put(
				"string", fileContent
			),
			_openAPIJSONObject, null, "postUpload", null);

		Assert.assertEquals("POST", request.getMethod());
		Assert.assertEquals("/v1.0/uploads", request.getPath());
		_assertMultipartContentType(request);

		fileItems = _getFileItems(request);

		Assert.assertEquals(fileItems.toString(), 3, fileItems.size());

		Assert.assertEquals("true", _getFileItemValue(fileItems, "boolean"));
		Assert.assertEquals("1", _getFileItemValue(fileItems, "integer"));
		Assert.assertEquals(
			fileContent, _getFileItemValue(fileItems, "string"));

		// Headers

		Map<String, String> headers = HashMapBuilder.put(
			RandomTestUtil.randomString(), RandomTestUtil.randomString()
		).build();

		request = OpenAPIUtil.getRequest(
			StringPool.BLANK, headers, JSONUtil.put("itemId", "123"),
			_openAPIJSONObject, null, "getItem", null);

		Assert.assertEquals(headers, request.getHeaders());

		AssertUtils.assertFailure(
			IllegalArgumentException.class,
			StringBundler.concat(
				"The \"postItem\" tool requires the request payload nested ",
				"under a \"body\" property. Pass any path or query parameters ",
				"as siblings of \"body\" rather than flattening the payload ",
				"into the input map."),
			() -> OpenAPIUtil.getRequest(
				StringPool.BLANK, null,
				JSONUtil.put(
					RandomTestUtil.randomString(),
					RandomTestUtil.randomString()),
				_openAPIJSONObject, null, "postItem", null));
	}

	@Test
	public void testGetTool() throws Exception {
		AssertUtils.assertFailure(
			IllegalArgumentException.class,
			"OpenAPI document has no tool with name \"missing\"",
			() -> OpenAPIUtil.getTool(
				true, _openAPIJSONObject, null, "missing"));
		AssertUtils.assertFailure(
			IllegalArgumentException.class,
			"OpenAPI document has no \"paths\" object",
			() -> OpenAPIUtil.getTool(
				true, JSONFactoryUtil.createJSONObject(), null,
				RandomTestUtil.randomString()));
		AssertUtils.assertFailure(
			IllegalArgumentException.class, "Request body has no content",
			() -> _getInputSchema(_openAPIJSONObject, "postEmptyContent"));
		AssertUtils.assertFailure(
			IllegalArgumentException.class,
			"Request body content has no \"schema\"",
			() -> _getInputSchema(_openAPIJSONObject, "postNoSchema"));

		_testGetTool(
			"This is the description", "get_test_v1.0_items_itemId.json",
			"getItem");
		_testGetTool(
			"This is the summary. This is the description",
			"get_test_v1.0_items.json", "getItems");
		_testGetTool(
			"This is the summary. This is the description",
			"get_test_v1.0_items_no_inject.json", false, "getItems");
		_testGetTool("This is the summary", "get_c_test.json", "getItemsPage");
		_testGetTool(
			"PATCH /v1.0/items/{itemId}", "patch_test_v1.0_items_itemId.json",
			"patchItem");
		_testGetTool(
			"POST /v1.0/batches", "post_test_v1.0_batches.json", "postBatch");
		_testGetTool(
			"POST /v1.0/binaries", "post_test_v1.0_binaries.json",
			"postBinary");
		_testGetTool(
			"POST /v1.0/described", "post_test_v1.0_described.json",
			"postDescribed");
		_testGetTool(
			"POST /v1.0/items", "post_test_v1.0_items.json", "postItem");
		_testGetTool(
			"POST /v1.0/levels", "post_test_v1.0_levels.json", "postLevel");
		_testGetTool(
			"POST /v1.0/no-content", "post_test_v1.0_no-content.json",
			"postNoContent");
		_testGetTool(
			"POST /v1.0/parents", "post_test_v1.0_parents.json", "postParent");
		_testGetTool(
			"POST /v1.0/undescribed", "post_test_v1.0_undescribed.json",
			"postUndescribed");
		_testGetTool(
			"POST /v1.0/uploads", "post_test_v1.0_uploads.json", "postUpload");
		_testGetTool(
			"PUT /v1.0/items/{itemId}", "put_test_v1.0_items_itemId.json",
			"putItem");

		// Restricted fields

		Tool tool = OpenAPIUtil.getTool(
			true, _openAPIJSONObject,
			new LinkedHashSet<>(Arrays.asList("boolean", "object1.name")),
			"getItems");

		Map<String, ?> inputSchemaMap = tool.getInputSchema();

		Map<String, ?> propertiesMap = (Map<String, ?>)inputSchemaMap.get(
			"properties");

		Map<String, ?> fieldsMap = (Map<String, ?>)propertiesMap.get("fields");

		Map<String, ?> itemsMap = (Map<String, ?>)fieldsMap.get("items");

		List<String> enumValues = (List<String>)itemsMap.get("enum");

		Assert.assertFalse(enumValues.contains("boolean"));
		Assert.assertTrue(enumValues.contains("object1"));

		Map<String, ?> bodyMap = _getBodyMap(
			Collections.singleton("string"), "postItem");

		Map<String, ?> bodyPropertiesMap = (Map<String, ?>)bodyMap.get(
			"properties");

		Assert.assertFalse(
			bodyPropertiesMap.toString(),
			bodyPropertiesMap.containsKey("string"));
		Assert.assertTrue(
			bodyPropertiesMap.toString(),
			bodyPropertiesMap.containsKey("boolean"));

		Assert.assertEquals(Collections.emptyList(), bodyMap.get("required"));

		bodyMap = _getBodyMap(Collections.singleton("next.name"), "postLevel");

		bodyPropertiesMap = (Map<String, ?>)bodyMap.get("properties");

		Assert.assertTrue(
			bodyPropertiesMap.toString(),
			bodyPropertiesMap.containsKey("name"));

		Map<String, ?> nextMap = (Map<String, ?>)bodyPropertiesMap.get("next");

		Map<String, ?> nextPropertiesMap = (Map<String, ?>)nextMap.get(
			"properties");

		Assert.assertFalse(
			nextPropertiesMap.toString(),
			nextPropertiesMap.containsKey("name"));
		Assert.assertTrue(
			nextPropertiesMap.toString(),
			nextPropertiesMap.containsKey("next"));

		bodyMap = _getBodyMap(Collections.singleton("next"), "postLevel");

		bodyPropertiesMap = (Map<String, ?>)bodyMap.get("properties");

		Assert.assertFalse(
			bodyPropertiesMap.toString(),
			bodyPropertiesMap.containsKey("next"));
		Assert.assertTrue(
			bodyPropertiesMap.toString(),
			bodyPropertiesMap.containsKey("name"));

		bodyMap = _getBodyMap(Collections.singleton("string"), "postBatch");

		Map<String, ?> bodyItemsMap = (Map<String, ?>)bodyMap.get("items");

		bodyPropertiesMap = (Map<String, ?>)bodyItemsMap.get("properties");

		Assert.assertFalse(
			bodyPropertiesMap.toString(),
			bodyPropertiesMap.containsKey("string"));
		Assert.assertTrue(
			bodyPropertiesMap.toString(),
			bodyPropertiesMap.containsKey("boolean"));

		// Keyword search

		Map<String, ?> itemsPropertiesMap = _getPropertiesMap(
			Collections.singleton("boolean"), "getItems");

		Assert.assertFalse(
			itemsPropertiesMap.toString(),
			itemsPropertiesMap.containsKey("search"));
		Assert.assertTrue(
			itemsPropertiesMap.toString(),
			itemsPropertiesMap.containsKey("aggregationTerms"));
		Assert.assertTrue(
			itemsPropertiesMap.toString(),
			itemsPropertiesMap.containsKey("filter"));
		Assert.assertTrue(
			itemsPropertiesMap.toString(),
			itemsPropertiesMap.containsKey("sort"));

		itemsPropertiesMap = _getPropertiesMap(
			Collections.emptySet(), "getItems");

		Assert.assertTrue(
			itemsPropertiesMap.toString(),
			itemsPropertiesMap.containsKey("search"));

		Map<String, ?> uploadPropertiesMap = _getPropertiesMap(
			Collections.singleton("string"), "postUpload");

		Assert.assertFalse(
			uploadPropertiesMap.toString(),
			uploadPropertiesMap.containsKey("string"));
		Assert.assertTrue(
			uploadPropertiesMap.toString(),
			uploadPropertiesMap.containsKey("boolean"));
	}

	@Test
	public void testGetToolSummaries() {
		List<ToolSummary> toolSummaries = OpenAPIUtil.getToolSummaries(
			_openAPIJSONObject);

		Assert.assertEquals(toolSummaries.toString(), 16, toolSummaries.size());
		_assertToolSummary(
			"POST /v1.0/levels", "postLevel", toolSummaries.get(0));
		_assertToolSummary(
			"This is the description", "getItem", toolSummaries.get(1));
		_assertToolSummary(
			"PATCH /v1.0/items/{itemId}", "patchItem", toolSummaries.get(2));
		_assertToolSummary(
			"PUT /v1.0/items/{itemId}", "putItem", toolSummaries.get(3));
		_assertToolSummary(
			"POST /v1.0/batches", "postBatch", toolSummaries.get(4));
		_assertToolSummary(
			"POST /v1.0/binaries", "postBinary", toolSummaries.get(5));
		_assertToolSummary(
			"POST /v1.0/no-content", "postNoContent", toolSummaries.get(6));
		_assertToolSummary(
			"POST /v1.0/parents", "postParent", toolSummaries.get(7));
		_assertToolSummary(
			"POST /v1.0/no-schema", "postNoSchema", toolSummaries.get(8));
		_assertToolSummary(
			"This is the summary", "getItemsPage", toolSummaries.get(9));
		_assertToolSummary(
			"POST /v1.0/described", "postDescribed", toolSummaries.get(10));
		_assertToolSummary(
			"POST /v1.0/undescribed", "postUndescribed", toolSummaries.get(11));
		_assertToolSummary(
			"POST /v1.0/empty-content", "postEmptyContent",
			toolSummaries.get(12));
		_assertToolSummary(
			"POST /v1.0/uploads", "postUpload", toolSummaries.get(13));
		_assertToolSummary(
			"This is the summary. This is the description", "getItems",
			toolSummaries.get(14));
		_assertToolSummary(
			"POST /v1.0/items", "postItem", toolSummaries.get(15));

		AssertUtils.assertFailure(
			IllegalArgumentException.class,
			"OpenAPI document has no \"paths\" object",
			() -> OpenAPIUtil.getToolSummaries(
				JSONFactoryUtil.createJSONObject()));
	}

	private void _assertMultipartContentType(
		VulcanRequestForwarder.Request request) {

		String contentType = request.getContentType();

		Assert.assertNotNull(contentType);
		Assert.assertTrue(
			contentType,
			contentType.startsWith("multipart/form-data; boundary="));
	}

	private void _assertToolSummary(
		String expectedDescription, String expectedName,
		ToolSummary toolSummary) {

		Assert.assertEquals(expectedDescription, toolSummary.getDescription());
		Assert.assertEquals(expectedName, toolSummary.getName());
	}

	private Map<String, ?> _getBodyMap(
		Set<String> restrictFieldNames, String toolName) {

		Map<String, ?> propertiesMap = _getPropertiesMap(
			restrictFieldNames, toolName);

		return (Map<String, ?>)propertiesMap.get("body");
	}

	private FileItem _getFileItem(List<FileItem> fileItems, String fieldName) {
		for (FileItem fileItem : fileItems) {
			if (Objects.equals(fileItem.getFieldName(), fieldName)) {
				return fileItem;
			}
		}

		throw new IllegalArgumentException(
			StringBundler.concat(
				"No part named \"", fieldName, "\" was found in ", fileItems));
	}

	private List<FileItem> _getFileItems(VulcanRequestForwarder.Request request)
		throws Exception {

		FileUpload fileUpload = new FileUpload(new DiskFileItemFactory());

		byte[] body = request.getBody();

		return fileUpload.parseRequest(
			new UploadContext() {

				@Override
				public long contentLength() {
					return body.length;
				}

				@Override
				public String getCharacterEncoding() {
					return StandardCharsets.UTF_8.name();
				}

				@Override
				public int getContentLength() {
					return body.length;
				}

				@Override
				public String getContentType() {
					return request.getContentType();
				}

				@Override
				public InputStream getInputStream() {
					return new ByteArrayInputStream(body);
				}

			});
	}

	private String _getFileItemValue(List<FileItem> fileItems, String name) {
		FileItem fileItem = _getFileItem(fileItems, name);

		return fileItem.getString();
	}

	private Map<String, ?> _getInputSchema(
		JSONObject openAPIJSONObject, String toolName) {

		Tool tool = OpenAPIUtil.getTool(
			true, openAPIJSONObject, null, toolName);

		return tool.getInputSchema();
	}

	private Map<String, ?> _getPropertiesMap(
		Set<String> restrictFieldNames, String toolName) {

		Tool tool = OpenAPIUtil.getTool(
			true, _openAPIJSONObject, restrictFieldNames, toolName);

		Map<String, ?> inputSchemaMap = tool.getInputSchema();

		return (Map<String, ?>)inputSchemaMap.get("properties");
	}

	private String _read(String fileName) throws Exception {
		return StringUtil.read(
			getClass().getResourceAsStream("dependencies/" + fileName));
	}

	private void _testGetRequest(
			String expectedBody, String expectedContentType,
			String expectedMethod, String expectedPathWithQuery,
			JSONObject inputJSONObject, Set<String> restrictFieldNames,
			String toolName)
		throws Exception {

		VulcanRequestForwarder.Request request = OpenAPIUtil.getRequest(
			StringPool.BLANK, null, inputJSONObject, _openAPIJSONObject,
			restrictFieldNames, toolName, null);

		if (expectedBody == null) {
			Assert.assertNull(request.getBody());
			Assert.assertNull(request.getContentType());
		}
		else {
			Assert.assertEquals(
				expectedBody,
				new String(request.getBody(), StandardCharsets.UTF_8));
			Assert.assertEquals(expectedContentType, request.getContentType());
		}

		Assert.assertEquals(expectedMethod, request.getMethod());
		Assert.assertEquals(expectedPathWithQuery, request.getPath());
	}

	private void _testGetRequest(
			String expectedBody, String expectedContentType,
			String expectedMethod, String expectedPathWithQuery,
			JSONObject inputJSONObject, String toolName)
		throws Exception {

		_testGetRequest(
			expectedBody, expectedContentType, expectedMethod,
			expectedPathWithQuery, inputJSONObject, null, toolName);
	}

	private void _testGetRequestFailure(
		String expectedMessage, JSONObject inputJSONObject,
		Set<String> restrictFieldNames, String toolName) {

		AssertUtils.assertFailure(
			RestrictedFieldException.class, expectedMessage,
			() -> OpenAPIUtil.getRequest(
				StringPool.BLANK, null, inputJSONObject, _openAPIJSONObject,
				restrictFieldNames, toolName, null));
	}

	private void _testGetTool(
			String expectedDescription, String expectedSchemaFileName,
			boolean injectVulcanParameters, String toolName)
		throws Exception {

		Tool tool = OpenAPIUtil.getTool(
			injectVulcanParameters, _openAPIJSONObject, null, toolName);

		Assert.assertEquals(expectedDescription, tool.getDescription());
		Assert.assertEquals(toolName, tool.getName());

		JSONAssert.assertEquals(
			_read(expectedSchemaFileName),
			new ObjectMapper(
			).writeValueAsString(
				tool.getInputSchema()
			),
			true);
	}

	private void _testGetTool(
			String expectedDescription, String expectedSchemaFileName,
			String toolName)
		throws Exception {

		_testGetTool(
			expectedDescription, expectedSchemaFileName, true, toolName);
	}

	private JSONObject _openAPIJSONObject;

}