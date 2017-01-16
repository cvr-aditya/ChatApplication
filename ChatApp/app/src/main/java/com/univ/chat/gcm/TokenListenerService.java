package com.univ.chat.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class TokenListenerService extends InstanceIDListenerService {

    private static final String TAG = TokenListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        Log.e(TAG, "onTokenRefresh");
        Intent intent = new Intent(this, GcmIntentService.class);
        startService(intent);
    }
}
