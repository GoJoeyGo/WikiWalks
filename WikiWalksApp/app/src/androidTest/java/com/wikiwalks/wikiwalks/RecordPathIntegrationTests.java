package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.maps.GoogleMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RecordPathIntegrationTests {
    Location[] exampleLocations;
    RecordingFragment fragment;
    Path parentPath;
    Context appContext;

    @Mock GoogleMap map;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fragment = RecordingFragment.newInstance(null);
        fragment.mMap = map;
        parentPath = new Path("Parent", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null);
        parentPath.id = 1;
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

    @Test
    public void recordPath() throws InterruptedException {
        fragment.addLocation(exampleLocations[0]);
        fragment.addLocation(exampleLocations[1]);
        fragment.addLocation(exampleLocations[2]);
        Path path = fragment.createPath("Test Path");
        path.submit(appContext, mock(Path.PathSubmitCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertTrue(path.getName().equals("Test Path") && path.getLatitudes().get(1).equals(exampleLocations[1].getLatitude()) && PathMap.getInstance().getPathList().containsValue(path));
    }

    @Test
    public void recordChildPath() throws InterruptedException {
        fragment.setParentPath(parentPath);
        fragment.addLocation(exampleLocations[0]);
        fragment.addLocation(exampleLocations[1]);
        fragment.addLocation(exampleLocations[2]);
        Path path = fragment.createPath("Test Path");
        path.submit(appContext, mock(Path.PathSubmitCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertTrue(path.getName().equals("Test Path") && path.getLatitudes().get(1).equals(exampleLocations[1].getLatitude()) && PathMap.getInstance().getPathList().containsValue(path) && path.getParentPath() == parentPath);
    }
}
