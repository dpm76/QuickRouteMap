package com.dpm.quickroutemap.tests;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.content.ContextWrapper;
import android.speech.tts.TextToSpeech;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.GuidancePointProximityManager;
import com.dpm.quickroutemap.navigation.IGuidanceProvider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by david on 4/09/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GuidancePointProximityManagerAndroidTest {

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
                new GuidancePoint("test1", 40.0, -3.0, "Test 1."),
                new GuidancePoint("test2", 40.1, -3.0, "Test 2."),
                new GuidancePoint("test3", 40.2, -3.0, "Test 3."),
                new GuidancePoint("test4", 40.3, -3.0, "Test 4."),
        });
    }

    @Test
    public void setPosition(){
        Assert.fail("Not yet implemented");
    }
}
