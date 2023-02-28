package com.tugasakhir.elderlycare;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

public class loadingDialog {
    private Activity activity;
    private AlertDialog dialog;

    loadingDialog(Activity myActivity) {
        activity = myActivity;
    }

    void startDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.my_lvdialog, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    void dismissDialog(){
        dialog.dismiss();
    }
}
