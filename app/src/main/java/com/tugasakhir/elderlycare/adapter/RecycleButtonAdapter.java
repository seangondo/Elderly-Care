package com.tugasakhir.elderlycare.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RecyclerViewInterface;

import java.util.ArrayList;

public class RecycleButtonAdapter extends RecyclerView.Adapter<RecycleButtonAdapter.ViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> name = new ArrayList<>();
    ArrayList<String> icon = new ArrayList<>();
    ArrayList<String> status = new ArrayList<>();
    Context mContext;
    LayoutInflater inflater;

    public RecycleButtonAdapter(ArrayList<String> title, ArrayList<String> name, ArrayList<String> status,
                                Context mContext, RecyclerViewInterface recyclerViewInterface) {
        this.title = title;
        this.name = name;
        this.status = status;
//        this.icon = icon;
        this.mContext = mContext;
        this.recyclerViewInterface = recyclerViewInterface;
        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public RecycleButtonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_list, parent, false);

        return new RecycleButtonAdapter.ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTitle.setText(title.get(position));
        holder.mStatus.setText(status.get(position));
        holder.mName.setText(name.get(position));
    }

    @Override
    public int getItemCount() {
        return title.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle, mStatus, mName;
        ImageView mIcon;
        public ViewHolder(@NonNull View itemView, RecyclerViewInterface buttonView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.buttonNameId);
            mStatus = itemView.findViewById(R.id.buttonLabel);
            mName = itemView.findViewById(R.id.buttonName);
            mIcon = itemView.findViewById(R.id.buttonIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (buttonView != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            buttonView.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}