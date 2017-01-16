package com.univ.chat.model;

import java.io.Serializable;


public class User implements Serializable {
    private String userId;
    private String name;
    private String email;
    private String gcm;
    private String type;
    private String password;

    public String getId() {
        return userId;
    }

    public void setId(String id) {
        this.userId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGcm(String gcm) {
        this.gcm = gcm;
    }

    public String getGcm() {
        return gcm;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
