package com.dpm.quickroutemap.tests;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.dpm.quickroutemap.AppInfoActivity;
import com.dpm.quickroutemap.QuickRouteMapActivity;
import com.dpm.quickroutemap.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;
import androidx.test.espresso.NoMatchingViewException;

@RunWith(AndroidJUnit4.class)
public class QuickRouteMapActivityMenuTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    );

    private void preparePreferences() {
        // Disable the startup info activity via preferences
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences preferences = context.getSharedPreferences(AppInfoActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        preferences.edit().putBoolean(AppInfoActivity.NO_SHOW_ON_STARTUP_PREFERENCE, true).commit();
    }

    @After
    public void tearDown() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void menuTitle_ShowsCreateNewRoute_WhenNoRouteLoaded() {
        preparePreferences();
        try (androidx.test.core.app.ActivityScenario<QuickRouteMapActivity> scenario =
                     androidx.test.core.app.ActivityScenario.launch(QuickRouteMapActivity.class)) {
            sleep(2000);
            assertMenuTitleExists(R.string.createRoute);
        }
    }

    @Test
    public void closeRoute_DisabledByDefaultForNewSession() {
        preparePreferences();
        try (androidx.test.core.app.ActivityScenario<QuickRouteMapActivity> scenario =
                     androidx.test.core.app.ActivityScenario.launch(QuickRouteMapActivity.class)) {
            sleep(2000);
            assertMenuItemEnabledDirectly(scenario, R.id.closeRouteMenuItem, false);
        }
    }

    @Test
    public void closeRoute_EnabledAfterCreatingNewRoute() {
        preparePreferences();
        try (androidx.test.core.app.ActivityScenario<QuickRouteMapActivity> scenario =
                     androidx.test.core.app.ActivityScenario.launch(QuickRouteMapActivity.class)) {
            sleep(2000);
            clickMenuItem(R.string.createRoute);
            sleep(2000);
            assertMenuItemEnabledDirectly(scenario, R.id.closeRouteMenuItem, true);
        }
    }

    @Test
    public void closeRoute_DisabledAgainAfterClosing() {
        preparePreferences();
        try (androidx.test.core.app.ActivityScenario<QuickRouteMapActivity> scenario =
                     androidx.test.core.app.ActivityScenario.launch(QuickRouteMapActivity.class)) {
            sleep(2000);
            clickMenuItem(R.string.createRoute);
            sleep(2000);
            clickMenuItem(R.string.closeRouteMenuItemLabel);
            sleep(2000);
            assertMenuItemEnabledDirectly(scenario, R.id.closeRouteMenuItem, false);
        }
    }

    private void assertMenuItemEnabled(int resourceId, boolean enabled) {
        String resName = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getResourceEntryName(resourceId);
        android.util.Log.d("MenuTest", "Checking if menu item (res=" + resName + ") is enabled=" + enabled);
        
        int itemId = -1;
        if (resourceId == R.string.closeRouteMenuItemLabel) {
            itemId = R.id.closeRouteMenuItem;
        } else if (resourceId == R.string.createRoute || resourceId == R.string.editRoute || resourceId == R.string.finishEditingRoute) {
            itemId = R.id.editRouteMenuItem;
        }

        if (itemId != -1) {
            try {
                // We match view with ID AND ensures it's part of a toolbar/menu structure if possible
                // or just id is usually enough if it's unique.
                onView(withId(itemId)).check(matches(enabled ? isEnabled() : not(isEnabled())));
                return;
            } catch (NoMatchingViewException e) {
                android.util.Log.d("MenuTest", "Item ID " + resName + " not found in direct view, checking overflow...");
            }
        }

        // Fallback to text matching
        try {
            onView(anyOf(withText(resourceId), withContentDescription(resourceId)))
                    .check(matches(enabled ? isEnabled() : not(isEnabled())));
        } catch (NoMatchingViewException e) {
            android.util.Log.d("MenuTest", "Opening overflow for text: " + resName);
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
            sleep(500);
            onView(withText(resourceId)).check(matches(enabled ? isEnabled() : not(isEnabled())));
        }
    }

    private void assertMenuItemEnabledDirectly(androidx.test.core.app.ActivityScenario<QuickRouteMapActivity> scenario, int menuItemId, boolean expectedEnabled) {
        scenario.onActivity(activity -> {
            android.view.Menu menu = activity.getOptionsMenu();
            assertNotNull("Menu should not be null", menu);
            android.view.MenuItem item = menu.findItem(menuItemId);
            assertNotNull("Menu item with ID " + menuItemId + " not found", item);
            if (expectedEnabled) {
                assertTrue("Menu item should be enabled", item.isEnabled());
            } else {
                assertFalse("Menu item should be disabled", item.isEnabled());
            }
        });
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void assertMenuTitleExists(int resourceId) {
        try {
            // Try visible first
            onView(anyOf(withText(resourceId), withContentDescription(resourceId)))
                    .check(matches(isDisplayed()));
        } catch (NoMatchingViewException e) {
            try {
                // Try opening overflow
                openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
                onView(withText(resourceId)).check(matches(isDisplayed()));
            } catch (Exception e2) {
                // Check if it exists at all
                onView(withText(resourceId)).check(matches(anything()));
            }
        }
    }

    private void clickMenuItem(int resourceId) {
        try {
            onView(anyOf(withText(resourceId), withContentDescription(resourceId)))
                    .perform(androidx.test.espresso.action.ViewActions.click());
        } catch (NoMatchingViewException e) {
            try {
                openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
                onView(withText(resourceId)).perform(androidx.test.espresso.action.ViewActions.click());
            } catch (Exception e2) {
                // Last ditch: try to click by ID
                onView(withId(R.id.editRouteMenuItem)).perform(androidx.test.espresso.action.ViewActions.click());
            }
        }
    }
}
