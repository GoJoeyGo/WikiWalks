package com.wikiwalks.wikiwalks.ui;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.wikiwalks.wikiwalks.R;

public class PermissionsFragment extends Fragment {
    Button permissionsButton;
    PermissionType type;

    public enum PermissionType {LOCATION, STORAGE}

    public static PermissionsFragment newInstance(PermissionType type) {
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        PermissionsFragment fragment = new PermissionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.permissions_fragment, container, false);
        TextView permissionInfo = rootView.findViewById(R.id.permission_info);
        type = (PermissionType) getArguments().getSerializable("type");
        if (type == PermissionType.LOCATION) {
            permissionInfo.setText("WikiWalks needs location permissions to run.");
            Toast.makeText(getContext(), "Test", Toast.LENGTH_SHORT).show();
        } else if (type == PermissionType.STORAGE) {
            permissionInfo.setText("WikiWalks needs storage permissions to submit photos.");
        }
        permissionsButton = rootView.findViewById(R.id.permissions_button);
        permissionsButton.setOnClickListener(v -> startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()))));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (type == PermissionType.LOCATION) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Handler handler = new Handler();
                handler.post(() -> getParentFragmentManager().beginTransaction().replace(R.id.main_frame, MapsFragment.newInstance()).commitNow());
            }
        } else if (type == PermissionType.STORAGE) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Handler handler = new Handler();
                handler.post(() -> getParentFragmentManager().popBackStack());
            }
        }
    }


}
