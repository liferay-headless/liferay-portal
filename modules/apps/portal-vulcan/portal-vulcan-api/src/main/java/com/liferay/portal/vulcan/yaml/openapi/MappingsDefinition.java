package com.liferay.portal.vulcan.yaml.openapi;

import java.util.Map;

/**
 * @author Matija Petanjek
 */
public class MappingsDefinition {

	public String getEntity() {
		return _entity;
	}

	public void setEntity(String entity) {
		_entity = entity;
	}

	public Map<String, String> getMappings() {
		return _mappings;
	}

	public void setMappings(Map<String, String> mappings) {
		_mappings = mappings;
	}

	public String getPrimaryKey() {
		return _primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		_primaryKey = primaryKey;
	}

//
//	public void setDto(Map<String, String> dto) {
//		_dto = dto;
//	}
//
//	public Map<String, String> getDto() {
//		return _dto;
//	}

//	private Map<String, String> _dto;
	private String _entity;
	private String _primaryKey;
	private Map<String, String> _mappings;

}