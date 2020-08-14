package com.muradismayilov.englishdictionaryoffline.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.muradismayilov.englishdictionaryoffline.R;
import com.muradismayilov.englishdictionaryoffline.activities.MeaningActivity;
import com.muradismayilov.englishdictionaryoffline.model.History;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private final Context context;
    private final List<History> historyList;

    public MyAdapter(Context context, List<History> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.recycler_item_wordTV.setText(historyList.get(position).get_en_word());
        holder.recycler_item_definitionTV.setText(historyList.get(position).get_def());
        holder.recycler_item_mainCV.setOnClickListener(v -> go(position));
    }

    private void go(int position) {
        String text = historyList.get(position).get_en_word();
        Intent intent = new Intent(context, MeaningActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("en_word", text);
        intent.putExtras(bundle);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
