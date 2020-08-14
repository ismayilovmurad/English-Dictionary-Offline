package com.muradismayilov.englishdictionaryoffline.tools;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.muradismayilov.englishdictionaryoffline.R;

class MyViewHolder extends RecyclerView.ViewHolder {
    final MaterialCardView recycler_item_mainCV;
    final MaterialTextView recycler_item_wordTV;
    final MaterialTextView recycler_item_definitionTV;

    MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recycler_item_mainCV = itemView.findViewById(R.id.recycler_item_mainCV);
        recycler_item_wordTV = itemView.findViewById(R.id.recycler_item_wordTV);
        recycler_item_definitionTV = itemView.findViewById(R.id.recycler_item_definitionTV);
    }
}
