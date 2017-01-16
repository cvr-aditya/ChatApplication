package com.univ.chat.util;


public enum UserType {

    STUDENT("student"),
    PROFESSOR("professor");

    private String userType;

    UserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }
}
