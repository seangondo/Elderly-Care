package com.tugasakhir.elderlycare.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tugasakhir.elderlycare.R;
import com.tugasakhir.elderlycare.api.RecyclerViewInterface;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    ArrayList<Integer> elder_id = new ArrayList<Integer>();
    ArrayList<String> myName = new ArrayList<>();
    ArrayList<String> myAddress = new ArrayList<>();
    ArrayList<String> myImage = new ArrayList<>();
    Context mContext;
    LayoutInflater inflater;

    public RecyclerViewAdapter(Context mContext, ArrayList<Integer> elder_id, ArrayList<String> myName, ArrayList<String> myAddress,
                               ArrayList<String> myImage, RecyclerViewInterface recyclerViewInterface) {
        this.elder_id = elder_id;
        this.myName = myName;
        this.myAddress = myAddress;
        this.myImage = myImage;
        this.mContext = mContext;
        this.recyclerViewInterface = recyclerViewInterface;
        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selection_card, parent, false);

        return new ViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        holder.elderImage.setImageURI(Uri.parse("https://private-server.uk.to/images/elder1.jpg"));
        Glide.with(mContext).asBitmap().load(myImage.get(position)).apply(new RequestOptions().override(600, 200)).into(holder.elderImage);
        holder.name.setText(myName.get(position));
        holder.address.setText(myAddress.get(position));
//        holder.cardElder.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                elderSelected = elder_id.get(position);
//                elderName = myName.get(position);
////                if ((recyclerViewInterface != null) && (position != RecyclerView.NO_POSITION)) {
////                    recyclerViewInterface.onItemClick(position);
////                }
////                Toast.makeText(mContext, myName.get(position), Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return myName.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;
        ImageView elderImage;
        CardView cardElder;
        public ViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            cardElder = itemView.findViewById(R.id.cardElder);
            name = itemView.findViewById(R.id.nameElder);
            address = itemView.findViewById(R.id.addressElder);
            elderImage = itemView.findViewById(R.id.imageElder);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (recyclerViewInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}
