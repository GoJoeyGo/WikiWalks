package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.SetNameDialog;
import com.wikiwalks.wikiwalks.ui.dialogs.SubmissionDialog;

public class SettingsFragment extends Fragment {

    public static SettingsFragment newInstance() {

        Bundle args = new Bundle();

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.settings_fragment, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        Button setNameButton = rootView.findViewById(R.id.settings_set_name_button);
        setNameButton.setOnClickListener(v -> {
                SetNameDialog dialog = new SetNameDialog();
                dialog.setTargetFragment(this, 0);
                dialog.show(getParentFragmentManager(), "NamePopup");
        });
        return rootView;
    }
}
