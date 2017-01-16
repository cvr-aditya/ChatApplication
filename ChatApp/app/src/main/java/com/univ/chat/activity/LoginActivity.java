package com.univ.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.univ.chat.util.URL;
import com.univ.chat.util.ChatApplication;
import com.univ.chat.helper.GsonHelper;
import com.univ.chat.model.InputValidator;
import com.univ.chat.model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private String TAG = LoginActivity.class.getSimpleName();
    private EditText email, password;
    private TextInputLayout inputLayoutEmail;
    private Button loginButton;
    private InputValidator inputValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ChatApplication.getInstance().getPrefManager().getUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);

        password = (EditText) findViewById(R.id.input_password);
        email = (EditText) findViewById(R.id.input_email);
        loginButton = (Button) findViewById(R.id.btn_enter);

        inputValidator = new InputValidator(email, inputLayoutEmail, this);
        email.addTextChangedListener(inputValidator);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        final String password = this.password.getText().toString();
        final String email = this.email.getText().toString();

        if (!inputValidator.validateEmail(email)) {
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        JSONObject body = new JSONObject();
        try {
            body = GsonHelper.toJsonObject(user);
        } catch (JSONException exception) {
            Log.e(TAG, "exception: " + exception);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL.LOGIN, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "response: " + response);
                try {
                    if (!response.has("success")) {
                        User user = (User) GsonHelper.fromJson(response, User.class);
                        Log.d(TAG,"printing user attributes ::   " + GsonHelper.toJson(user));
                        ChatApplication.getInstance().getPrefManager().storeUser(user);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "" + response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException | ClassNotFoundException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                    Toast.makeText(getApplicationContext(), "Json parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, "Response " + response.toString());
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

    public void register(View view) {
        Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(intent);
        finish();
    }

}
