/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import React, {useState} from 'react';

import {Wizard, WizardStep} from '../../components/Wizard';
import {ContentSelection} from '../../components/forms/content_selector/ContentSelector';
import {postImportProcess} from '../../services/postImportProcess';
import {ImportPreview} from '../../types/exportImportPreview';
import {DataStrategy, UserIdStrategy} from '../../types/exportImportProcess';
import {Scope} from '../../types/scope';
import {toProcessRequestFlags} from '../../utils/contentSelection';
import {startProcess} from '../../utils/startProcess';
import {toRequestPortletDataHandlers} from '../../utils/toRequestPortletDataHandlers';
import DataSelectionStep from './steps/DataSelectionStep';
import FileSelectionStep from './steps/FileSelectionStep';
import SettingsStep, {SETTINGS_STEP_INITIAL_VALUES} from './steps/SettingsStep';

export function NewImport({
	backURL,
	commentsAndRatingsEnabled = false,
	importFDSName,
	importPreviewAPIURL,
	importProcessAPIURL,
	lookAndFeelEnabled = false,
	scope,
	siteSelectionEnabled = false,
}: {
	backURL: string;
	commentsAndRatingsEnabled?: boolean;
	importFDSName: string;
	importPreviewAPIURL: string;
	importProcessAPIURL: string;
	lookAndFeelEnabled?: boolean;
	scope: Scope;
	siteSelectionEnabled?: boolean;
}) {
	const [importPreview, setImportPreview] = useState<
		ImportPreview | undefined
	>();

	return (
		<Wizard backURL={backURL}>
			<WizardStep
				initialValues={{
					fileSelector: undefined,
					name: '',
				}}
				isStepValid={(values) =>
					values.fileSelector instanceof File &&
					!!values.name.trim() &&
					!!importPreview
				}
				title={Liferay.Language.get('setup')}
			>
				<FileSelectionStep
					importPreviewAPIURL={importPreviewAPIURL}
					setImportPreview={setImportPreview}
				/>
			</WizardStep>

			<WizardStep
				initialValues={{
					contentSelection: undefined,
					deletions: false,
					permissions: false,
					siteExternalReferenceCodes: [],
				}}
				isStepValid={(values) =>
					!!values.contentSelection ||
					!!values.siteExternalReferenceCodes?.length
				}
				title={Liferay.Language.get('data-selection')}
			>
				<DataSelectionStep
					commentsAndRatingsEnabled={commentsAndRatingsEnabled}
					importPreview={importPreview}
					lookAndFeelEnabled={lookAndFeelEnabled}
					siteSelectionEnabled={siteSelectionEnabled}
				/>
			</WizardStep>

			<WizardStep
				actionButton={
					<ClayButton type="submit">
						<span className="inline-item inline-item-before">
							<ClayIcon className="mr-1" symbol="import" />
						</span>

						{Liferay.Language.get('import')}
					</ClayButton>
				}
				initialValues={SETTINGS_STEP_INITIAL_VALUES}
				onSubmit={(values, formikHelpers) => {
					if (!importPreview) {
						Liferay.Util.openToast({
							message: Liferay.Language.get(
								'an-unexpected-error-occurred'
							),
							type: 'danger',
						});

						return;
					}

					const contentSelection = values.contentSelection as
						| ContentSelection
						| undefined;

					return startProcess({
						fdsName: importFDSName,
						formikHelpers,
						message: Liferay.Language.get(
							'the-process-has-started'
						),
						request: postImportProcess({
							importProcessRequest: {
								...toProcessRequestFlags(contentSelection),
								dataStrategy:
									values.dataStrategy as DataStrategy,
								deletions: !!values.deletions,
								name: values.name,
								permissions: !!values.permissions,
								requestPortletDataHandlers:
									toRequestPortletDataHandlers(
										importPreview.previewPortletDataHandlerSections ??
											[],
										values.contentSelection
									),
								siteExternalReferenceCodes:
									values.siteExternalReferenceCodes,
								userIdStrategy:
									values.userIdStrategy as UserIdStrategy,
							},
							url: importProcessAPIURL,
						}),
						url: backURL,
					});
				}}
				title={Liferay.Language.get('settings')}
			>
				<SettingsStep scope={scope} />
			</WizardStep>
		</Wizard>
	);
}
