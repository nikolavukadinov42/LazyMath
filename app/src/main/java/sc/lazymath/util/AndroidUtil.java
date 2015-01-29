package sc.lazymath.util;

import android.app.Activity;
import android.graphics.Rect;
import android.util.Log;
import android.view.Window;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.List;

import sc.lazymath.ocr.neuralnetwork.NeuralNetwork;

/**
 * Created by nikola42 on 1/27/2015.
 */
public class AndroidUtil {
    public static int getStatusBarHeight(Activity activity){
        Rect rectangle = new Rect();

        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        int statusBarHeight = rectangle.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();


        return  contentViewTop - statusBarHeight;
    }
}
