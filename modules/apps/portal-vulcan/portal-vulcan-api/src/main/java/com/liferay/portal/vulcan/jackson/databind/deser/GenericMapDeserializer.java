package com.liferay.portal.vulcan.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GenericMapDeserializer<T> extends StdDeserializer<Map<String, T>>
	implements ContextualDeserializer {

	private Class<T> clazz;

	// Default constructor needed for Jackson
	public GenericMapDeserializer() {
		super(Map.class);
	}

	public GenericMapDeserializer(Class<T> clazz) {
		super(Map.class);
		this.clazz = clazz;
	}

	@Override
	public Map<String, T> deserialize(JsonParser p, DeserializationContext ctxt)
		throws IOException {
		ObjectMapper mapper = (ObjectMapper) p.getCodec();
		JsonNode node = mapper.readTree(p);
		Map<String, T> result = new HashMap<>();

		Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> entry = fields.next();
			T value = mapper.treeToValue(entry.getValue(), clazz);
			result.put(entry.getKey(), value);
		}
		return result;
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
												BeanProperty property)
		throws JsonMappingException {

		// Extract the actual type of the Map's value from the property
		if (property != null) {
			JavaType mapType = property.getType();
			JavaType valueType = mapType.containedType(1);
			if (valueType != null) {
				return new GenericMapDeserializer<>(valueType.getRawClass());
			}
		}
		return this;
	}
}

