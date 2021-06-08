package martino.barcodedistancefinder2.cameraUtilities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.RecommendedStreamConfigurationMap;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.VolumeShaper;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentFactory;

import com.google.android.gms.common.internal.IAccountAccessor;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import android.renderscript.RenderScript;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import martino.barcodedistancefinder2.activities.BarcodeCaptureActivity;
import martino.barcodedistancefinder2.barcodeGraphic.GraphicOverlay;
import martino.barcodedistancefinder2.utilities.BarcodeAsyncDetector;
import martino.barcodedistancefinder2.utilities.BarcodeTracker;

public class Camera2Handler {
    private String cameraId;
    protected CameraDevice cameraDevice;
    TextureView previewTexture;

    protected CaptureRequest.Builder captureRequestBuilder;
    protected CameraCaptureSession cameraCaptureSessions;
    private Size imageDimension;
    private Size[] supportedPreviewSizes;
    CameraManager manager;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private Handler mImageReaderHandler;
    private HandlerThread mImageReaderThread;


    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    Context context;
    Detector<?> detector;
    ImageReader imgreader;
    GraphicOverlay overlay;
    //constructor without a detector
    public Camera2Handler(TextureView textureView, Context context) {
        previewTexture = textureView;
        this.context = context;

    }
    //constructor with a detector
    public Camera2Handler(TextureView textureView, Context context, Detector<?> detector, GraphicOverlay overlay, Size wantedResolution) {
        previewTexture = textureView;
        this.context = context;
        this.detector = detector;
        this.overlay = overlay;
        imageDimension = wantedResolution;
        try {
            manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            cameraId = manager.getCameraIdList()[0];

        } catch (SecurityException | CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void start(){
        startBackgroundThread();
        startCamera();

    }

    private void startCamera() {
        try {
            manager.openCamera(cameraId, stateCallback, null); //open the selected camera
        } catch (SecurityException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public List<Float> getFocalLenghtAndSensorSize(){
        List<Float> info = new ArrayList<>();
        try{

            CameraCharacteristics c = manager.getCameraCharacteristics(cameraId);

            float[] r = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

            info.add(r[0]);
            //Sensor Size
            info.add(c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth());
            //Quality
            info.add((float)c.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION));
            info.add((float)c.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE));
            info.add((float)c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE));
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
        return info;
    }
    public Size getCameraOutputsize(){
        return imageDimension;
    }


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback(){
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //This is called when the camera is open
            Log.e("DEBUG", "onOpened");
            cameraDevice = camera; //this allow to create a link to the CameraDevice selected by id previously
            previewTexture.setSurfaceTextureListener(textureListener);

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void createSessionPipelines(int width, int height) {
        try {
            int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

            imageDimension = new Size(width, height);
            //Set Pipelines for the preview
            SurfaceTexture texture = previewTexture.getSurfaceTexture();
            if(rotation == 0){
                texture.setDefaultBufferSize(imageDimension.getHeight(), imageDimension.getWidth());
            }else{
                texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            }
            Surface surface = new Surface(texture);


            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            captureRequestBuilder.addTarget(surface);



            //Create list of the target pipelines
            List<Surface> targets = new ArrayList<>();
            targets.add(surface);

            if(detector != null){


                imgreader = ImageReader.newInstance(imageDimension.getWidth() , imageDimension.getHeight(),ImageFormat.YUV_420_888
                        ,2);

                imgreader.setOnImageAvailableListener((reader) -> {
                    Image img = reader.acquireLatestImage();
                    if(img != null){
                        byte[] bytes = ImageUtil.imageToByteArray(img);
                        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap rotatedImg = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);
                        Frame frame = new Frame.Builder().setBitmap(rotatedImg).build();
                        overlay.setImageSizeReference(bitmapImage.getHeight(), bitmapImage.getWidth());
                        img.close();
                        detector.receiveFrame(frame);
                    }

                },mImageReaderHandler );

                targets.add(imgreader.getSurface());
                captureRequestBuilder.addTarget(imgreader.getSurface());
            }


            cameraDevice.createCaptureSession(targets, new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;

                    /*
                        Once the session is configured set the repeating requests
                     */
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    try{

                        cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null,null);
                    }catch (CameraAccessException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("DEBUG", "ERROr");
                }
            }, null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener(){
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height){
            //configureTransform(width, height);
           // surface.setDefaultBufferSize(720, 1280);
            createSessionPipelines(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            //surface.setDefaultBufferSize(720, 1280);

            //configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    protected void startBackgroundThread() {
        mImageReaderThread = new HandlerThread("ImageReader Thread");
        mImageReaderThread.start();
        mImageReaderHandler = new Handler(mImageReaderThread.getLooper());


    }
    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Size mPreviewSize = new Size(imageDimension.getWidth(),imageDimension.getHeight());
        if (null == previewTexture || null == mPreviewSize) {
            return;
        }
        int rotation = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();//getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        //RectF viewRect = new RectF(0, 0, viewHeight, viewWidth);
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        //RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            //matrix.setRectToRect(bufferRect, viewRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }

        previewTexture.setTransform(matrix);
    }
}
