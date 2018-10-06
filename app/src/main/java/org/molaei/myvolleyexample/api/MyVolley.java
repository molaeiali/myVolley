package org.molaei.myvolleyexample.api;

import android.content.Context;

import java.util.HashMap;

public class MyVolley extends org.molaei.myvolley.MyVolley{

    public MyVolley(Context context) {
        super(context);
    }

    @Override
    public LoadingView getDefaultLoading(Context context) {
        return null;
    }

    @Override
    public HashMap<String, String> getDefaultHeaders() {
        return null;
    }
}