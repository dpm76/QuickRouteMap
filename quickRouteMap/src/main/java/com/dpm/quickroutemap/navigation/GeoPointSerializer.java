/**
 * 
 */
package com.dpm.quickroutemap.navigation;

import java.lang.reflect.Type;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author David
 *
 */
public class GeoPointSerializer implements JsonDeserializer<IGeoPoint>, JsonSerializer<IGeoPoint> {

	@Override
	public IGeoPoint deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		
		JsonArray values = json.getAsJsonArray();
		double latitude = values.get(0).getAsDouble();
		double longitude = values.get(1).getAsDouble();

		return new GeoPoint(latitude, longitude);
	}

	@Override
	public JsonElement serialize(IGeoPoint point, Type type,
			JsonSerializationContext context) {
		
		double latitude = point.getLatitude();
		double longitude = point.getLongitude();

		JsonArray jsonArray = new JsonArray();
		jsonArray.add(new JsonPrimitive(latitude)); 
		jsonArray.add(new JsonPrimitive(longitude));
		
		return jsonArray;
	}

	

}
