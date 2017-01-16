package com.univ.chat.util;


import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.univ.chat.activity.LoginActivity;
import com.univ.chat.helper.SharedPrefHelper;


public class ChatApplication extends Application {

    public static final String TAG = ChatApplication.class
            .getSimpleName();

    private RequestQueue volleyQueue;

    private static ChatApplication application;

    private SharedPrefHelper pref;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static synchronized ChatApplication getInstance() {
        return application;
    }

    public RequestQueue getRequestQueue() {
        if (volleyQueue == null) {
            volleyQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return volleyQueue;
    }

    public SharedPrefHelper getPrefManager() {
        if (pref == null) {
            pref = new SharedPrefHelper(this);
        }

        return pref;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void logout() {
        pref.clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
