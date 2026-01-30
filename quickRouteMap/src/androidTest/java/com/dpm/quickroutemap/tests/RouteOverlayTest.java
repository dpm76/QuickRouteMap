package com.dpm.quickroutemap.tests;

import android.view.MotionEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.api.IGeoPoint;

import com.dpm.quickroutemap.RouteOverlay;
import com.dpm.quickroutemap.navigation.Route;
import com.dpm.quickroutemap.navigation.GuidancePoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class RouteOverlayTest {

    @Mock
    private Route _route;
    @Mock
    private MapView _mapView;
    @Mock
    private Projection _projection;
    @Mock
    private IGeoPoint _geoPoint;

    private RouteOverlay _routeOverlay;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        _routeOverlay = new RouteOverlay(_route, 0, 0f);
        _routeOverlay.setEditMode(true);
        
        when(_mapView.getProjection()).thenReturn(_projection);
        when(_projection.fromPixels(anyInt(), anyInt())).thenReturn(_geoPoint);
        when(_geoPoint.getLatitude()).thenReturn(40.0);
        when(_geoPoint.getLongitude()).thenReturn(-3.0);
    }

    @Test
    public void onSingleTapConfirmed_InEditMode_CreatesGuidancePoint() {
        MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);

        when(_route.getGuidancePoints()).thenReturn(new GuidancePoint[0]);

        boolean result = _routeOverlay.onSingleTapConfirmed(motionEvent, _mapView);

        Assert.assertTrue(result);
        
        ArgumentCaptor<GuidancePoint[]> argument = ArgumentCaptor.forClass(GuidancePoint[].class);
        verify(_route).setGuidancePoints(argument.capture());
        
        GuidancePoint[] capturedPoints = argument.getValue();
        Assert.assertEquals(1, capturedPoints.length);
        Assert.assertEquals(40.0, capturedPoints[0].getLatitude(), 0.00001);
        Assert.assertEquals(-3.0, capturedPoints[0].getLongitude(), 0.00001);
        
        motionEvent.recycle();
    }

    @Test
    public void onSingleTapConfirmed_NotInEditMode_ReturnsFalse() {
        _routeOverlay.setEditMode(false);
        MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        
        boolean result = _routeOverlay.onSingleTapConfirmed(motionEvent, _mapView);
        Assert.assertFalse(result);
        verify(_route, never()).setGuidancePoints(any());
        
        motionEvent.recycle();
    }
}
