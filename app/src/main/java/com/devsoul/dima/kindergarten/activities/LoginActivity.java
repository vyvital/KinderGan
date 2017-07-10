package com.devsoul.dima.kindergarten.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.devsoul.dima.kindergarten.R;
import com.devsoul.dima.kindergarten.app.AppConfig;
import com.devsoul.dima.kindergarten.app.AppController;
import com.devsoul.dima.kindergarten.helper.SQLiteHandler;
import com.devsoul.dima.kindergarten.helper.SessionManager;
import com.devsoul.dima.kindergarten.model.Kid;
import com.devsoul.dima.kindergarten.model.KinderGan;
import com.devsoul.dima.kindergarten.model.Parent;
import com.devsoul.dima.kindergarten.model.Teacher;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The Login Activity is the main activity of the application.
 */
public class LoginActivity extends Activity
{
    private static final String TAG = LoginActivity.class.getSimpleName();

    private RadioGroup radioGroup;
    private RadioButton parent, nanny;
    private ImageButton imgbtnLogin;

    @InjectView(R.id.input_email) EditText inputEmail;
    @InjectView(R.id.input_password) EditText inputPassword;
    @InjectView(R.id.link_SignUp) TextView btnLinkToRegister;

    private int type;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);
        parent = (RadioButton) findViewById(R.id.parent);
        nanny = (RadioButton) findViewById(R.id.nanny);
        imgbtnLogin = (ImageButton) findViewById(R.id.img_btn_login);

        // Inject the ButterKnife design
        ButterKnife.inject(this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn())
        {
            // User is already logged in. Take him to user screen
            Intent intent = new Intent(LoginActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        }

        // Radio button selection
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId)
            {
                // find which radio button is selected
                if(checkedId == R.id.nanny)
                {
                    type = 1;
                }
                else if(checkedId == R.id.parent)
                {
                    type = 2;
                }
            }
        });

        // Login button Click Event
        imgbtnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                login();
            }
        });

        // Link to register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Start the registration activity
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * This function performs the login operation.
     */
    public void login()
    {
        Log.d(TAG, "login");

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // One of the fields is invalid
        if (!validate(email, password))
        {
            onLoginFailed("login failed");
            return;
        }

        // login user
        checkLogin(email, password, type);
    }

    /**
     * This function shows a message to the user that the login has failed.
     */
    public void onLoginFailed(String message)
    {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * This is a validation function that checks two fields: Email and Password, and that radio button is selected.
     * @param email - The email that was entered.
     * @param password - The password that was entered.
     * @return boolean - This returns true if all the fields are valid, false if one of the fields is invalid.
     */
    public boolean validate(final String email, final String password)
    {
        boolean valid = true;

        // Email validation
        if (email.isEmpty())
        {
            inputEmail.setError("Enter a valid email address");
            requestFocus(inputEmail);
            valid = false;
        }
        else
        {
            inputEmail.setError(null);
        }

        // Password validation
        if (password.isEmpty() || password.length() > 20)
        {
            inputPassword.setError("Enter a valid password");
            if (valid == true)
                requestFocus(inputPassword);
            valid = false;
        }
        else
        {
            inputPassword.setError(null);
        }

        // Radio button validation
        if ((!parent.isChecked()) && (!nanny.isChecked()))
        {
            onLoginFailed("Please choose login option: Parent or Nanny");
            valid = false;
        }

        return valid;
    }

    /**
     * Set focus on view
     * @param view
     */
    private void requestFocus(View view)
    {
        if (view.requestFocus())
        {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * Function to verify login details in mysql db using volley http request
     * @param email
     * @param password
     * @param type - Nanny or Parent
     */
    private void checkLogin(final String email, final String password, final int type)
    {
        // Tag used to cancel the request
        String tag_string_req = "login_request";

        pDialog.setMessage("Logging in ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "login Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // user successfully logged in

                        // Now store the user in SQLite
                        JSONObject user = jObj.getJSONObject("user");
                        // Get the type of user to know if it Teacher or Parent
                        int jType = Integer.parseInt(user.getString("type"));
                        if (jType == 1)
                        // Teacher
                        {
                            Teacher Nanny = new Teacher();
                            KinderGan Gan = new KinderGan();

                            Nanny.SetID(user.getString("ID"));
                            Nanny.SetFirstName(user.getString("firstname"));
                            Nanny.SetLastName(user.getString("lastname"));
                            Nanny.SetPhone(user.getString("phone"));
                            Nanny.SetPicture(user.getString("photo"));
                            Gan.SetName(user.getString("kindergan_name"));
                            Gan.SetCity(user.getString("kindergan_city"));
                            Nanny.SetClass(user.getString("kindergan_class"));
                            Nanny.SetEmail(user.getString("email"));
                            Nanny.SetNotificationTime(user.getString("notification_time"));
                            Nanny.SetCreatedAt(user.getString("created_at"));

                            // Inserting row in teachers table
                            db.addTeacher(Nanny, Gan);

                            // Create type session
                            session.setType(1);
                        }
                        else if (jType == 2)
                        // Parent
                        {
                            Parent parent = new Parent();
                            Kid child = new Kid();
                            KinderGan Gan = new KinderGan();

                            parent.SetID(user.getString("ID"));
                            parent.SetFirstName(user.getString("firstname"));
                            parent.SetLastName(user.getString("lastname"));
                            parent.SetAddress(user.getString("address"));
                            parent.SetPhone(user.getString("phone"));
                            parent.SetEmail(user.getString("email"));
                            parent.SetCreatedAt(user.getString("created_at"));

                            // Inserting row in parents table
                            db.addParent(parent);

                            child.SetName(user.getString("kid_name"));
                            child.SetBirthDate(user.getString("kid_birthdate"));
                            child.SetPicture(user.getString("kid_photo"));
                            child.SetClass(user.getString("kid_class"));
                            Gan.SetName(user.getString("kindergan_name"));
                            child.SetParentID(user.getString("ID"));
                            child.SetCreatedAt(user.getString("created_at"));

                            child.SetPresence(user.getString("presence"));
                            child.SetSpecial(user.getString("special"));
                            child.SetContact1(user.getString("contact1"));
                            child.SetContact2(user.getString("contact2"));
                            child.SetContact3(user.getString("contact3"));

                            // Inserting row in kids table
                            db.addKid(child, Gan);

                            // Create type session
                            session.setType(2);
                        }

                        // Create login session
                        session.setLogin(true);

                        // Launch user activity
                        Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onLoginFailed(errorMsg);
                    }
                }
                catch (JSONException e)
                {
                    // JSON error
                    e.printStackTrace();
                    onLoginFailed("Json error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "login Error: " + error.getMessage());
                onLoginFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);
                params.put("type", Integer.toString(type));

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Show the progress dialog
     */
    private void showDialog()
    {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    /**
     * Hide the progress dialog
     */
    private void hideDialog()
    {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
