/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ApiHelper from '../../../../src/main/resources/META-INF/resources/revamp/js/services/ApiHelper';

class FakeXMLHttpRequest {
	static current: FakeXMLHttpRequest;

	upload: {onprogress?: (event: Partial<ProgressEvent>) => void} = {};

	open() {}

	send() {
		FakeXMLHttpRequest.current = this;
	}

	setRequestHeader() {}
}

describe('ApiHelper.postFormDataWithProgress', () => {
	beforeEach(() => {
		jest.spyOn(window, 'XMLHttpRequest').mockImplementation(
			() => new FakeXMLHttpRequest() as unknown as XMLHttpRequest
		);
	});

	afterEach(() => {
		jest.restoreAllMocks();
	});

	it('reports 100 only once every byte of the file has been sent', () => {
		const onProgress = jest.fn();

		ApiHelper.postFormDataWithProgress(
			'/o/export-import/v1.0/import-preview',
			new FormData(),
			onProgress
		);

		const {upload} = FakeXMLHttpRequest.current;

		upload.onprogress!({
			lengthComputable: true,
			loaded: 9995,
			total: 10000,
		});

		expect(onProgress).toHaveBeenLastCalledWith(99);

		upload.onprogress!({
			lengthComputable: true,
			loaded: 10000,
			total: 10000,
		});

		expect(onProgress).toHaveBeenLastCalledWith(100);
	});
});
