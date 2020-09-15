package com.wikiwalks.wikiwalks;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LocationService extends Service {

    public static final String CHANNEL_ID = "ROUTE_RECORDER";
    public static final String START_SERVICE = "START_ROUTE_RECORDER";
    public static final String END_SERVICE = "END_ROUTE_RECORDER";
    public static final String RECORDED_LOCATIONS = "com.wikiwalks.wikiwalks";

    private Location lastLocation;
    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private float distanceWalked;
    private FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            addLocation(locationResult.getLastLocation());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (intent.getAction().equals(START_SERVICE)) {
            createNotification();
            startLocationUpdates();
        } else if (intent.getAction().equals(END_SERVICE)) {
            stopLocationUpdates();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification() {
        Notification notification;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Route recorder service", NotificationManager.IMPORTANCE_NONE);
            channel.setDescription("Foreground service for recording paths");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this, CHANNEL_ID).setOngoing(true).setPriority(NotificationManager.IMPORTANCE_MIN).setCategory(Notification.CATEGORY_SERVICE).setContentTitle("Recording route...").build();
        } else {
            notification = new Notification();
        }
        startForeground(1, notification);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void stopLocationUpdates() {
        Intent returnData = new Intent(RECORDED_LOCATIONS);
        returnData.putExtra("latitudes", latitudes);
        returnData.putExtra("longitudes", longitudes);
        returnData.putExtra("altitudes", altitudes);
        returnData.putExtra("distance_walked", distanceWalked);
        returnData.putParcelableArrayListExtra("latLngs", latLngs);
        sendBroadcast(returnData);
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    private void addLocation(Location location) {
        if (lastLocation == null || (location.distanceTo(lastLocation) > 2) && location.distanceTo(lastLocation) < 25) {
            if (lastLocation != null) {
                distanceWalked += location.distanceTo(lastLocation);
            }
            latitudes.add(location.getLatitude());
            longitudes.add(location.getLongitude());
            altitudes.add(location.getAltitude());
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
            lastLocation = location;
        }
    }
}
