package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import androidx.core.app.ActivityCompat;
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

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<Integer, Polyline> polylines = new HashMap<>();
    private HashMap<Integer, Marker> markers = new HashMap<>();
    private boolean hasFailed = false;

    public static MapsFragment newInstance() {
        Bundle args = new Bundle();
        MapsFragment fragment = new MapsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.maps_fragment, container, false);

        PathMap.getInstance().addListener(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        MaterialToolbar toolbar = rootView.findViewById(R.id.main_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.maps_menu_settings:
                    getParentFragmentManager().beginTransaction().add(R.id.main_frame, SettingsFragment.newInstance()).addToBackStack(null).commit();
                    break;
                case R.id.maps_menu_bookmarks:
                    getParentFragmentManager().beginTransaction().add(R.id.main_frame, BookmarksFragment.newInstance()).addToBackStack(null).commit();
                    break;
            }
            return true;
        });

        Button createPath = rootView.findViewById(R.id.create_path_button);
        createPath.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(-1)).addToBackStack("Map").commit());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        setMapLocation();
        MainActivity.checkPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION, granted -> {
                if (granted) mMap.setMyLocationEnabled(true);
            }
        );
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);
        PathMap pathMap = PathMap.getInstance();
        pathMap.addListener(this);
        mMap.setOnCameraIdleListener(() -> pathMap.updatePaths(mMap.getProjection().getVisibleRegion().latLngBounds, getActivity()));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        getParentFragmentManager().beginTransaction().add(R.id.main_frame, PathFragment.newInstance((Integer) marker.getTag())).addToBackStack("Map").commit();
        return true;
    }

    @Override
    public void onDestroy() {
        PathMap.getInstance().removeListener(this);
        super.onDestroy();
    }

    public void setMapLocation() {
        MainActivity.checkPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION, granted -> {
            if (granted) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    else setMapLocation();
                });
            } else {
                getParentFragmentManager().beginTransaction().add(R.id.main_frame, PermissionsFragment.newInstance(Manifest.permission.ACCESS_FINE_LOCATION)).addToBackStack(null).commit();
                setMapLocation();
            }
        });
    }

    @Override
    public void onPathMapUpdateSuccess() {
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
    public void onPathMapUpdateFailure() {
        if (!hasFailed) {
            hasFailed = true;
            Toast.makeText(getContext(), "Failed to get paths...", Toast.LENGTH_LONG).show();
        }
    }
}
