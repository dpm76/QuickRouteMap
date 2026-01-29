package com.dpm.quickroutemap.tests;

import com.dpm.quickroutemap.navigation.GeoPointSerializer;
import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.Route;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

public class RouteSerializationTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(IGeoPoint.class, new GeoPointSerializer())
                .create();
    }

    @Test
    public void serialize_RouteObject_UsesUnderscoreNames() {
        IGeoPoint[] wayPoints = new IGeoPoint[]{new GeoPoint(10.0, 20.0)};
        GuidancePoint[] guidancePoints = new GuidancePoint[]{new GuidancePoint("gp1", 10.0, 20.0, "Start")};
        Route route = new Route("key1", "Route1", "Desc1", true, wayPoints, guidancePoints, 100.0, 50.0);

        String json = gson.toJson(route);

        Assert.assertTrue(json.contains("\"_key\":\"key1\""));
        Assert.assertTrue(json.contains("\"_name\":\"Route1\""));
        Assert.assertTrue(json.contains("\"_description\":\"Desc1\""));
        Assert.assertTrue(json.contains("\"_isClosed\":true"));
        Assert.assertTrue(json.contains("\"_totalDistance\":100.0"));
        Assert.assertTrue(json.contains("\"_totalTime\":50.0"));
        Assert.assertTrue(json.contains("\"_wayPoints\":[[10.0,20.0]]"));
        Assert.assertTrue(json.contains("\"_guidancePoints\":[{\"_key\":\"gp1\",\"_point\":[10.0,20.0],\"_narrative\":\"Start\",\"_radius\":500}]"));
    }

    @Test
    public void deserialize_JsonWithUnderscoreNames_PopulatesObject() {
        String json = "{\"_key\":\"k2\",\"_name\":\"N2\",\"_description\":\"D2\",\"_isClosed\":false,\"_totalDistance\":25.5,\"_totalTime\":10.0,\"_wayPoints\":[[5.0,6.0]],\"_guidancePoints\":[{\"_key\":\"g2\",\"_point\":[5.0,6.0],\"_narrative\":\"N2\",\"_radius\":300}]}";

        Route route = gson.fromJson(json, Route.class);

        Assert.assertEquals("k2", route.getKey());
        Assert.assertEquals("N2", route.getName());
        Assert.assertEquals("D2", route.getDescription());
        Assert.assertFalse(route.isClosed());
        Assert.assertEquals(25.5, route.getTotalDistance(), 0.0);
        Assert.assertEquals(10.0, route.getTotalTime(), 0.0);
        Assert.assertEquals(1, route.getWayPoints().size());
        Assert.assertEquals(5.0, route.getWayPoints().get(0).getLatitude(), 0.0);
        Assert.assertEquals(1, route.getGuidancePoints().length);
        Assert.assertEquals("g2", route.getGuidancePoints()[0].getKey());
        Assert.assertEquals(300.0, route.getGuidancePoints()[0].getRadius(), 0.0);
    }
}
