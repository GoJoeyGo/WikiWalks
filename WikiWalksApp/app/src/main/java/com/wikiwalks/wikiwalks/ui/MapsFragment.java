package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;

import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, PathMap.PathMapListener {

    GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<Integer, Polyline> polylines = new HashMap<>();
    private HashMap<Integer, Marker> markers = new HashMap<>();
    private Button createPath;
    private boolean hasFailed = false;
    Toolbar toolbar;

    public static MapsFragment newInstance() {
        Bundle args = new Bundle();
        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        PathMap.getInstance().addListener(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        final View rootView = inflater.inflate(R.layout.maps_fragment, container, false);
        toolbar = rootView.findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.app_name);
        createPath = rootView.findViewById(R.id.create_path_button);
        createPath.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(-1)).addToBackStack("Map").commit());
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
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
        getParentFragmentManager().beginTransaction().add(R.id.main_frame, PathFragment.newInstance((Integer) marker.getTag())).addToBackStack("Map").commit();
        return true;
    }

    public void setMapLocation() {
        MainActivity.checkLocationPermission(this.getActivity());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            else setMapLocation();
        });
    }

    @Override
    public void OnPathMapChange() {
        PathMap pathMap = PathMap.getInstance();
        HashMap<Integer, Path> paths = pathMap.getPathList();
        for (Map.Entry<Integer, Polyline> polylineEntry : polylines.entrySet()) {
            polylineEntry.getValue().remove();
            polylines.remove(polylineEntry);
        }
        for (Map.Entry<Integer, Marker> markerEntry : markers.entrySet()) {
            markerEntry.getValue().remove();
            markers.remove(markerEntry);
        }
        for (Map.Entry<Integer, Path> pathEntry : paths.entrySet()) {
            Path path = pathEntry.getValue();
            for (Route route : path.getRoutes()) {
                Polyline polyline = route.makePolyline(mMap);
                polylines.put(route.getId(), polyline);
            }
            Marker marker = path.makeMarker(mMap);
            markers.put(pathEntry.getKey(), marker);
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
