package com.dpm.quickroutemap.navigation;

import android.content.ContextWrapper;
import android.speech.tts.TextToSpeech;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityTestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.junit.Assert;

/**
 * Created by david on 4/09/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GuidancePointProximityManagerUnitTest extends ActivityTestCase {

    GuidancePointProximityManager _manager = null;

    @Before
    public void setup(){
        IGuidanceProvider guidanceProvider = null;
        TextToSpeech tts = null;
        ContextWrapper context = (ContextWrapper)getInstrumentation().getContext();
        Assert.assertNotNull("Context not created correctly", context);
        _manager = new GuidancePointProximityManager(guidanceProvider, tts, context);
    }

    @After
    public void teardown(){
        _manager.close();
    }

    @Test
    public void createProximityManager(){
        _manager.setRouteGuidance(new GuidancePoint[]{
                new GuidancePoint("test1", 40.0, -3.0, "Ejemplo 1."),
                new GuidancePoint("test2", 40.1, -3.0, "Ejemplo 2."),
                new GuidancePoint("test3", 40.2, -3.0, "Ejemplo 3."),
                new GuidancePoint("test4", 40.3, -3.0, "Ejemplo 4."),
        });
    }

    @Test
    public void setPosition(){
        Assert.fail("No implementado");
    }
}
