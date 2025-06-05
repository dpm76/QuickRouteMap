package com.dpm.quickroutemap.tests;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.Manifest;
import android.content.ContextWrapper;
import android.speech.tts.TextToSpeech;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.GuidancePointProximityManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * Created by david on 4/09/2017.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GuidancePointProximityManagerAndroidTest {

    GuidancePointProximityManager _manager = null;

    @Rule
    public GrantPermissionRule locationPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void setup(){
        getInstrumentation().runOnMainSync(() -> {
            ContextWrapper context = new ContextWrapper(getInstrumentation().getTargetContext());
            Assert.assertNotNull("Context not created correctly", context);
            TextToSpeech textToSpeechMock = Mockito.mock(TextToSpeech.class);
            _manager = new GuidancePointProximityManager(null, textToSpeechMock, context);
        });
    }

    @After
    public void teardown(){
        _manager.close();
    }

    @Test
    public void createProximityManager(){

        GuidancePoint[] guidancePoints = new GuidancePoint[]{
                new GuidancePoint("test1", 40.0, -3.0, "Test 1."),
                new GuidancePoint("test2", 40.1, -3.0, "Test 2."),
                new GuidancePoint("test3", 40.2, -3.0, "Test 3."),
                new GuidancePoint("test4", 40.3, -3.0, "Test 4."),
        };

        _manager.setRouteGuidance(guidancePoints);

        Assert.assertEquals(guidancePoints.length, _manager.countProximityAlerts());
    }

    @Test
    public void setPosition(){
        Assert.fail("Not yet implemented");
    }
}
