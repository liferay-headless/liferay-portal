/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen} from '@testing-library/react';
import React from 'react';

import '@testing-library/jest-dom';

import ContentSection from '../../../../../../src/main/resources/META-INF/resources/revamp/js/components/forms/content_selector/ContentSection';
import {PortletDataHandlerSection} from '../../../../../../src/main/resources/META-INF/resources/revamp/js/types/portletDataHandler';
import {SECTION_KEY_CONTENT} from '../../../../../../src/main/resources/META-INF/resources/revamp/js/utils/contentSelection';

function getPortletDataHandlerSection(
	key: string,
	counts: {additionCount?: number; deletionCount?: number} = {}
): PortletDataHandlerSection {
	return {
		...counts,
		key,
		label: key,
		portletDataHandlers: [{label: 'Handler', name: 'handler'}],
	};
}

const portletDataHandlerSection: PortletDataHandlerSection = {
	key: SECTION_KEY_CONTENT,
	label: SECTION_KEY_CONTENT,
	portletDataHandlers: [
		{
			label: 'Handler',
			name: 'handler',
			portletDataHandlerControls: [
				{label: 'Child 1', name: 'child1', type: 'Boolean'},
				{label: 'Child 2', name: 'child2', type: 'Boolean'},
			],
		},
	],
};

describe('ContentSection', () => {
	afterEach(() => {
		(Liferay.Language.get as jest.Mock).mockImplementation(
			(key: string) => key
		);
	});

	it('renders a compact scrollable body for sections in SECTION_KEYS_COMPACT', () => {
		const {container} = render(
			<ContentSection
				onChange={jest.fn()}
				portletDataHandlerSection={getPortletDataHandlerSection(
					'objects'
				)}
				sectionSelection={undefined}
			/>
		);

		expect(
			container.querySelector('.content-section-scroll')
		).not.toBeNull();
	});

	it('renders a regular body for sections not in SECTION_KEYS_COMPACT', () => {
		const {container} = render(
			<ContentSection
				onChange={jest.fn()}
				portletDataHandlerSection={getPortletDataHandlerSection(
					'web-content'
				)}
				sectionSelection={undefined}
			/>
		);

		expect(container.querySelector('.content-section-scroll')).toBeNull();
	});

	it('shows the selected out of the total additions and deletions', () => {
		(Liferay.Language.get as jest.Mock).mockImplementation(
			(key: string) =>
				({
					'x-of-x-deletions-selected':
						'{0} of {1} Deletions Selected',
					'x-of-x-items-selected': '{0} of {1} Items Selected',
				})[key] ?? key
		);

		render(
			<ContentSection
				onChange={jest.fn()}
				portletDataHandlerSection={{
					additionCount: 6,
					deletionCount: 2,
					key: SECTION_KEY_CONTENT,
					label: SECTION_KEY_CONTENT,
					portletDataHandlers: [
						{
							additionCount: 4,
							deletionCount: 2,
							label: 'Handler 1',
							name: 'handler1',
						},
						{
							additionCount: 2,
							deletionCount: 0,
							label: 'Handler 2',
							name: 'handler2',
						},
					],
				}}
				sectionSelection={{handler1: true}}
				showDeletions
			/>
		);

		expect(screen.getByText('4 of 6 Items Selected')).toBeInTheDocument();
		expect(screen.getByText('4 of 4 Items Selected')).toBeInTheDocument();
		expect(screen.getByText('0 of 2 Items Selected')).toBeInTheDocument();
		expect(screen.getAllByText('2 of 2 Deletions Selected')).toHaveLength(
			2
		);
	});

	it('hides the deletion count label when deletions are not shown', () => {
		const {queryByText} = render(
			<ContentSection
				onChange={jest.fn()}
				portletDataHandlerSection={getPortletDataHandlerSection(
					'web-content',
					{additionCount: 1, deletionCount: 1}
				)}
				sectionSelection={undefined}
			/>
		);

		expect(queryByText('x-of-x-items-selected')).not.toBeNull();
		expect(queryByText('x-of-x-deletions-selected')).toBeNull();
	});

	it('renders the handler addition and deletion counts', () => {
		const {queryByText} = render(
			<ContentSection
				onChange={jest.fn()}
				portletDataHandlerSection={{
					key: SECTION_KEY_CONTENT,
					label: SECTION_KEY_CONTENT,
					portletDataHandlers: [
						{
							additionCount: 2,
							deletionCount: 1,
							label: 'Handler',
							name: 'handler',
						},
					],
				}}
				sectionSelection={undefined}
				showDeletions
			/>
		);

		expect(queryByText('x-of-x-items-selected')).not.toBeNull();
		expect(queryByText('x-of-x-deletions-selected')).not.toBeNull();
	});

	it('hides the comments and ratings footer when nothing is selected', () => {
		const {queryByText} = render(
			<ContentSection
				commentsAndRatingsEnabled
				onChange={jest.fn()}
				portletDataHandlerSection={portletDataHandlerSection}
				sectionSelection={undefined}
			/>
		);

		expect(queryByText('comments-and-ratings')).toBeNull();
	});

	it('shows the comments and ratings footer when a handler is fully selected', () => {
		const {queryByText} = render(
			<ContentSection
				commentsAndRatingsEnabled
				onChange={jest.fn()}
				portletDataHandlerSection={portletDataHandlerSection}
				sectionSelection={{handler: {child1: true, child2: true}}}
			/>
		);

		expect(queryByText('comments-and-ratings')).not.toBeNull();
	});

	it('shows the comments and ratings footer when a lower-level node is selected', () => {
		const {queryByText} = render(
			<ContentSection
				commentsAndRatingsEnabled
				onChange={jest.fn()}
				portletDataHandlerSection={portletDataHandlerSection}
				sectionSelection={{handler: {child1: true}}}
			/>
		);

		expect(queryByText('comments-and-ratings')).not.toBeNull();
	});

	it('hides the comments and ratings footer when comments and ratings are disabled', () => {
		const {queryByText} = render(
			<ContentSection
				commentsAndRatingsEnabled={false}
				onChange={jest.fn()}
				portletDataHandlerSection={portletDataHandlerSection}
				sectionSelection={{handler: {child1: true, child2: true}}}
			/>
		);

		expect(queryByText('comments-and-ratings')).toBeNull();
	});
});
