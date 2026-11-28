/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen} from '@testing-library/react';
import React from 'react';

import '@testing-library/jest-dom';

import SectionTags from '../../../../../../src/main/resources/META-INF/resources/revamp/js/components/forms/content_selector/SectionTags';

const LABELS: Record<string, string> = {
	'x-deletion': '{0} Deletion',
	'x-deletions': '{0} Deletions',
	'x-item': '{0} Item',
	'x-items': '{0} Items',
	'x-of-x-deletions-selected': '{0} of {1} Deletions Selected',
	'x-of-x-items-selected': '{0} of {1} Items Selected',
};

describe('SectionTags', () => {
	beforeEach(() => {
		(Liferay.Language.get as jest.Mock).mockImplementation(
			(key: string) => LABELS[key] ?? key
		);
	});

	afterEach(() => {
		(Liferay.Language.get as jest.Mock).mockImplementation(
			(key: string) => key
		);
	});

	it('shows the selected out of the total additions and deletions', () => {
		render(
			<SectionTags
				additionCount={12}
				deletionCount={3}
				selectedAdditionCount={5}
				selectedDeletionCount={0}
			/>
		);

		expect(screen.getByText('5 of 12 Items Selected')).toBeInTheDocument();
		expect(
			screen.getByText('0 of 3 Deletions Selected')
		).toBeInTheDocument();
	});

	it('shows only the totals when the selected counts are unknown', () => {
		render(<SectionTags additionCount={12} deletionCount={3} />);

		expect(screen.getByText('12 Items')).toBeInTheDocument();
		expect(screen.getByText('3 Deletions')).toBeInTheDocument();
	});

	it('uses singular labels for a single unknown addition and deletion', () => {
		render(<SectionTags additionCount={1} deletionCount={1} />);

		expect(screen.getByText('1 Item')).toBeInTheDocument();
		expect(screen.getByText('1 Deletion')).toBeInTheDocument();
	});

	it('renders nothing without counts or tag', () => {
		const {container} = render(<SectionTags selectedAdditionCount={0} />);

		expect(container).toBeEmptyDOMElement();
	});
});
