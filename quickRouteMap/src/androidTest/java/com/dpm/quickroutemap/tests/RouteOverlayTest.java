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
        // For our test, we want to simulate that the geoPoint maps to roughly where we
        // clicked (100, 200)
        doAnswer(new Answer<Point>() {
            @Override
            public Point answer(InvocationOnMock invocation) throws Throwable {
                Point out = (Point) invocation.getArguments()[1];
                if (out == null)
                    out = new Point();
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
    public void onContextButton_GuidancePoint_CreatesPoint_OnActionUp() {
        // Setup: Show menu first
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);

        // Force draw to calculate button positions
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);

        // Buttons at Y = 200 - 100 = 100.
        // xGuidance = 70 (due to boundary shift from (30) to (30+40=70)).

        // Step 1: ACTION_DOWN on button
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 70f, 100f, 0);
        boolean handledDown = _routeOverlay.onTouchEvent(down, _mapView);
        Assert.assertTrue("Should handle DOWN on button", handledDown);

        // Verify NO point created on DOWN
        verify(_route, never()).setGuidancePoints(any());

        // Step 2: ACTION_UP on button
        MotionEvent up = MotionEvent.obtain(0, 50, MotionEvent.ACTION_UP, 70f, 100f, 0);
        boolean handledUp = _routeOverlay.onTouchEvent(up, _mapView);
        Assert.assertTrue("Should handle UP on button", handledUp);

        // Verify point IS created now
        verify(_route).setGuidancePoints(any(GuidancePoint[].class));

        tap1.recycle();
        down.recycle();
        up.recycle();
    }

    @Test
    public void onContextButton_NoExecution_IfReleaseOffset() {
        // Setup: Show menu
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);

        // Step 1: ACTION_DOWN on button (X=70, Y=100)
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 70f, 100f, 0);
        _routeOverlay.onTouchEvent(down, _mapView);

        // Step 2: ACTION_UP far away (X=500, Y=500)
        MotionEvent up = MotionEvent.obtain(0, 50, MotionEvent.ACTION_UP, 500f, 500f, 0);
        boolean handledUp = _routeOverlay.onTouchEvent(up, _mapView);

        // Should NOT execute action
        verify(_route, never()).setGuidancePoints(any());
        // But should still return true because it was a captured gesture
        Assert.assertTrue(handledUp);

        tap1.recycle();
        down.recycle();
        up.recycle();
    }

    @Test
    public void movement_Threshold_PreventsUnintendedJump() {
        // Setup: Point at (100, 200)
        java.util.List<IGeoPoint> points = new java.util.ArrayList<>();
        points.add(_geoPoint);
        when(_route.getWayPoints()).thenReturn(points);

        // ACTION_DOWN on point (radius=20, so 110, 210 is inside)
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 110f, 210f, 0);
        _routeOverlay.onTouchEvent(down, _mapView);

        // ACTION_MOVE below threshold (threshold=25. 110->120 = 10 px)
        MotionEvent moveSmall = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 120f, 210f, 0);
        _routeOverlay.onTouchEvent(moveSmall, _mapView);

        // Verify projection NOT called to update point
        verify(_route, never()).setWayPoints(any());

        // ACTION_MOVE above threshold (110->150 = 40 px)
        MotionEvent moveLarge = MotionEvent.obtain(0, 100, MotionEvent.ACTION_MOVE, 150f, 210f, 0);
        _routeOverlay.onTouchEvent(moveLarge, _mapView);

        // Verify point IS updated now
        verify(_route, atLeastOnce()).setWayPoints(any());

        down.recycle();
        moveSmall.recycle();
        moveLarge.recycle();
    }

    @Test
    public void hitArea_Increased_AllowsDirtyTap() {
        // Setup: Geometry Button at (170, 100). Size=100 (radius=50).
        // Normal bounds: X in [120, 220], Y in [50, 150].
        // Hit area increased by 40: X in [80, 260], Y in [10, 190].

        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);
        android.graphics.Canvas canvas = new android.graphics.Canvas();
        _routeOverlay.draw(canvas, _mapView, false);

        // Tap at (250, 180) -> 30px outside normal visual circle, but inside 40px hit
        // area
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 250f, 180f, 0);
        boolean handled = _routeOverlay.onTouchEvent(down, _mapView);

        Assert.assertTrue("Should register hit within extended hit area", handled);

        // Complete the tap
        MotionEvent up = MotionEvent.obtain(0, 50, MotionEvent.ACTION_UP, 250f, 180f, 0);
        _routeOverlay.onTouchEvent(up, _mapView);

        // verify geometry added (extendRoute called)
        verify(_route).setWayPoints(any(IGeoPoint[].class));

        tap1.recycle();
        down.recycle();
        up.recycle();
    }

    @Test
    public void gestureCapture_ConsumesMove_ToPreventPanning() {
        // ACTION_DOWN on a point
        MotionEvent down = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        boolean handledDown = _routeOverlay.onTouchEvent(down, _mapView);
        Assert.assertTrue(handledDown);

        // Simulate dragging. Even if we haven't crossed threshold yet, it should return
        // true
        MotionEvent move = MotionEvent.obtain(0, 50, MotionEvent.ACTION_MOVE, 110f, 210f, 0);
        boolean handledMove = _routeOverlay.onTouchEvent(move, _mapView);

        Assert.assertTrue("MOVE must be consumed once capture started", handledMove);

        down.recycle();
        move.recycle();
    }

    @Test
    public void onSingleTapConfirmed_ReturnsTrue_IfMenuJustDismissed() {
        // This tests the mechanism that prevents a tap that closes a menu from opening
        // it again elsewhere
        // 1. Show menu
        MotionEvent tap1 = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 100f, 200f, 0);
        _routeOverlay.onSingleTapConfirmed(tap1, _mapView);

        // 2. Click outside to dismiss (in ACTION_DOWN)
        MotionEvent dismiss = MotionEvent.obtain(0, 100, MotionEvent.ACTION_DOWN, 500f, 500f, 0);
        _routeOverlay.onTouchEvent(dismiss, _mapView);

        // 3. System sends onSingleTapConfirmed for that same tap
        MotionEvent tap2 = MotionEvent.obtain(0, 100, MotionEvent.ACTION_DOWN, 500f, 500f, 0);
        boolean result = _routeOverlay.onSingleTapConfirmed(tap2, _mapView);

        Assert.assertTrue("Should consume tap that was used for dismissal", result);
        // MapView.invalidate() is called:
        // 1. by onSingleTapConfirmed (tap1)
        // 2. by onTouchEvent (dismiss)
        verify(_mapView, times(2)).invalidate();

        tap1.recycle();
        dismiss.recycle();
        tap2.recycle();
    }

    @Test
    public void draw_HandlesEmptyWaypoints() {
        // Setup: 0 waypoints
        when(_route.getWayPoints()).thenReturn(new java.util.ArrayList<IGeoPoint>());
        android.graphics.Canvas canvas = mock(android.graphics.Canvas.class);

        // Verify draw doesn't crash
        _routeOverlay.draw(canvas, _mapView, false);

        // Verify projection was NOT called (no waypoints to convert)
        verify(_projection, never()).toPixels(any(IGeoPoint.class), any(Point.class));
    }

    @Test
    public void draw_HandlesSingleWaypoint() {
        // Setup: 1 waypoint
        java.util.List<IGeoPoint> points = new java.util.ArrayList<>();
        points.add(_geoPoint);
        when(_route.getWayPoints()).thenReturn(points);
        android.graphics.Canvas canvas = mock(android.graphics.Canvas.class);

        _routeOverlay.draw(canvas, _mapView, false);

        // Verify projection WAS called for the single point
        verify(_projection).toPixels(eq(_geoPoint), any(Point.class));
        // Verify circle drawn for that point
        verify(canvas).drawCircle(anyFloat(), anyFloat(), anyFloat(), any(android.graphics.Paint.class));
    }
}
