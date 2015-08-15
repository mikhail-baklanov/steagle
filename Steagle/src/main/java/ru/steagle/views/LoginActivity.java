package ru.steagle.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import ru.steagle.ExceptionHandler;
import ru.steagle.R;
import ru.steagle.SteagleApplication;
import ru.steagle.config.Config;
import ru.steagle.config.Keys;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.UserInfo;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.AuthCommand;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

    private static final String AUTO_LOGIN_YES = "yes";
    private static final String AUTO_LOGIN_NEVER = "never";
    private static final String AUTO_LOGIN_ASK = "ask";
    private static final String TAG = LoginActivity.class.getName();

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RequestTask authTask = null;
    private final SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);

    // Values for email and password at the time of the login attempt.
    private String login;
    private String password;

    // UI references.
    private EditText loginTextEdit;
    private EditText passwordTextEdit;
    private View loginFormView;
    private View loginStatusView;
    private TextView loginStatusTextView;
    private RadioButton radioAutoLogin;
    private RadioButton radioDontSavePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        ((SteagleApplication) getApplication()).bindSteagleService();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        final String autoLogin = prefs.getString(Keys.AUTO_LOGIN.getPrefKey(), null);
        final String prefLogin = prefs.getString(Keys.LOGIN.getPrefKey(), null);
        final String prefPassword = prefs.getString(Keys.PASSWORD.getPrefKey(), null);

        loginFormView = findViewById(R.id.login_form);
        loginFormView.setVisibility(View.GONE);

        serviceConnector.bind(this, new Runnable() {
            @Override
            public void run() {

                if (AUTO_LOGIN_YES.equals(autoLogin) && prefLogin != null &&
                        prefPassword != null && prefLogin.trim().length() > 0 &&
                        prefPassword.trim().length() > 0) {
                    attemptLogin();
                } else loginFormView.setVisibility(View.VISIBLE);
            }
        });

        // Set up the login form.
        loginTextEdit = (EditText) findViewById(R.id.login);

        passwordTextEdit = (EditText) findViewById(R.id.password);
        passwordTextEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        loginStatusView = findViewById(R.id.login_status);
        loginStatusTextView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        radioAutoLogin = (RadioButton) findViewById(R.id.radioAutoLogin);
        RadioButton radioSavePassword = (RadioButton) findViewById(R.id.radioSavePassword);
        radioDontSavePassword = (RadioButton) findViewById(R.id.radioDontSavePassword);
        if (autoLogin == null) {
            radioSavePassword.setChecked(true);
        } else {
            switch (autoLogin) {
                case AUTO_LOGIN_YES:
                    radioAutoLogin.setChecked(true);
                    loginTextEdit.setText(prefLogin);
                    passwordTextEdit.setText(prefPassword);
                    break;
                case AUTO_LOGIN_ASK:
                    radioSavePassword.setChecked(true);
                    loginTextEdit.setText(prefLogin);
                    passwordTextEdit.setText(prefPassword);
                    break;
                case AUTO_LOGIN_NEVER:
                    radioDontSavePassword.setChecked(true);
                    break;
                default:
                    radioSavePassword.setChecked(true);
                    break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (authTask != null) {
            return;
        }

        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.detect3G(this) && !Utils.detectWiFi(this)) {
            Utils.showNetworkError(this);
            return;
        }

        // Reset errors.
        loginTextEdit.setError(null);
        passwordTextEdit.setError(null);

        // Store values at the time of the login attempt.
        login = loginTextEdit.getText().toString();
        password = passwordTextEdit.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            passwordTextEdit.setError(getString(R.string.error_field_required));
            focusView = passwordTextEdit;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(login)) {
            loginTextEdit.setError(getString(R.string.error_field_required));
            focusView = loginTextEdit;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginStatusTextView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            Request request = new Request().add(new AuthCommand(login, password));
            Log.d(TAG, "Auth request: " + request);
            authTask = new RequestTask(Config.getRegServer(this)) {
                @Override
                public void onPostExecute(String result) {
                    Log.d(TAG, "Auth response: " + result);
                    authTask = null;
                    showProgress(false);

                    UserInfo userInfo = new UserInfo(result);
                    if (userInfo.isOk()) {
                        afterLogin(userInfo);
                    } else {
                        passwordTextEdit.setError(getString(R.string.error_incorrect_password));
                        passwordTextEdit.requestFocus();
                    }
                }

                @Override
                protected void onCancelled() {
                    authTask = null;
                    showProgress(false);
                }


            };
            authTask.execute(request.serialize());
        }
    }

    private void afterLogin(UserInfo userInfo) {
        String autoLogin = AUTO_LOGIN_ASK;
        if (radioAutoLogin.isChecked()) {
            autoLogin = AUTO_LOGIN_YES;
        } else if (radioDontSavePassword.isChecked()) {
            autoLogin = AUTO_LOGIN_NEVER;
        }

        boolean authDataChanged = saveAuthDataToPreferences(login, password, autoLogin);
        if (authDataChanged)
            serviceConnector.getServiceBinder().clearUserData();
        DataModel dm = serviceConnector.getServiceBinder().getDataModel();
        dm.setUserInfo(userInfo);
        finish();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    @Override
    protected void onDestroy() {
        serviceConnector.unbind();
        ((SteagleApplication) getApplication()).unBindSteagleService();
        super.onDestroy();
    }

    private boolean saveAuthDataToPreferences(String login, String password, String autoLogin) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String prefLogin = prefs.getString(Keys.LOGIN.getPrefKey(), null);
        String prefPassword = prefs.getString(Keys.PASSWORD.getPrefKey(), null);
        boolean result = prefLogin == null || !prefLogin.equals(login) || prefPassword == null || !prefPassword.equals(password);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Keys.LOGIN.getPrefKey(), login);
        editor.putString(Keys.PASSWORD.getPrefKey(), password);
        editor.putString(Keys.AUTO_LOGIN.getPrefKey(), autoLogin);
        editor.apply();
        return result;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginStatusView.setVisibility(View.VISIBLE);
            loginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            loginFormView.setVisibility(View.VISIBLE);
            loginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
