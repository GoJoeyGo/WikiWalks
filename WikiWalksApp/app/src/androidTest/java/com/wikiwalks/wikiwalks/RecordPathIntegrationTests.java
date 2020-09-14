package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.location.Location;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.wikiwalks.wikiwalks.ui.RecordingFragment;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

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
    static Path path;
    RecordingFragment fragment;
    Context appContext;

    @Before
    public void setup() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fragment = RecordingFragment.newInstance(-1);
    }

    @Test
    public void A_recordRoute() throws InterruptedException {
        int initialSize = DataMap.getInstance().getPathList().size();
        for (int i = 0; i < 10; i++) {
            Location newLocation = new Location("Test");
            newLocation.setLatitude(1 + ((double) (i * 2) / 100000));
            newLocation.setLongitude(1 + ((double) (i * 2) / 100000));
            newLocation.setAltitude(1);
            fragment.addLocation(newLocation);
        }
        Route.RouteModifyCallback callback = new Route.RouteModifyCallback() {
            @Override
            public void onRouteEditSuccess(Path path) {
                RecordPathIntegrationTests.path = path;
            }

            @Override
            public void onRouteEditFailure() {

            }
        };
        fragment.context = appContext;
        fragment.submitRoute("Test", callback);
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertTrue(DataMap.getInstance().getPathList().size() > initialSize);
    }

    @Test
    public void B_addPointOfInterest() throws InterruptedException {
        PointOfInterest.submit(appContext, "Test Point of Interest", 1.0, 1.0, path, mock(PointOfInterest.PointOfInterestSubmitCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertEquals(1, path.getPointsOfInterest().size());
    }

    @Test
    public void C_editPointOfInterest() throws InterruptedException {
        PointOfInterest pointOfInterest = path.getPointsOfInterest().get(0);
        pointOfInterest.edit(appContext, "New Name", mock(PointOfInterest.PointOfInterestEditCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertTrue(pointOfInterest.getName().matches("New Name"));
    }

    @Test
    public void D_deletePointOfInterest() throws InterruptedException {
        PointOfInterest pointOfInterest = path.getPointsOfInterest().get(0);
        pointOfInterest.delete(appContext, mock(PointOfInterest.PointOfInterestEditCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertEquals(0, path.getPointsOfInterest().size());
    }

    @Test
    public void E_editPath() throws InterruptedException {
        path.edit(appContext, "Test Edited Title", mock(Path.PathChangeCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertEquals("Test Edited Title", path.getName());
    }

    @Test
    public void F_deleteRoute() throws InterruptedException {
        assertTrue(DataMap.getInstance().getPathList().containsValue(path));
        path.getRoutes().get(0).delete(appContext, mock(Route.RouteModifyCallback.class));
        new CountDownLatch(1).await(2000, TimeUnit.MILLISECONDS);
        assertFalse(DataMap.getInstance().getPathList().containsValue(path));
    }

}
