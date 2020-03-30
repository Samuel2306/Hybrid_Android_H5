package com.nio.imooc;

import android.webkit.JavascriptInterface;
import android.widget.TextView;

public class JsInterface {
    public TextView textView;

    @JavascriptInterface
    public void setTextContent(String str) {
        System.out.println("H5调用了Native的方法");
        this.textView.setText(str);
    }
}
