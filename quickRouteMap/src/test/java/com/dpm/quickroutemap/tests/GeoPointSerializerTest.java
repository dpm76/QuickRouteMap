package com.dpm.quickroutemap.tests;

import com.dpm.quickroutemap.navigation.GeoPointSerializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class GeoPointSerializerTest {

    private GeoPointSerializer serializer;

    @Before
    public void setUp() {
        serializer = new GeoPointSerializer();
    }

    @Test
    public void serialize_ValidPoint_ReturnsJsonArray() {
        IGeoPoint point = new GeoPoint(40.416775, -3.703790);
        JsonElement result = serializer.serialize(point, null, null);

        Assert.assertTrue(result.isJsonArray());
        JsonArray array = result.getAsJsonArray();
        Assert.assertEquals(2, array.size());
        Assert.assertEquals(40.416775, array.get(0).getAsDouble(), 0.000001);
        Assert.assertEquals(-3.703790, array.get(1).getAsDouble(), 0.000001);
    }

    @Test
    public void deserialize_ValidJson_ReturnsGeoPoint() {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(40.416775));
        array.add(new JsonPrimitive(-3.703790));

        IGeoPoint result = serializer.deserialize(array, null, null);

        Assert.assertNotNull(result);
        Assert.assertEquals(40.416775, result.getLatitude(), 0.000001);
        Assert.assertEquals(-3.703790, result.getLongitude(), 0.000001);
    }
}
