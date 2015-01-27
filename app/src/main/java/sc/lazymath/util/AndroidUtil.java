package sc.lazymath.util;

import android.app.Activity;
import android.graphics.Rect;
import android.view.Window;

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
