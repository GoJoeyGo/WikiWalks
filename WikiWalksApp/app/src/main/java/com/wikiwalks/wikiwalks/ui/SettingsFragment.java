package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.JsonElement;
import com.wikiwalks.wikiwalks.CustomActivityResultContracts;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.EditNameDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment implements EditNameDialog.EditDialogListener {

    EditNameDialog editNameDialog;
    ActivityResultLauncher<String> exportSettings = registerForActivityResult(new CustomActivityResultContracts.ExportSettings(), uri -> {
        if (uri != null) {
            PreferencesManager.getInstance(getContext()).exportPreferences(uri);
        }
    });
    ActivityResultLauncher<String[]> importSettings = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
        if (uri != null) {
            PreferencesManager.getInstance(getContext()).importPreferences(uri);
        }
    });

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
        try {
            request.put("device_id", PreferencesManager.getInstance(getContext()).getDeviceId());
            request.put("name", name);
            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
            Call<JsonElement> setName = MainActivity.getRetrofitRequests(getContext()).setName(body);
            setName.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        editNameDialog.dismiss();
                        PreferencesManager.getInstance(getContext()).setName(name);
                        Toast.makeText(getContext(), R.string.save_name_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.save_name_failure, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.e("SettingsFragment", "Sending name update request", t);
                    Toast.makeText(getContext(), R.string.save_name_failure, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            Log.e("SettingsFragment", "Creating name update request", e);
            Toast.makeText(getContext(), R.string.save_name_failure, Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.settings_fragment, container, false);

        MaterialToolbar toolbar = rootView.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(R.string.settings_title);

        Button setNameButton = rootView.findViewById(R.id.settings_set_name_button);
        setNameButton.setOnClickListener(v -> EditNameDialog.newInstance(EditNameDialog.EditNameDialogType.USERNAME, -1).show(getChildFragmentManager(), "NamePopup"));

        Button statisticsButton = rootView.findViewById(R.id.settings_statistics_button);
        statisticsButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, StatisticsFragment.newInstance()).addToBackStack(null).commit());

        Button goalsButton = rootView.findViewById(R.id.settings_goals_button);
        goalsButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, GoalsFragment.newInstance()).addToBackStack(null).commit());

        Button exportSettingsButton = rootView.findViewById(R.id.settings_export_settings_button);
        exportSettingsButton.setOnClickListener(v -> MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, granted -> {
            if (granted) {
                exportSettings.launch("wikiwalks_backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json");
            } else {
                getParentFragmentManager().beginTransaction().add(R.id.main_frame, PermissionsFragment.newInstance(Manifest.permission.WRITE_EXTERNAL_STORAGE)).addToBackStack(null).commit();
            }
        }));

        Button importSettingsButton = rootView.findViewById(R.id.settings_import_settings_button);
        importSettingsButton.setOnClickListener(v -> MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, granted -> {
            if (granted) {
                importSettings.launch(new String[]{"application/json", "application/octet-stream"});
            } else {
                getParentFragmentManager().beginTransaction().add(R.id.main_frame, PermissionsFragment.newInstance(Manifest.permission.WRITE_EXTERNAL_STORAGE)).addToBackStack(null).commit();
            }
        }));

        return rootView;
    }
}
