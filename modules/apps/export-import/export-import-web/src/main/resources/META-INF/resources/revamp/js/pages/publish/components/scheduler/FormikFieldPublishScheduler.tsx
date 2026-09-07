/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useField} from 'formik';
import React, {useState} from 'react';

import PublishScheduler from './PublishScheduler';
import {isCompleteDateTime} from './cron';
import {ScheduleValues, TimeZoneOption} from './types';
import {getScheduleValuesErrors} from './utils';

export function FormikFieldPublishScheduler({
	name,
	timeZones,
}: {
	name: string;
	timeZones: TimeZoneOption[];
}) {
	const [field, , helpers] = useField<ScheduleValues>(name);

	const [cronExpressionTouched, setCronExpressionTouched] = useState(false);
	const [endDateTimeTouched, setEndDateTimeTouched] = useState(false);
	const [startDateTimeTouched, setStartDateTimeTouched] = useState(false);

	const scheduleValuesErrors = getScheduleValuesErrors(field.value);

	return (
		<PublishScheduler
			cronExpressionErrorMessage={
				cronExpressionTouched
					? scheduleValuesErrors.cronExpression
					: undefined
			}
			endDateTimeErrorMessage={
				endDateTimeTouched ||
				isCompleteDateTime(field.value.endDateTime)
					? scheduleValuesErrors.endDateTime
					: undefined
			}
			onChange={(scheduleValues) => helpers.setValue(scheduleValues)}
			onCronExpressionBlur={() => setCronExpressionTouched(true)}
			onEndDateTimeBlur={() => setEndDateTimeTouched(true)}
			onStartDateTimeBlur={() => setStartDateTimeTouched(true)}
			startDateTimeErrorMessage={
				startDateTimeTouched ||
				isCompleteDateTime(field.value.startDateTime)
					? scheduleValuesErrors.startDateTime
					: undefined
			}
			timeZones={timeZones}
			value={field.value}
		/>
	);
}
