package org.molaei.myvolley;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MyVolleySingleton {
    private static MyVolleySingleton instance;
    private RequestQueue requestQueue;

    public static MyVolleySingleton getInstance(Context context) {
        if(instance == null){
            instance = new MyVolleySingleton(context);
        }
        return instance;
    }

    private MyVolleySingleton(Context context){
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
    }

    public void addRequest(Request request){
        requestQueue.add(request);
    }
}
