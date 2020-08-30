package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.wikiwalks.wikiwalks.ui.MapsFragment;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    static RetrofitRequests retrofitRequests;
    Bundle savedInstanceState = null;

    public interface CheckPermissionsCallback {
        void onCheckPermissionsResult(boolean granted);
    }

    public static void checkPermission(FragmentActivity fragmentActivity, String permission, CheckPermissionsCallback callback) {
        if (ContextCompat.checkSelfPermission(fragmentActivity, permission) == PackageManager.PERMISSION_GRANTED) {
            callback.onCheckPermissionsResult(true);
        } else {
            fragmentActivity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), callback::onCheckPermissionsResult).launch(permission);
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
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, MapsFragment.newInstance()).commitNow();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();

        } else {
            finish();
        }
    }
}
