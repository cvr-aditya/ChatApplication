package com.univ.chat.activity;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class RegistrationActivity extends AppCompatActivity {

    private String TAG = RegistrationActivity.class.getSimpleName();
    private EditText email, password, confirmPassword, name;
    private TextInputLayout regEmailLayout,passwordLayout;
    private Button registerButton;
    private InputValidator inputValidator, passwordValidator;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ChatApplication.getInstance().getPrefManager().getUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_registration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        regEmailLayout = (TextInputLayout) findViewById(R.id.input_register_layout_email);
        passwordLayout = (TextInputLayout) findViewById(R.id.input_register_layout_reenter_password);

        name = (EditText) findViewById(R.id.register_name);
        password = (EditText) findViewById(R.id.register_password);
        email = (EditText) findViewById(R.id.register_email);
        confirmPassword = (EditText) findViewById(R.id.register_reenter_password);
        spinner = (Spinner) findViewById(R.id.userType);

        registerButton = (Button) findViewById(R.id.register);

        inputValidator = new InputValidator(email, regEmailLayout, this);
        passwordValidator = new InputValidator(password,confirmPassword,passwordLayout,this);

        email.addTextChangedListener(inputValidator);
        confirmPassword.addTextChangedListener(passwordValidator);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void register() {
        final String password = this.password.getText().toString();
        final String email = this.email.getText().toString();
        final String name = this.name.getText().toString();
        final String confirmPassword = this.confirmPassword.getText().toString();
        final String type = this.spinner.getSelectedItem().toString().toLowerCase();

        if (!inputValidator.validateEmail(email)) {
            return;
        }

        if (!passwordValidator.validatePassword(confirmPassword)) {
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setType(type);
        JSONObject body = new JSONObject();
        try {
            body = GsonHelper.toJsonObject(user);
        } catch (JSONException exception) {
            Log.e(TAG, "exception: " + exception);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL.REGISTER, body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "response: " + response);
                try {
                    if (!response.has("message")) {
                        User user = (User) GsonHelper.fromJson(response, User.class);
                        Log.d(TAG, "user: " + user.getEmail() + "  " + user.getId());
                        ChatApplication.getInstance().getPrefManager().storeUser(user);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "" + response.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
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
}
