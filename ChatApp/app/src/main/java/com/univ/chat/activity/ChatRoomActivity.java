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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.univ.chat.R;
import com.univ.chat.adapter.ChatRoomThreadAdapter;
import com.univ.chat.util.Constants;
import com.univ.chat.util.URL;
import com.univ.chat.util.ChatApplication;
import com.univ.chat.gcm.NotificationUtils;
import com.univ.chat.helper.GsonHelper;
import com.univ.chat.model.Message;
import com.univ.chat.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity {

    private String TAG = ChatRoomActivity.class.getSimpleName();

    private String chatRoomId;
    private RecyclerView recyclerView;
    private ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private EditText inputMessage;
    Button btnSend;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputMessage = (EditText) findViewById(R.id.message);
        btnSend = (Button) findViewById(R.id.btn_send);

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        ChatApplication.getInstance().getPrefManager().clearUserChatInfo(chatRoomId);
        title = intent.getStringExtra("name");

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (chatRoomId == null) {
            Toast.makeText(getApplicationContext(), "Chat room not found!", Toast.LENGTH_SHORT).show();
            finish();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner
        String selfUserId = ChatApplication.getInstance().getPrefManager().getUser().getId();

        mAdapter = new ChatRoomThreadAdapter(this, messageArrayList, selfUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
                else if (intent.getAction().equals(Constants.TYPING_NOTIFICATION)) {
                    String state = intent.getStringExtra("state");
                    String id = intent.getStringExtra("id");
                    String name = intent.getStringExtra("name");
                    Log.d(TAG,"typing notif received ");
                    if (id != null && id.equals(chatRoomId)) {
                        if (state != null && state.equals(Constants.TYPING)) {
                            if (name != null && !name.equals("")) {
                                Log.d(TAG,"setting typing");
                                getSupportActionBar().setTitle(name + " is typing...");
                            } else {
                                getSupportActionBar().setTitle(title + " is typing...");
                            }
                        } else if (state != null && state.equals(Constants.TYPING_STOP)){
                            if (name != null && !name.equals("")) {
                                getSupportActionBar().setTitle(name);
                            } else {
                                getSupportActionBar().setTitle(title);
                            }
                        }
                    }
                }
            }
        };

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG,"before typing message :: " + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG,"while typing :: " + charSequence.toString());
                //send event that user is typing
                sendTypingStatus(Constants.TYPING);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //send event that user has stopped typing
                sendTypingStatus(Constants.TYPING_STOP);
            }
        });
        fetchChatThread();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // registering the receiver for new notification
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

    private void handlePushNotification(Intent intent) {
        Message message = (Message) intent.getSerializableExtra("message");
        String senderId = intent.getStringExtra("chat_room_id");
        String title = intent.getStringExtra("title");
        User sender = (User) intent.getSerializableExtra("user");

        title = (title == null) ? "Rivier Chat" : title;
        if (message != null && senderId != null) {
            //put message in the list if the message received is from the user whom you are chatting with
            if (senderId.equals(chatRoomId)) {
                messageArrayList.add(message);
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                }
            }
            else { //Show notification if it is from another user
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                notificationUtils.showNotificationMessage(title, sender, message, resultIntent);
            }
        }
    }


    private void sendMessage() {
        final String message = this.inputMessage.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        this.inputMessage.setText("");

        Message messageObj = new Message();
        messageObj.setMessage(message);
        messageObj.setSenderId(ChatApplication.getInstance().getPrefManager().getUser().getId());
        messageObj.setReceiverId(chatRoomId);

        JSONObject body = new JSONObject();
        try {
            body = GsonHelper.toJsonObject(messageObj);
        }
        catch (JSONException exception) {

        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL.SEND_MESSAGE, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    if (!response.has("success")) {

                        String commentId = response.getString("messageId");
                        String commentText = response.getString("message");
                        String createdAt = response.getString("timestamp");
                        Message message = new Message();
                        message.setMessageId(commentId);
                        message.setMessage(commentText);
                        message.setTimestamp(createdAt);
                        message.setUser(ChatApplication.getInstance().getPrefManager().getUser());

                        messageArrayList.add(message);

                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "" + response.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

    private void launchLoginActivity() {
        Intent intent = new Intent(ChatRoomActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void fetchChatThread() {

        User user = ChatApplication.getInstance().getPrefManager().getUser();
        if (user == null) {
            launchLoginActivity();
        }

        String endPoint = String.format(URL.FETCH_CHAT_THREAD, chatRoomId , user.getId());
        Log.e(TAG, "endPoint: " + endPoint);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, endPoint ,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "response: " + response);

                try {
                    if (response.getBoolean("success")) {
                        JSONArray commentsObj = response.getJSONArray("messages");

                        for (int i = 0; i < commentsObj.length(); i++) {
                            JSONObject commentObj = (JSONObject) commentsObj.get(i);

                            String commentId = commentObj.getString("messageId");
                            String commentText = commentObj.getString("message");
                            String createdAt = commentObj.getString("timestamp");

                            User user = new User();
                            user.setId(commentObj.getString("senderId"));
                            user.setName(commentObj.getString("name"));
                            Message message = new Message();
                            message.setMessageId(commentId);
                            message.setMessage(commentText);
                            message.setTimestamp(createdAt);
                            message.setUser(user);

                            messageArrayList.add(message);
                        }

                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "" + response.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

    private void sendTypingStatus(String state) {

        Message messageObj = new Message();
        messageObj.setMessage(state);
        messageObj.setSenderId(ChatApplication.getInstance().getPrefManager().getUser().getId());
        messageObj.setReceiverId(chatRoomId);
        JSONObject body = new JSONObject();
        try {
            body = GsonHelper.toJsonObject(messageObj);
        }
        catch (JSONException exception) {

        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL.TYPING, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //do nothing
                Log.e(TAG, "success: " +  response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Ignore the error , not so important
                Log.e(TAG, "Volley error: " + error.getMessage() );
            }
        });
        ChatApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}
