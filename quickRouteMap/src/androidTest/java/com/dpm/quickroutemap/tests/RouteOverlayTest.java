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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import android.graphics.Point;

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
        // Ensure route has points so draw() proceeds
        java.util.List<IGeoPoint> points = new java.util.ArrayList<>();
        points.add(_geoPoint);
        points.add(_geoPoint);
        when(_route.getWayPoints()).thenReturn(points);
        
        // Mock toPixels to simply return the inputs as offsets or fixed values
        // For our test, we want to simulate that the geoPoint maps to roughly where we clicked (100, 200)
        doAnswer(new Answer<Point>() {
            @Override
            public Point answer(InvocationOnMock invocation) throws Throwable {
                Point out = (Point) invocation.getArguments()[1];
                if (out == null) out = new Point();
                out.set(100, 200);
                return out;
            }
        }).when(_projection).toPixels(any(IGeoPoint.class), any(Point.class));
    }

    @Test
    public void onSingleTapConfirmed_InEditMode_ShowsContextMenu() {
        MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);

        when(_route.getGuidancePoints()).thenReturn(new GuidancePoint[0]);

        // First tap: Show menu
        boolean result = _routeOverlay.onSingleTapConfirmed(motionEvent, _mapView);

        Assert.assertTrue(result);
        
        // Verify NO point is created yet
        verify(_route, never()).setGuidancePoints(any());
        verify(_mapView).invalidate();
        
        motionEvent.recycle();
    }

    @Test
    public void onContextButton_GuidancePoint_CreatesPoint() {
        // Setup: Show menu first
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);
        
        // Force draw to calculate button positions
        // In this test environment (instrumented), Canvas is available.
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);
        
        // Guidance button should be at shifted position due to left boundary check
        // Original xGuidance = 30. Shifted by 40 -> 70.
        // Y = 100.
        
        MotionEvent tap2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 70f, 100f, 0);
        boolean handled = _routeOverlay.onTouchEvent(tap2, _mapView);
        
        Assert.assertTrue(handled);
        
        ArgumentCaptor<GuidancePoint[]> argument = ArgumentCaptor.forClass(GuidancePoint[].class);
        verify(_route).setGuidancePoints(argument.capture());
        
        GuidancePoint[] capturedPoints = argument.getValue();
        Assert.assertEquals(1, capturedPoints.length);
        Assert.assertEquals(40.0, capturedPoints[0].getLatitude(), 0.00001);
        Assert.assertEquals(-3.0, capturedPoints[0].getLongitude(), 0.00001);

        tap1.recycle();
        tap2.recycle();
    }
    
    @Test
    public void extendRoute_AddsToStart_WhenCloserToStart() {
        // Setup Route: Start(0,0), End(0,10)
        IGeoPoint startPoint = mock(IGeoPoint.class);
        when(startPoint.getLatitude()).thenReturn(0.0);
        when(startPoint.getLongitude()).thenReturn(0.0);
        
        IGeoPoint endPoint = mock(IGeoPoint.class);
        when(endPoint.getLatitude()).thenReturn(0.0);
        when(endPoint.getLongitude()).thenReturn(10.0); // Far away
        
        java.util.List<IGeoPoint> points = new java.util.ArrayList<>();
        points.add(startPoint);
        points.add(endPoint);
        when(_route.getWayPoints()).thenReturn(points);
        
        // Setup New Point: (0, -1) -> Dist to Start=1, Dist to End=11
        IGeoPoint newPoint = mock(IGeoPoint.class);
        when(newPoint.getLatitude()).thenReturn(0.0);
        when(newPoint.getLongitude()).thenReturn(-1.0);
        
        // Click at (100,100) -> returns newPoint
        when(_projection.fromPixels(eq(100), eq(100))).thenReturn(newPoint);
        
        // Trick: set context menu pos to allow button drawing math to work 
        // We simulate that we opened the menu at (100, 100) previously
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);
        
        // Force draw to calc positions (geometry button will be at x+offset)
        // We don't care about exact button position validation here, just that we hit it
        // But to hit it, we need to know where it is.
        // Let's assume standard offset logic applies.
        // xGeometry = 100 + 100/2 + 40/2 = 170.
        
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);
        
        // Tap Geometry Button
        // Menu at (100, 200) due to setUp mock.
        // Y = 200 - 100 = 100.
        // xGeometry = 170.
        
        MotionEvent tap2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 170f, 100f, 0); 
        boolean handled = _routeOverlay.onTouchEvent(tap2, _mapView);
        
        Assert.assertTrue("Should handle touch on geometry button", handled);
        
        ArgumentCaptor<IGeoPoint[]> argument = ArgumentCaptor.forClass(IGeoPoint[].class);
        verify(_route).setWayPoints(argument.capture());
        
        IGeoPoint[] resultPoints = argument.getValue();
        Assert.assertEquals(3, resultPoints.length);
        Assert.assertEquals(newPoint, resultPoints[0]); // Added to START
        Assert.assertEquals(startPoint, resultPoints[1]);
        Assert.assertEquals(endPoint, resultPoints[2]);

        tap1.recycle();
        tap2.recycle();
    }
    
    @Test
    public void extendRoute_AddsToEnd_WhenCloserToEnd() {
        // Setup Route: Start(0,0), End(0,10)
        IGeoPoint startPoint = mock(IGeoPoint.class);
        when(startPoint.getLatitude()).thenReturn(0.0);
        when(startPoint.getLongitude()).thenReturn(0.0);
        
        IGeoPoint endPoint = mock(IGeoPoint.class);
        when(endPoint.getLatitude()).thenReturn(0.0);
        when(endPoint.getLongitude()).thenReturn(10.0); 
        
        java.util.List<IGeoPoint> points = new java.util.ArrayList<>();
        points.add(startPoint);
        points.add(endPoint);
        when(_route.getWayPoints()).thenReturn(points);
        
        // Setup New Point: (0, 11) -> Dist to Start=11, Dist to End=1
        IGeoPoint newPoint = mock(IGeoPoint.class);
        when(newPoint.getLatitude()).thenReturn(0.0);
        when(newPoint.getLongitude()).thenReturn(11.0);
        
        // Click at (100,100) -> returns newPoint
        when(_projection.fromPixels(eq(100), eq(100))).thenReturn(newPoint);
        
        // Open menu
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);
        
        // Draw
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);
        
        // Tap Geometry Button (x=170, y=100)
        MotionEvent tap2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 170f, 100f, 0); 
        boolean handled = _routeOverlay.onTouchEvent(tap2, _mapView);
        
        Assert.assertTrue(handled);
        
        ArgumentCaptor<IGeoPoint[]> argument = ArgumentCaptor.forClass(IGeoPoint[].class);
        verify(_route).setWayPoints(argument.capture());
        
        IGeoPoint[] resultPoints = argument.getValue();
        Assert.assertEquals(3, resultPoints.length);
        Assert.assertEquals(startPoint, resultPoints[0]);
        Assert.assertEquals(endPoint, resultPoints[1]);
        Assert.assertEquals(newPoint, resultPoints[2]); // Added to END

        tap1.recycle();
        tap2.recycle();
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
    @Test
    public void extendRoute_AddsToEmpty_WhenRouteIsEmpty() {
        // Setup Empty Route
        when(_route.getWayPoints()).thenReturn(new java.util.ArrayList<IGeoPoint>());
        
        IGeoPoint newPoint = mock(IGeoPoint.class);
        when(newPoint.getLatitude()).thenReturn(10.0);
        when(newPoint.getLongitude()).thenReturn(10.0);
        
        when(_projection.fromPixels(eq(100), eq(100))).thenReturn(newPoint);
        
        // Show menu
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);
        
        // Draw (required so touches register on buttons)
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);
        
        // Tap Geometry Button
        // Menu at (100, 200) -> Y=100.
        // Geometry button X=170.
        MotionEvent tap2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 170f, 100f, 0);
        boolean handled = _routeOverlay.onTouchEvent(tap2, _mapView);
        
        Assert.assertTrue(handled);
        
        // Verify 1 point added
        ArgumentCaptor<IGeoPoint[]> argument = ArgumentCaptor.forClass(IGeoPoint[].class);
        verify(_route).setWayPoints(argument.capture());
        
        IGeoPoint[] capturedPoints = argument.getValue();
        Assert.assertEquals(1, capturedPoints.length);
        Assert.assertEquals(newPoint, capturedPoints[0]);
        
        tap1.recycle();
        tap2.recycle();
    }

    @Test
    public void onTouchEvent_DismissesMenu_WhenTapOutside() {
        // Show menu first
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);
        
        // Draw
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);
        
        // Button positions:
        // Menu at (100, 200).
        // Buttons at Y = 200 - 100 = 100.
        // xGuidance = 30, xGeometry = 170.
        // Size = 100 (radius 50).
        
        // Tap far away at (500, 500)
        MotionEvent tap2 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 500f, 500f, 0);
        boolean handled = _routeOverlay.onTouchEvent(tap2, _mapView);
        
        // It should return true (consumed) to indicate we handled the dismissal
        Assert.assertTrue(handled);
        
        // Verify NO actions taken
        verify(_route, never()).setGuidancePoints(any()); // No new guidance point
        verify(_route, never()).setWayPoints(any());      // No new route point
        
        // Verify invalidate called to redraw (removing menu)
        verify(_mapView, atLeast(1)).invalidate();
        
        tap1.recycle();
        tap2.recycle();
    }
}
