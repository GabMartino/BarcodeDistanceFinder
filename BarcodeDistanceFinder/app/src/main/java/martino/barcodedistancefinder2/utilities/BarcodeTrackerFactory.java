package martino.barcodedistancefinder2.utilities;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import martino.barcodedistancefinder2.barcodeGraphic.GraphicOverlay;

/**
 * This class is a factory that is used to create a set of trackers ( one for each barcode detected)
 *
 *
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private GraphicOverlay overlay;
    public BarcodeTrackerFactory( GraphicOverlay overlay) {
            this.overlay = overlay;

    }

    /**
     * This method return a tracker of an object Barcode
     * @param barcode
     * @return
     */
    @NonNull
    @Override
    public Tracker<Barcode> create(@NonNull Barcode barcode) {
        return new BarcodeTracker(overlay);
    }
}
