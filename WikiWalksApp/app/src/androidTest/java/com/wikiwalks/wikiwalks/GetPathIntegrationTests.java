package com.wikiwalks.wikiwalks;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.wikiwalks.wikiwalks.ui.MapsFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GetPathIntegrationTests {
    MapsFragment fragment;
    Context appContext;
    LatLngBounds bounds;

    GoogleMap map;
    PathMap pathMap;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fragment = MapsFragment.newInstance();
        fragment.setmMap(map);
        pathMap = spy(PathMap.getInstance());

    }

    @Test
    public void getAllPaths() throws InterruptedException {
        bounds = new LatLngBounds(new LatLng(-90, -180), new LatLng(90, 179));
        pathMap.updatePaths(bounds, appContext);
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertTrue(PathMap.getInstance().getPathList().size() > 0);
    }

    @Test
    public void getSomePaths() throws InterruptedException {
        bounds = new LatLngBounds(new LatLng(-28, 152), new LatLng(-27, 153));
        pathMap.updatePaths(bounds, appContext);
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        boolean inRange = true;
        for (Map.Entry<Integer, Path> pathEntry : pathMap.getPathList().entrySet()) {
            Path path = pathEntry.getValue();
            if (!bounds.including(path.getBounds().northeast).including(path.getBounds().southwest).equals(bounds)) {
                inRange = false;
            }
        }
        assertTrue(inRange);
    }

    @Test
    public void makeNewRoute() {
        ArrayList<Double> latitudes = new ArrayList<>();
        ArrayList<Double> longitudes = new ArrayList<>();
        ArrayList<Double> altitudes = new ArrayList<>();
        latitudes.add(1.00);
        longitudes.add(1.00);
        altitudes.add(1.00);
        Route.submit(appContext, null, "Test Path", latitudes, longitudes, altitudes, mock(Route.RouteModifyCallback.class));
    }

}
