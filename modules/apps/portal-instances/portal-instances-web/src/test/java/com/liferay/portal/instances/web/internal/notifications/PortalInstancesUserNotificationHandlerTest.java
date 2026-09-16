/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.instances.web.internal.notifications;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskConstants;
import com.liferay.portal.instances.background.task.constants.PortalInstanceBackgroundTaskExecutorNames;
import com.liferay.portal.instances.constants.PortalInstancesPortletKeys;
import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.model.UserNotificationEventWrapper;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Luis Ortiz
 */
public class PortalInstancesUserNotificationHandlerTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() {
		ReflectionTestUtil.setFieldValue(
			_portalInstancesUserNotificationHandler, "_jsonFactory",
			new JSONFactoryImpl());
		ReflectionTestUtil.setFieldValue(
			_portalInstancesUserNotificationHandler, "_language", _language);
		ReflectionTestUtil.setFieldValue(
			_portalInstancesUserNotificationHandler, "_portal", _portal);

		Mockito.when(
			_portal.getPortletTitle(
				PortalInstancesPortletKeys.PORTAL_INSTANCES, LocaleUtil.US)
		).thenReturn(
			_PORTLET_TITLE
		);

		Mockito.when(
			_serviceContext.getLocale()
		).thenReturn(
			LocaleUtil.US
		);
	}

	@Test
	public void testGetBodyWhenErrorMessageKeyIsNull() throws Exception {
		Mockito.when(
			_language.format(
				LocaleUtil.US, "the-virtual-instance-x-could-not-be-added",
				_WEB_ID, false)
		).thenReturn(
			"The virtual instance " + _WEB_ID + " could not be added."
		);

		Mockito.when(
			_language.get(LocaleUtil.US, "an-unexpected-error-occurred")
		).thenReturn(
			"An unexpected error occurred."
		);

		Assert.assertEquals(
			_getExpectedBody(
				StringBundler.concat(
					"The virtual instance ", _WEB_ID,
					" could not be added. An unexpected error occurred.")),
			_portalInstancesUserNotificationHandler.getBody(
				_createUserNotificationEvent(
					null, BackgroundTaskConstants.STATUS_FAILED,
					PortalInstanceBackgroundTaskExecutorNames.
						ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR),
				_serviceContext));
	}

	@Test
	public void testGetBodyWhenStatusIsFailed() throws Exception {
		Mockito.when(
			_language.format(
				LocaleUtil.US, "the-virtual-instance-x-could-not-be-added",
				_WEB_ID, false)
		).thenReturn(
			"The virtual instance " + _WEB_ID + " could not be added."
		);

		Mockito.when(
			_language.get(LocaleUtil.US, "please-enter-a-valid-web-id")
		).thenReturn(
			"Please enter a valid web ID."
		);

		Assert.assertEquals(
			_getExpectedBody(
				StringBundler.concat(
					"The virtual instance ", _WEB_ID,
					" could not be added. Please enter a valid web ID.")),
			_portalInstancesUserNotificationHandler.getBody(
				_createUserNotificationEvent(
					"please-enter-a-valid-web-id",
					BackgroundTaskConstants.STATUS_FAILED,
					PortalInstanceBackgroundTaskExecutorNames.
						ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR),
				_serviceContext));
	}

	@Test
	public void testGetBodyWhenStatusIsSuccessful() throws Exception {
		Mockito.when(
			_language.format(
				LocaleUtil.US, "the-virtual-instance-x-was-added-successfully",
				_WEB_ID, false)
		).thenReturn(
			"The virtual instance " + _WEB_ID + " was added successfully."
		);

		Assert.assertEquals(
			_getExpectedBody(
				"The virtual instance " + _WEB_ID + " was added successfully."),
			_portalInstancesUserNotificationHandler.getBody(
				_createUserNotificationEvent(
					null, BackgroundTaskConstants.STATUS_SUCCESSFUL,
					PortalInstanceBackgroundTaskExecutorNames.
						ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR),
				_serviceContext));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetBodyWhenTaskExecutorClassNameIsNotSupported()
		throws Exception {

		_portalInstancesUserNotificationHandler.getBody(
			_createUserNotificationEvent(
				null, BackgroundTaskConstants.STATUS_SUCCESSFUL,
				"com.liferay.portal.instances.internal.background.task." +
					"DeletePortalInstanceBackgroundTaskExecutor"),
			_serviceContext);
	}

	@Test
	public void testGetTitle() throws Exception {
		Assert.assertEquals(
			_PORTLET_TITLE,
			_portalInstancesUserNotificationHandler.getTitle(
				_createUserNotificationEvent(
					null, BackgroundTaskConstants.STATUS_SUCCESSFUL,
					PortalInstanceBackgroundTaskExecutorNames.
						ADD_PORTAL_INSTANCE_BACKGROUND_TASK_EXECUTOR),
				_serviceContext));
	}

	private UserNotificationEvent _createUserNotificationEvent(
		String errorMessageKey, int status, String taskExecutorClassName) {

		JSONObject payloadJSONObject = JSONUtil.put(
			PortalInstanceBackgroundTaskConstants.ERROR_MESSAGE_KEY,
			errorMessageKey
		).put(
			PortalInstanceBackgroundTaskConstants.STATUS, status
		).put(
			PortalInstanceBackgroundTaskConstants.TASK_EXECUTOR_CLASS_NAME,
			taskExecutorClassName
		).put(
			PortalInstanceBackgroundTaskConstants.WEB_ID, _WEB_ID
		);

		return new UserNotificationEventWrapper(null) {

			@Override
			public String getPayload() {
				return payloadJSONObject.toString();
			}

		};
	}

	private String _getExpectedBody(String body) {
		return String.format(
			"<div class=\"title\">%s</div><div class=\"body\">%s</div>",
			_PORTLET_TITLE, body);
	}

	private static final String _PORTLET_TITLE = "Virtual Instances";

	private static final String _WEB_ID = "test.com";

	private final Language _language = Mockito.mock(Language.class);
	private final Portal _portal = Mockito.mock(Portal.class);
	private final PortalInstancesUserNotificationHandler
		_portalInstancesUserNotificationHandler =
			new PortalInstancesUserNotificationHandler();
	private final ServiceContext _serviceContext = Mockito.mock(
		ServiceContext.class);

}