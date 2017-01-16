package com.univ.chat.util;


import com.univ.chat.model.Message;
import com.univ.chat.model.User;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HttpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    public static JSONObject sendPushNotification(User receiver, Message message,User sender) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(Constants.GCM_URL);
        LOGGER.debug("sending push notification with message :: " + message);
        LOGGER.debug("registration id :: " + receiver.getGcm());
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        post.setHeader(HttpHeaders.AUTHORIZATION, "key=" + Constants.SERVER_KEY);


        JSONObject body = new JSONObject();
        JSONObject messageBody = new JSONObject();

        JSONObject m = GsonHelper.toJsonObject(message);
        JSONObject u = GsonHelper.toJsonObject(sender);
        JSONObject x = new JSONObject();
        x.put("message", m);
        x.put("user", u);
        messageBody.put("title","Rivier Chat");
        messageBody.put("is_background",false);
        messageBody.put("flag", "1");
        messageBody.put("data", x.toString());

        body.put("to", receiver.getGcm());
        body.put("data", messageBody);

        LOGGER.info("post body :: " + body.toString());
        post.setEntity(new StringEntity(body.toString()));

        HttpResponse response = client.execute(post);

        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        LOGGER.info("response is :: " + result.toString());
        return new JSONObject(result.toString());
    }

    public static JSONObject sendTypingStatus(User receiver, User sender,String message) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(Constants.GCM_URL);
        LOGGER.debug("registration id :: " + receiver.getGcm());
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        post.setHeader(HttpHeaders.AUTHORIZATION, "key=" + Constants.SERVER_KEY);


        JSONObject body = new JSONObject();
        JSONObject messageBody = new JSONObject();


        JSONObject x = new JSONObject();
        x.put("name", sender.getName());
        x.put("state", message);
        x.put("id", sender.getUserId());

        messageBody.put("title","Rivier Chat");
        messageBody.put("is_background", false);
        messageBody.put("flag", "2");
        messageBody.put("data", x.toString());

        body.put("to", receiver.getGcm());
        body.put("data", messageBody);

        LOGGER.info("post body :: " + body.toString());
        post.setEntity(new StringEntity(body.toString()));

        HttpResponse response = client.execute(post);

        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        LOGGER.info("response is :: " + result.toString());
        return new JSONObject(result.toString());
    }
}
