package sc.lazymath.entities;

import android.graphics.Bitmap;
import android.net.Uri;

import java.net.URL;

/**
 * WA pod can contain text or image.
 */
public class WolframAlphaPod {

    private String text;
    private Bitmap image;

    public WolframAlphaPod() {
    }

    public WolframAlphaPod(String text, Bitmap image) {
        this.text = text;
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Pod{" +
                "text='" + text + '\'' +
                '}';
    }
}

