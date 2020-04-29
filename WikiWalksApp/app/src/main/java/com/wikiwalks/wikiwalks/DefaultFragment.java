package com.wikiwalks.wikiwalks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DefaultFragment extends Fragment {

    FloatingActionButton floatingActionButton;

    public static DefaultFragment newInstance() {
        Bundle args = new Bundle();
        DefaultFragment fragment = new DefaultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.default_fragment, container, false);
        floatingActionButton = rootView.findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText( getContext(),"Test" , Toast.LENGTH_SHORT)
                        .show();
            }
        });
        return rootView;
    }
}
