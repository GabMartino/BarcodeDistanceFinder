package martino.barcodedistancefinder2.utilities;

import android.os.AsyncTask;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class BarcodeAsyncDetector extends AsyncTask<Frame, Void, Void> {
    BarcodeDetector detector;
    public BarcodeAsyncDetector(BarcodeDetector detector) {
        this.detector = detector;

    }


    @Override
    protected Void doInBackground(Frame... frames) {
        detector.detect(frames[0]);
        return null;
    }
}
