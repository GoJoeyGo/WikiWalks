package com.wikiwalks.wikiwalks;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class WalkFragment extends Fragment implements OnMapReadyCallback, ScaleGestureDetector.OnScaleGestureListener {

    private boolean isRoute;
    private GoogleMap mMap;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Path path;
    private ConstraintLayout outOfRangeBanner;
    TextView offTrackVariable;
    ImageView offTrackDirectionIndicator;
    private ArrayList<Double> pathLatitudes;
    private ArrayList<Double> pathLongitudes;

    public static WalkFragment newInstance(Path path, boolean isRoute) {
        Bundle args = new Bundle();
        WalkFragment fragment = new WalkFragment();
        fragment.setArguments(args);
        fragment.setPath(path);
        fragment.setRoute(isRoute);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        final View rootView = inflater.inflate(R.layout.walk_fragment, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.walk_map_frag);
        mapFragment.getMapAsync(this);
        outOfRangeBanner = rootView.findViewById(R.id.out_of_range_banner);
        offTrackVariable = rootView.findViewById(R.id.off_track_variable);
        offTrackDirectionIndicator = rootView.findViewById(R.id.off_track_direction_indicator);
        context = getContext();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setCompassEnabled(false);
        if (isRoute) {
            path.makePolyLine(mMap);
            pathLatitudes = path.getLatitudes();
            pathLongitudes = path.getLongitudes();
        } else {
            Path parent = path;
            while (parent.getParentPath() != null) {
                parent = parent.getParentPath();
            }
            parent.makeAllPolyLines(mMap);
            pathLatitudes = parent.getAllLatitudes();
            pathLongitudes = parent.getAllLongitudes();
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20)));
        startLocationUpdates();
    }

    public void setPath(Path path) {
        this.path = path;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private void onLocationChanged(Location location) {
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(mMap.getCameraPosition().zoom).bearing(location.getBearing()).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        boolean inRange = false;
        float[] shortestDistance = new float[3];
        for (int i = 0; i < pathLatitudes.size(); i++) {
            if (pathLatitudes.get(i) - 0.00005 < location.getLatitude() && location.getLatitude() < pathLatitudes.get(i) + 0.00005 && pathLongitudes.get(i) - 0.00005 < location.getLongitude() && location.getLongitude() < pathLongitudes.get(i) + 0.00005) {
                inRange = true;
                break;
            }
            else {
                Location destination = new Location(LocationManager.GPS_PROVIDER);
                destination.setLatitude(pathLatitudes.get(i));
                destination.setLongitude(pathLongitudes.get(i));
                float distance = location.distanceTo(destination);
                float bearing = ((((location.bearingTo(destination) + 360) % 360) - location.getBearing()) + 360) % 360;
                if (distance < shortestDistance[0] || shortestDistance[0] == 0) {
                    shortestDistance[0] = distance;
                    shortestDistance[1] = bearing;
                    shortestDistance[2] = i;
                }
            }
        }
        if (!inRange) {
            outOfRangeBanner.setVisibility(View.VISIBLE);
            offTrackVariable.setText(String.format("You are %d metres away.", (int) shortestDistance[0]));
            offTrackDirectionIndicator.setRotation(shortestDistance[1]);
        }
        else {
            outOfRangeBanner.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    public void setRoute(boolean route) {
        isRoute = route;
    }
}
