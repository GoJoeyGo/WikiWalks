package net.tyleroneill.wikiwalkstest.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.tyleroneill.wikiwalkstest.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class FormFragment extends Fragment {

    Button submitButton;
    EditText latitude;
    EditText longitude;

    public static FormFragment newInstance() {
        Bundle args = new Bundle();
        FormFragment fragment = new FormFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.add_path_fragment, container, false);
        submitButton = (Button) rootView.findViewById(R.id.submit_path);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    getNewPath();
            }
        });
        latitude = (EditText) rootView.findViewById(R.id.latitude);
        longitude = (EditText) rootView.findViewById(R.id.longitude);
        return rootView;
    }

    public void getNewPath() {
        String latitudeCoords = latitude.getText().toString().trim().replaceAll("\\s+","");
        String longitudeCoords = longitude.getText().toString().replaceAll("\\s+","");
        String coordPattern = "^[0-9.,\\- ]*$";
        if (latitudeCoords.matches(coordPattern) && longitudeCoords.matches(coordPattern)) {
            String[] latitudeList = latitudeCoords.split(",");
            String[] longitudeList = longitudeCoords.split(",");
            if (latitudeList.length == longitudeList.length && latitudeList.length >= 2) {
                for (int i = 0; i < latitudeList.length; i++) {
                    if (Float.parseFloat(latitudeList[i]) < -90 || Float.parseFloat(latitudeList[i]) > 90) {
                        showToast("Invalid coordinate in latitude.");
                        return;
                    } else if (Float.parseFloat(longitudeList[i]) < -180 || Float.parseFloat(longitudeList[i]) > 180) {
                        showToast("Invalid coordinate in longitude.");
                        return;
                    }
                }
                showToast("Valid coordinates.");
                JSONObject path = new JSONObject();
                try {
                    path.put("starting_point", new JSONObject().put("latitude", latitudeList[0]).put("longitude", longitudeList[0]));
                    path.put("ending_point", new JSONObject().put("latitude", latitudeList[latitudeList.length-1]).put("longitude", longitudeList[longitudeList.length-1]));
                    path.put("latitude", new JSONObject().put("points", new JSONArray(Arrays.asList(latitudeList))));
                    path.put("longitude", new JSONObject().put("points", new JSONArray(Arrays.asList(longitudeList))));
                    Log.v("PathGen", path.toString());
                    RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                    JsonObjectRequest sendPath = new JsonObjectRequest(Request.Method.POST, "http://192.168.0.50:5000/new/path", new JSONObject().put("attributes", path), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.v("Request", response.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Request", error.getMessage());
                        }
                    });
                    requestQueue.add(sendPath);
                } catch (JSONException e) {
                    showToast("Error");
                    Log.v("PathGen", e.getMessage());
                }
            } else {
                showToast("Both lists must be the same length.");
            }
        } else {
            showToast("Invalid format...");
        }
    }

    public void showToast(String text) {
        Toast.makeText( getContext(),text , Toast.LENGTH_SHORT)
                .show();
    }
}
