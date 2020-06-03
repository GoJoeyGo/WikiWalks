package com.wikiwalks.wikiwalks;

import android.content.Context;
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

import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, PathMap.PathMapListener {

    GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<Integer, Polyline> polylines = new HashMap<>();
    private Button createPath;
    private boolean hasFailed = false;

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
        createPath = rootView.findViewById(R.id.create_path_button);
        createPath.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(null)).addToBackStack(null).commit());
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);
        context = getContext();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        setMapLocation();
        MainActivity.checkLocationPermission(this.getActivity());
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);
        final PathMap pathMap = PathMap.getInstance();
        pathMap.addListener(this);
        mMap.setOnCameraIdleListener(() -> pathMap.updatePaths(mMap.getProjection().getVisibleRegion().latLngBounds, getActivity()));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Path path = PathMap.getInstance().getPathList().get(marker.getTag());
        getParentFragmentManager().beginTransaction().add(R.id.main_frame, PathFragment.newInstance(path)).addToBackStack(null).commit();
        return true;
    }

    public void setMapLocation() {
        MainActivity.checkLocationPermission(this.getActivity());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            else setMapLocation();
        });
    }

    @Override
    public void OnPathMapChange() {
        PathMap pathMap = PathMap.getInstance();
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
    public void OnPathMapUpdateFailure() {
        if (!hasFailed) {
            hasFailed = true;
            Toast.makeText(getContext(), "Failed to get paths...", Toast.LENGTH_LONG).show();
        }
    }
}
