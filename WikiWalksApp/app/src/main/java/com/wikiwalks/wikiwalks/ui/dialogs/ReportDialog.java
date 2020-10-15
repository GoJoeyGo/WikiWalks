package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDialog extends DialogFragment {

    public enum ReportType {PATH, POINT_OF_INTEREST, PHOTO, REVIEW}

    private ReportType type;
    private int id;
    private TextInputLayout text;
    private Button sendButton;

    public static ReportDialog newInstance(ReportType type, int objectId) {
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        args.putInt("id", objectId);
        ReportDialog fragment = new ReportDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.DialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.report_dialog, null);
        builder.setTitle(R.string.report_title);

        text = view.findViewById(R.id.report_dialog_report_input);

        sendButton = view.findViewById(R.id.report_dialog_save_button);
        sendButton.setOnClickListener(v -> sendReport());

        Button cancelButton = view.findViewById(R.id.report_dialog_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());

        type = (ReportType) getArguments().getSerializable("type");
        id = getArguments().getInt("id");

        if (savedInstanceState != null) {
            text.getEditText().setText(savedInstanceState.getString("text"));
        }

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("text", text.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void sendReport() {
        sendButton.setEnabled(false);
        JsonObject request = new JsonObject();
        request.addProperty("type", type.name());
        request.addProperty("id", id);
        request.addProperty("info", text.getEditText().getText().toString());
        request.addProperty("device_id", PreferencesManager.getInstance(getContext()).getDeviceId());
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), request.toString());
        Call<JsonElement> sendReport = MainActivity.getRetrofitRequests(getContext()).sendReport(body);
        sendReport.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), R.string.report_success, Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                else {
                    Toast.makeText(getContext(), R.string.report_failure, Toast.LENGTH_SHORT).show();
                    sendButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                sendButton.setEnabled(true);
                Log.e("Report", "Sending report", t);
                Toast.makeText(getContext(), R.string.report_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
