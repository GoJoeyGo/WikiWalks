package com.wikiwalks.wikiwalks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionsFragment extends Fragment {
    Button permissionsButton;

    public static PermissionsFragment newInstance() {
        Bundle args = new Bundle();
        PermissionsFragment fragment = new PermissionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.permissions_fragment, container, false);
        permissionsButton = rootView.findViewById(R.id.permissions_button);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName())));

            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, MapsFragment.newInstance())
                            .commitNow();
                }
            });
        }
    }


}
