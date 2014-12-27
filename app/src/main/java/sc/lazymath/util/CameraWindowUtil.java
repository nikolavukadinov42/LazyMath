package sc.lazymath.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;

import sc.lazymath.R;

/**
 * Created by nikola42 on 12/27/2014.
 */
public class CameraWindowUtil {

    private static Point size;
    private static final float marginX = 250;
    private static final float marginY = 300;

    public static void initCorners(final Activity activity) {
        Button seButton = (Button) activity.findViewById(R.id.button_se);
        Button swButton = (Button) activity.findViewById(R.id.button_sw);
        Button neButton = (Button) activity.findViewById(R.id.button_ne);
        Button nwButton = (Button) activity.findViewById(R.id.button_nw);

        // get screen size
        Display display = activity.getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        View.OnTouchListener myOnTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    moveCorner(activity, (Button) view, e);
                }
                return true;
            }
        };

        seButton.setOnTouchListener(myOnTouchListener);
        swButton.setOnTouchListener(myOnTouchListener);
        neButton.setOnTouchListener(myOnTouchListener);
        nwButton.setOnTouchListener(myOnTouchListener);
    }

    public static void moveCorner(Activity activity, Button button, MotionEvent e) {
        int width = button.getWidth();
        int height = button.getHeight();

        float x = e.getRawX() - width / 2;
        float y = e.getRawY() - height * 2;

        boolean flagX = false;
        boolean flagY = false;

        // move adjacent buttons
        switch (button.getId()) {
            case R.id.button_se: {
                Button neButton = (Button) activity.findViewById(R.id.button_ne);
                Button swButton = (Button) activity.findViewById(R.id.button_sw);

                if (((x - swButton.getX()) < marginX)
                    //|| (x > (size.x - (width / 2)))
                        ) {
                    flagX = true;
                } else {
                    // move sw by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) neButton.getY());
                    neButton.setLayoutParams(params);
                }

                if (((y - neButton.getY()) < marginY)
                    //|| (y > (size.y - (height / 2)))
                        ) {
                    flagY = true;
                } else {
                    // move ne by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) swButton.getX(), (int) y);
                    swButton.setLayoutParams(params);
                }
            }
            break;
            case R.id.button_sw: {
                Button nwButton = (Button) activity.findViewById(R.id.button_nw);
                Button seButton = (Button) activity.findViewById(R.id.button_se);

                if (((seButton.getX() - x) < marginX)
                    //|| (x < (width / 2))
                        ) {
                    flagX = true;
                } else {
                    // move se by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) nwButton.getY());
                    nwButton.setLayoutParams(params);
                }

                if (((y - nwButton.getY()) < marginY)
                    //|| (y > (size.y - (height / 2)))
                        ) {
                    flagY = true;
                } else {
                    // move nw by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) seButton.getX(), (int) y);
                    seButton.setLayoutParams(params);
                }
            }
            break;
            case R.id.button_ne: {
                Button seButton = (Button) activity.findViewById(R.id.button_se);
                Button nwButton = (Button) activity.findViewById(R.id.button_nw);

                if (((x - nwButton.getX()) < marginX)
                    //|| (x > (size.x - (width / 2)))
                        ) {
                    flagX = true;
                } else {
                    // move se by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) seButton.getY());
                    seButton.setLayoutParams(params);
                }

                if (((seButton.getY() - y) < marginY)
                    //|| (y < (height / 2))
                        ) {
                    flagY = true;
                } else {
                    // move nw by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) nwButton.getX(), (int) y);
                    nwButton.setLayoutParams(params);
                }
            }
            break;
            case R.id.button_nw: {
                Button swButton = (Button) activity.findViewById(R.id.button_sw);
                Button neButton = (Button) activity.findViewById(R.id.button_ne);

                if (((neButton.getX() - x) < marginX)
                    //|| (x < (width / 2))
                        ) {
                    flagX = true;
                } else {
                    // move sw by x
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) x, (int) swButton.getY());
                    swButton.setLayoutParams(params);
                }

                if (((swButton.getY() - y) < marginY)
                    //|| (y < (height / 2))
                        ) {
                    flagY = true;
                } else {
                    // move ne by y
                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                            height, (int) neButton.getX(), (int) y);
                    neButton.setLayoutParams(params);
                }
            }
            break;
            default:
                break;
        }

        // move button
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width,
                height, (int) (flagX ? button.getX() : x), (int) (flagY ? button.getY() : y));
        button.setLayoutParams(params);
    }

    private static class Window extends View {

        private Button seButton;
        private Button nwButton;

        public Window(Context context, Button seButton, Button nwButton) {
            super(context);

            this.seButton = seButton;
            this.nwButton = nwButton;
        }

        @Override
        public void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(nwButton.getLeft(), nwButton.getTop(),
                    seButton.getRight(), seButton.getBottom(), paint);
        }
    }
}
