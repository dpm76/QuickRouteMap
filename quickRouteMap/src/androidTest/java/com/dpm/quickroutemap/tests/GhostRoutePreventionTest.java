package com.dpm.quickroutemap.tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.dpm.quickroutemap.QuickRouteMapActivity;
import com.dpm.quickroutemap.RouteOverlay;
import com.dpm.quickroutemap.navigation.Route;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GhostRoutePreventionTest {

    @Test
    public void showRoute_DoesNotAddMultipleOverlaysForSameRoute() throws Exception {
        try (ActivityScenario<QuickRouteMapActivity> scenario = ActivityScenario.launch(QuickRouteMapActivity.class)) {
            scenario.onActivity(activity -> {
                try {
                    // Create a dummy route
                    Route route = new Route();
                    Field keyField = Route.class.getDeclaredField("_key");
                    keyField.setAccessible(true);
                    keyField.set(route, "test-route-key");

                    // Set as current route
                    Field currentRouteField = QuickRouteMapActivity.class.getDeclaredField("_currentRoute");
                    currentRouteField.setAccessible(true);
                    currentRouteField.set(null, route);

                    Method showRouteMethod = QuickRouteMapActivity.class.getDeclaredMethod("showRoute");
                    showRouteMethod.setAccessible(true);

                    // Call multiple times to simulate re-entries or multiple loads
                    showRouteMethod.invoke(activity);
                    showRouteMethod.invoke(activity);
                    showRouteMethod.invoke(activity);

                    // Verify overlay count in MapView
                    Field mapViewField = QuickRouteMapActivity.class.getDeclaredField("_mapView");
                    mapViewField.setAccessible(true);
                    MapView mapView = (MapView) mapViewField.get(activity);

                    List<Overlay> overlays = mapView.getOverlays();
                    int count = 0;
                    for (Overlay o : overlays) {
                        if (o instanceof RouteOverlay) {
                            count++;
                        }
                    }

                    assertEquals(
                            "Multiple calls to showRoute should still result in only one RouteOverlay instance for a given key",
                            1, count);

                } catch (Exception e) {
                    throw new RuntimeException("Error in testing ghost route prevention", e);
                }
            });
        }
    }
}
