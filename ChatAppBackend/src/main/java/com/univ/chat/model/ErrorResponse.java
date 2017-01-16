package com.univ.chat.model;


import com.google.gson.annotations.Expose;
import com.univ.chat.util.GsonHelper;

public class ErrorResponse {

    @Expose
    private boolean success = false;

    @Expose
    private String message;

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return GsonHelper.toJson(this);
    }
}
