package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Button createPathButton;
    SupportMapFragment mapFragment;
    Context context;
    FusedLocationProviderClient fusedLocationProviderClient;
    HashMap<Integer,Polyline> polylines = new HashMap<>();
    boolean hasFailed = false;

    public static MapsFragment newInstance() {
        Bundle args = new Bundle();
        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        final View rootView = inflater.inflate(R.layout.maps_fragment, container, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);
        context = getContext();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            }
        });
        mMap.setOnMarkerClickListener(this);
        final PathMap pathMap = PathMap.getInstance();
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                pathMap.updatePaths(mMap, getActivity(), new PathCallback() {
                    @Override
                    public void onSuccess(String result) {
                        HashMap<Integer, Path> paths = pathMap.getPathList();
                        for (Map.Entry<Integer, Path> pathEntry : paths.entrySet()) {
                            if (!polylines.containsKey(pathEntry.getKey())) {
                                Path path = pathEntry.getValue();
                                LatLng startingPoint = new LatLng(path.getLatitudes().get(0), path.getLongitudes().get(0));
                                Polyline polyline = path.makePolyLine(mMap);
                                polylines.put(pathEntry.getKey(), polyline);
                                if (path.getParentPath() == null) {
                                    Marker marker = mMap.addMarker(new MarkerOptions().position(startingPoint));
                                    marker.setTag(path.getId());
                                    marker.setTitle(path.getName());
                                }
                            }

                        }
                    }

                    @Override
                    public void onFailure(String result) {
                        if (!hasFailed) {
                            hasFailed = true;
                            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Path path = PathMap.getInstance().getPathList().get(marker.getTag());
        getFragmentManager().beginTransaction()
                    .add(R.id.main_frame, PathFragment.newInstance(path)).addToBackStack(null)
                    .commit();

        return true;
    }

}
