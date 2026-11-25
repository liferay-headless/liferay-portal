/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayIcon from '@clayui/icon';
import ClayLayout from '@clayui/layout';
import ClayLink from '@clayui/link';
import {ReactPortal} from '@liferay/frontend-js-react-web';
import {useFormikContext} from 'formik';
import React, {useState} from 'react';

function Footer({
	actionButtonLabel = Liferay.Language.get('continue'),
	actionButtonSymbol,
	backURL,
	continueDisabled,
	onPrevious,
}: {
	actionButtonLabel?: string;
	actionButtonSymbol?: string;
	backURL: string;
	continueDisabled?: boolean;
	onPrevious?: () => void;
}) {
	const {isSubmitting, isValid, status, submitForm} = useFormikContext();

	const [headerActionsElement] = useState(() =>
		document.querySelector('.export-import-header-actions')
	);

	const actionButtonDisabled =
		(continueDisabled ?? !isValid) || isSubmitting || status?.submitted;

	const actionButtonContent = (
		<>
			{actionButtonSymbol && (
				<span className="inline-item inline-item-before">
					<ClayIcon className="mr-1" symbol={actionButtonSymbol} />
				</span>
			)}

			{actionButtonLabel}
		</>
	);

	const previousButton = onPrevious && (
		<ClayButton borderless displayType="secondary" onClick={onPrevious}>
			<span className="inline-item inline-item-before">
				<ClayIcon symbol="order-arrow-left" />
			</span>

			{Liferay.Language.get('previous')}
		</ClayButton>
	);

	if (headerActionsElement) {
		return (
			<>
				<ReactPortal
					className="d-flex"
					container={headerActionsElement}
				>
					<ClayLink
						borderless
						button
						className="c-mr-2"
						displayType="secondary"
						href={backURL}
						small
					>
						{Liferay.Language.get('cancel')}
					</ClayLink>

					<ClayButton
						disabled={actionButtonDisabled}
						onClick={submitForm}
						size="sm"
					>
						{actionButtonContent}
					</ClayButton>
				</ReactPortal>

				{previousButton && (
					<ClayLayout.SheetFooter className="mt-5">
						{previousButton}
					</ClayLayout.SheetFooter>
				)}
			</>
		);
	}

	return (
		<ClayLayout.SheetFooter className="mt-5">
			<ClayLayout.Row className="flex-fill">
				{previousButton && (
					<ClayLayout.Col lg={4}>{previousButton}</ClayLayout.Col>
				)}

				<ClayLayout.Col lg={onPrevious && 8}>
					<div className="d-flex justify-content-end">
						<ClayLink button displayType="secondary" href={backURL}>
							{Liferay.Language.get('cancel')}
						</ClayLink>

						<ClayButton
							disabled={actionButtonDisabled}
							type="submit"
						>
							{actionButtonContent}
						</ClayButton>
					</div>
				</ClayLayout.Col>
			</ClayLayout.Row>
		</ClayLayout.SheetFooter>
	);
}

export default Footer;
