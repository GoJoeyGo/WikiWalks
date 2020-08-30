package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.R;

public class PermissionsFragment extends Fragment {

    private String type;

    public static PermissionsFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString("type", type);
        PermissionsFragment fragment = new PermissionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.permissions_fragment, container, false);

        type = getArguments().getString("type");

        TextView permissionInfo = rootView.findViewById(R.id.permission_info);
        if (type.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionInfo.setText(R.string.requires_location);
        } else if (type.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissionInfo.setText(R.string.requires_storage);
        }

        Button permissionsButton = rootView.findViewById(R.id.permissions_button);
        permissionsButton.setOnClickListener(v -> startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()))));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.checkPermission(this.getActivity(), type, granted -> {
            if (granted) {
                getParentFragmentManager().popBackStack();
            }
        });
    }
}
