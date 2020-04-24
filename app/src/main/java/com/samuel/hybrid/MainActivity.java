package com.samuel.hybrid;

import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private JsInterface jsInterface;
    private Button btnLogin;;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        webView = (WebView)findViewById(R.id.webview);

        //加载url
        webView.loadUrl("file:///android_asset/index.html");


        jsInterface = new JsInterface();
        // 把布局文件的TextView对象绑定到jsInterface对象的textView属性上，便于jsInterface对象操作UI组件
        jsInterface.textView = (TextView)findViewById(R.id.textView);
        // Android提供的Js与Native通信的官方解决方案：这个方法的作用是，将 java 对象注入到 Js 中直接作为window的某一变量来使用
        webView.addJavascriptInterface(jsInterface,"AndroidNative");


        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);

        // 通过getSettings方法获取WebSettings对象，设置允许加载js，设置缓存模式，支持缩放
        WebSettings webSettings=webView.getSettings();
        // 允许Native调用javascript
        webSettings.setJavaScriptEnabled(true);

        /**
         * LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
         * LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
         * LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
         * LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
         */
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用缓存，只从网络获取数据.

        //支持屏幕缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {//实际处理button的click事件的方法
                webView.loadUrl("javascript:pauseVideo()");
            }
        });
    }

//    private void pauseVideo() {
//        // pauseVideo()是H5那边定义好的
//        webView.loadUrl("javascript:pauseVideo()");
//    }

    // WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient = new WebViewClient() {

        @Override // 重写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("ansen","拦截url:"+url);
            if(url.equals("http://www.google.com/")){
                Toast.makeText(MainActivity.this,"国内不能访问google,拦截该url",Toast.LENGTH_LONG).show();
                return true;//表示我已经处理过了
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    };

    // WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient = new WebChromeClient(){

        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("确定",null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();

            //注意:必须要这一句代码:result.confirm(),表示处理结果为确定状态同时唤醒WebCore线程,否则不能继续点击按钮
            result.confirm();
            return true;
        }

        @Override
        public void onConsoleMessage(String message, int lineNumber,String sourceID) {
            super.onConsoleMessage(message, lineNumber, sourceID);
            Log.i("网页中输出以下内容：",message);
        }


        //获取网页标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.i("ansen","网页标题:"+title);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("ansen","是否有上一个页面:"+webView.canGoBack());
        if (webView.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){//点击返回按钮的时候判断有没有上一页
            webView.goBack(); // goBack()表示返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * @JavascriptInterface 与 webView.addJavascriptInterface配合使用，下面的方法是用来让js调用的java方法
     * 如果没有方法带有@JavascriptInterface， 在android4.2版本以上会报错,提示None of the methods in the added interface have been annotated with @android.webkit.JavascriptInterface; they will not be visible in API 17
     * @param str
     */
    @JavascriptInterface
    public void  getClient(String str){
        Log.w("ansen","html调用客户端:"+str);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //释放资源
        webView.destroy();
        webView=null;
    }
}
