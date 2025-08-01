/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.exportimport.test.util;

import com.liferay.batch.engine.BatchEngineImportTaskExecutor;
import com.liferay.batch.engine.BatchEngineTaskContentType;
import com.liferay.batch.engine.BatchEngineTaskExecuteStatus;
import com.liferay.batch.engine.BatchEngineTaskOperation;
import com.liferay.batch.engine.constants.BatchEngineImportTaskConstants;
import com.liferay.batch.engine.constants.CreateStrategy;
import com.liferay.batch.engine.model.BatchEngineImportTask;
import com.liferay.batch.engine.service.BatchEngineImportTaskLocalService;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.PortletDataContextFactoryUtil;
import com.liferay.exportimport.kernel.lar.PortletDataHandler;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.petra.function.UnsafeRunnable;
import com.liferay.petra.io.StreamUtil;
import com.liferay.petra.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.petra.lang.SafeCloseable;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.lazy.referencing.LazyReferencingThreadLocal;
import com.liferay.portal.kernel.module.service.Snapshot;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Máté Thurzó
 */
public class ExportImportTestUtil {

	public static PortletDataContext getExportPortletDataContext()
		throws Exception {

		return getExportPortletDataContext(TestPropsValues.getGroupId());
	}

	public static PortletDataContext getExportPortletDataContext(long groupId)
		throws Exception {

		return getExportPortletDataContext(
			TestPropsValues.getCompanyId(), groupId);
	}

	public static PortletDataContext getExportPortletDataContext(
			long companyId, long groupId)
		throws Exception {

		return getExportPortletDataContext(
			companyId, groupId, new HashMap<String, String[]>());
	}

	public static PortletDataContext getExportPortletDataContext(
			long companyId, long groupId, Map<String, String[]> parameterMap)
		throws Exception {

		return getExportPortletDataContext(
			companyId, groupId, parameterMap, null, null);
	}

	public static PortletDataContext getExportPortletDataContext(
			long companyId, long groupId, Map<String, String[]> parameterMap,
			Date startDate, Date endDate)
		throws Exception {

		TestReaderWriter testReaderWriter = new TestReaderWriter();

		Document document = SAXReaderUtil.createDocument();

		Element manifestRootElement = document.addElement("root");

		manifestRootElement.addElement("header");

		testReaderWriter.addEntry("/manifest.xml", document.asXML());

		PortletDataContext portletDataContext =
			PortletDataContextFactoryUtil.createExportPortletDataContext(
				companyId, groupId, parameterMap, startDate, endDate,
				testReaderWriter);

		Element rootElement = SAXReaderUtil.createElement("root");

		portletDataContext.setExportDataRootElement(rootElement);
		portletDataContext.setMissingReferencesElement(
			rootElement.addElement("missing-references"));

		return portletDataContext;
	}

	public static PortletDataContext getImportPortletDataContext()
		throws Exception {

		return getImportPortletDataContext(TestPropsValues.getGroupId());
	}

	public static PortletDataContext getImportPortletDataContext(long groupId)
		throws Exception {

		return getImportPortletPreferences(
			TestPropsValues.getCompanyId(), groupId);
	}

	public static PortletDataContext getImportPortletDataContext(
			long companyId, long groupId, Map<String, String[]> parameterMap)
		throws Exception {

		TestReaderWriter testReaderWriter = new TestReaderWriter();

		Document document = SAXReaderUtil.createDocument();

		Element manifestRootElement = document.addElement("root");

		manifestRootElement.addElement("header");

		testReaderWriter.addEntry("/manifest.xml", document.asXML());

		PortletDataContext portletDataContext =
			PortletDataContextFactoryUtil.createImportPortletDataContext(
				companyId, groupId, parameterMap, new TestUserIdStrategy(),
				testReaderWriter);

		Element rootElement = SAXReaderUtil.createElement("root");

		portletDataContext.setImportDataRootElement(rootElement);
		portletDataContext.setMissingReferencesElement(
			rootElement.addElement("missing-references"));

		return portletDataContext;
	}

	public static PortletDataContext getImportPortletPreferences(
			long companyId, long groupId)
		throws Exception {

		return getImportPortletDataContext(
			companyId, groupId, new HashMap<String, String[]>());
	}

	public static Status importJSONArray(
			long companyId, JSONArray jsonArray, String portletId)
		throws Exception {

		BatchEngineImportTaskLocalService batchEngineImportTaskLocalService =
			_batchEngineImportTaskLocalServiceSnapshot.get();

		String[] nameParts = StringUtil.split(
			_serviceTrackerMap.getService(portletId), StringPool.POUND);

		String taskItemDelegateName = nameParts[1];

		if (Validator.isNull(taskItemDelegateName)) {
			taskItemDelegateName = null;
		}

		BatchEngineImportTask batchEngineImportTask =
			batchEngineImportTaskLocalService.addBatchEngineImportTask(
				RandomTestUtil.randomString(), companyId,
				TestPropsValues.getUserId(), 100, null, nameParts[0],
				_toByteArray(jsonArray.toString()),
				BatchEngineTaskContentType.JSON.toString(),
				BatchEngineTaskExecuteStatus.INITIAL.name(), new HashMap<>(),
				BatchEngineImportTaskConstants.
					IMPORT_STRATEGY_ON_ERROR_CONTINUE,
				BatchEngineTaskOperation.CREATE.name(),
				HashMapBuilder.<String, Serializable>put(
					"createStrategy", CreateStrategy.UPSERT.getDBOperation()
				).build(),
				taskItemDelegateName);

		BatchEngineImportTaskExecutor batchEngineImportTaskExecutor =
			_batchEngineImportTaskExecutorSnapshot.get();

		try (SafeCloseable safeCloseable =
				LazyReferencingThreadLocal.setEnabledWithSafeCloseable(true)) {

			batchEngineImportTaskExecutor.execute(batchEngineImportTask);
		}

		batchEngineImportTask =
			batchEngineImportTaskLocalService.getBatchEngineImportTask(
				batchEngineImportTask.getBatchEngineImportTaskId());

		BatchEngineTaskExecuteStatus batchEngineTaskExecuteStatus =
			BatchEngineTaskExecuteStatus.valueOf(
				batchEngineImportTask.getExecuteStatus());

		if (BatchEngineTaskExecuteStatus.COMPLETED ==
				batchEngineTaskExecuteStatus) {

			return Status.SUCCESS;
		}
		else if (BatchEngineTaskExecuteStatus.FAILED ==
					batchEngineTaskExecuteStatus) {

			return Status.FAILURE;
		}

		throw new IllegalStateException();
	}

	public static void retryAssert(
			long pause, TimeUnit pauseTimeUnit, long timeout,
			TimeUnit timeoutTimeUnit, UnsafeRunnable<Exception> unsafeRunnable)
		throws Exception {

		long deadline =
			System.currentTimeMillis() + timeoutTimeUnit.toMillis(timeout);

		while (true) {
			try {
				unsafeRunnable.run();

				return;
			}
			catch (AssertionError assertionError) {
				if (System.currentTimeMillis() > deadline) {
					throw assertionError;
				}
			}

			Thread.sleep(pauseTimeUnit.toMillis(pause));
		}
	}

	public enum Status {

		FAILURE, SUCCESS

	}

	private static byte[] _toByteArray(String content) throws Exception {
		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				unsyncByteArrayOutputStream)) {

			ZipEntry zipEntry = new ZipEntry("fileName");

			zipOutputStream.putNextEntry(zipEntry);

			StreamUtil.transfer(
				new ByteArrayInputStream(
					content.getBytes(StandardCharsets.UTF_8)),
				zipOutputStream, false);
		}

		return unsyncByteArrayOutputStream.toByteArray();
	}

	private static final Snapshot<BatchEngineImportTaskExecutor>
		_batchEngineImportTaskExecutorSnapshot = new Snapshot<>(
			ExportImportTestUtil.class, BatchEngineImportTaskExecutor.class);
	private static final Snapshot<BatchEngineImportTaskLocalService>
		_batchEngineImportTaskLocalServiceSnapshot = new Snapshot<>(
			ExportImportTestUtil.class,
			BatchEngineImportTaskLocalService.class);
	private static final ServiceTrackerMap<String, String> _serviceTrackerMap;

	static {
		Bundle bundle = FrameworkUtil.getBundle(ExportImportTestUtil.class);

		BundleContext bundleContext = bundle.getBundleContext();

		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, PortletDataHandler.class, null,
			(serviceReference, emitter) -> {
				PortletDataHandler portletDataHandler =
					bundleContext.getService(serviceReference);

				if (portletDataHandler.isBatch()) {
					emitter.emit(portletDataHandler.getPortletId());
				}

				bundleContext.ungetService(serviceReference);
			},
			new ServiceTrackerCustomizer<PortletDataHandler, String>() {

				@Override
				public String addingService(
					ServiceReference<PortletDataHandler> serviceReference) {

					PortletDataHandler portletDataHandler =
						bundleContext.getService(serviceReference);

					return portletDataHandler.getName();
				}

				@Override
				public void modifiedService(
					ServiceReference<PortletDataHandler> serviceReference,
					String string) {
				}

				@Override
				public void removedService(
					ServiceReference<PortletDataHandler> serviceReference,
					String string) {

					bundleContext.ungetService(serviceReference);
				}

			});
	}

}