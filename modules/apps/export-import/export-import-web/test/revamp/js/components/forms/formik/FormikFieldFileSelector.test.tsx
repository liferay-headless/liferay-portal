/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

// eslint-disable-next-line @liferay/portal/no-cross-module-deep-import
import {checkAccessibility} from '@liferay/layout-js-components-web/test/__lib__/index';

import '@testing-library/jest-dom';
import {act, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Formik} from 'formik';
import React from 'react';

import {FormikFieldFileSelector} from '../../../../../../src/main/resources/META-INF/resources/revamp/js/components/forms/formik/FormikFieldFileSelector';
import {RequestResult} from '../../../../../../src/main/resources/META-INF/resources/revamp/js/services/ApiHelper';

const renderFileSelector = () => {
	const upload = {} as {
		reportProgress: (percent: number) => void;
		resolve: (result: RequestResult<unknown>) => void;
	};

	const uploadRequest = jest.fn(
		(_file: File, onProgress: (percent: number) => void) =>
			new Promise<RequestResult<unknown>>((resolve) => {
				upload.reportProgress = onProgress;
				upload.resolve = resolve;
			})
	);

	const {container} = render(
		<Formik initialValues={{fileSelector: undefined}} onSubmit={jest.fn()}>
			<FormikFieldFileSelector
				name="fileSelector"
				uploadRequest={uploadRequest}
				validExtensions=".lar"
			/>
		</Formik>
	);

	return {container, upload};
};

const uploadFile = async () => {
	const user = userEvent.setup();

	await user.upload(
		document.querySelector('input[type="file"]') as HTMLInputElement,
		new File(['test'], 'site.lar', {type: '.lar'})
	);
};

describe('FormikFieldFileSelector', () => {
	it('shows the upload progress while the browser is sending the file', async () => {
		const {upload} = renderFileSelector();

		await uploadFile();

		act(() => {
			upload.reportProgress(40);
		});

		expect(screen.getByText('uploading...')).toBeInTheDocument();
		expect(screen.getByRole('progressbar')).toHaveAttribute(
			'aria-valuenow',
			'40'
		);
	});

	it('shows the validating state instead of a completed bar once the file has been sent and the server has not answered yet', async () => {
		const {upload} = renderFileSelector();

		await uploadFile();

		act(() => {
			upload.reportProgress(100);
		});

		expect(screen.getByText('validating...')).toBeInTheDocument();
		expect(screen.queryByText('uploading...')).not.toBeInTheDocument();
		expect(screen.queryByText(/%$/)).not.toBeInTheDocument();
		expect(screen.getByRole('progressbar')).toHaveAccessibleName(
			'validating'
		);
	});

	it('shows the completed bar when the server answers', async () => {
		const {upload} = renderFileSelector();

		await uploadFile();

		act(() => {
			upload.reportProgress(100);
		});

		await act(async () => {
			upload.resolve({data: {}, error: null});
		});

		expect(screen.getByText('completed...')).toBeInTheDocument();
		expect(screen.getByRole('progressbar')).toHaveAccessibleName(
			'Complete'
		);
	});

	it('has no accessibility violations while validating', async () => {
		const {container, upload} = renderFileSelector();

		await uploadFile();

		act(() => {
			upload.reportProgress(100);
		});

		await checkAccessibility({bestPractices: true, context: container});
	});
});
