package com.univ.chat.helper;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;


public class GsonHelper {
    private static Gson gson;

    public static String toJson(Object object) {
        return getGson().toJson(object);
    }

    public static JSONObject toJsonObject(Object object) throws JSONException {
        return new JSONObject(toJson(object));
    }

    public static Object fromJson(JSONObject object, Class<?> className)
            throws ClassNotFoundException {
        return fromJson(object.toString(), className);
    }

    public static Object fromJson(String jsonString, Class<?> className)
            throws ClassNotFoundException {
        return getGson().fromJson(jsonString, className);
    }

    public static Gson getGson() {
        if (gson == null)
            gson = new GsonBuilder().create();
        return gson;
    }

}

