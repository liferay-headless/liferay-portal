/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.internal.background.task;

import com.liferay.exportimport.kernel.model.ExportImportConfiguration;
import com.liferay.portal.kernel.backgroundtask.BackgroundTask;
import com.liferay.portal.kernel.backgroundtask.BackgroundTaskManager;
import com.liferay.portal.kernel.backgroundtask.constants.BackgroundTaskConstants;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.File;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

/**
 * @author Magdalena Jedraszak
 */
public class LayoutSetPrototypeImportBackgroundTaskExecutorTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Before
	public void setUp() throws Exception {
		_backgroundTaskManager = Mockito.mock(BackgroundTaskManager.class);
		_exportImportConfiguration = Mockito.mock(
			ExportImportConfiguration.class);
		_executor = new LayoutSetPrototypeImportBackgroundTaskExecutor();

		Field field =
			LayoutSetPrototypeImportBackgroundTaskExecutor.class.
				getDeclaredField("_backgroundTaskManager");

		field.setAccessible(true);
		field.set(_executor, _backgroundTaskManager);

		Mockito.when(
			_exportImportConfiguration.getGroupId()
		).thenReturn(
			12345L
		);
	}

	@Test
	public void testCleanUpPreviousBackgroundTasks() throws Exception {
		BackgroundTask cancelledBackgroundTask = Mockito.mock(
			BackgroundTask.class);

		Mockito.when(
			cancelledBackgroundTask.getStatus()
		).thenReturn(
			BackgroundTaskConstants.STATUS_CANCELLED
		);

		Mockito.when(
			cancelledBackgroundTask.getBackgroundTaskId()
		).thenReturn(
			1L
		);

		BackgroundTask completedBackgroundTask = Mockito.mock(
			BackgroundTask.class);

		Mockito.when(
			completedBackgroundTask.getStatus()
		).thenReturn(
			BackgroundTaskConstants.STATUS_SUCCESSFUL
		);

		Mockito.when(
			completedBackgroundTask.getBackgroundTaskId()
		).thenReturn(
			2L
		);

		BackgroundTask failedBackgroundTask = Mockito.mock(
			BackgroundTask.class);

		Mockito.when(
			failedBackgroundTask.getStatus()
		).thenReturn(
			BackgroundTaskConstants.STATUS_FAILED
		);

		Mockito.when(
			failedBackgroundTask.getBackgroundTaskId()
		).thenReturn(
			3L
		);

		BackgroundTask inProgressBackgroundTask = Mockito.mock(
			BackgroundTask.class);

		Mockito.when(
			inProgressBackgroundTask.getStatus()
		).thenReturn(
			BackgroundTaskConstants.STATUS_IN_PROGRESS
		);

		Mockito.when(
			inProgressBackgroundTask.getBackgroundTaskId()
		).thenReturn(
			4L
		);

		List<BackgroundTask> backgroundTasks = Arrays.asList(
			completedBackgroundTask, inProgressBackgroundTask,
			cancelledBackgroundTask, failedBackgroundTask);

		Mockito.when(
			_backgroundTaskManager.getBackgroundTasks(
				Mockito.eq(12345L), Mockito.anyString())
		).thenReturn(
			backgroundTasks
		);

		Class<?> innerClass = null;

		for (Class<?> cls :
				LayoutSetPrototypeImportBackgroundTaskExecutor.class.
					getDeclaredClasses()) {

			if (Objects.equals(cls.getSimpleName(), "LayoutImportCallable")) {
				innerClass = cls;

				break;
			}
		}

		if (innerClass == null) {
			throw new RuntimeException("Class not found");
		}

		Constructor<?> constructor = innerClass.getDeclaredConstructor(
			LayoutSetPrototypeImportBackgroundTaskExecutor.class,
			ExportImportConfiguration.class, File.class);

		constructor.setAccessible(true);

		Object layoutImportCallable = constructor.newInstance(
			_executor, _exportImportConfiguration, null);

		Method method = innerClass.getDeclaredMethod(
			"_cleanUpPreviousBackgroundTasks");

		method.setAccessible(true);
		method.invoke(layoutImportCallable);

		Mockito.verify(
			_backgroundTaskManager, Mockito.times(1)
		).deleteBackgroundTask(
			1L
		);

		Mockito.verify(
			_backgroundTaskManager, Mockito.times(1)
		).deleteBackgroundTask(
			2L
		);

		Mockito.verify(
			_backgroundTaskManager, Mockito.times(1)
		).deleteBackgroundTask(
			3L
		);

		Mockito.verify(
			_backgroundTaskManager, Mockito.never()
		).deleteBackgroundTask(
			4L
		);
	}

	private BackgroundTaskManager _backgroundTaskManager;
	private LayoutSetPrototypeImportBackgroundTaskExecutor _executor;
	private ExportImportConfiguration _exportImportConfiguration;

}