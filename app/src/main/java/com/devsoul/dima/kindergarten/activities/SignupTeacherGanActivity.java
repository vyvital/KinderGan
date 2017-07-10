package com.devsoul.dima.kindergarten.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.devsoul.dima.kindergarten.R;
import com.devsoul.dima.kindergarten.app.AppConfig;
import com.devsoul.dima.kindergarten.app.AppController;
import com.devsoul.dima.kindergarten.helper.BitmapHandler;
import com.devsoul.dima.kindergarten.helper.SQLiteHandler;
import com.devsoul.dima.kindergarten.helper.SessionManager;
import com.devsoul.dima.kindergarten.model.KinderGan;
import com.devsoul.dima.kindergarten.model.Teacher;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The Signup TeacherGan Activity enables the user that is a teacher to create an account in the application,
 * and is generally displayed via the next button on the Sign Up Teacher Activity.
 */
public class SignupTeacherGanActivity extends Activity
{
    private static final String TAG = SignupTeacherGanActivity.class.getSimpleName();

    private static final String PASSWORD_PATTERN =
                    "((?=.*\\d)" +        // must contains one digit from 0-9
                    "(?=.*[a-z])" +       // must contains one lowercase characters
                    "(?=.*[A-Z])" +       // must contains one uppercase characters
                    "(?=.*[!@#$%])" +     // must contains one special symbols in the list "!@#$%"
                    "(?!.*\\s)" +         // disallow spaces
                    ".{8,15})";           // length at least 8 characters and maximum of 15

    //An ArrayLists for Spinners Items
    private ArrayList<String> CITY_LIST;
    private ArrayList<String> GAN_LIST;
    private ArrayList<String> CLASS_LIST;

    //Declaring Spinners
    private MaterialBetterSpinner dropdownKinderGanCity;
    private MaterialBetterSpinner dropdownKinderGanName;
    private MaterialBetterSpinner dropdownTeacherClass;

    @InjectView(R.id.input_email) EditText inputEmail;
    @InjectView(R.id.input_password) EditText inputPassword;
    @InjectView(R.id.link_login) TextView btnLinkToLogin;

    private ImageButton img_btnBack, img_btnRegister;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private BitmapHandler bmpHandler;

    private Teacher Nanny;
    private KinderGan Gan;

    private int classes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_teacher_gan);

        //Initializing the ArrayLists
        CITY_LIST = new ArrayList<String>();
        GAN_LIST = new ArrayList<String>();
        CLASS_LIST = new ArrayList<String>();

        //Initializing Spinners
        dropdownKinderGanCity = (MaterialBetterSpinner)findViewById(R.id.android_material_design_spinner3);
        dropdownKinderGanName =(MaterialBetterSpinner)findViewById(R.id.android_material_design_spinner);
        dropdownTeacherClass =(MaterialBetterSpinner)findViewById(R.id.android_material_design_spinner2);

        // Back button
        img_btnBack = (ImageButton) findViewById(R.id.img_btn_back);
        // Register button
        img_btnRegister = (ImageButton) findViewById(R.id.img_btn_SignUp);

        // Inject the ButterKnife design
        ButterKnife.inject(this);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn())
        {
            // User is already logged in. Take him to user activity
            Intent intent = new Intent(SignupTeacherGanActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        }

        // This method will fetch the Kindergans data from the database
        loadCitiesSpinnerData();

        // To retrieve objects in current activity
        Nanny = (Teacher) getIntent().getSerializableExtra("teacher");
        Gan = (KinderGan) getIntent().getSerializableExtra("kindergan");
        if (Gan != null)
        // Has object of Gan from previous activity (SignUpTeacher Activity)
        {
            // Load fields of KinderGan
            LoadFields();
        }
        else
        {
            classes = 0;
        }

        //Setting adapters to show the items in the spinners
        dropdownKinderGanCity.setAdapter(new ArrayAdapter<String>(SignupTeacherGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CITY_LIST));
        dropdownKinderGanName.setAdapter(new ArrayAdapter<String>(SignupTeacherGanActivity.this, android.R.layout.simple_spinner_dropdown_item, GAN_LIST));
        dropdownTeacherClass.setAdapter(new ArrayAdapter<String>(SignupTeacherGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CLASS_LIST));

        // On KinderGan city item selected
        dropdownKinderGanCity.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                // Selected item
                String selected = dropdownKinderGanCity.getText().toString();
                Log.d(TAG, selected);

                // Get Kindergan names from SQLite
                GAN_LIST = db.getKinderGanNamesbyCity(selected);
                Log.d(TAG, "Gan Names List: " + GAN_LIST);
                //Setting adapter to show the items in the spinner
                dropdownKinderGanName.setAdapter(new ArrayAdapter<String>(SignupTeacherGanActivity.this, android.R.layout.simple_spinner_dropdown_item, GAN_LIST));
                // Clear the showing text in name spinner
                dropdownKinderGanName.setText("");
            }
        });

        // On KinderGan name item selected
        dropdownKinderGanName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                // Selected item
                String selected = dropdownKinderGanName.getText().toString();
                Log.d(TAG, selected);

                // Get number of classes from SQLite
                classes = db.getKinderGanClasses(selected);
                Gan.SetClasses(classes);
                LoadClassSpinner(Gan.GetClasses());
                // Clear the showing text in class spinner
                dropdownTeacherClass.setText("");
            }
        });

        // Previous page Button Click event
        img_btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Save all the fields in current page
                String KinderGan_city = dropdownKinderGanCity.getText().toString();
                String KinderGan_name = dropdownKinderGanName.getText().toString();
                String teacher_class = dropdownTeacherClass.getText().toString();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                Gan = new KinderGan(KinderGan_name, KinderGan_city);
                if (classes != 0)
                    Gan.SetClasses(classes);
                Nanny.SetClass(teacher_class);
                Nanny.SetEmail(email);
                Nanny.SetPassword(password);

                // Go to previous page of registration (teacher Info)
                Intent i = new Intent(SignupTeacherGanActivity.this, SignupTeacherActivity.class);
                //To pass object of teacher to previous activity
                i.putExtra("teacher", Nanny);
                //To pass object of gan to previous activity
                i.putExtra("kindergan", Gan);
                startActivity(i);
                finish();
            }
        });

        // Sign up Button Click event
        img_btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signup();
            }
        });

        // Link to login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(SignupTeacherGanActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * This function load the fields of KinderGan in current activity
     */
    public void LoadFields()
    {
        dropdownKinderGanCity.setText(Gan.GetCity());
        if (!Gan.GetCity().isEmpty())
        {
            // Populate the KinderGan name spinner according to the city
            // Get Kindergan names from SQLite
            GAN_LIST = db.getKinderGanNamesbyCity(Gan.GetCity());
            Log.d(TAG, "Gan Names List: " + GAN_LIST);
        }
        dropdownKinderGanName.setText(Gan.GetName());
        if (Gan.GetClasses() != 0 )
        {
            // Populate the class spinner
            LoadClassSpinner(Gan.GetClasses());
        }
        dropdownTeacherClass.setText(Nanny.GetClass());

        inputEmail.setText(Nanny.GetEmail());
        inputPassword.setText(Nanny.GetPassword());
    }

    /**
     * This function performs the sign up operation.
     */
    public void signup()
    {
        Log.d(TAG, "SignupTeacherGan");

        String KinderGanCity = dropdownKinderGanCity.getText().toString().trim();
        String KinderGanName = dropdownKinderGanName.getText().toString().trim();
        String TeacherClass = dropdownTeacherClass.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        Gan = new KinderGan(KinderGanName, KinderGanCity);
        Nanny.SetClass(TeacherClass);
        Nanny.SetEmail(email);
        Nanny.SetPassword(password);

        // One of the fields is invalid
        if (!validate())
        {
            onSignupFailed("Sign up failed");
            return;
        }

        bmpHandler = new BitmapHandler(getApplicationContext());
        registerUser();
    }

    /**
     * This function shows a message to the user that the sign up has failed.
     */
    public void onSignupFailed(String message)
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

        // KinderGan City validation
        if (Gan.GetCity().isEmpty())
        {
            dropdownKinderGanCity.setError("Choose your kindergan city !");
            requestFocus(dropdownKinderGanCity);
            valid = false;
        }
        else
        {
            dropdownKinderGanCity.setError(null);
        }

        // KinderGan Name validation
        if (Gan.GetName().isEmpty())
        {
            dropdownKinderGanName.setError("Choose your kindergan name !");
            if (valid == true)
                requestFocus(dropdownKinderGanName);
            valid = false;
        }
        else
        {
            dropdownKinderGanName.setError(null);
        }

        // Teacher's class validation
        if (Nanny.GetClass().isEmpty())
        {
            dropdownTeacherClass.setError("Choose your kindergan class !");
            if (valid == true)
                requestFocus(dropdownTeacherClass);
            valid = false;
        }
        else
        {
            dropdownTeacherClass.setError(null);
        }

        // Email validation
        if (Nanny.GetEmail().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(Nanny.GetEmail()).matches())
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
        if (!Pattern.compile(PASSWORD_PATTERN).matcher(Nanny.GetPassword()).matches())
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
     * Function to store user in MySQL database,
     * will post all params to register url
     */
    private void registerUser()
    {
        // Tag used to cancel the request
        String tag_string_req = "register_request";

        pDialog.setMessage("Creating Account ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.REGISTER_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "register Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // Teacher user successfully stored in MySQL
                        // Now store the teacher user in SQLite
                        JSONObject user = jObj.getJSONObject("user");
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

                        session.setLogin(true);
                        // Create type session
                        session.setType(1);

                        Toast.makeText(getApplicationContext(), "User successfully registered.", Toast.LENGTH_LONG).show();

                        // Launch user activity
                        Intent intent = new Intent(SignupTeacherGanActivity.this, UserActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        // Error occurred in registration. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onSignupFailed(errorMsg);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                onSignupFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "register_teacher");
                params.put("ID", Nanny.GetID());
                params.put("First_Name", Nanny.GetFirstName());
                params.put("Last_Name", Nanny.GetLastName());
                params.put("Phone", Nanny.GetPhone());
                //Converting Bitmap to String
                String image = bmpHandler.getStringImage(bmpHandler.decodeSampledBitmapFromStream(Uri.parse(Nanny.GetPicture()), 300, 300));
                params.put("Picture", image);
                params.put("KinderGan_Name", Gan.GetName());
                params.put("KinderGan_City", Gan.GetCity());
                params.put("Class", Nanny.GetClass());
                params.put("Email", Nanny.GetEmail());
                params.put("Password", Nanny.GetPassword());

                return params;
            }
        };

        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        //Volley does retry for you if you have specified the policy.
        strReq.setRetryPolicy(new DefaultRetryPolicy(7000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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

    /**
     * Function to load Kindergans from MySQL database to SQLite,
     * will post all params to register url
     */
    private void LoadKinderGans()
    {
        // Tag used to cancel the request
        String tag_string_req = "kindergans_request";

        pDialog.setMessage("Loading kindergans data ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.REGISTER_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "kindergans Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // Kindergans successfully loaded from MySQL
                        // Now store the Kindergans in SQLite
                        JSONArray gans = jObj.getJSONArray("Gans");
                        Gan = new KinderGan();
                        for (int i = 0; i < gans.length(); i++)
                        {
                            JSONObject gan = gans.getJSONObject(i);
                            Gan.SetID(gan.getString("uid"));
                            Gan.SetName(gan.getString("name"));
                            Gan.SetClasses(Integer.parseInt(gan.getString("classes")));
                            Gan.SetAddress(gan.getString("address"));
                            Gan.SetCity(gan.getString("city"));
                            Gan.SetPhone(gan.getString("phone"));

                            // Inserting row in kindergans table
                            db.addKindergan(Gan);
                        }

                        // Load all cities of Kindergartens from SQLite
                        LoadFromSQLite();
                    }
                    else
                    {
                        // Error occurred in registration. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onSignupFailed(errorMsg);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Load Error: " + error.getMessage());
                onSignupFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get_kindergans");
                return params;
            }
        };

        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        //Volley does retry for you if you have specified the policy.
        strReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Function to load the cities spinner data from SQLite database or MySQL, and if loaded from MySQL after load from SQLite
     */
    private void loadCitiesSpinnerData()
    {
        // SQLite KinderGans table is empty
        if (db.getRowCount(db.TABLE_GANS) == 0)
        {
            // Load all Gans from mysql
            LoadKinderGans();
        }
        // SQLite Gans table isn't empty
        else
        {
            LoadFromSQLite();
        }
    }

    /**
     * Load all cities of Kindergartens from SQLite
     */
    private void LoadFromSQLite()
    {
        CITY_LIST = db.getKinderGanCities();
        Log.d(TAG, "Gan Cities List: " + CITY_LIST);
        //Setting adapters to show the items in the spinners
        dropdownKinderGanCity.setAdapter(new ArrayAdapter<String>(SignupTeacherGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CITY_LIST));
    }

    /**
     * Load class spinner
     */
    private void LoadClassSpinner(int classes)
    {
        // Clear the list of classes
        CLASS_LIST.clear();
        for (int i = 0; i < classes; i++)
        {
            // Add the classes to list
            CLASS_LIST.add(Integer.toString(i+1));
        }
        Log.d(TAG, "Class List: " + CLASS_LIST);
        //Setting adapter to show the items in the spinner
        dropdownTeacherClass.setAdapter(new ArrayAdapter<String>(SignupTeacherGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CLASS_LIST));
    }
}
