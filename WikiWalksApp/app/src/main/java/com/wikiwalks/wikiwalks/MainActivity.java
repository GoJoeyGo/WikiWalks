package com.wikiwalks.wikiwalks;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.wikiwalks.wikiwalks.ui.MapsFragment;
import com.wikiwalks.wikiwalks.ui.PermissionsFragment;

import java.io.File;
import java.util.UUID;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 0;
    static RetrofitRequests retrofitRequests;
    Bundle savedInstanceState = null;

    public static String getDeviceId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("preferences", MODE_PRIVATE);
        if (!preferences.contains("device_id")) {
            preferences.edit().putString("device_id", UUID.randomUUID().toString()).apply();
        }
        return preferences.getString("device_id", null);
    }

    public static void checkLocationPermission(FragmentActivity fragmentActivity) {
        if (ActivityCompat.checkSelfPermission(fragmentActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(fragmentActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fragmentActivity.getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, PermissionsFragment.newInstance(PermissionsFragment.PermissionType.LOCATION)).commitNow();
        }
    }

    public static RetrofitRequests getRetrofitRequests(Context context) {
        if (retrofitRequests == null) {
            retrofitRequests = new Retrofit.Builder().baseUrl(context.getString(R.string.local_url)).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitRequests.class);
        }
        return retrofitRequests;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            if (savedInstanceState == null) {
                initialiseMap();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (savedInstanceState == null) {
                    initialiseMap();
                }
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame, PermissionsFragment.newInstance(PermissionsFragment.PermissionType.LOCATION))
                        .commitNow();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initialiseMap() {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, MapsFragment.newInstance()).commitNow();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();

        } else {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        for (File file : getExternalCacheDir().listFiles()) {
            if (file.getAbsolutePath().endsWith(".jpg")) {
                file.delete();
            }
        }
        super.onDestroy();
    }
}
