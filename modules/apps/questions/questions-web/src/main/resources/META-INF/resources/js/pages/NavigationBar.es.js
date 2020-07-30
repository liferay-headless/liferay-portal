/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import ClayLink from '@clayui/link';
import ClayNavigationBar from '@clayui/navigation-bar';
import React, {useContext, useEffect, useState} from 'react';
import {withRouter} from 'react-router-dom';

import {AppContext} from '../AppContext.es';
import useQueryParams from '../hooks/useQueryParams.es';
import {getSections} from '../utils/client.es';
import {historyPushWithSlug, slugToText} from '../utils/utils.es';

export default withRouter(
	({
		history,
		location,
		match: {
			params: {sectionTitle},
		},
	}) => {
		const context = useContext(AppContext);

		const queryParams = useQueryParams(location);

		const [section, setSection] = useState({});

		sectionTitle = sectionTitle || queryParams.get('sectiontitle');

		const isActive = (value) => location.pathname.includes(value);

		const label = () => {
			if (location.pathname.includes('tags')) {
				return Liferay.Language.get('tags');
			}
			else if (location.pathname.includes('activity')) {
				return Liferay.Language.get('my-activity');
			}
			else if (location.pathname.includes('subscriptions')) {
				return Liferay.Language.get('my-subscriptions');
			}

			return Liferay.Language.get('questions');
		};

		const historyPushParser = historyPushWithSlug(history.push);

		useEffect(() => {
			if (sectionTitle) {
				getSections(slugToText(sectionTitle), context.siteKey).then(
					setSection
				);
			}
		}, [sectionTitle, context.siteKey]);

		return (
			<section className="border-bottom questions-section questions-section-nav">
				<div className="questions-container">
					<div className="row">
						{location.pathname !== '/' && (
							<div className="align-items-center col d-flex justify-content-between">
								<ClayNavigationBar
									className="navigation-bar"
									triggerLabel={label()}
								>
									<ClayNavigationBar.Item
										active={
											!isActive('activity') &&
											!isActive('tags') &&
											!isActive('subscriptions')
										}
										onClick={() =>
											historyPushParser(
												section
													? `/questions/${sectionTitle}`
													: '/'
											)
										}
									>
										<ClayLink
											className="nav-link"
											displayType="unstyled"
										>
											{Liferay.Language.get('questions')}
										</ClayLink>
									</ClayNavigationBar.Item>

									<ClayNavigationBar.Item
										active={isActive('tags')}
										onClick={() =>
											historyPushParser(
												section
													? `/questions/${sectionTitle}/tags`
													: '/'
											)
										}
									>
										<ClayLink
											className="nav-link"
											displayType="unstyled"
										>
											{Liferay.Language.get('tags')}
										</ClayLink>
									</ClayNavigationBar.Item>

									<ClayNavigationBar.Item
										active={isActive('subscriptions')}
										className={
											Liferay.ThemeDisplay.isSignedIn()
												? 'ml-md-auto'
												: 'd-none'
										}
										onClick={() =>
											historyPushParser(
												section
													? `/subscriptions/${context.userId}?sectionTitle=${sectionTitle}`
													: '/'
											)
										}
									>
										<ClayLink
											className="nav-link"
											displayType="unstyled"
										>
											{Liferay.Language.get(
												'my-subscriptions'
											)}
										</ClayLink>
									</ClayNavigationBar.Item>

									<ClayNavigationBar.Item
										active={isActive('activity')}
										className={
											Liferay.ThemeDisplay.isSignedIn()
												? ''
												: 'd-none'
										}
										onClick={() =>
											historyPushParser(
												section
													? `/activity/${context.userId}?sectionTitle=${sectionTitle}`
													: '/'
											)
										}
									>
										<ClayLink
											className="nav-link"
											displayType="unstyled"
										>
											{Liferay.Language.get(
												'my-activity'
											)}
										</ClayLink>
									</ClayNavigationBar.Item>
								</ClayNavigationBar>
							</div>
						)}
					</div>
				</div>
			</section>
		);
	}
);
