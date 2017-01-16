package com.univ.chat.model;


import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.univ.chat.R;

public class InputValidator implements TextWatcher{

    private String TAG = InputValidator.class.getSimpleName();
    private View view;
    private View relatedView;
    private TextInputLayout inputLayout;
    private Activity activity;

    public InputValidator(View view, TextInputLayout inputLayout, Activity activity) {
        this.view = view;
        this.inputLayout = inputLayout;
        this.activity = activity;
    }

    public InputValidator(View view,View relatedView,TextInputLayout inputLayout,Activity activity) {
        this.view = view;
        this.relatedView = relatedView;
        this.inputLayout = inputLayout;
        this.activity = activity;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public boolean validateEmail(String email) {

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayout.setError("Enter valid email address");
            requestFocus(view);
            return false;
        } else {
            inputLayout.setErrorEnabled(false);
        }

        return true;
    }

    public boolean validatePassword(String confirmPassword) {
        String password = ((EditText)view).getText().toString();
        if (password.isEmpty() || !password.equals(confirmPassword)) {
            inputLayout.setError("Passwords do not match");
            requestFocus(relatedView);
            return false;
        } else {
            inputLayout.setErrorEnabled(false);
        }
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.d(TAG,"beforeTextChanged :: " + charSequence.toString());
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.d(TAG,"onTextChanged :: " + charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {
        Log.d(TAG,"afterTextChanged :: " + editable.toString());
        switch (view.getId()) {
            case R.id.input_email:
            case R.id.register_email:
                validateEmail(editable.toString());
                break;
            case R.id.register_reenter_password:
                validatePassword(editable.toString());
                break;
        }
    }
}
