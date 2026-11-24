/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import '@testing-library/jest-dom';
import {render, screen} from '@testing-library/react';
import React from 'react';

import {Launch} from '../../src/main/resources/META-INF/resources/js/api/launches';
import LaunchStatusCell from '../../src/main/resources/META-INF/resources/js/renderers/LaunchStatusCell';

const LAUNCH: Launch = {
	id: 42,
	name: 'Halloween Campaign',
	status: {code: 2},
};

describe('LaunchStatusCell', () => {
	it.each([
		['failed', {scheduledDate: '2030-10-31T13:00:00Z', status: {code: 2}}],
		['in-progress', {status: {code: 2}}],
		['published', {status: {code: 0}}],
		[
			'scheduled',
			{scheduledDate: '2030-10-31T13:00:00Z', status: {code: 7}},
		],
	])('renders the %s label', (label, launch) => {
		render(<LaunchStatusCell itemData={{...LAUNCH, ...launch}} />);

		expect(screen.getByText(label)).toBeInTheDocument();
	});
});
