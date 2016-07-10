package com.keemsa.news;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by sebastian on 09/07/16.
 */
public class DownloadThumbnailTask extends AsyncTask<String, Void, Bitmap> {

    ImageView img_thumbnail;

    public DownloadThumbnailTask(ImageView img_thumbnail) {
        this.img_thumbnail = img_thumbnail;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        String urlThumbnail = strings[0];
        Bitmap thumb = null;
        try{
            InputStream is = new java.net.URL(urlThumbnail).openStream();
            thumb = BitmapFactory.decodeStream(is);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return  thumb;
    }

    protected void onPostExecute(Bitmap result){
        img_thumbnail.setImageBitmap(result);
    }
}
