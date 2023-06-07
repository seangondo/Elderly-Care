package com.tugasakhir.elderlycare.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tugasakhir.elderlycare.R;

import java.util.ArrayList;

public class CustomAlarmLogAdapter extends BaseAdapter {
    Context c;
    ArrayList date, time, type, msg, status;
    LayoutInflater inflater;

    public CustomAlarmLogAdapter(Context c, ArrayList date, ArrayList time, ArrayList type, ArrayList msg, ArrayList status) {
        this.c = c;
        this.date = date;
        this.time = time;
        this.type = type;
        this.msg = msg;
        this.status = status;
        inflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return date.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.lv_alarmlog_selected, null);
        TextView mydate = (TextView) view.findViewById(R.id.tvDate);
        TextView mytime = (TextView) view.findViewById(R.id.tvTime);
        TextView mymsg = (TextView) view.findViewById(R.id.tvMessage);
        ImageButton img = (ImageButton) view.findViewById(R.id.clear);

        View.OnClickListener clickList = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("delete_log");

                intent.putExtra("id", i);
                intent.putExtra("date", String.valueOf(date.get(i)));
                intent.putExtra("time", String.valueOf(time.get(i)));
                intent.putExtra("type", String.valueOf(type.get(i)));
                intent.putExtra("stats", String.valueOf(status.get(i)));
                intent.putExtra("msg", String.valueOf(msg.get(i)));
                c.sendBroadcast(intent);
            }
        };

        img.setOnClickListener(clickList);

        mydate.setText(String.valueOf("Date : " + date.get(i)));
        mytime.setText(String.valueOf("Time : " + time.get(i)));
        mymsg.setText(String.valueOf(msg.get(i)));

        if(String.valueOf(status.get(i)).equals("1")) {
            view.setBackgroundColor(Color.YELLOW);
        } else {
            view.setBackgroundColor(Color.WHITE);
        }

        return view;
    }
}
