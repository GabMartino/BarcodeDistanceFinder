package martino.barcodedistancefinder2.barcodeGraphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.util.Size;
import android.view.View;

import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeGraphic  {

    private static int mCurrentColorIndex = 0;
    private Barcode mBarcode;

    private Paint mRectPaint;
    private Paint mTextPaint;
    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.RED,
            Color.YELLOW
    };

    public BarcodeGraphic(Barcode barcode) {
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mRectPaint = new Paint();
        mRectPaint.setColor(selectedColor);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(4.0f);

        mTextPaint = new Paint();
        mTextPaint.setColor(selectedColor);
        mTextPaint.setTextSize(36.0f);
        this.mBarcode = barcode;
    }
    public Barcode getBarcode(){
        return mBarcode;
    }
    public void updateBarcode(Barcode barcode){
        this.mBarcode = barcode;
    }
    public void draw(Canvas canvas, Size referenceSize){

        if(mBarcode == null){
            return;
        }
        RectF rect = new RectF(mBarcode.getBoundingBox());
        rect.set(rect.left, rect.top + (canvas.getHeight() - referenceSize.getHeight())/2, rect.right, rect.bottom + (canvas.getHeight() - referenceSize.getHeight())/2);
        canvas.drawRect(rect, mRectPaint);


    }


}
