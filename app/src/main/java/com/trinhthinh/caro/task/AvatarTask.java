package com.trinhthinh.caro.task;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.trinhthinh.caro.model.User;

import java.net.URL;

public class AvatarTask extends AsyncTask<Void,Bitmap,Void> {
    private final ImageView avatar;
    private final User user;
    private final ProgressDialog dialog;

    public AvatarTask(ImageView avatar, User user, ProgressDialog dialog) {
        this.avatar = avatar;
        this.user = user;
        this.dialog = dialog;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(user.getInfo().getAvatarPath());
            Bitmap image = BitmapFactory
                    .decodeStream(url.openConnection().getInputStream());
            publishProgress(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
       Bitmap avatarImage = values[0];
       this.avatar.setImageBitmap(avatarImage);
       if(this.dialog instanceof Dialog) {
           this.dialog.dismiss();
       }
    }
}
