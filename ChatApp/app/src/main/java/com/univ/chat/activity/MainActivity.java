package com.univ.chat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.univ.chat.R;
import com.univ.chat.adapter.ChatRoomsAdapter;
import com.univ.chat.util.Constants;
import com.univ.chat.util.URL;
import com.univ.chat.util.ChatApplication;
import com.univ.chat.gcm.GcmIntentService;
import com.univ.chat.gcm.NotificationUtils;
import com.univ.chat.helper.SimpleDividerItemDecoration;
import com.univ.chat.model.Chat;
import com.univ.chat.model.Message;
import com.univ.chat.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ArrayList<Chat> chatArrayList;
    private ChatRoomsAdapter mAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ChatApplication.getInstance().getPrefManager().getUser() == null) {
            launchLoginActivity();
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Constants.SENT_TOKEN_TO_SERVER)) {
                    Log.e(TAG, "GCM registration id is sent to our server");

                } else if (intent.getAction().equals(Constants.PUSH_NOTIFICATION)) {
                    handlePushNotification(intent);
                } else if (intent.getAction().equals(Constants.TYPING_NOTIFICATION)) {
                    String state = intent.getStringExtra("state");
                    String id = intent.getStringExtra("id");
                    String notifName = intent.getStringExtra("name");
                    for (int i=0; i<chatArrayList.size(); i++) {
                        if (id != null && id.equals(chatArrayList.get(i).getId())) {
                            String name = chatArrayList.get(i).getName();
                            if (state != null && state.equals(Constants.TYPING)) {
                                chatArrayList.get(i).setName(name + " is typing...");
                            } else if (state != null && state.equals(Constants.TYPING_STOP)){
                                chatArrayList.get(i).setName(notifName);
                            }
                            break;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        };

        chatArrayList = new ArrayList<>();
        mAdapter = new ChatRoomsAdapter(this, chatArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(
                getApplicationContext()
        ));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new ChatRoomsAdapter.RecyclerTouchListener(getApplicationContext(), recyclerView, new ChatRoomsAdapter.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Chat chat = chatArrayList.get(position);
                Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
                intent.putExtra("chat_room_id", chat.getId());
                intent.putExtra("name", chat.getName());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        if (checkPlayServices()) {
            registerGCM();
            fetchChatRooms();
        }
        ChatApplication.getInstance().getPrefManager().clearNotifications();
    }

    private void handlePushNotification(Intent intent) {

        Message message = (Message) intent.getSerializableExtra("message");
        String chatRoomId = intent.getStringExtra("chat_room_id");

        if (message != null && chatRoomId != null) {
            updateRow(chatRoomId, message);
        }
    }

    private void updateRow(String chatRoomId, Message message) {
        for (Chat chat : chatArrayList) {
            if (chat.getId().equals(chatRoomId)) {
                int index = chatArrayList.indexOf(chat);
                chat.setLastMessage(message.getMessage());
                chat.setUnreadCount(chat.getUnreadCount() + 1);
                chatArrayList.remove(index);
                chatArrayList.add(index, chat);
                ChatApplication.getInstance().getPrefManager().storeUserChatInfo(chat);
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void fetchChatRooms() {

        User user = ChatApplication.getInstance().getPrefManager().getUser();
        if (user == null) {
            Log.e(TAG,"user null in fetch chat rooms");
            launchLoginActivity();
            return;
        }
        String userId = user.getId();
        String url = String.format(URL.GET_ALL_USERS,userId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "response: " + response);
                try {
                    if (response.getBoolean("success")) {
                        JSONArray rooms = response.getJSONArray("users");
                        for (int i = 0; i < rooms.length(); i++) {
                            JSONObject chatRoomsObj = rooms.getJSONObject(i);
                            String senderId = chatRoomsObj.getString("userId");
                            Chat previousChat = ChatApplication.getInstance().getPrefManager().getUserChatInfo(senderId);
                            Chat cr = new Chat();
                            cr.setId(senderId);
                            cr.setName(chatRoomsObj.getString("name"));
                            if (previousChat != null) {
                                cr.setLastMessage(previousChat.getLastMessage());
                                cr.setUnreadCount(previousChat.getUnreadCount());
                            }
                            else {
                                cr.setUnreadCount(0);
                                cr.setLastMessage("");
                            }
                            cr.setTimestamp(chatRoomsObj.getString("timestamp"));
                            chatArrayList.add(cr);
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "" + response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                mAdapter.notifyDataSetChanged();
                subscribeToAllTopics();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse networkResponse = error.networkResponse;
                String response = null;
                try {
                    response = (new JSONObject(new String(networkResponse.data, "UTF-8"))).getString("message");
                } catch (Exception exception) {

                }
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + response);
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

            }
        });

        ChatApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void subscribeToAllTopics() {
        for (Chat cr : chatArrayList) {

            Intent intent = new Intent(this, GcmIntentService.class);
            intent.putExtra(GcmIntentService.KEY, GcmIntentService.SUBSCRIBE);
            intent.putExtra(GcmIntentService.TOPIC, "topic_" + cr.getId());
            startService(intent);
        }
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.PUSH_NOTIFICATION));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.TYPING_NOTIFICATION));
        NotificationUtils.clearNotifications();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported. Google Play Services not installed!");
                Toast.makeText(getApplicationContext(), "This device is not supported. Google Play Services not installed!", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_logout:
                ChatApplication.getInstance().logout();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}
