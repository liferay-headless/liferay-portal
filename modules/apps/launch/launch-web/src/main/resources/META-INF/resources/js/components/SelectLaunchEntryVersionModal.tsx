/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayLabel from '@clayui/label';
import ClayModal, {useModal} from '@clayui/modal';
import ClayTable from '@clayui/table';
import React, {useState} from 'react';

import {LaunchEntryVersion} from '../api/launches';

const WORKFLOW_STATUS_LABELS: Record<
	number,
	{
		displayType: 'danger' | 'info' | 'secondary' | 'success' | 'warning';
		key: string;
	}
> = {
	0: {displayType: 'success', key: 'approved'},
	1: {displayType: 'info', key: 'pending'},
	2: {displayType: 'secondary', key: 'draft'},
	3: {displayType: 'warning', key: 'expired'},
	4: {displayType: 'danger', key: 'denied'},
	5: {displayType: 'secondary', key: 'inactive'},
	6: {displayType: 'warning', key: 'incomplete'},
	7: {displayType: 'info', key: 'scheduled'},
	8: {displayType: 'secondary', key: 'in-trash'},
};

function StatusLabel({status}: {status: number}) {
	const {displayType, key} = WORKFLOW_STATUS_LABELS[status] ?? {
		displayType: 'secondary',
		key: 'unknown',
	};

	return (
		<ClayLabel displayType={displayType}>
			{Liferay.Language.get(key)}
		</ClayLabel>
	);
}

interface Props {
	launchEntryVersions: LaunchEntryVersion[];
	onClose: () => void;
	onSelect: (classVersion: string) => void;
}

export default function SelectLaunchEntryVersionModal({
	launchEntryVersions,
	onClose: onCloseModal,
	onSelect,
}: Props) {
	const [classVersion, setClassVersion] = useState(
		launchEntryVersions[0]?.classVersion ?? ''
	);

	const {observer, onClose} = useModal({onClose: onCloseModal});

	return (
		<ClayModal observer={observer} size="lg">
			<ClayModal.Header>
				{Liferay.Language.get('select-version')}
			</ClayModal.Header>

			<ClayModal.Body>
				<ClayTable>
					<ClayTable.Head>
						<ClayTable.Row>
							<ClayTable.Cell headingCell />

							<ClayTable.Cell headingCell>
								{Liferay.Language.get('version')}
							</ClayTable.Cell>

							<ClayTable.Cell headingCell>
								{Liferay.Language.get('status')}
							</ClayTable.Cell>

							<ClayTable.Cell headingCell>
								{Liferay.Language.get('author')}
							</ClayTable.Cell>

							<ClayTable.Cell headingCell>
								{Liferay.Language.get('modified')}
							</ClayTable.Cell>
						</ClayTable.Row>
					</ClayTable.Head>

					<ClayTable.Body>
						{launchEntryVersions.map((launchEntryVersion) => (
							<ClayTable.Row
								key={launchEntryVersion.classVersion}
								onClick={() =>
									setClassVersion(
										launchEntryVersion.classVersion
									)
								}
							>
								<ClayTable.Cell>
									<input
										checked={
											classVersion ===
											launchEntryVersion.classVersion
										}
										onChange={() =>
											setClassVersion(
												launchEntryVersion.classVersion
											)
										}
										type="radio"
									/>
								</ClayTable.Cell>

								<ClayTable.Cell>
									{launchEntryVersion.label}
								</ClayTable.Cell>

								<ClayTable.Cell>
									<StatusLabel
										status={launchEntryVersion.status}
									/>
								</ClayTable.Cell>

								<ClayTable.Cell>
									{launchEntryVersion.userName}
								</ClayTable.Cell>

								<ClayTable.Cell>
									{new Date(
										launchEntryVersion.modified
									).toLocaleString()}
								</ClayTable.Cell>
							</ClayTable.Row>
						))}
					</ClayTable.Body>
				</ClayTable>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton displayType="secondary" onClick={onClose}>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={!classVersion}
							onClick={() => {
								onSelect(classVersion);

								onClose();
							}}
						>
							{Liferay.Language.get('add')}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</ClayModal>
	);
}
