/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.vulcan.internal.jaxrs.sse;

import com.liferay.portal.vulcan.internal.jaxrs.context.provider.ContextProviderUtil;
import com.liferay.portal.vulcan.jaxrs.sse.SseEventSinkDecoratorFactory;

import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.SseEventSink;

import java.util.concurrent.CompletionStage;

import org.apache.cxf.jaxrs.utils.JAXRSUtils;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alejandro Tardín
 */
@Component(service = SseEventSinkDecoratorFactory.class)
public class SseEventSinkDecoratorFactoryImpl
	implements SseEventSinkDecoratorFactory {

	@Override
	public SseEventSink decorate(SseEventSink sseEventSink) {
		return new SseEventSink() {

			@Override
			public void close() {
				try {
					sseEventSink.close();
				}
				finally {
					ContextProviderUtil.releaseResourceInstance(
						JAXRSUtils.getContextMessage(
							JAXRSUtils.getCurrentMessage()));
				}
			}

			@Override
			public boolean isClosed() {
				return sseEventSink.isClosed();
			}

			@Override
			public CompletionStage<?> send(OutboundSseEvent outboundSseEvent) {
				return sseEventSink.send(outboundSseEvent);
			}

		};
	}

}