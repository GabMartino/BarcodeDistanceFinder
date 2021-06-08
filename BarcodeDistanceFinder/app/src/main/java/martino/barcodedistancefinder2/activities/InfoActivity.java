package martino.barcodedistancefinder2.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.view.Display;
import android.view.SurfaceHolder;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import martino.barcodedistancefinder2.R;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        InputStream is = null;
        byte[] buffer = null;
        try {
            is = getAssets().open("info.html");
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = new String(buffer);



        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        System.out.println(width);
        WebView web = (WebView)findViewById(R.id.info);

        web.setBackgroundColor(getColor(R.color.gray) );
        web.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        web.loadDataWithBaseURL(null, "<head>\n" +
                "<style>img{display: inline;height: auto;width:"+width+"; max-width: 100%;}</style></head>" + str, "text/html", "UTF-8", null);


        //web.loadUrl(getString(R.string.info_content_path));



    }
}