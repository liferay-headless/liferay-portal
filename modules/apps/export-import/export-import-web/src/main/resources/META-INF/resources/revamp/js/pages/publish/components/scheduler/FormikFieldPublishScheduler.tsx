/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useField} from 'formik';
import React from 'react';

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

	const [, cronExpressionMeta, cronExpressionHelpers] = useField<string>(
		`${name}.cronExpression`
	);
	const [, endDateTimeMeta, endDateTimeHelpers] = useField<string>(
		`${name}.endDateTime`
	);
	const [, repeatOnTimeMeta, repeatOnTimeHelpers] = useField<string>(
		`${name}.repeatOnTime`
	);
	const [, startDateTimeMeta, startDateTimeHelpers] = useField<string>(
		`${name}.startDateTime`
	);

	const scheduleValuesErrors = getScheduleValuesErrors(field.value);

	return (
		<PublishScheduler
			cronExpressionErrorMessage={
				cronExpressionMeta.touched
					? scheduleValuesErrors.cronExpression
					: undefined
			}
			endDateTimeErrorMessage={
				endDateTimeMeta.touched ||
				isCompleteDateTime(field.value.endDateTime)
					? scheduleValuesErrors.endDateTime
					: undefined
			}
			onChange={(scheduleValues) => helpers.setValue(scheduleValues)}
			onCronExpressionBlur={() => cronExpressionHelpers.setTouched(true)}
			onEndDateTimeBlur={() => endDateTimeHelpers.setTouched(true)}
			onRepeatOnTimeBlur={() => repeatOnTimeHelpers.setTouched(true)}
			onStartDateTimeBlur={() => startDateTimeHelpers.setTouched(true)}
			repeatOnTimeErrorMessage={
				repeatOnTimeMeta.touched
					? scheduleValuesErrors.repeatOnTime
					: undefined
			}
			startDateTimeErrorMessage={
				startDateTimeMeta.touched ||
				isCompleteDateTime(field.value.startDateTime)
					? scheduleValuesErrors.startDateTime
					: undefined
			}
			timeZones={timeZones}
			value={field.value}
		/>
	);
}
