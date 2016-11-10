package net.earthcomputer.meme.util.launcher;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class DummyTypeAdapter
		implements JsonSerializer<DummyTypeAdapter.DummyProperty>, JsonDeserializer<DummyTypeAdapter.DummyProperty> {

	@Override
	public DummyProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return new DummyProperty(json);
	}

	@Override
	public JsonElement serialize(DummyProperty src, Type typeOfSrc, JsonSerializationContext context) {
		return src.getJson();
	}

	public static class DummyProperty {
		private JsonElement json;

		public DummyProperty(JsonElement json) {
			this.json = json;
		}

		public JsonElement getJson() {
			return json;
		}
	}

}
