package sc.lazymath.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;

import sc.lazymath.entities.WolframAlphaPod;

/**
 * Created by dejan on 7.1.2015..
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    WolframAlphaPod wolframAlphaPod;

    public ImageDownloader(WolframAlphaPod wolframAlphaPod) {
        this.wolframAlphaPod = wolframAlphaPod;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap mIcon = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            mIcon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return mIcon;
    }

    protected void onPostExecute(Bitmap result) {
        wolframAlphaPod.setImage(result);

        // TODO resize slike

        wolframAlphaPod.setText("");
    }
}