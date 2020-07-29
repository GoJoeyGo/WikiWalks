package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.maps.GoogleMap;
import com.wikiwalks.wikiwalks.ui.RecordingFragment;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordPathIntegrationTests {
    Location[] exampleLocations;
    RecordingFragment fragment;
    Context appContext;

    @Mock
    GoogleMap map;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fragment = RecordingFragment.newInstance(-1);
        fragment.setmMap(map);
        exampleLocations = new Location[]{new Location(LocationManager.GPS_PROVIDER), new Location(LocationManager.GPS_PROVIDER), new Location(LocationManager.GPS_PROVIDER)};
        exampleLocations[0].setLatitude(1);
        exampleLocations[0].setLongitude(1);
        exampleLocations[0].setAltitude(1);
        exampleLocations[1].setLatitude(1.00002);
        exampleLocations[1].setLongitude(1.00002);
        exampleLocations[1].setAltitude(1);
        exampleLocations[2].setLatitude(1.00004);
        exampleLocations[2].setLongitude(1.00004);
        exampleLocations[2].setAltitude(1);
    }

//    @Test
//    public void A_recordRoute() throws InterruptedException {
//        int initialSize = PathMap.getInstance().getPathList().size();
//        fragment.addLocation(exampleLocations[0]);
//        fragment.addLocation(exampleLocations[1]);
//        fragment.addLocation(exampleLocations[2]);
//        Route route = fragment.generateRoute();
//        Route.RouteSubmitCallback callback = mock(Route.RouteSubmitCallback.class);
//        route.submit(appContext, "Test Title", callback);
//        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
//        verify(callback).onRouteSubmitSuccess();
//        assertTrue(PathMap.getInstance().getPathList().size() > initialSize);
//    }

    @Test
    public void B_editPath() throws InterruptedException {
        Path testPath = null;
        for (Map.Entry<Integer, Path> entry : PathMap.getInstance().getPathList().entrySet()) {
            testPath = entry.getValue();
        }
        testPath.edit(appContext, "Test Edited Title", mock(Path.PathChangeCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertEquals("Test Edited Title", testPath.getName());
    }

    @Test
    public void C_deleteRoute() throws InterruptedException {
        Path testPath = null;
        for (Map.Entry<Integer, Path> entry : PathMap.getInstance().getPathList().entrySet()) {
            testPath = entry.getValue();
        }
        assertTrue(PathMap.getInstance().getPathList().containsValue(testPath));
        testPath.getRoutes().get(0).delete(appContext, mock(Route.RouteModifyCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertFalse(PathMap.getInstance().getPathList().containsValue(testPath));
    }


}
