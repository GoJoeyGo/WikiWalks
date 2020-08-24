package com.wikiwalks.wikiwalks.ui;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.EditNameDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

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
            attributes.put("device_id", MainActivity.getDeviceId(getContext()));
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
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("preferences", MODE_PRIVATE);
            if (requestCode == REQUEST_CODE_EXPORT) {
                JSONObject prefs = new JSONObject();
                try {
                    for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
                        prefs.put(entry.getKey(), entry.getValue().toString());
                    }
                    OutputStream outputStream = getContext().getContentResolver().openOutputStream(data.getData());
                    Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
                    writer.write(prefs.toString());
                    writer.close();
                } catch (FileNotFoundException | JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                    JsonObject jsonObject = JsonParser.parseReader(new InputStreamReader(inputStream, "UTF-8")).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(entry.getKey(), entry.getValue().getAsString()).apply();
                    }
                } catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
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

        Button setGoalButton= rootView.findViewById(R.id.settings_set_goal_button);
        setGoalButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(rootView.getContext(), SetDistanceGoalFragment.class);
                startActivityForResult(intent, 0);
            }
        });

        Button setNameButton = rootView.findViewById(R.id.settings_set_name_button);
        setNameButton.setOnClickListener(v -> EditNameDialog.newInstance(EditNameDialog.EditNameDialogType.USERNAME, -1).show(getChildFragmentManager(), "NamePopup"));
        Button exportSettingsButton = rootView.findViewById(R.id.settings_export_settings_button);
        exportSettingsButton.setOnClickListener(v -> {
            Intent exportSettingsIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            exportSettingsIntent.addCategory(Intent.CATEGORY_OPENABLE);
            exportSettingsIntent.setType("application/json");
            exportSettingsIntent.putExtra(Intent.EXTRA_TITLE, "wikiwalks_backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json");
            startActivityForResult(exportSettingsIntent, REQUEST_CODE_EXPORT);
        });
        Button importSettingsButton = rootView.findViewById(R.id.settings_import_settings_button);
        importSettingsButton.setOnClickListener(v -> {
            Intent importSettingsIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            importSettingsIntent.setType("application/json");
            importSettingsIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(importSettingsIntent, REQUEST_CODE_IMPORT);
        });
        return rootView;
    }
}
