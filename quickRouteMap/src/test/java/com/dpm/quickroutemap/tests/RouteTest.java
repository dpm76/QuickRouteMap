package com.dpm.quickroutemap.tests;

import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.Route;

import org.junit.Assert;
import org.junit.Test;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.List;

public class RouteTest {

    @Test
    public void constructorAndGetters_ValidData_ReturnsCorrectValues() {
        String key = "test_key";
        String name = "test_name";
        String description = "test_description";
        boolean isClosed = true;
        IGeoPoint[] wayPoints = new IGeoPoint[]{new GeoPoint(1.0, 2.0), new GeoPoint(3.0, 4.0)};
        GuidancePoint[] guidancePoints = new GuidancePoint[]{new GuidancePoint("g1", 1.0, 2.0, "nar")};
        double totalDistance = 100.5;
        double totalTime = 3600.0;

        Route route = new Route(key, name, description, isClosed, wayPoints, guidancePoints, totalDistance, totalTime);

        Assert.assertEquals(key, route.getKey());
        Assert.assertEquals(name, route.getName());
        Assert.assertEquals(description, route.getDescription());
        Assert.assertTrue(route.isClosed());
        Assert.assertArrayEquals(guidancePoints, route.getGuidancePoints());
        Assert.assertEquals(totalDistance, route.getTotalDistance(), 0.0);
        Assert.assertEquals(totalTime, route.getTotalTime(), 0.0);
        
        List<IGeoPoint> wayPointsList = route.getWayPoints();
        Assert.assertEquals(2, wayPointsList.size());
        Assert.assertEquals(1.0, wayPointsList.get(0).getLatitude(), 0.0);
    }

    @Test
    public void setters_ValidData_UpdatesValues() {
        Route route = new Route();
        route.setKey("new_key");
        route.setName("new_name");
        route.setDescription("new_description");
        route.setClosed(false);
        route.setTotalDistance(50.0);
        route.setTotalTime(120.0);

        Assert.assertEquals("new_key", route.getKey());
        Assert.assertEquals("new_name", route.getName());
        Assert.assertEquals("new_description", route.getDescription());
        Assert.assertFalse(route.isClosed());
        Assert.assertEquals(50.0, route.getTotalDistance(), 0.0);
        Assert.assertEquals(120.0, route.getTotalTime(), 0.0);
    }
}
