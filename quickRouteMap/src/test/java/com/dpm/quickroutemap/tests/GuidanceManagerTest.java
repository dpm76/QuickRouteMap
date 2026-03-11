package com.dpm.quickroutemap.tests;

import com.dpm.quickroutemap.navigation.GuidanceManager;
import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.IGuidanceConsumer;
import com.dpm.quickroutemap.navigation.Route;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class GuidanceManagerTest {

    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
       Field instance = GuidanceManager.class.getDeclaredField("_instance");
       instance.setAccessible(true);
       instance.set(null, null);
    }

    @Test
    public void getInstance_ReturnsSameInstance() {
        GuidanceManager instance1 = GuidanceManager.getInstance();
        GuidanceManager instance2 = GuidanceManager.getInstance();
        Assert.assertSame(instance1, instance2);
    }

    @Test
    public void setCurrentRouteGuidance_WithConsumer_NotifiesConsumer() {
        GuidanceManager manager = GuidanceManager.getInstance();
        IGuidanceConsumer consumer = Mockito.mock(IGuidanceConsumer.class);
        GuidancePoint[] points = new GuidancePoint[]{new GuidancePoint("k1", 1, 1, "n1")};
        
        manager.setConsumer(consumer);
        manager.setCurrentRouteGuidance(points);
        
        Mockito.verify(consumer).setCurrentRouteGuidance(points);
        Assert.assertArrayEquals(points, manager.getCurrentRouteGuidance());
    }

    @Test
    public void setCurrentRouteGuidance_WithoutConsumer_DoesNotThrow() {
        GuidanceManager manager = GuidanceManager.getInstance();
        GuidancePoint[] points = new GuidancePoint[]{new GuidancePoint("k1", 1, 1, "n1")};
        
        manager.setCurrentRouteGuidance(points);
        
        Assert.assertArrayEquals(points, manager.getCurrentRouteGuidance());
    }

    @Test
    public void setCurrentRoute_StoresAndReturnsRoute() {
        GuidanceManager manager = GuidanceManager.getInstance();
        Route route = new Route();
        route.setName("Test Route");
        route.setGuidancePoints(new GuidancePoint[0]);
        
        manager.setCurrentRoute(route);
        
        Assert.assertSame(route, manager.getCurrentRoute());
        Assert.assertNotNull(manager.getCurrentRouteGuidance());
    }

    @Test
    public void setCurrentRoute_WithConsumer_NotifiesConsumer() {
        GuidanceManager manager = GuidanceManager.getInstance();
        IGuidanceConsumer consumer = Mockito.mock(IGuidanceConsumer.class);
        Route route = new Route();
        GuidancePoint[] points = new GuidancePoint[]{new GuidancePoint("k1", 1, 1, "n1")};
        route.setGuidancePoints(points);
        
        manager.setConsumer(consumer);
        manager.setCurrentRoute(route);
        
        Mockito.verify(consumer).setCurrentRouteGuidance(points);
        Assert.assertSame(route, manager.getCurrentRoute());
    }
}
