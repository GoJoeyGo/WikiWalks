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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.Picture;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.dialogs.EditNameDialog;
import com.wikiwalks.wikiwalks.ui.dialogs.EditPictureDialog;

import java.util.ArrayList;

public class WalkFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, EditNameDialog.EditDialogListener, PointOfInterest.PointOfInterestSubmitCallback, EditPictureDialog.EditPictureDialogListener {

    TextView offTrackVariable;
    ImageView offTrackDirectionIndicator;
    Location lastLocation;
    EditNameDialog editNameDialog;
    EditPictureDialog editPictureDialog;
    private int routeNumber;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Path path;
    private ConstraintLayout outOfRangeBanner;
    private ArrayList<Double> pathLatitudes;
    private ArrayList<Double> pathLongitudes;
    private Toolbar toolbar;

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
        path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        new CountDownTimer(60000, 60000) {
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
        final View rootView = inflater.inflate(R.layout.walk_fragment, container, false);
        toolbar = rootView.findViewById(R.id.walk_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(path.getName());
        Button markPointButton = rootView.findViewById(R.id.mark_poi_button);
        markPointButton.setOnClickListener(v -> EditNameDialog.newInstance(EditNameDialog.EditNameDialogType.POINT_OF_INTEREST, -1).show(getChildFragmentManager(), "EditPopup"));
        Button addPhotoButton = rootView.findViewById(R.id.take_picture_button);
        addPhotoButton.setOnClickListener(v -> EditPictureDialog.newInstance(Picture.PictureType.PATH, path.getId(), -1, null).show(getChildFragmentManager(), "PicturePopup"));
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.walk_map_frag);
        mapFragment.getMapAsync(this);
        outOfRangeBanner = rootView.findViewById(R.id.out_of_range_banner);
        offTrackVariable = rootView.findViewById(R.id.off_track_variable);
        offTrackDirectionIndicator = rootView.findViewById(R.id.off_track_direction_indicator);
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
        lastLocation = location;
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
            offTrackVariable.setText(String.format("You are %d metres away.", (int) shortestDistance[0]));
            offTrackDirectionIndicator.setRotation(shortestDistance[1]);
        } else {
            outOfRangeBanner.setVisibility(View.INVISIBLE);
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
    public void setEditNameDialog(EditNameDialog editNameDialog) {
        this.editNameDialog = editNameDialog;
    }

    @Override
    public void onEditName(EditNameDialog.EditNameDialogType type, String name) {
        if (name.isEmpty())
            name = String.format("Point at %f, %f", lastLocation.getLatitude(), lastLocation.getLongitude());
        PointOfInterest.submit(getContext(), name, lastLocation.getLatitude(), lastLocation.getLongitude(), path, this);
    }

    @Override
    public void onSubmitPointOfInterestSuccess(PointOfInterest pointOfInterest) {
        editNameDialog.dismiss();
        Toast.makeText(getContext(), "Successfully submitted point of interest!", Toast.LENGTH_SHORT).show();
        pointOfInterest.makeMarker(mMap, BitmapDescriptorFactory.HUE_RED);
    }

    @Override
    public void onSubmitPointOfInterestFailure() {
        Toast.makeText(getContext(), "Failed to submit point of interest...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPictureDialog(EditPictureDialog editPictureDialog) {
        this.editPictureDialog = editPictureDialog;
    }

    @Override
    public void onEditPicture() {
        editPictureDialog.dismiss();
        Toast.makeText(getContext(), "Successfully added picture!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePicture() {

    }
}
