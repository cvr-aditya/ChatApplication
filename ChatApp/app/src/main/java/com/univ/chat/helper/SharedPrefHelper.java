package com.univ.chat.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.univ.chat.model.Chat;
import com.univ.chat.model.User;

import static com.univ.chat.helper.GsonHelper.fromJson;


public class SharedPrefHelper {

    private String TAG = SharedPrefHelper.class.getSimpleName();
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context context;

    int MODE = 0;

    private static final String PREF_NAME = "revier_repo";

    private static final String USER_ID = "userId";
    private static final String NAME = "name";
    private static final String EMAIL = "email";
    private static final String MESSAGES = "messages";


    public SharedPrefHelper(Context context) {
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences(PREF_NAME, MODE);
        editor = sharedPreferences.edit();
        editor.commit();
    }


    public void storeUser(User user) {
        editor.putString(USER_ID, user.getId());
        editor.putString(NAME, user.getName());
        editor.putString(EMAIL, user.getEmail());
        editor.commit();
        Log.e(TAG, "User is stored in shared preferences." + user.getName() + ", " + user.getEmail());
    }

    public User getUser() {
        if (sharedPreferences.getString(USER_ID, null) != null) {
            User user = new User();
            user.setId(sharedPreferences.getString(USER_ID, null));
            user.setName(sharedPreferences.getString(NAME, null));
            user.setEmail(sharedPreferences.getString(EMAIL, null));
            return user;
        }
        return null;
    }

    public void storeUserChatInfo(Chat chat) {
        if (chat != null) {
            String chatString = GsonHelper.toJson(chat);
            Log.e(TAG, "chat stored in shared prefs." + chatString);
            editor.putString(chat.getId(), chatString);
            editor.commit();
        }
    }

    public Chat getUserChatInfo(String userId) {
        if (sharedPreferences.getString(userId,null) != null) {
            String chatString = sharedPreferences.getString(userId, null);
            try {
                return (Chat) fromJson(chatString, Chat.class);
            }
            catch (ClassNotFoundException exception) {
                return null;
            }
        }
        return null;
    }

    public void addNotification(String notification) {

        String oldNotifications = getNotifications();

        if (oldNotifications != null) {
            oldNotifications += "|" + notification;
        } else {
            oldNotifications = notification;
        }

        editor.putString(MESSAGES, oldNotifications);
        editor.commit();
    }

    public String getNotifications() {
        return sharedPreferences.getString(MESSAGES, null);
    }

    public void clearNotifications() {
        editor.putString(MESSAGES, null);
        editor.commit();
    }

    public void clearUserChatInfo(String userId) {
        editor.putString(userId, null);
        editor.commit();
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }
}
