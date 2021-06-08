package martino.barcodedistancefinder2.barcodeGraphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.View;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import martino.barcodedistancefinder2.activities.BarcodeCaptureActivity;

public class GraphicOverlay<T extends BarcodeGraphic> extends View {
    private final Object mLock = new Object();
    private Set<T> mGraphics;
    private ConcurrentHashMap mGraphicSet = new ConcurrentHashMap();
    private BarcodeCaptureActivity.BarcodeDetectedListener listener;
    private Size inputImageSizeReference;

    public GraphicOverlay(Context context) {
        super(context);
        mGraphics = Collections.newSetFromMap(mGraphicSet);
    }
    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGraphics = Collections.newSetFromMap(mGraphicSet);
    }
    public void setImageSizeReference(int width, int height){
        inputImageSizeReference = new Size(width, height);
    }
    public void setListener(BarcodeCaptureActivity.BarcodeDetectedListener listener){
        this.listener = listener;
    }
    public  Set<T> getGraphics(){
        return mGraphics;
    }
    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
        }
        this.listener.onChangingNumberOfBarcodeDetected(mGraphics);
        postInvalidate();
    }
    /**
     * Adds a graphic to the overlay.
     */
    public void add(T graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
        }

        synchronized (mLock){
            this.listener.onChangingNumberOfBarcodeDetected(mGraphics);
        }
        postInvalidate();

    }

    /**
     * Removes a graphic from the overlay.
     */
    public void remove(T graphic) {
        synchronized (mLock) {
            mGraphics.remove(graphic);
        }
        synchronized (mLock){
            this.listener.onChangingNumberOfBarcodeDetected(mGraphics);
        }
        postInvalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        synchronized (mLock) {
            for (T graphic : mGraphics) {
                /*
                Paint p = new Paint();
                p.setColor(Color.CYAN);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(4.0f);
                */
                graphic.draw(canvas, inputImageSizeReference);
                /*
                RectF imageAnalyzed = new RectF();
                imageAnalyzed.set(0,0, inputImageSizeReference.getWidth(), inputImageSizeReference.getHeight());
                canvas.drawRect(imageAnalyzed, p);
                RectF canvasRect = new RectF();


                canvasRect.set(0,0, canvas.getWidth(), canvas.getHeight());
                canvas.drawRect(canvasRect, p);

                 */
            }


        }

    }

}
