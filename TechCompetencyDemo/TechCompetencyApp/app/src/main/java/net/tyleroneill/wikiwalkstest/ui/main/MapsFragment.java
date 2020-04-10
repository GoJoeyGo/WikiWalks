package net.tyleroneill.wikiwalkstest.ui.main;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.tyleroneill.wikiwalkstest.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    FloatingActionButton floatingActionButton;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;

    public static MapsFragment newInstance() {

        Bundle args = new Bundle();

        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.maps_fragment, container, false);
        floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText( getContext(),"Denied..." , Toast.LENGTH_SHORT)
                        .show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, FormFragment.newInstance())
                        .commitNow();
            }
        });
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);
        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
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
