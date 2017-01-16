package com.univ.chat.util;


public class Constants {

    public static final String FIND_ALL_USERS = "com.univ.chat.model.User.getAll";
    public static final String FIND_ALL_ROOMS = "com.univ.chat.model.Room.getAll";
    public static final String FIND_ALL_MESSAGES = "com.univ.chat.model.Message.getAll";

    public static final String FIND_USER_BY_TYPE = "com.univ.chat.model.User.getByType";
    public static final String FIND_USER_BY_TYPE_EXCEPT = "com.univ.chat.model.User.getByTypeExcept";
    public static final String FIND_USER_BY_EMAIL = "com.univ.chat.model.User.getByEmail";
    public static final String GET_ALL_EXCEPT = "com.univ.chat.model.User.getAllExcept";

    public static final String FIND_MESSAGE_BY_ID = "com.univ.chat.model.Message.findById";
    public static final String FETCH_CHAT = "com.univ.chat.model.Message.fetchChat";
    public static final String UPDATE_USER_GCM = "com.univ.chat.model.User.updateUserGCM";

    public static final String TYPE = "type";
    public static final String EMAIL = "email";
    public static final String NEW_GCM = "gcm";
    public static final String USER_ID = "userId";
    public static final String SENDER_ID = "senderId";
    public static final String RECEIVER_ID = "receiverId";
    public static final String MESSAGE_ID = "messageId";

    public static final String GCM_URL = "https://gcm-http.googleapis.com/gcm/send";
    public static final String SERVER_KEY  = "AIzaSyAGaiwy0Ppb4nxCwVjeipqVLEtBD6ru4OA";
}
