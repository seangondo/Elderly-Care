package com.tugasakhir.elderlycare.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RecyclerViewInterface;

import java.util.ArrayList;

public class RecycleSensorAdapter extends RecyclerView.Adapter<RecycleSensorAdapter.ViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    ArrayList<String> sensorTitle = new ArrayList<>();
    ArrayList<String> sensorText = new ArrayList<>();
    ArrayList<String> sensorTrend = new ArrayList<>();
    Context mContext;
    LayoutInflater inflater;

    public RecycleSensorAdapter(ArrayList<String> sensorTitle, ArrayList<String> sensorTrend,
                                Context mContext, RecyclerViewInterface recyclerViewInterface) {
        this.sensorTitle = sensorTitle;
        this.sensorTrend = sensorTrend;
        this.mContext = mContext;
        this.recyclerViewInterface = recyclerViewInterface;
        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public RecycleSensorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sensor_list, parent, false);

        return new RecycleSensorAdapter.ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleSensorAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.mTitle.setText(sensorTitle.get(position));
        if(sensorTrend.get(position).equals("no")) {
            holder.lineChart.setVisibility(View.GONE);
        }
//        holder.sensorMode.setText(sensorText.get(position));
    }

    @Override
    public int getItemCount() {
        return sensorTitle.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle, sensorMode;
        LineChart lineChart;
        ImageView sensorImage;
        public ViewHolder(@NonNull View itemView, RecyclerViewInterface buttonView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.sensorNameId);
            lineChart = itemView.findViewById(R.id.trendSensor);
//            name = itemView.findViewById(R.id.buttonLabel);
//            sensorMode = itemView.findViewById(R.id.buttonName);
//            sensorImage = itemView.findViewById(R.id.buttonIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("Pressed", "Masok");
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
