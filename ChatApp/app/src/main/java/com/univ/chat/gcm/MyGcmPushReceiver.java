/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.univ.chat.gcm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.univ.chat.activity.MainActivity;
import com.univ.chat.util.Constants;
import com.univ.chat.util.ChatApplication;
import com.univ.chat.helper.GsonHelper;
import com.univ.chat.model.Message;
import com.univ.chat.model.User;

import org.json.JSONException;
import org.json.JSONObject;


public class MyGcmPushReceiver extends GcmListenerService {

    private static final String TAG = MyGcmPushReceiver.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(String from, Bundle bundle) {

        String title = bundle.getString("title");
        String data = bundle.getString("data");
        String flag = bundle.getString("flag");
        Log.d(TAG, "title: " + title);
        Log.d(TAG, "data: " + data);

        if (ChatApplication.getInstance().getPrefManager().getUser() == null) {
            Log.e(TAG, "user is not logged in, skipping push notification");
            return;
        }
        if (flag.equals("1")) {
            processMessage(title, data);
        }
        else if(flag.equals("2")) {
            processTypingState(data);
        }
    }

    private void processMessage(String title, String data) {
        try {
            JSONObject dataJSON = new JSONObject(data);
            JSONObject messageJSON = dataJSON.getJSONObject("message");
            Message message = (Message) GsonHelper.fromJson(messageJSON,Message.class);
            JSONObject userJSON = dataJSON.getJSONObject("user");
            User user = (User) GsonHelper.fromJson(userJSON,User.class);
            message.setUser(user);

            if (!NotificationUtils.isAppInBackground(getApplicationContext())) {

                Intent pushNotification = new Intent(Constants.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", message);
                pushNotification.putExtra("chat_room_id", user.getId());
                pushNotification.putExtra("title", title);
                pushNotification.putExtra("user", user);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                NotificationUtils notificationUtils = new NotificationUtils();
                notificationUtils.playNotificationSound();
            } else {
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                showNotificationMessage(getApplicationContext(), title, user, message, resultIntent);
            }
        } catch (JSONException | ClassNotFoundException e) {
            Log.e(TAG, "json parsing error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showNotificationMessage(Context context, String title, User user,Message message, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, user, message, intent);
    }

    private void processTypingState(String data) {
        try {
            if (!NotificationUtils.isAppInBackground(getApplicationContext())) {
                JSONObject dataJSON = new JSONObject(data);
                String senderId = dataJSON.getString("id");
                String name = dataJSON.getString("name");
                String state = dataJSON.getString("state");

                Intent typingNotification = new Intent(Constants.TYPING_NOTIFICATION);
                typingNotification.putExtra("name", name);
                typingNotification.putExtra("id", senderId);
                typingNotification.putExtra("state", state);
                Log.d(TAG, "sending typing notification : " + name + " : " + senderId + " : " + state);
                LocalBroadcastManager.getInstance(this).sendBroadcast(typingNotification);
            }
        } catch (JSONException exception) {

        }
    }
}
