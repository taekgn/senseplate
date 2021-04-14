
package kent.group8.senseplateandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;

public class WebActivity extends AppCompatActivity {
    private WebView mWebView; //Webview Declaration
    private WebSettings mWebSettings; //Setting Webview
    private final String foodsite = "https://www.calorieking.com/us/en/foods/search?keywords=";
    Signalton signal = Signalton.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_web);

        Intent webintent = getIntent();
        webintent.getExtras().getString("visionobj");

        // WebView Initiate
        mWebView = (WebView) findViewById(R.id.webView);

        mWebView.setWebViewClient(new WebViewClient()); // prevents new window poping up
        mWebSettings = mWebView.getSettings(); //Detail setting
        mWebSettings.setJavaScriptEnabled(true); // permission of webpage using javascript
        mWebSettings.setSupportMultipleWindows(false); // New window permission
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // Multiviewing by Javascript permission
        mWebSettings.setLoadWithOverviewMode(true); // Metatag permission 
        mWebSettings.setUseWideViewPort(true); // Fitting size of screen permission
        mWebSettings.setSupportZoom(false); // Zooming in permission 
        mWebSettings.setBuiltInZoomControls(false); // Scaling screen permission
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // Fitting size of content
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // Browser cache permission 
        mWebSettings.setDomStorageEnabled(true); // Local storage permission 
        merge(signal.getPic());
        mWebView.loadUrl(merge(webintent.getExtras().getString("visionobj"))); // inserts URL to surf and Starts webview 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
    }
    public String merge(String s)
    {
        s = foodsite + s;
        return s;
    }
}