package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class WalkFragment extends Fragment implements OnMapReadyCallback {

    private boolean inRange = false;
    private int closestPoint;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Path path;
    private LocationRequest locationRequest;
    private ConstraintLayout outOfRangeBanner;
    TextView offTrackVariable;
    ImageView offTrackDirectionIndicator;
    Button splitPathButton;

    public static WalkFragment newInstance(Path path) {
        Bundle args = new Bundle();
        WalkFragment fragment = new WalkFragment();
        fragment.setArguments(args);
        fragment.setPath(path);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        final View rootView = inflater.inflate(R.layout.walk_fragment, container, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.walk_map_frag);
        mapFragment.getMapAsync(this);
        outOfRangeBanner = rootView.findViewById(R.id.out_of_range_banner);
        offTrackVariable = rootView.findViewById(R.id.off_track_variable);
        offTrackDirectionIndicator = rootView.findViewById(R.id.off_track_direction_indicator);
        splitPathButton = rootView.findViewById(R.id.split_path_button);
        splitPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (closestPoint >= 0) {
                    fusedLocationProviderClient.removeLocationUpdates(new LocationCallback());
                    Double[] startingCoords = {path.getAllLatitudes().get(closestPoint), path.getAllLongitudes().get(closestPoint), path.getAllAltitudes().get(closestPoint)};
                    getFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(path, startingCoords)).addToBackStack(null).commit();
                }
                else {
                    Toast.makeText(context, "You are too far away!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        context = getContext();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        path.makePolyLine(mMap);
        for (Path child : path.getChildPaths()) {
            child.makePolyLine(mMap);
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            }
        });
        startLocationUpdates();
    }

    public void setPath(Path path) {
        this.path = path;
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
               onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private void onLocationChanged(Location location) {
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).bearing(location.getBearing()).zoom(20).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        inRange = false;
        float shortestDistance[] = new float[3];
        ArrayList<Double> pathLatitudes = path.getAllLatitudes();
        ArrayList<Double> pathLongitudes = path.getAllLongitudes();
        for (int i = 0; i < pathLatitudes.size(); i++) {
            if (pathLatitudes.get(i) - 0.00005 < location.getLatitude() && location.getLatitude() < pathLatitudes.get(i) + 0.00005 && pathLongitudes.get(i) - 0.00005 < location.getLongitude() && location.getLongitude() < pathLongitudes.get(i) + 0.00005) {
                closestPoint = i;
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
                closestPoint = -1;
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
}
