package com.wikiwalks.wikiwalks;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PathUnitTest {
    @Mock
    Path mockpath;

    @Test
    public void path_constructor() {
        String name = "Testpath";
        ArrayList<Double> latitudes = new ArrayList<Double>();
        latitudes.add(152.1);
        ArrayList<Double> longitudes = new ArrayList<Double>();
        longitudes.add(154.2);
        ArrayList<Double> altitudes = new ArrayList<Double>();
        altitudes.add(7.61);

        Route testpath = new Route(-1,mockpath,true,latitudes,longitudes,altitudes);

        assertEquals(testpath.getId(),-1);
        assertEquals(latitudes.get(0),new Double(152.1));
        assertEquals(longitudes.get(0),new Double(154.2));
        assertEquals(altitudes.get(0),new Double(7.61));
    }
    @Test
    public void path_constructor_json() throws Exception {
        JSONObject jsonObject = new JSONObject();
        Path testpath;
        testpath = new Path(jsonObject);

        assertTrue(testpath instanceof Path);
    }
}