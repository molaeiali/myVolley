package org.molaei.myvolleyexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.VolleyError;

import org.molaei.myvolleyexample.api.MyVolley;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MyVolley(this).GET("ASD").withLoading().send(new MyVolley.Result() {
            @Override
            public void onSuccess(String string) {

            }

            @Override
            public void onFailure(VolleyError volleyError) {

            }
        });
    }
}
