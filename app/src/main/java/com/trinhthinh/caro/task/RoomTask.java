package com.trinhthinh.caro.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.trinhthinh.caro.model.Room;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class RoomTask extends AsyncTask<Void, Bitmap,Void> {
    private final ImageView roomImage;
    private final Room room;

    public RoomTask(ImageView roomImage, Room room) {
        this.roomImage = roomImage;
        this.room = room;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL(room.getUser1().getInfo().getAvatarPath());
            Bitmap image = BitmapFactory
                    .decodeStream(url.openConnection().getInputStream());
            publishProgress(image);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
        Bitmap image = values[0];
        this.roomImage.setImageBitmap(image);
    }
}
