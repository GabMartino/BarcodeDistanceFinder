package martino.barcodedistancefinder2.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import martino.barcodedistancefinder2.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        findViewById(R.id.startButton).setOnClickListener(this);


        //CHECK IF all the permission are granted
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            Log.e("DEBUG", "Error permission");
            return;
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.startButton){
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            startActivity(intent);
        }
    }

}