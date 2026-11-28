/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {
	PortletDataHandlerBoolean,
	PortletDataHandlerSection,
} from '../../../../src/main/resources/META-INF/resources/revamp/js/types/portletDataHandler';
import {
	CHOICE_NAME_PRIVATE_PAGES,
	CHOICE_NAME_PUBLIC_PAGES,
	CONTROL_NAME_VISIBILITY,
	PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS,
	getLayoutSetCount,
	getSelectedCount,
	getSelectedDeletionCount,
	getSelectedItemsCount,
	getSelectedSectionCount,
	isPrivateLayoutSelected,
	toPortletDataHandlerSections,
	withSelectedLayoutSetCount,
} from '../../../../src/main/resources/META-INF/resources/revamp/js/utils/contentSelection';

function mockLayoutSetSections({
	privateAdditionCount = 5,
	privateDeletionCount = 0,
	publicAdditionCount = 2,
	publicDeletionCount = 0,
}: {
	privateAdditionCount?: number;
	privateDeletionCount?: number;
	publicAdditionCount?: number;
	publicDeletionCount?: number;
} = {}): PortletDataHandlerSection[] {
	return [
		{
			additionCount: publicAdditionCount,
			deletionCount: publicDeletionCount,
			key: 'category.site_administration.build',
			label: 'Site Builder',
			portletDataHandlers: [
				{
					additionCount: publicAdditionCount,
					deletionCount: publicDeletionCount,
					label: 'Static Pages',
					name: PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS,
					portletDataHandlerControls: [
						{
							choices: [
								{
									additionCount: publicAdditionCount,
									deletionCount: publicDeletionCount,
									label: 'Public Pages',
									name: CHOICE_NAME_PUBLIC_PAGES,
								},
								{
									additionCount: privateAdditionCount,
									deletionCount: privateDeletionCount,
									label: 'Private Pages',
									name: CHOICE_NAME_PRIVATE_PAGES,
								},
							],
							label: 'Visibility',
							name: CONTROL_NAME_VISIBILITY,
							type: 'Choice',
						},
					],
				},
			],
		},
	];
}

const privateSelection = {
	'category.site_administration.build': {
		[PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS]: {all: true, privateLayout: true},
	},
};

const publicSelection = {
	'category.site_administration.build': {
		[PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS]: {
			all: true,
			privateLayout: false,
		},
	},
};

describe('contentSelection layout set counts', () => {
	it('reads the public and private addition counts from the choices', () => {
		const portletDataHandlerSections = mockLayoutSetSections();

		expect(getLayoutSetCount(portletDataHandlerSections, false)).toBe(2);
		expect(getLayoutSetCount(portletDataHandlerSections, true)).toBe(5);
	});

	it('reads the public and private deletion counts from the choices', () => {
		const portletDataHandlerSections = mockLayoutSetSections({
			privateDeletionCount: 3,
			publicDeletionCount: 1,
		});

		expect(
			getLayoutSetCount(
				portletDataHandlerSections,
				false,
				'deletionCount'
			)
		).toBe(1);
		expect(
			getLayoutSetCount(portletDataHandlerSections, true, 'deletionCount')
		).toBe(3);
	});

	it('detects whether the private layout set is selected', () => {
		expect(isPrivateLayoutSelected(undefined)).toBe(false);
		expect(isPrivateLayoutSelected(publicSelection)).toBe(false);
		expect(isPrivateLayoutSelected(privateSelection)).toBe(true);
	});

	it('keeps the public counts in the totals when the public set is selected', () => {
		const portletDataHandlerSections = mockLayoutSetSections({
			privateDeletionCount: 3,
			publicDeletionCount: 1,
		});

		expect(
			getSelectedItemsCount(
				10,
				portletDataHandlerSections,
				publicSelection
			)
		).toBe(10);
		expect(
			getSelectedDeletionCount(
				4,
				portletDataHandlerSections,
				publicSelection
			)
		).toBe(4);
	});

	it('swaps in the private counts in the totals when private is selected', () => {
		const portletDataHandlerSections = mockLayoutSetSections({
			privateDeletionCount: 3,
			publicDeletionCount: 1,
		});

		expect(
			getSelectedItemsCount(
				10,
				portletDataHandlerSections,
				privateSelection
			)
		).toBe(13);
		expect(
			getSelectedDeletionCount(
				4,
				portletDataHandlerSections,
				privateSelection
			)
		).toBe(6);
	});

	it('leaves the sections untouched when the public set is selected', () => {
		const portletDataHandlerSections = mockLayoutSetSections({
			privateDeletionCount: 3,
			publicDeletionCount: 1,
		});

		expect(
			withSelectedLayoutSetCount(
				portletDataHandlerSections,
				publicSelection
			)
		).toBe(portletDataHandlerSections);
	});

	it('adjusts the section and handler counts when private is selected', () => {
		const portletDataHandlerSections = mockLayoutSetSections({
			privateDeletionCount: 3,
			publicDeletionCount: 1,
		});

		const [portletDataHandlerSection] = withSelectedLayoutSetCount(
			portletDataHandlerSections,
			privateSelection
		);

		expect(portletDataHandlerSection.additionCount).toBe(5);
		expect(portletDataHandlerSection.deletionCount).toBe(3);

		const portletDataHandler =
			portletDataHandlerSection.portletDataHandlers.find(
				({name}) => name === PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS
			);

		expect(portletDataHandler?.additionCount).toBe(5);
		expect(portletDataHandler?.deletionCount).toBe(3);
	});
});

describe('toPortletDataHandlerSections', () => {
	it('groups the handlers by section in order of first appearance and sums the counts', () => {
		expect(
			toPortletDataHandlerSections([
				{
					additionCount: 2,
					deletionCount: 1,
					name: 'a',
					sectionKey: 'category.content',
					sectionLabel: 'Content',
				},
				{
					additionCount: 3,
					name: 'b',
					sectionKey: 'category.site_administration.build',
					sectionLabel: 'Site Builder',
				},
				{
					additionCount: 4,
					deletionCount: 2,
					name: 'c',
					sectionKey: 'category.content',
					sectionLabel: 'Content',
				},
			])
		).toEqual([
			{
				additionCount: 6,
				deletionCount: 3,
				key: 'category.content',
				label: 'Content',
				portletDataHandlers: [
					expect.objectContaining({name: 'a'}),
					expect.objectContaining({name: 'c'}),
				],
			},
			{
				additionCount: 3,
				deletionCount: 0,
				key: 'category.site_administration.build',
				label: 'Site Builder',
				portletDataHandlers: [expect.objectContaining({name: 'b'})],
			},
		]);
	});
});

const portletDataHandlerBoolean: PortletDataHandlerBoolean = {
	additionCount: 10,
	deletionCount: 4,
	label: 'Documents and Media',
	name: 'documents',
	portletDataHandlerControls: [
		{
			additionCount: 6,
			deletionCount: 3,
			label: 'Documents',
			name: 'files',
			type: 'Boolean',
		},
		{
			additionCount: 4,
			deletionCount: 1,
			label: 'Folders',
			name: 'folders',
			type: 'Boolean',
		},
		{label: 'Previews and Thumbnails', name: 'previews', type: 'Boolean'},
	],
	type: 'Boolean',
};

const nestedPortletDataHandlerBoolean: PortletDataHandlerBoolean = {
	additionCount: 11,
	label: 'Web Content',
	name: 'journal',
	portletDataHandlerControls: [
		{
			label: 'Content',
			name: 'content',
			portletDataHandlerControls: [
				{
					additionCount: 4,
					label: 'Articles',
					name: 'articles',
					type: 'Boolean',
				},
				{
					additionCount: 1,
					label: 'Feeds',
					name: 'feeds',
					type: 'Boolean',
				},
			],
			type: 'Boolean',
		},
		{
			additionCount: 3,
			label: 'Structures',
			name: 'structures',
			portletDataHandlerControls: [
				{
					additionCount: 1,
					label: 'Data Providers',
					name: 'data-providers',
					type: 'Boolean',
				},
				{
					additionCount: 2,
					label: 'Templates',
					name: 'templates',
					type: 'Boolean',
				},
			],
			type: 'Boolean',
		},
	],
	type: 'Boolean',
};

const layoutSetPortletDataHandlerBoolean: PortletDataHandlerBoolean = {
	additionCount: 4,
	deletionCount: 2,
	label: 'Pages',
	name: PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS,
	type: 'Boolean',
};

describe('getSelectedCount', () => {
	it('counts nothing when the control is not selected', () => {
		expect(
			getSelectedCount(
				portletDataHandlerBoolean,
				undefined,
				'additionCount'
			)
		).toBe(0);
	});

	it('counts the total when every option is selected', () => {
		expect(
			getSelectedCount(
				portletDataHandlerBoolean,
				{files: true, folders: true, previews: true},
				'additionCount'
			)
		).toBe(10);
	});

	it('subtracts the counts of the deselected options', () => {
		expect(
			getSelectedCount(
				portletDataHandlerBoolean,
				{folders: true, previews: true},
				'additionCount'
			)
		).toBe(4);
		expect(
			getSelectedCount(
				portletDataHandlerBoolean,
				{folders: true, previews: true},
				'deletionCount'
			)
		).toBe(1);
	});

	it('keeps the total when only an option without counts is deselected', () => {
		expect(
			getSelectedCount(
				portletDataHandlerBoolean,
				{files: true, folders: true},
				'additionCount'
			)
		).toBe(10);
	});

	it('subtracts the deselected options under a group without counts', () => {
		expect(
			getSelectedCount(
				nestedPortletDataHandlerBoolean,
				{
					content: {articles: true},
					structures: {'data-providers': true, 'templates': true},
				},
				'additionCount'
			)
		).toBe(10);
	});

	it('subtracts every option under a deselected nested control', () => {
		expect(
			getSelectedCount(
				nestedPortletDataHandlerBoolean,
				{content: {articles: true, feeds: true}},
				'additionCount'
			)
		).toBe(5);
		expect(
			getSelectedCount(
				nestedPortletDataHandlerBoolean,
				{structures: {'data-providers': true, 'templates': true}},
				'additionCount'
			)
		).toBe(6);
	});

	it('counts only the own items of a nested control', () => {
		const [, structuresPortletDataHandlerControl] =
			nestedPortletDataHandlerBoolean.portletDataHandlerControls!;

		expect(
			getSelectedCount(
				structuresPortletDataHandlerControl,
				{templates: true},
				'additionCount',
				{nested: true}
			)
		).toBe(3);
		expect(
			getSelectedCount(
				structuresPortletDataHandlerControl,
				undefined,
				'additionCount',
				{nested: true}
			)
		).toBe(0);
	});

	it('returns undefined for deselected options when the nested counts do not add up to the total', () => {
		expect(
			getSelectedCount(
				{...portletDataHandlerBoolean, additionCount: 12},
				{folders: true, previews: true},
				'additionCount'
			)
		).toBeUndefined();
		expect(
			getSelectedCount(
				{...portletDataHandlerBoolean, additionCount: 12},
				{files: true, folders: true, previews: true},
				'additionCount'
			)
		).toBe(12);
	});

	it('returns undefined for a control without counts', () => {
		expect(
			getSelectedCount(
				{label: 'Previews', name: 'previews', type: 'Boolean'},
				true,
				'additionCount'
			)
		).toBeUndefined();
	});

	it('counts every page when all pages are selected', () => {
		expect(
			getSelectedCount(
				layoutSetPortletDataHandlerBoolean,
				{all: true, privateLayout: false},
				'additionCount'
			)
		).toBe(4);
	});

	it('counts the pages picked in the page tree', () => {
		expect(
			getSelectedCount(
				layoutSetPortletDataHandlerBoolean,
				{items: ['home'], privateLayout: false, selectionCount: 1},
				'additionCount'
			)
		).toBe(1);
	});

	it('caps the picked pages at the total', () => {
		expect(
			getSelectedCount(
				layoutSetPortletDataHandlerBoolean,
				{
					all: true,
					excludedItems: ['home'],
					privateLayout: false,
					selectionCount: 7,
				},
				'additionCount'
			)
		).toBe(4);
	});

	it('returns undefined for picked pages without a count, with a date filter, or for deletions', () => {
		const layoutSetSelection = {
			items: ['home'],
			privateLayout: false,
			selectionCount: 1,
		};

		expect(
			getSelectedCount(
				layoutSetPortletDataHandlerBoolean,
				{items: ['home'], privateLayout: false},
				'additionCount'
			)
		).toBeUndefined();
		expect(
			getSelectedCount(
				layoutSetPortletDataHandlerBoolean,
				layoutSetSelection,
				'additionCount',
				{dateFiltered: true}
			)
		).toBeUndefined();
		expect(
			getSelectedCount(
				layoutSetPortletDataHandlerBoolean,
				layoutSetSelection,
				'deletionCount'
			)
		).toBeUndefined();
	});
});

describe('getSelectedSectionCount', () => {
	const portletDataHandlerSection: PortletDataHandlerSection = {
		additionCount: 14,
		deletionCount: 6,
		key: 'category.content',
		label: 'Content',
		portletDataHandlers: [
			portletDataHandlerBoolean,
			layoutSetPortletDataHandlerBoolean,
		],
	};

	it('sums the selected counts of the handlers', () => {
		expect(
			getSelectedSectionCount(
				portletDataHandlerSection,
				{
					[PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS]: {
						items: ['home'],
						privateLayout: false,
						selectionCount: 1,
					},
					documents: {folders: true},
				},
				'additionCount'
			)
		).toBe(5);
		expect(
			getSelectedSectionCount(
				portletDataHandlerSection,
				{},
				'additionCount'
			)
		).toBe(0);
	});

	it('returns undefined when a handler count is unknown', () => {
		expect(
			getSelectedSectionCount(
				portletDataHandlerSection,
				{
					[PORTLET_DATA_KEY_LAYOUT_SET_LAYOUTS]: {
						items: ['home'],
						privateLayout: false,
					},
					documents: true,
				},
				'additionCount'
			)
		).toBeUndefined();
	});
});
