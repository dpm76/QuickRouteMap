package com.dpm.quickroutemap.tests;

import com.dpm.quickroutemap.navigation.GuidancePoint;

import org.junit.Assert;
import org.junit.Test;

public class GuidancePointTest {

    @Test
    public void constructor_WithoutRadius_UsesDefaultRadius() {
        GuidancePoint gp = new GuidancePoint("key1", 40.0, -3.0, "Narrative");
        
        Assert.assertEquals("key1", gp.getKey());
        Assert.assertEquals(40.0, gp.getLatitude(), 0.000001);
        Assert.assertEquals(-3.0, gp.getLongitude(), 0.000001);
        Assert.assertEquals("Narrative", gp.getNarrative());
        Assert.assertEquals(500f, gp.getRadius(), 0.0f);
    }

    @Test
    public void constructor_WithRadius_UsesProvidedRadius() {
        GuidancePoint gp = new GuidancePoint("key2", 41.0, -2.0, "Narrative 2", 1000);
        
        Assert.assertEquals("key2", gp.getKey());
        Assert.assertEquals(41.0, gp.getLatitude(), 0.000001);
        Assert.assertEquals(-2.0, gp.getLongitude(), 0.000001);
        Assert.assertEquals("Narrative 2", gp.getNarrative());
        Assert.assertEquals(1000f, gp.getRadius(), 0.0f);
    }
}
