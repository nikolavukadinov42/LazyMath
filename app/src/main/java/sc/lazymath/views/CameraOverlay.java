package sc.lazymath.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Button;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by nikola42 on 1/26/2015.
 */
public class CameraOverlay extends ImageView {

    private List<Button> buttons;

    public CameraOverlay(Context context) {
        super(context);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if(buttons != null) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;

            int x = getWidth();
            int y = getHeight();

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);

            for (Button button : buttons) {
                int[] values = new int[4];
                values[0] = button.getLeft();
                values[1] = button.getRight();
                values[2] = button.getTop();
                values[3] = button.getBottom();

                for (int i = 0; i <= 1; i++) {
                    if (values[i] > maxX) {
                        maxX = values[i];
                    }

                    if (values[i] < minX) {
                        minX = values[i];
                    }
                }

                for (int i = 2; i <= 3; i++) {
                    if (values[i] > maxY) {
                        maxY = values[i];
                    }

                    if (values[i] < minY) {
                        minY = values[i];
                    }
                }
            }

            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(150);

            canvas.drawRect(0, 0, x, minY, paint);
            canvas.drawRect(0, minY, minX, y, paint);
            canvas.drawRect(maxX, minY, x, y, paint);
            canvas.drawRect(minX, maxY, maxX, y, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);

            int dX = maxX-minX;
            int dY = maxY - minY;

            canvas.drawLine(minX, 0, minX, y, paint);
            canvas.drawLine(minX + dX / 2, 0, minX + dX / 2, y, paint);
            canvas.drawLine(maxX, 0, maxX, y, paint);
            canvas.drawLine(0, minY, x, minY, paint);
            canvas.drawLine(0, minY + dY / 2, x, minY + dY / 2, paint);
            canvas.drawLine(0, maxY, x, maxY, paint);
        }
    }

    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }
}
