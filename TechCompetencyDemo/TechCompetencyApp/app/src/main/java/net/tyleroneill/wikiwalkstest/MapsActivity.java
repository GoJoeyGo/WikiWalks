package net.tyleroneill.wikiwalkstest;

import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        mMap = googleMap;
        JsonObjectRequest paths = new JsonObjectRequest(Request.Method.GET, "http://192.168.0.50:5000/paths/", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("Request", response.toString());
                    JSONArray array = response.getJSONArray("paths");
                    Log.v("Request", array.toString());
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject pathJson = array.getJSONObject(i);
                        Log.v("Request", pathJson.toString());
                        JSONArray latitude = pathJson.getJSONObject("latitude").getJSONArray("points");
                        JSONArray longitude = pathJson.getJSONObject("longitude").getJSONArray("points");
                        Log.v("Request", latitude.toString());
                        Log.v("Request", longitude.toString());
                        Log.v("Request", pathJson.get("id").toString());
                        ArrayList<LatLng> points = new ArrayList<>();
                        for (int j = 0; j < latitude.length(); j++) {

                            points.add(new LatLng(Double.parseDouble((String) latitude.get(j)), Double.parseDouble((String) longitude.get(j))));
                        }
                        Polyline polyline = mMap.addPolyline(new PolylineOptions().clickable(true));
                        polyline.setTag(pathJson.get("id"));
                        polyline.setPoints(points);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Request", error.getMessage());

            }
        });
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Log.v("Request", polyline.getTag().toString());
            }
        });
        requestQueue.add(paths);
        LatLng murrumbaDowns = new LatLng(-27.271472, 153.014389);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(murrumbaDowns, 15));
    }
}
