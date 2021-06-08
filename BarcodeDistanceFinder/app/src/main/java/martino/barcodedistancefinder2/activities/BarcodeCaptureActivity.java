package martino.barcodedistancefinder2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;


import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import martino.barcodedistancefinder2.R;
import martino.barcodedistancefinder2.barcodeGraphic.BarcodeGraphic;
import martino.barcodedistancefinder2.barcodeGraphic.GraphicOverlay;
import martino.barcodedistancefinder2.cameraUtilities.Camera2Handler;
import martino.barcodedistancefinder2.utilities.BarcodeTrackerFactory;


import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

public class BarcodeCaptureActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    TextureView textureView;
    Camera2Handler camerahandler;
    Float BarcodeReferenceSize;
    Float WarningReferenceDistance;
    BarcodeListener listener;

    ConstraintLayout banner;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(getResources().getString(R.string.BarcodeSizeSettingNamekey))){
            String t = sharedPreferences.getString(key, "50");
            if(!t.equals("")  &&  t.matches("\\d*") ){
                BarcodeReferenceSize = Float.valueOf(t);
            }

            if(listener != null){
                listener.updateObjectReferenceSize(BarcodeReferenceSize);
            }
        }else if(key.equals(getResources().getString(R.string.WarningDistanceNameKey))){
            String t = sharedPreferences.getString(key, "15");
            if(!t.equals("")  &&  t.matches("\\d*") ){
                WarningReferenceDistance = Float.valueOf(t);
            }

        }else if(key.equals(getResources().getString(R.string.DangerousLevelKey))){
            String t = sharedPreferences.getString(key, "0.2");
            if(!t.equals("")  &&  t.matches("\\d*") ){
                Float v = Float.valueOf(t);
                if(v > 0.5 || v < 0){
                    Toast.makeText(getApplicationContext(),
                            "NOT VALID VALUE", Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().putFloat(key,0.2f).commit();

                }
            }

        }

    }


    /**
     * This listener is used to fetch the barcode detected
     */
    public interface BarcodeDetectedListener {
        void onChangingNumberOfBarcodeDetected(Set mGraphics);
    }

    class BarcodeListener implements BarcodeDetectedListener{

        int sampleF = 20;

        List<Float> cameraInfo;
        Float realObjectReferenceSize;
        float correctionfactor;
        private Handler mBackgroundHandler;
        private HandlerThread mBackgroundThread;
        Size ResolutionReference;
        private TextView mytext;
        public BarcodeListener(TextView text, List<Float> info, Float realObjectReferenceSize, float correctionFactor, Size ResolutionReference) {
            mytext = text;
            mBackgroundThread = new HandlerThread("Text Writer Thread");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(Looper.getMainLooper());
            this.correctionfactor = correctionFactor;
            this.ResolutionReference = ResolutionReference;
            cameraInfo = info;
            this.realObjectReferenceSize = realObjectReferenceSize;
        }
        public void updateObjectReferenceSize(Float realObjectReferenceSize){
            this.realObjectReferenceSize = realObjectReferenceSize;
        }
        @SuppressLint("ResourceAsColor")
        @Override
        public void onChangingNumberOfBarcodeDetected(Set mGraphics) {
            if(mGraphics != null){
                mBackgroundHandler.post(() -> {
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(getApplicationContext());
                    String listOfValues = prefs.getString(getString(R.string.listOfDistances),"[]");
                    if(mGraphics.size() > 0){

                            double mean = 0;
                            for( Object graphic : mGraphics){
                                mean += ((BarcodeGraphic) graphic).getBarcode().getBoundingBox().width();
                            }
                            mean /= mGraphics.size();

                            double Distance = (cameraInfo.get(0)*realObjectReferenceSize*this.ResolutionReference.getHeight())*correctionfactor/(mean*cameraInfo.get(1));//mm
                            DecimalFormat df = new DecimalFormat("##.##");
                            df.setRoundingMode(RoundingMode.DOWN);
                            mytext.setText(df.format(Distance/10) + "cm");

                            if(Distance <= WarningReferenceDistance*10){
                                //banner.setBackgroundColor(R.color.warning);
                                banner.setBackgroundTintList(getResources().getColorStateList(R.color.warning));
                                Toast.makeText(getApplicationContext(),
                                        "KEEP AWAY, YOU ARE TOO CLOSE", Toast.LENGTH_SHORT).show();
                            }else{
                                banner.setBackgroundTintList(getResources().getColorStateList(R.color.bg_screen1));
                                //banner.setBackgroundColor(R.color.bg_screen1);
                            }
                            if(listOfValues == null){
                                JSONArray values = new JSONArray();
                                try {
                                    values.put(Distance);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(getString(R.string.listOfDistances),values.toString());
                                editor.commit();
                            }else{
                                try {
                                    JSONArray values = new JSONArray(prefs.getString(getString(R.string.listOfDistances),"[]"));
                                    values.put(Distance);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString(getString(R.string.listOfDistances),values.toString());
                                    editor.commit();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                });

            }
        }
    }
    ///--------------------------------ACTITIVITY METHODS---------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getSupportActionBar().hide();

        setContentView(R.layout.activity_barcode_capture);


        setupPreferences();

        System.out.println("Barcode Size Barcode activity");
        //TEXTURE VIEW OF CAMERA PREVIEW

        setupView();

        setupAndStartBarcodeDetector();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    private void setupPreferences(){
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);


        String temp =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.BarcodeSizeSettingNamekey), "50");
        if( !temp.equals("")  &&  temp.matches("\\d*") ) {
            BarcodeReferenceSize = new Float(Float.valueOf(temp));
        }else {
            Toast.makeText(getApplicationContext(),
                    "Not a valid number", Toast.LENGTH_SHORT).show();
        }

        temp =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.WarningDistanceNameKey), "30");
        if( !temp.equals("")  &&  temp.matches("\\d*") ) {
            WarningReferenceDistance = new Float(Float.valueOf(temp));
        }else {
            Toast.makeText(getApplicationContext(),
                    "Not a valid number", Toast.LENGTH_SHORT).show();
        }





    }

    private void setupView(){
        textureView = (TextureView) findViewById(R.id.textureView);
        ImageButton setting = (ImageButton) findViewById(R.id.settingButton);
        setting.bringToFront();
        ImageButton info = (ImageButton) findViewById(R.id.infoButton);
        info.bringToFront();

        banner = (ConstraintLayout) findViewById(R.id.lowerBanner);

    }

    private void setupAndStartBarcodeDetector(){

        //INVISIBLE OVERLAY OF RECTANGLES REPRESENTING THE BARCODES IN REAL TIME
        GraphicOverlay overlay = (GraphicOverlay) findViewById(R.id.overlay);

        //Create Barcode Detector and set the factory of the tracker
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(overlay);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());
        if (!barcodeDetector.isOperational()) {
            Toast.makeText(getApplicationContext(),
                    "Could not set up the detector!", Toast.LENGTH_SHORT).show();
            return;
        }



        /*
            Set the camera handler with the preview texture and the detector to use
         */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        int screenheight = displayMetrics.heightPixels;
        int screenwidth = displayMetrics.widthPixels;
        System.out.println("Display size"+ displayMetrics.heightPixels + "x" + displayMetrics.widthPixels);


        Size resolution = new Size(1080, 1440);

        float widthCorrectionFactor = (float)((float) screenwidth/ (float)resolution.getWidth());
        System.out.println(widthCorrectionFactor);
        camerahandler = new Camera2Handler(textureView, getApplicationContext(), barcodeDetector, overlay, resolution);
        /*
            Create the listener for the new barcode detected and do what necessary
         */
        listener = new BarcodeListener((TextView)findViewById(R.id.distanceValue),
                camerahandler.getFocalLenghtAndSensorSize(), BarcodeReferenceSize, widthCorrectionFactor >= 0? widthCorrectionFactor : 1, resolution);

        ((TextView)findViewById(R.id.qualityValue)).setText(String.valueOf(camerahandler.getFocalLenghtAndSensorSize().get(2)));
        ((TextView)findViewById(R.id.HyperFocalDistanceValue)).setText(String.valueOf(camerahandler.getFocalLenghtAndSensorSize().get(4)));
        ((TextView)findViewById(R.id.minFocusDistanceValue)).setText(String.valueOf(camerahandler.getFocalLenghtAndSensorSize().get(3)));

        //Insert the Listener for the barcode in the overlay
        /*
            The listener doesn fetch the barcodes directly from the detector but from the changin of the list of the barcode
            written in the overlay
            (Possible optimization)
         */
        overlay.setListener(listener);

        /*
            Start the camera
         */
        camerahandler.start();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.settingButton){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent,  ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }else if(v.getId() == R.id.infoButton){
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent,  ActivityOptions.makeSceneTransitionAnimation(this).toBundle());

            Log.w("DEBUG", "Clicking info activity");
        }else{
            Log.w("DEBUG", "Error clicking");
        }
    }
}