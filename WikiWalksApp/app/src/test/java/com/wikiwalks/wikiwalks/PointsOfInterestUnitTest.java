package com.wikiwalks.wikiwalks;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class PointsOfInterestUnitTest {
    @Mock
    Path mockpath;

    LatLng cords;

    @Test
    public void poi_constructor() {

        String name = "Testpath";
        ArrayList<Double> latitudes = new ArrayList<Double>();
        latitudes.add(152.1);
        ArrayList<Double> longitudes = new ArrayList<Double>();
        longitudes.add(154.2);
        ArrayList<Double> altitudes = new ArrayList<Double>();
        altitudes.add(7.61);

        Path testpath = new Path(name,latitudes,longitudes,altitudes,mockpath);

        int id = 1;
        String poiName = "lookout";
        double poilatitude = 151.1;
        double poilongitude = 153.6;

        PointOfInterest testpoint = new PointOfInterest( id, name, poilatitude, poilongitude, testpath);

        cords = new LatLng(poilatitude, poilongitude);

        assertEquals(testpoint.getName(),"Testpath");
       ;


    }
}