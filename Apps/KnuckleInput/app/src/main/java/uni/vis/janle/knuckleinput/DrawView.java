package uni.vis.janle.knuckleinput;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;

public class DrawView extends View {
    Paint paint = new Paint();

    private CapacitiveImageTS capacitiveImage;
    private int boxWidth = 1080 / 15;
    private int boxHeight = 1920 / 27;

    public DrawView(Context context) {
        super(context);
    }

    public void setImage(CapacitiveImageTS image) {
        this.capacitiveImage = image;
        invalidate();
    }


    @Override
    public void onDraw(Canvas canvas) {
        System.out.println("capture");
        paint.setStyle(Paint.Style.FILL);
        int[][] matrix = capacitiveImage.getMatrix();

        // Draw capacitive matrix
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                int val = matrix[i][j];

                if (val < 0)
                    val = 0;

                if (val > 255)
                    val = 255;

                // Draw rectangle
                paint.setColor(new Color().rgb(val, val, val));
                paint.setAlpha(50);
                Rect r = new Rect(j * boxWidth, (i) * boxHeight, (j + 1) * boxWidth, (i + 1) * boxHeight);
                canvas.drawRect(r, paint);

                // Write number
                paint.setTextSize(15);
                paint.setColor(new Color().rgb(255 - val, 255 - val, 255 - val));
                paint.setAlpha(50);
                canvas.drawText(val + "", j * boxWidth + (int) (0.5 * boxWidth), i * boxHeight + (int) (0.5 * boxHeight), paint);
            }
        }
    }
}