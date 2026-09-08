/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React, {useState} from 'react';

import '@testing-library/jest-dom';

import FieldTimePicker from '../../../../../src/main/resources/META-INF/resources/revamp/js/components/forms/FieldTimePicker';

const user = userEvent.setup({delay: null});

function ControlledFieldTimePicker({
	onChange,
}: {
	onChange: (value: string) => void;
}) {
	const [value, setValue] = useState('');

	return (
		<FieldTimePicker
			label="Time of Day"
			name="time"
			onChange={(newValue) => {
				setValue(newValue);
				onChange(newValue);
			}}
			value={value}
		/>
	);
}

describe('FieldTimePicker', () => {
	it('renders with an initial value', () => {
		render(
			<FieldTimePicker label="Time of Day" name="time" value="09:45" />
		);

		expect(screen.getByLabelText('Time of Day')).toHaveValue('09');
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

	it('reports a complete value once both hours and minutes are typed', async () => {
		const onChange = jest.fn();

		render(<ControlledFieldTimePicker onChange={onChange} />);

		await user.click(screen.getByLabelText('Time of Day'));
		await user.keyboard('09');

		expect(onChange).toHaveBeenLastCalledWith('09:--');

		await user.keyboard('{Tab}45');

		expect(onChange).toHaveBeenLastCalledWith('09:45');
	});

	it('calls onBlur once focus leaves the whole field', async () => {
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

	it('does not call onBlur when tabbing from hours to minutes', async () => {
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
		await user.keyboard('{Tab}');

		expect(onBlur).not.toHaveBeenCalled();

		await user.click(screen.getByRole('button', {name: 'outside'}));

		expect(onBlur).toHaveBeenCalledTimes(1);
	});
});
