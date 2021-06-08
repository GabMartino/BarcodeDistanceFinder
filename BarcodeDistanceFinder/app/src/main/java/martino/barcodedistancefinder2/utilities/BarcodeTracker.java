package martino.barcodedistancefinder2.utilities;

import android.util.Log;
import android.view.View;

import martino.barcodedistancefinder2.barcodeGraphic.*;
import androidx.annotation.NonNull;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

public class BarcodeTracker  extends Tracker<Barcode> {
    private GraphicOverlay overlay;
    private BarcodeGraphic mBarcodeGraphic;
    BarcodeGraphic barcodeGraphic;
    public BarcodeTracker(GraphicOverlay overlay) {
        super();
        this.overlay = overlay;
    }

    @Override
    public void onNewItem(int i, @NonNull Barcode barcode) {

        super.onNewItem(i, barcode);
        barcodeGraphic = new BarcodeGraphic(barcode);
        overlay.add(barcodeGraphic);
        Log.w("DEBUG","BARCODE FOUND");
    }

    @Override
    public void onUpdate(@NonNull Detector.Detections<Barcode> detections, @NonNull Barcode barcode) {
        super.onUpdate(detections, barcode);
        overlay.remove(barcodeGraphic);
        barcodeGraphic.updateBarcode(barcode);
        overlay.add(barcodeGraphic);
    }

    @Override
    public void onMissing(@NonNull Detector.Detections<Barcode> detections) {
        super.onMissing(detections);
        overlay.remove(barcodeGraphic);
    }

    @Override
    public void onDone() {
        super.onDone();
        overlay.remove(barcodeGraphic);
    }
}
