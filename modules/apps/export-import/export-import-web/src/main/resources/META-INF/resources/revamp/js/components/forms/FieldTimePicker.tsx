/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {FieldBase} from 'frontend-js-components-web';
import React from 'react';

export type FieldTimePickerProps = {
	disabled?: boolean;
	errorMessage?: string;
	helpMessage?: string;
	id?: string;
	label: string;
	name?: string;
	onBlur?: () => void;
	onChange?: (value: string) => void;
	required?: boolean;
	value?: string;
};

const FieldTimePicker = ({
	disabled,
	errorMessage,
	helpMessage,
	id,
	label,
	name,
	onBlur,
	onChange,
	required,
	value = '',
}: FieldTimePickerProps) => {
	const fieldId = id ?? name;

	return (
		<FieldBase
			disabled={disabled}
			errorMessage={errorMessage}
			helpMessage={helpMessage}
			id={fieldId}
			label={label}
			required={required}
		>
			<input
				aria-describedby={
					errorMessage || helpMessage
						? `${fieldId}fieldFeedback`
						: undefined
				}
				aria-invalid={!!errorMessage}
				className="form-control"
				disabled={disabled}
				id={fieldId}
				name={name}
				onBlur={onBlur}
				onChange={(event) => onChange?.(event.target.value)}
				type="time"
				value={value}
			/>
		</FieldBase>
	);
};

export default FieldTimePicker;
