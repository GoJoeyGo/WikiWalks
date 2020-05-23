package com.wikiwalks.wikiwalks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 0;
    Bundle savedInstanceState = null;

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
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (savedInstanceState == null) {
                        initialiseMap();
                    }
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, PermissionsFragment.newInstance())
                            .commitNow();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initialiseMap() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, MapsFragment.newInstance())
                .commitNow();
    }

    @Override
    public void onBackPressed(){
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();

        } else {
            finish();
        }
    }

    public static String getDeviceId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("preferences", MODE_PRIVATE);
        if (!preferences.contains("device_id")) {
            preferences.edit().putString("device_id", UUID.randomUUID().toString()).commit();
        }
        return preferences.getString("device_id", null);
    }
}
