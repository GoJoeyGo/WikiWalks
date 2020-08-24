package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonElement;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.EditNameDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment implements EditNameDialog.EditDialogListener {

    private static final int REQUEST_CODE_EXPORT = 0;
    private static final int REQUEST_CODE_IMPORT = 1;
    EditNameDialog editNameDialog;

    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setEditNameDialog(EditNameDialog editNameDialog) {
        this.editNameDialog = editNameDialog;
    }

    @Override
    public void onEditName(EditNameDialog.EditNameDialogType type, String name) {
        JSONObject request = new JSONObject();
        JSONObject attributes = new JSONObject();
        try {
            attributes.put("device_id", PreferencesManager.getInstance(getContext()).getDeviceId());
            attributes.put("name", name);
            request.put("attributes", attributes);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> setName = MainActivity.getRetrofitRequests(getContext()).setName(body);
            setName.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Name set successfully!", Toast.LENGTH_SHORT).show();
                        getContext().getSharedPreferences("preferences", MODE_PRIVATE).edit().putString("name", name).apply();
                    } else {
                        Toast.makeText(getContext(), "Failed to set name...", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(getContext(), "Failed to set name...", Toast.LENGTH_SHORT).show();
                    Log.e("SET_NAME1", Arrays.toString(t.getStackTrace()));
                }
            });
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Failed to set name...", Toast.LENGTH_SHORT).show();
            Log.e("SET_NAME2", Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_EXPORT) {
                PreferencesManager.getInstance(getContext()).exportPreferences(data.getData());
            } else {
                PreferencesManager.getInstance(getContext()).importPreferences(data.getData());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.settings_fragment, container, false);

        Toolbar toolbar = rootView.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Settings");

        Button setNameButton = rootView.findViewById(R.id.settings_set_name_button);
        setNameButton.setOnClickListener(v -> EditNameDialog.newInstance(EditNameDialog.EditNameDialogType.USERNAME, -1).show(getChildFragmentManager(), "NamePopup"));

        Button exportSettingsButton = rootView.findViewById(R.id.settings_export_settings_button);
        exportSettingsButton.setOnClickListener(v -> MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, (granted -> {
            if (granted) {
                Intent exportSettingsIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                exportSettingsIntent.addCategory(Intent.CATEGORY_OPENABLE);
                exportSettingsIntent.setType("application/json");
                exportSettingsIntent.putExtra(Intent.EXTRA_TITLE, "wikiwalks_backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json");
                startActivityForResult(exportSettingsIntent, REQUEST_CODE_EXPORT);
            } else {
                getParentFragmentManager().beginTransaction().add(R.id.main_frame, PermissionsFragment.newInstance(Manifest.permission.WRITE_EXTERNAL_STORAGE)).addToBackStack(null).commit();
            }
        })));

        Button importSettingsButton = rootView.findViewById(R.id.settings_import_settings_button);
        importSettingsButton.setOnClickListener(v -> MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, granted -> {
            if (granted) {
                Intent importSettingsIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                importSettingsIntent.setType("application/json");
                importSettingsIntent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(importSettingsIntent, REQUEST_CODE_IMPORT);
            } else {
                getParentFragmentManager().beginTransaction().add(R.id.main_frame, PermissionsFragment.newInstance(Manifest.permission.WRITE_EXTERNAL_STORAGE)).addToBackStack(null).commit();
            }
        }));

        return rootView;
    }
}
