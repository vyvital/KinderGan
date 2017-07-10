package com.devsoul.dima.kindergarten.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.devsoul.dima.kindergarten.R;
import com.devsoul.dima.kindergarten.helper.SessionManager;
import com.devsoul.dima.kindergarten.model.Kid;
import com.devsoul.dima.kindergarten.model.KinderGan;
import com.devsoul.dima.kindergarten.model.Parent;

import java.util.regex.Pattern;

/**
 * The Signup parent Activity enables the user that is a parent to create an account in the application,
 * and is generally displayed via the link on the Registration Activity.
 */
public class SignupParentActivity extends Activity
{
    private static final String TAG = SignupParentActivity.class.getSimpleName();

    private static final String PASSWORD_PATTERN =
                    "((?=.*\\d)" +        // must contains one digit from 0-9
                    "(?=.*[a-z])" +       // must contains one lowercase characters
                    "(?=.*[A-Z])" +       // must contains one uppercase characters
                    "(?=.*[!@#$%])" +     // must contains one special symbols in the list "!@#$%"
                    "(?!.*\\s)" +         // disallow spaces
                    ".{8,15})";           // length at least 8 characters and maximum of 15

    @InjectView(R.id.input_id) EditText inputID;
    @InjectView(R.id.input_FName) EditText inputFirstName;
    @InjectView(R.id.input_LName) EditText inputLastName;
    @InjectView(R.id.input_phone) EditText inputPhone;
    @InjectView(R.id.input_address) EditText inputAddress;
    @InjectView(R.id.input_email) EditText inputEmail;
    @InjectView(R.id.input_password) EditText inputPassword;
    @InjectView(R.id.link_login) TextView btnLinkToLogin;
    private ImageButton img_btnNext;

    private SessionManager session;
    private Bundle extras;

    private Parent parent;
    private Kid child;
    private KinderGan Gan;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_parent);

        // Next button
        img_btnNext = (ImageButton) findViewById(R.id.img_btn_next);

        // Inject the ButterKnife design
        ButterKnife.inject(this);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Create parent object
        parent = new Parent();

        // Check if user is already logged in or not
        if (session.isLoggedIn())
        {
            // User is already logged in. Take him to user activity
            Intent intent = new Intent(SignupParentActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        }

        // To retrieve objects (Parent, Kid, KinderGan) in current activity
        extras = getIntent().getExtras();
        if (extras != null)
        // Has objects from previous activity (SignUpParentGan Activity)
        {
            //Obtaining data
            parent = (Parent) getIntent().getSerializableExtra("parent");
            child = (Kid) getIntent().getSerializableExtra("kid");
            Gan = (KinderGan) getIntent().getSerializableExtra("kindergan");
            // Load fields of Parent
            LoadFields();
        }

        // Next page Button Click event
        img_btnNext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Check validation of the fields
                if (!validate())
                // One of the fields is invalid
                {
                    onValidationFailed("Invalid credentials entered!");
                    return;
                }
                else
                // All the fields is valid
                {
                    // Save all the fields in current page
                    String ID = inputID.getText().toString().trim();
                    String FName = inputFirstName.getText().toString().trim();
                    String LName = inputLastName.getText().toString().trim();
                    String Phone = inputPhone.getText().toString().trim();
                    String Address = inputAddress.getText().toString().trim();
                    String Email = inputEmail.getText().toString().trim();
                    String password = inputPassword.getText().toString().trim();

                    parent.SetID(ID);
                    parent.SetFirstName(FName);
                    parent.SetLastName(LName);
                    parent.SetPhone(Phone);
                    parent.SetAddress(Address);
                    parent.SetEmail(Email);
                    parent.SetPassword(password);

                    // Go to next page of registration (KinderGan + Kid Info)
                    Intent i = new Intent(SignupParentActivity.this, SignupParentGanActivity.class);
                    // To pass object of Parent to next activity
                    i.putExtra("parent", parent);
                    // Has object of KinderGan and Kid
                    if (extras != null)
                    {
                        // To pass object of Gan to next activity
                        i.putExtra("kindergan", Gan);
                        // To pass object of Kid to next activity
                        i.putExtra("kid", child);
                    }
                    startActivity(i);
                    finish();
                }
            }
        });

        // Link to login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(SignupParentActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * This function shows a message to the user that validation of credentials has failed.
     * @param message - The message to show to the user
     */
    public void onValidationFailed(String message)
    {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * This is a validation function that checks all the fields.
     * @return boolean - This returns true if all the fields are valid, false if one of the fields is invalid.
     */
    public boolean validate()
    {
        boolean valid = true;

        // ID validation
        if (inputID.getText().toString().isEmpty())
        {
            inputID.setError("Enter your ID !");
            requestFocus(inputID);
            valid = false;
        }
        else
        {
            inputID.setError(null);
        }

        // First name validation
        if (inputFirstName.getText().toString().isEmpty() || inputFirstName.getText().toString().contains("-"))
        {
            inputFirstName.setError("Enter your first name !");
            if (valid == true)
                requestFocus(inputFirstName);
            valid = false;
        }
        else
        {
            inputFirstName.setError(null);
        }

        // Last name validation
        if (inputLastName.getText().toString().isEmpty() || inputLastName.getText().toString().contains("-"))
        {
            inputLastName.setError("Enter your last name !");
            if (valid == true)
                requestFocus(inputLastName);
            valid = false;
        }
        else
        {
            inputLastName.setError(null);
        }

        // Phone validation
        if (inputPhone.getText().toString().isEmpty())
        {
            inputPhone.setError("Enter your phone !");
            if (valid == true)
                requestFocus(inputPhone);
            valid = false;
        }
        else
        {
            inputPhone.setError(null);
        }

        // Address validation
        if (inputAddress.getText().toString().isEmpty())
        {
            inputAddress.setError("Enter your address !");
            if (valid == true)
                requestFocus(inputAddress);
            valid = false;
        }
        else
        {
            inputAddress.setError(null);
        }

        // Email validation
        if (inputEmail.getText().toString().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches())
        {
            inputEmail.setError("Enter a valid email address !");
            if (valid == true)
                requestFocus(inputEmail);
            valid = false;
        }
        else
        {
            inputEmail.setError(null);
        }

        // Password validation
        if (!Pattern.compile(PASSWORD_PATTERN).matcher(inputPassword.getText().toString()).matches())
        {
            inputPassword.setError("Password must be at least 8 characters.\n" +
                    "Use numbers, symbols and mix of upper and lower case letters !");
            if (valid == true)
                requestFocus(inputPassword);
            valid = false;
        }
        else
        {
            inputPassword.setError(null);
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
     * This function load the fields of parent in current activity
     */
    public void LoadFields()
    {
        inputID.setText(parent.GetID());
        inputFirstName.setText(parent.GetFirstName());
        inputLastName.setText(parent.GetLastName());
        inputPhone.setText(parent.GetPhone());
        inputAddress.setText(parent.GetAddress());
        inputEmail.setText(parent.GetEmail());
        inputPassword.setText(parent.GetPassword());
    }
}
