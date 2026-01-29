package com.dpm.quickroutemap.tests;

import com.dpm.quickroutemap.navigation.Route;
import com.dpm.quickroutemap.navigation.RouteUtils;

import org.junit.Assert;
import org.junit.Test;

public class RouteUtilsTest {

    @Test
    public void populateMissingProperties_AllMissing_PopulatesDefaults() {
        Route route = new Route();
        String fallbackName = "Fallback";

        RouteUtils.populateMissingProperties(route, fallbackName);

        Assert.assertNotNull(route.getKey());
        Assert.assertEquals(8, route.getKey().length());
        Assert.assertEquals("Fallback", route.getName());
        Assert.assertEquals("", route.getDescription());
    }

    @Test
    public void populateMissingProperties_SomePresent_PreservesValues() {
        Route route = new Route();
        route.setKey("existing_key");
        route.setName("existing_name");
        route.setDescription("existing_description");

        RouteUtils.populateMissingProperties(route, "ignored");

        Assert.assertEquals("existing_key", route.getKey());
        Assert.assertEquals("existing_name", route.getName());
        Assert.assertEquals("existing_description", route.getDescription());
    }

    @Test
    public void populateMissingProperties_NullDescription_SetsEmpty() {
        Route route = new Route();
        route.setDescription(null);

        RouteUtils.populateMissingProperties(route, "Name");

        Assert.assertEquals("", route.getDescription());
    }
}
