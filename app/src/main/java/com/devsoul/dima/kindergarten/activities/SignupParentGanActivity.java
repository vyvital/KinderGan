package com.devsoul.dima.kindergarten.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import com.devsoul.dima.kindergarten.model.Kid;
import com.devsoul.dima.kindergarten.model.KinderGan;
import com.devsoul.dima.kindergarten.model.Parent;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The Signup ParentGan Activity enables the user that is a parent to create an account in the application,
 * and is generally displayed via the next button on the Sign Up Parent Activity.
 */
public class SignupParentGanActivity extends Activity
{
    private static final String TAG = SignupParentGanActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 1; // To get Image from gallery
    private static final int DIALOG_ID = 0;         // Dialog for date
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    //An ArrayLists for Spinners Items
    private ArrayList<String> CITY_LIST;
    private ArrayList<String> GAN_LIST;
    private ArrayList<String> CLASS_LIST;

    //Declaring Spinners
    private MaterialBetterSpinner dropdownKinderGanCity;
    private MaterialBetterSpinner dropdownKinderGanName;
    private MaterialBetterSpinner dropdownKidClass;

    @InjectView(R.id.input_FName) EditText inputKidFirstName;
    @InjectView(R.id.input_KidBirthDate) EditText inputKidBirthDate;
    @InjectView(R.id.link_login) TextView btnLinkToLogin;

    private ImageButton btnImgChoose;    // Choose the kid's profile image
    private ImageView imageView;         // To show the selected image
    private Uri image_path;              // Path of the image

    private ImageButton img_btnBack, img_btnRegister;

    // For kid birth date
    private Calendar calendar;
    private int year, month, day;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private BitmapHandler bmpHandler;

    private Parent parent;
    private Kid child;
    private KinderGan Gan;

    private int classes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_parent_gan);

        //Initializing the ArrayLists
        CITY_LIST = new ArrayList<String>();
        GAN_LIST = new ArrayList<String>();
        CLASS_LIST = new ArrayList<String>();

        //Initializing Spinners
        dropdownKinderGanCity = (MaterialBetterSpinner)findViewById(R.id.android_material_design_spinner3);
        dropdownKinderGanName =(MaterialBetterSpinner)findViewById(R.id.android_material_design_spinner);
        dropdownKidClass =(MaterialBetterSpinner)findViewById(R.id.android_material_design_spinner2);

        // Image choose button
        btnImgChoose = (ImageButton) findViewById(R.id.btn_KidPic);
        // View of the image
        imageView = (ImageView) findViewById(R.id.imageView);
        // Back button
        img_btnBack = (ImageButton) findViewById(R.id.img_btn_back);
        // Register button
        img_btnRegister = (ImageButton) findViewById(R.id.img_btn_SignUp);

        // Inject the ButterKnife design
        ButterKnife.inject(this);

        // Set calendar date to today date
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Bitmap handler
        bmpHandler = new BitmapHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn())
        {
            // User is already logged in. Take him to user activity
            Intent intent = new Intent(SignupParentGanActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        }

        // This method will fetch the Kindergans data from the database
        loadCitiesSpinnerData();

        // To retrieve objects in current activity
        parent = (Parent) getIntent().getSerializableExtra("parent");
        Gan = (KinderGan) getIntent().getSerializableExtra("kindergan");
        child = (Kid) getIntent().getSerializableExtra("kid");
        if ((Gan != null) && (child != null))
        // Has object of gan and child from previous activity (SignUpParent Activity)
        {
            // Load fields of KinderGan and Kid
            LoadFields();
        }
        else
        {
            // Create new objects
            child = new Kid();
            Gan = new KinderGan();
            classes = 0;
        }

        //Setting adapters to show the items in the spinners
        dropdownKinderGanCity.setAdapter(new ArrayAdapter<String>(SignupParentGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CITY_LIST));
        dropdownKinderGanName.setAdapter(new ArrayAdapter<String>(SignupParentGanActivity.this, android.R.layout.simple_spinner_dropdown_item, GAN_LIST));
        dropdownKidClass.setAdapter(new ArrayAdapter<String>(SignupParentGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CLASS_LIST));

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
                dropdownKinderGanName.setAdapter(new ArrayAdapter<String>(SignupParentGanActivity.this, android.R.layout.simple_spinner_dropdown_item, GAN_LIST));
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
                dropdownKidClass.setText("");
            }
        });

        // Kid birth date Button Click event
        inputKidBirthDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setDate();
            }
        });

        // Image choose Button Click event
        btnImgChoose.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Show images in gallery
                showFileChooser();
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
                String Kid_Class = dropdownKidClass.getText().toString();
                String Kid_FName = inputKidFirstName.getText().toString().trim();
                String Kid_BirthDate = inputKidBirthDate.getText().toString().trim();

                if (Gan != null)
                {
                    Gan.SetCity(KinderGan_city);
                    Gan.SetName(KinderGan_name);
                    // Class already in Gan object
                }
                else
                {
                    Gan = new KinderGan(KinderGan_name, KinderGan_city);
                    if (classes != 0)
                        Gan.SetClasses(classes);
                }

                if (child != null)
                {
                    child.SetName(Kid_FName);
                    child.SetClass(Kid_Class);
                    child.SetBirthDate(Kid_BirthDate);
                }
                else
                // First visit in activity
                {
                    // With picture
                    if (image_path != null)
                        child = new Kid(Kid_FName, Kid_Class, Kid_BirthDate, image_path.toString());
                    // Without picture
                    else
                        child = new Kid(Kid_FName, Kid_Class, Kid_BirthDate);
                }

                // Go to previous page of registration (parent Info)
                Intent i = new Intent(SignupParentGanActivity.this, SignupParentActivity.class);
                //To pass object of parent to previous activity
                i.putExtra("parent", parent);
                //To pass object of gan to previous activity
                i.putExtra("kindergan", Gan);
                //To pass object of kid to previous activity
                i.putExtra("kid", child);
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
                Intent i = new Intent(SignupParentGanActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void setDate()
    {
        // onCreateDialog method called
        showDialog(DIALOG_ID);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id)
    {
        if (id == DIALOG_ID)
        {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener()
    {
        /**
         * Set the date that was chosen
         * @param arg0 - The object
         * @param arg1 - Year
         * @param arg2 - Month
         * @param arg3 - Day
         */
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3)
        {
            showDate(arg1, arg2+1, arg3);
        }
    };

    /**
     * Show the date that was chosen in the kid birth date input text
     * @param year
     * @param month
     * @param day
     */
    private void showDate(int year, int month, int day)
    {
        inputKidBirthDate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }

    /**
     * This function load the fields of KinderGan and Kid in current activity
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
        dropdownKidClass.setText(child.GetClass());
        inputKidFirstName.setText(child.GetName());

        // Birth date selected
        if (child.GetBirthDate() != null)
        {
            // Load the birth date
            inputKidBirthDate.setText(child.GetBirthDate());
        }

        // Picture selected
        if (child.GetPicture() != null)
        {
            // Load the image
            bmpHandler.loadBitmap(Uri.parse(child.GetPicture()), imageView);
        }
    }

    /**
     * This method for choosing image from gallery
     */
    private void showFileChooser()
    {
        Intent intent;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        // The current version is Kitkat or higher
        {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }
        else
        // The current version is lower than Kitkat
        {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    /**
     * Get the Uri of bitmap from gallery
     * Decode the image and set it on the image view
     */
    //@TargetApi(Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            image_path = data.getData();
            try
            {
                //final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                //getContentResolver().takePersistableUriPermission(image_path, takeFlags);

                // Decode the image and set on the image view
                bmpHandler.loadBitmap(image_path, imageView);

                // Save the picture path in child object
                child.SetPicture(image_path.toString());
            }
            catch (Exception e)
            {
                //handle exception
                e.printStackTrace();
            }
        }
    }

    /**
     * This function performs the sign up operation.
     */
    public void signup()
    {
        Log.d(TAG, "SignupParentGan");

        String KinderGanCity = dropdownKinderGanCity.getText().toString().trim();
        String KinderGanName = dropdownKinderGanName.getText().toString().trim();
        String KidClass = dropdownKidClass.getText().toString().trim();
        String KidName = inputKidFirstName.getText().toString().trim();
        String KidBirthDate = inputKidBirthDate.getText().toString().trim();

        Gan.SetCity(KinderGanCity);
        Gan.SetName(KinderGanName);
        child.SetName(KidName);
        child.SetClass(KidClass);
        child.SetBirthDate(KidBirthDate);

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
            dropdownKinderGanCity.setError("Choose your child's kindergan city !");
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
            dropdownKinderGanName.setError("Choose your child's kindergan name !");
            if (valid == true)
                requestFocus(dropdownKinderGanName);
            valid = false;
        }
        else
        {
            dropdownKinderGanName.setError(null);
        }

        // Kid's class validation
        if (child.GetClass().isEmpty())
        {
            dropdownKidClass.setError("Choose your child's kindergan class !");
            if (valid == true)
                requestFocus(dropdownKidClass);
            valid = false;
        }
        else
        {
            dropdownKidClass.setError(null);
        }

        // Kid First Name validation
        if (child.GetName().isEmpty())
        {
            inputKidFirstName.setError("Enter your child's first name !");
            if (valid == true)
                requestFocus(inputKidFirstName);
            valid = false;
        }
        else
        {
            inputKidFirstName.setError(null);
        }

        // Kid Birth Date validation
        try
        {
            if (inputKidBirthDate.getText().toString().isEmpty() ||
                    (formatter.parse(inputKidBirthDate.getText().toString()).after(new Date())))
            {
                inputKidBirthDate.setError("Enter your child's birth date !");
                valid = false;
            }
            else
            {
                inputKidBirthDate.setError(null);
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        // Kid Picture validation
        if (bmpHandler.GetBitmap() == null)
        {
            onSignupFailed("Enter your child's picture !");
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
                        // Parent user successfully stored in MySQL
                        // Now store the parent user in SQLite
                        JSONObject user = jObj.getJSONObject("user");
                        parent.SetID(user.getString("ID"));
                        parent.SetFirstName(user.getString("firstname"));
                        parent.SetLastName(user.getString("lastname"));
                        parent.SetAddress(user.getString("address"));
                        parent.SetPhone(user.getString("phone"));
                        parent.SetEmail(user.getString("email"));
                        parent.SetCreatedAt(user.getString("created_at"));

                        // Inserting row in parents table
                        db.addParent(parent);

                        // Now store the child user in SQLite
                        child.SetName(user.getString("kid_name"));
                        child.SetBirthDate(user.getString("kid_birthdate"));
                        child.SetPicture(user.getString("kid_photo"));
                        child.SetClass(user.getString("kid_class"));
                        Gan.SetName(user.getString("kindergan_name"));
                        child.SetParentID(user.getString("ID"));
                        child.SetCreatedAt(user.getString("created_at"));
                        child.SetPresence(user.getString("presence"));

                        // Inserting row in kids table
                        db.addKid(child, Gan);

                        session.setLogin(true);
                        // Create type session
                        session.setType(2);

                        Toast.makeText(getApplicationContext(), "User successfully registered.", Toast.LENGTH_LONG).show();

                        // Launch user activity
                        Intent intent = new Intent(SignupParentGanActivity.this, UserActivity.class);
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
                params.put("tag", "register_parent");
                params.put("ID", parent.GetID());
                params.put("First_Name", parent.GetFirstName());
                params.put("Last_Name", parent.GetLastName());
                params.put("Address", parent.GetAddress());
                params.put("Phone", parent.GetPhone());

                params.put("Kid_Name", child.GetName());
                params.put("Kid_BirthDate", child.GetBirthDate());
                //Converting Bitmap to String
                String image = bmpHandler.getStringImage(bmpHandler.decodeSampledBitmapFromStream(Uri.parse(child.GetPicture()), 300, 300));
                params.put("Kid_Picture", image);
                params.put("KinderGan_Name", Gan.GetName());
                params.put("Kid_Class", child.GetClass());
                params.put("Email", parent.GetEmail());
                params.put("Password", parent.GetPassword());

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
        dropdownKinderGanCity.setAdapter(new ArrayAdapter<String>(SignupParentGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CITY_LIST));
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
        dropdownKidClass.setAdapter(new ArrayAdapter<String>(SignupParentGanActivity.this, android.R.layout.simple_spinner_dropdown_item, CLASS_LIST));
    }
}