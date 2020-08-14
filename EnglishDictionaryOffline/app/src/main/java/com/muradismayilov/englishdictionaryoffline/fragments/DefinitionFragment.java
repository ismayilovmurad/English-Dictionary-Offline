package com.muradismayilov.englishdictionaryoffline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textview.MaterialTextView;
import com.muradismayilov.englishdictionaryoffline.R;
import com.muradismayilov.englishdictionaryoffline.activities.MeaningActivity;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DefinitionFragment extends Fragment {

    // UI Components
    // MaterialTextView
    @BindView(R.id.fragment_meaning_mainTV)
    MaterialTextView fragment_meaning_mainTV;

    // String
    @BindString(R.string.no_definition_found)
    String no_definition_found;

    // Variables
    // String
    private String definition;

    public DefinitionFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meaning, container, false);
        ButterKnife.bind(this, view);
        initialFunctions();
        return view;
    }

    private void initialFunctions() {
        declareVariables();
        setText();
    }

    private void declareVariables() {
        // Context
        Context context = getActivity();
        assert context != null;
        // String
        definition = ((MeaningActivity) context).definition;
    }

    private void setText() {
        if (definition != null) {
            fragment_meaning_mainTV.setText(definition);
        } else {
            fragment_meaning_mainTV.setText(no_definition_found);
        }
    }
}