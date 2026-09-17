/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {fireEvent, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';

import '@testing-library/jest-dom';

import FieldTimePicker from '../../../../../src/main/resources/META-INF/resources/revamp/js/components/forms/FieldTimePicker';

const user = userEvent.setup({delay: null});

describe('FieldTimePicker', () => {
	it('renders with an initial value', () => {
		render(
			<FieldTimePicker label="Time of Day" name="time" value="09:45" />
		);

		expect(screen.getByLabelText('Time of Day')).toHaveValue('09:45');
	});

	it('renders with an error message', () => {
		const errorMessage = 'This field is required';

		render(
			<FieldTimePicker
				errorMessage={errorMessage}
				label="Time of Day"
				name="time"
			/>
		);

		expect(screen.getByText(errorMessage)).toBeInTheDocument();
		expect(screen.getByLabelText('Time of Day')).toHaveAttribute(
			'aria-invalid',
			'true'
		);
	});

	it('renders with a help message', () => {
		const helpMessage = 'Uses the server time zone';

		render(
			<FieldTimePicker
				helpMessage={helpMessage}
				label="Time of Day"
				name="time"
			/>
		);

		expect(screen.getByText(helpMessage)).toBeInTheDocument();
		expect(screen.getByLabelText('Time of Day')).toHaveAttribute(
			'aria-describedby',
			'timefieldFeedback'
		);
	});

	it('renders as disabled when disabled prop is true', () => {
		render(
			<FieldTimePicker
				disabled
				label="Time of Day"
				name="time"
				value="09:45"
			/>
		);

		expect(screen.getByLabelText('Time of Day')).toBeDisabled();
	});

	it('reports the new value on change', () => {
		const onChange = jest.fn();

		render(
			<FieldTimePicker
				label="Time of Day"
				name="time"
				onChange={onChange}
				value="09:45"
			/>
		);

		fireEvent.change(screen.getByLabelText('Time of Day'), {
			target: {value: '10:30'},
		});

		expect(onChange).toHaveBeenCalledWith('10:30');
	});

	it('calls onBlur once the field loses focus', async () => {
		const onBlur = jest.fn();

		render(
			<>
				<FieldTimePicker
					label="Time of Day"
					name="time"
					onBlur={onBlur}
					value="09:45"
				/>

				<button type="button">outside</button>
			</>
		);

		await user.click(screen.getByLabelText('Time of Day'));

		expect(onBlur).not.toHaveBeenCalled();

		await user.click(screen.getByRole('button', {name: 'outside'}));

		expect(onBlur).toHaveBeenCalledTimes(1);
	});
});
