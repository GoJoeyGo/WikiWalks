package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.DataMap;
import com.wikiwalks.wikiwalks.Photo;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.dialogs.NameDialog;
import com.wikiwalks.wikiwalks.ui.dialogs.PhotoDialog;

import java.util.ArrayList;
import java.util.Locale;

public class WalkFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, NameDialog.EditDialogListener, PointOfInterest.PointOfInterestSubmitCallback, PhotoDialog.EditPhotoDialogListener {

    private float distanceWalked = 0;
    private TextView offTrackVariable;
    private ImageView offTrackDirectionIndicator;
    private Location lastLocation;
    private NameDialog nameDialog;
    private Button markPointButton;
    private int routeNumber;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Path path;
    private LinearLayout outOfRangeBanner;
    private ArrayList<Double> pathLatitudes;
    private ArrayList<Double> pathLongitudes;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    public static WalkFragment newInstance(int pathId, int routeNumber) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        args.putInt("routeNumber", routeNumber);
        WalkFragment fragment = new WalkFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.walk_fragment, container, false);

        path = DataMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        new CountDownTimer(30000, 30000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                path.walk(getContext());
            }
        }.start();

        routeNumber = getArguments().getInt("routeNumber");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        MaterialToolbar toolbar = rootView.findViewById(R.id.walk_fragment_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(path.getName());

        markPointButton = rootView.findViewById(R.id.walk_fragment_mark_point_button);
        markPointButton.setOnClickListener(v -> NameDialog.newInstance(NameDialog.EditNameDialogType.POINT_OF_INTEREST, -1).show(getChildFragmentManager(), "EditPopup"));

        Button addPhotoButton = rootView.findViewById(R.id.walk_fragment_add_photo_button);
        addPhotoButton.setOnClickListener(v -> PhotoDialog.newInstance(Photo.PhotoType.PATH, path.getId(), -1, null).show(getChildFragmentManager(), "PhotoPopup"));

        outOfRangeBanner = rootView.findViewById(R.id.walk_fragment_out_of_range_banner);
        offTrackVariable = rootView.findViewById(R.id.walk_fragment_off_track_distance);
        offTrackDirectionIndicator = rootView.findViewById(R.id.walk_fragment_off_track_direction_indicator);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.walk_fragment_map);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        PreferencesManager.getInstance(getContext()).addDistanceWalked(distanceWalked);
        super.onDestroy();
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
        if (routeNumber > -1) {
            path.getRoutes().get(routeNumber).makePolyline(mMap);
            pathLatitudes = path.getRoutes().get(routeNumber).getLatitudes();
            pathLongitudes = path.getRoutes().get(routeNumber).getLongitudes();
        } else {
            for (Route route : path.getRoutes()) route.makePolyline(mMap);
            pathLatitudes = path.getAllLatitudes();
            pathLongitudes = path.getAllLongitudes();
        }
        for (PointOfInterest pointOfInterest : path.getPointsOfInterest()) {
            pointOfInterest.makeMarker(mMap, BitmapDescriptorFactory.HUE_RED);
        }
        mMap.setOnMarkerClickListener(this);
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

        MainActivity.checkPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION, granted -> {
            if (granted) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        });

    }

    private void onLocationChanged(Location location) {
        if (lastLocation != null) {
            distanceWalked += location.distanceTo(lastLocation);
        }
        lastLocation = location;
        markPointButton.setEnabled(true);
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(mMap.getCameraPosition().zoom).bearing(location.getBearing()).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        boolean inRange = false;
        float[] shortestDistance = new float[3];
        for (int i = 0; i < pathLatitudes.size(); i++) {
            if (pathLatitudes.get(i) - 0.00005 < location.getLatitude() && location.getLatitude() < pathLatitudes.get(i) + 0.00005 && pathLongitudes.get(i) - 0.00005 < location.getLongitude() && location.getLongitude() < pathLongitudes.get(i) + 0.00005) {
                inRange = true;
                break;
            } else {
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
            String country = Locale.getDefault().getCountry();
            if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
                offTrackVariable.setText(String.format(getString(R.string.off_track), (int) (shortestDistance[0] * 3.28084), getString(R.string.feet)));
            } else {
                offTrackVariable.setText(String.format(getString(R.string.off_track), (int) shortestDistance[0], getString(R.string.metres)));
            }
            offTrackDirectionIndicator.setRotation(shortestDistance[1]);
        } else {
            outOfRangeBanner.setVisibility(View.GONE);
        }
    }

    public void setRoute(Integer routeNumber) {
        this.routeNumber = routeNumber;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        getParentFragmentManager().beginTransaction().add(R.id.main_frame, PointOfInterestFragment.newInstance((int) marker.getTag())).addToBackStack(null).commit();
        return true;
    }

    @Override
    public void setNameDialog(NameDialog nameDialog) {
        this.nameDialog = nameDialog;
    }

    @Override
    public void onEditName(NameDialog.EditNameDialogType type, String name) {
        if (name.isEmpty()) {
            name = String.format("Point at %f, %f", lastLocation.getLatitude(), lastLocation.getLongitude());
        }
        PointOfInterest.submit(getContext(), name, lastLocation.getLatitude(), lastLocation.getLongitude(), path, this);
    }

    @Override
    public void onSubmitPointOfInterestSuccess(PointOfInterest pointOfInterest) {
        nameDialog.dismiss();
        Toast.makeText(getContext(), R.string.save_point_of_interest_success, Toast.LENGTH_SHORT).show();
        pointOfInterest.makeMarker(mMap, BitmapDescriptorFactory.HUE_RED);
    }

    @Override
    public void onSubmitPointOfInterestFailure() {
        Toast.makeText(getContext(), R.string.save_point_of_interest_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditPhoto() {
        Toast.makeText(getContext(), R.string.save_photo_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePhoto(int position) {

    }
}
