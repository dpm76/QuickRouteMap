package com.dpm.quickroutemap.tests;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.dpm.quickroutemap.RouteOverlay;
import com.dpm.quickroutemap.navigation.GuidancePoint;
import com.dpm.quickroutemap.navigation.Route;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class RouteLayerUIOrderTest {

    @Mock
    private Canvas _canvas;
    @Mock
    private MapView _mapView;
    @Mock
    private Projection _projection;
    @Mock
    private Route _route;
    @Mock
    private IGeoPoint _geoPoint;

    private RouteOverlay _routeOverlay;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        List<IGeoPoint> wayPoints = new ArrayList<>();
        wayPoints.add(_geoPoint);
        wayPoints.add(_geoPoint);
        when(_route.getWayPoints()).thenReturn(wayPoints);
        when(_route.getGuidancePoints()).thenReturn(new GuidancePoint[0]);

        when(_mapView.getProjection()).thenReturn(_projection);

        doAnswer(invocation -> {
            Point p = invocation.getArgument(1);
            p.set(100, 100);
            return p;
        }).when(_projection).toPixels(any(IGeoPoint.class), any(Point.class));

        _routeOverlay = new RouteOverlay(_route, 0xFF0000, 5.0f);
        _routeOverlay.setEditMode(true);

        // Inject dummy bitmaps to prevent NPE during drawing
        Bitmap dummyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        String[] bitmapFields = { "_deleteBitmap", "_addBitmap", "_editBitmap" };
        for (String fieldName : bitmapFields) {
            try {
                Field f = RouteOverlay.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(_routeOverlay, dummyBitmap);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Ignore if field doesn't exist or access is restricted in current version
            }
        }
    }

    @Test
    public void draw_WayPointIconsDrawnAfterGeometry() throws Exception {
        // Set a waypoint as long-pressed to trigger icon drawing
        Field field = RouteOverlay.class.getDeclaredField("_longPressedPointIndex");
        field.setAccessible(true);
        field.set(_routeOverlay, 0);

        // We use a mocked canvas to record the order of calls
        _routeOverlay.draw(_canvas, _mapView, false);

        InOrder inOrder = inOrder(_canvas);

        // Verify that geometry (like the path or building point circles) is drawn
        // before UI items (bitmaps)
        // We verify drawPath as a solid indicator of route geometry
        inOrder.verify(_canvas, atLeastOnce()).drawPath(any(Path.class), any(Paint.class));

        // Then the icons are drawn (must be after geometry)
        inOrder.verify(_canvas, atLeastOnce()).drawBitmap(any(Bitmap.class), anyFloat(), anyFloat(), any(Paint.class));
    }

    @Test
    public void draw_GuidancePointIconsDrawnAfterRadii() throws Exception {
        // Setup a guidance point
        GuidancePoint gp = mock(GuidancePoint.class);
        when(gp.getPoint()).thenReturn(_geoPoint);
        when(gp.getRadius()).thenReturn(200f);
        when(_route.getGuidancePoints()).thenReturn(new GuidancePoint[] { gp });

        // Set guidance point as long-pressed
        Field field = RouteOverlay.class.getDeclaredField("_longPressedGuidancePointIndex");
        field.setAccessible(true);
        field.set(_routeOverlay, 0);

        _routeOverlay.draw(_canvas, _mapView, false);

        InOrder inOrder = inOrder(_canvas);

        // 1. Path (Geometry) - Drawn early in the draw() method
        inOrder.verify(_canvas, atLeastOnce()).drawPath(any(Path.class), any(Paint.class));

        // 2. Icons (UI Overlay) - Drawn at the end
        inOrder.verify(_canvas, atLeastOnce()).drawBitmap(any(Bitmap.class), anyFloat(), anyFloat(), any(Paint.class));
    }
}
