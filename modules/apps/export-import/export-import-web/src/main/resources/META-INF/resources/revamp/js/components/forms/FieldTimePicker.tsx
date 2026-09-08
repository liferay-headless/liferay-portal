/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayTimePicker, {Input} from '@clayui/time-picker';
import {FieldBase} from 'frontend-js-components-web';
import React from 'react';

const DEFAULT_SEGMENT = '--';

export type FieldTimePickerProps = {
	disabled?: boolean;
	errorMessage?: string;
	id?: string;
	label: string;
	name?: string;
	onBlur?: () => void;
	onChange?: (value: string) => void;
	required?: boolean;
	value?: string;
};

function toInput(value: string): Input {
	const [hours, minutes] = value.split(':');

	return {
		hours: hours || DEFAULT_SEGMENT,
		minutes: minutes || DEFAULT_SEGMENT,
	};
}

function toValue(input: Input): string {
	return `${input.hours}:${input.minutes}`;
}

const FieldTimePicker = ({
	disabled,
	errorMessage,
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
			id={fieldId}
			label={label}
			required={required}
		>
			<div
				onBlur={(event) => {
					if (
						!event.currentTarget.contains(
							event.relatedTarget as Node
						)
					) {
						onBlur?.();
					}
				}}
			>
				<ClayTimePicker
					disabled={disabled}
					id={fieldId}
					name={name}
					onChange={(input) => onChange?.(toValue(input))}
					use12Hours={false}
					value={toInput(value)}
				/>
			</div>
		</FieldBase>
	);
};

export default FieldTimePicker;
