package com.univ.chat.util;


public class SqlQuery {

    public static final String GET_ALL_USERS = "Select user from User user";
    public static final String GET_USERS_BY_TYPE = "Select user from User user where user.type = :" + Constants.TYPE;
    public static final String GET_USERS_BY_TYPE_EXCEPT = "Select user from User user where user.type = :" + Constants.TYPE + " and user.userId <> :" + Constants.USER_ID;
    public static final String GET_USER_BY_EMAIL = "Select user from User user where user.email = :" + Constants.EMAIL;
    public static final String GET_MESSAGE_BY_ID = "Select message from Message message where message.messageId = :" + Constants.MESSAGE_ID;
    public static final String GET_ALL_USERS_EXCEPT = "Select user from User user where user.userId <> :" + Constants.USER_ID;
    public static final String GET_ALL_ROOMS = "Select r from Room r";
    public static final String GET_ALL_MESSAGES = "Select message from Message message";
    public static final String PUT_USER_GCM = "Update User set gcm = :" + Constants.NEW_GCM + " where userId = :" + Constants.USER_ID;
    public static final String FETCH_CHAT = "Select message from Message message where (message.senderId = :" + Constants.SENDER_ID + " and message.receiverId = :" + Constants.RECEIVER_ID +
            ") or (message.senderId = :" + Constants.RECEIVER_ID + " and message.receiverId = :" + Constants.SENDER_ID + ")";

}
