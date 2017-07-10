package com.devsoul.dima.kindergarten.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.*;
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
import com.devsoul.dima.kindergarten.helper.jobs.ShowNotificationJob;
import com.devsoul.dima.kindergarten.model.Kid;
import com.devsoul.dima.kindergarten.model.KinderGan;
import com.devsoul.dima.kindergarten.model.Parent;
import com.devsoul.dima.kindergarten.model.Teacher;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The User Activity fetching the logged user information from SQLite and displaying it on the screen.
 * The logout button will logout the user by clearing the session and deleting the user from SQLite table.
 */
public class UserActivity extends Activity
{
    private static final String TAG = UserActivity.class.getSimpleName();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private static final int PICK_IMAGE_REQUEST = 1; // To get Image from gallery

    private TextView txtName;
    private TextView txtEmail;
    private CircleImageView imageView;
    private ImageButton btnEnter;
    private ImageButton btnLogout;
    private ImageButton btnContacts;
    private ImageButton btnPick;
    private ImageButton btnSpecial;
    private ImageButton btnSchlt;
    private ImageButton btnSchlp;

    private ProgressDialog pDialog, mProgressDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private BitmapHandler bmpHandler;

    private Kid Child;
    private Parent Prnt;
    private KinderGan Gan;
    private Teacher Nanny;

    private HashMap<String, String> kid;
    private HashMap<String, String> user;

    private ArrayList<String> FULLNAME_LIST;
    private String phone_number;
    private Uri image_path;               // Path of the image
    private Boolean exist = false;       // url exist online or not
    private String schedule_planURL;
    private AlertDialog dialog;         // Dialog of schedule

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        verifyStoragePermissions(this);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        imageView = (CircleImageView) findViewById(R.id.circle_profile);
        btnEnter = (ImageButton) findViewById(R.id.btnEnter);
        btnLogout = (ImageButton) findViewById(R.id.btnLogout);
        btnContacts = (ImageButton) findViewById(R.id.btnContacts);
        btnPick = (ImageButton) findViewById(R.id.btnPick);
        btnSpecial = (ImageButton) findViewById(R.id.btnSpecial);
        btnSchlt = (ImageButton) findViewById(R.id.btnSchlt);
        btnSchlp = (ImageButton) findViewById(R.id.btnSchlp);

        Child = new Kid();
        Prnt = new Parent();
        Gan = new KinderGan();
        Nanny = new Teacher();

        FULLNAME_LIST = new ArrayList<String>();

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Bitmap handler
        bmpHandler = new BitmapHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (!session.isLoggedIn())
        {
            logoutUser();
        }

        String path;

        if (db.getRowCount(SQLiteHandler.TABLE_GANS) != 0)
            // Delete the table from SQLite, because we need to load kindergan schedule from MySQL server
            db.deleteTable(db.TABLE_GANS);

        // Get the user type from session
        switch (session.getType())
        {
            // Teacher type
            case 1:
            {
                // Fetching user details from SQLite teachers table
                user = db.getTeacherDetails();
                path = user.get(db.KEY_PHOTO);

                // Fetching schedule plan
                LoadSchedule(user.get(db.KEY_KINDERGAN_NAME));

                btnSchlt.setVisibility(View.VISIBLE);
                btnSchlp.setVisibility(View.GONE);
                break;
            }
            // Parent type
            case 2:
            {
                // Fetching user details from SQLite parents table
                user = db.getParentDetails();

                // Fetching kid details from SQLite kids table
                kid = db.getKidDetails();
                path = kid.get(db.KEY_PHOTO);

                if (db.getRowCount(SQLiteHandler.TABLE_TEACHERS) == 0)
                {
                    LoadTeachers(user.get(db.KEY_ID), kid.get(db.KEY_KINDERGAN_NAME), kid.get(db.KEY_CLASS));
                }

                // Fetching schedule plan
                LoadSchedule(kid.get(db.KEY_KINDERGAN_NAME));

                btnSchlp.setVisibility(View.VISIBLE);
                btnSchlt.setVisibility(View.GONE);
                btnEnter.setVisibility(View.GONE);
                btnContacts.setVisibility(View.VISIBLE);
                btnPick.setVisibility(View.VISIBLE);
                btnSpecial.setVisibility(View.VISIBLE);
                break;
            }
            default:
            {
                return;
            }
        }

        String name = user.get(db.KEY_FIRST_NAME) + " " + user.get(db.KEY_LAST_NAME);
        String email = user.get(db.KEY_EMAIL);

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Show user profile image in circle image view
        Picasso.with(getApplicationContext()).load(path).placeholder(R.drawable.profile).error(R.drawable.profile)
                .into(imageView);

        // Schedule click event in Nanny
        btnSchlt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Checking if schedule file exist online
                MyTask task = new MyTask();
                task.execute(schedule_planURL);

                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                builder.setTitle("Schedule");
                builder.setMessage("Would you like to Upload, View or Download the Schedule?");
                builder.setPositiveButton("Download", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        startDownload(user.get(db.KEY_KINDERGAN_NAME));
                    }
                });

                builder.setNeutralButton("Upload", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showFileChooser();
                    }
                });
                builder.setNegativeButton("View", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"));
                        intent.setType("image/*");
                        startActivity(intent);
                    }
                });
                dialog = builder.create();
                dialog.show();
            }
        });

        // Schedule click event in Parent
        btnSchlp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Checking if schedule file exist online
                MyTask task = new MyTask();
                task.execute(schedule_planURL);

                AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                builder.setTitle("Schedule");
                builder.setMessage("Would you like to View or Download the Schedule?");
                builder.setPositiveButton("Download", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        startDownload(kid.get(db.KEY_KINDERGAN_NAME));
                    }
                });

                builder.setNegativeButton("View", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"));
                        intent.setType("image/*");
                        startActivity(intent);
                    }
                });
                dialog = builder.create();
                dialog.show();
            }
        });

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                logoutUser();
            }
        });

        btnEnter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if ((db.getRowCount(SQLiteHandler.TABLE_KIDS) == 0) && (db.getRowCount(SQLiteHandler.TABLE_PARENTS) == 0))
                {
                    // Load kids that belong to specific teacher from MySQL
                    LoadKids(user.get(db.KEY_KINDERGAN_NAME), user.get(db.KEY_KINDERGAN_CLASS));
                }
                else
                {
                    // Delete the tables from SQLite, because we need to load kids presence from MySQL server
                    db.deleteTable(db.TABLE_PARENTS);
                    db.deleteTable(db.TABLE_KIDS);
                    // Load kids that belong to specific teacher from MySQL
                    LoadKids(user.get(db.KEY_KINDERGAN_NAME), user.get(db.KEY_KINDERGAN_CLASS));
                }
            }
        });

        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(UserActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_pick,null);
                final EditText mPerson1 = (EditText) mView.findViewById(R.id.person1);
                final EditText mPerson2 = (EditText) mView.findViewById(R.id.person2);
                final EditText mPerson3 = (EditText) mView.findViewById(R.id.person3);
                Button mEdit = (Button) mView.findViewById(R.id.EditBtn);

                // Have already contact1
                if (!kid.get(db.KEY_CONTACT1).contentEquals("null"))
                {
                    // Set contact1 from SQLite to edit text
                    mPerson1.setText(kid.get(db.KEY_CONTACT1));
                }

                // Have already contact2
                if (!kid.get(db.KEY_CONTACT2).contentEquals("null"))
                {
                    // Set contact2 from SQLite to edit text
                    mPerson2.setText(kid.get(db.KEY_CONTACT2));
                }

                // Have already contact3
                if (!kid.get(db.KEY_CONTACT3).contentEquals("null"))
                {
                    // Set contact3 from SQLite to edit text
                    mPerson3.setText(kid.get(db.KEY_CONTACT3));
                }

                mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Update kid contact1
                        kid.put(db.KEY_CONTACT1, mPerson1.getText().toString());
                        // Update kid contact2
                        kid.put(db.KEY_CONTACT2, mPerson2.getText().toString());
                        // Update kid contact3
                        kid.put(db.KEY_CONTACT3, mPerson3.getText().toString());
                        // Update in SQLite contacts columns in kid table
                        db.UpdateKidContacts(user.get(db.KEY_ID), kid.get(db.KEY_CONTACT1), kid.get(db.KEY_CONTACT2), kid.get(db.KEY_CONTACT3));
                        // Update contacts columns in MySQL
                        UpdateKidContacts();
                        Toast.makeText(UserActivity.this,"Person added to list successfully",Toast.LENGTH_SHORT).show();
                    }
                });
                mBuilder.setView(mView);
                AlertDialog dialogz = mBuilder.create();
                dialogz.show();
            }
        });

        btnSpecial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(UserActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_special,null);
                final EditText sTxt = (EditText) mView.findViewById(R.id.sTxt);
                Button mEdit2 = (Button) mView.findViewById(R.id.EditBtn2);

                // Have already a special request
                if (!kid.get(db.KEY_SPECIAL).contentEquals("null"))
                {
                    // Set special request from SQLite to edit text
                    sTxt.setText(kid.get(db.KEY_SPECIAL));
                }

                mEdit2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Update kid special request
                        kid.put(db.KEY_SPECIAL, sTxt.getText().toString());
                        // Update in SQLite special column in kid table
                        db.UpdateKidSpecialRequest(user.get(db.KEY_ID), kid.get(db.KEY_SPECIAL));
                        // Update column in MySQL
                        UpdateSpecialRequest();
                        Toast.makeText(UserActivity.this,"Special requests were added successfully",Toast.LENGTH_SHORT).show();
                    }
                });
                mBuilder.setView(mView);
                AlertDialog dialogz = mBuilder.create();
                dialogz.show();
            }
        });

        btnContacts.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Load the child's nannies from SQLite
                getTeacherNames();
                ShowAlertDialogNamesWithListview();
            }
        });
    }

    /**
     * Logging out the user.
     * Will set isLoggedIn flag to false in shared preferences, type flag to 0
     * and clears the user data from SQLite users table
     */
    private void logoutUser()
    {
        session.setLogin(false);
        session.setType(0);
        db.deleteTable(db.TABLE_TEACHERS);
        db.deleteTable(db.TABLE_PARENTS);
        db.deleteTable(db.TABLE_KIDS);
        db.deleteTable(db.TABLE_GANS);

        // Launching the login activity
        Intent intent = new Intent(UserActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Function to load Kids and Parents that belong to specific Teacher from MySQL database to SQLite,
     * will post all params to login url
     * @param TeacherKinderGanName
     * @param TeacherKinderGanClass
     */
    private void LoadKids(final String TeacherKinderGanName, final String TeacherKinderGanClass)
    {
        // Tag used to cancel the request
        String tag_string_req = "kids_request";

        pDialog.setMessage("Loading kids data ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "kids Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // Kids successfully loaded from MySQL
                        // Now store the Kids in SQLite
                        JSONArray kids = jObj.getJSONArray("Kids");
                        for (int i = 0; i < kids.length(); i++)
                        {
                            JSONObject kid = kids.getJSONObject(i);
                            Child.SetName(kid.getString("name"));
                            Child.SetBirthDate(kid.getString("birth_date"));
                            Child.SetPicture(kid.getString("photo"));
                            Gan.SetName(kid.getString("kindergan_name"));
                            Child.SetClass(kid.getString("class"));
                            Child.SetParentID(kid.getString("parent_id"));

                            Child.SetPresence(kid.getString("presence"));
                            Child.SetSpecial(kid.getString("special"));
                            Child.SetContact1(kid.getString("contact1"));
                            Child.SetContact2(kid.getString("contact2"));
                            Child.SetContact3(kid.getString("contact3"));

                            // Inserting row in kids table
                            db.addKid(Child, Gan);
                        }

                        // Parents successfully loaded from MySQL
                        // Now store the Parents in SQLite
                        JSONArray parents = jObj.getJSONArray("Parents");
                        for (int i = 0; i < parents.length(); i++)
                        {
                            JSONObject parent = parents.getJSONObject(i);
                            Prnt.SetID(parent.getString("ID"));
                            Prnt.SetFirstName(parent.getString("first_name"));
                            Prnt.SetLastName(parent.getString("last_name"));
                            Prnt.SetAddress(parent.getString("address"));
                            Prnt.SetPhone(parent.getString("phone"));
                            Prnt.SetEmail(parent.getString("email"));

                            // Inserting row in parents table
                            db.addParent(Prnt);
                        }

                        // Go to teacher activity
                        Intent intent = new Intent(UserActivity.this,TeacherActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        // Error occurred in registration. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onLoadFailed(errorMsg);
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
                onLoadFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get_kids");
                params.put("KinderGan_Name", TeacherKinderGanName);
                params.put("KinderGan_Class", TeacherKinderGanClass);
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
     * Function to load Teachers of specific kid from MySQL database to SQLite,
     * will post all params to login url
     * @param ParentID
     * @param KidKinderGanName
     * @param KidKinderGanClass
     */
    private void LoadTeachers(final String ParentID, final String KidKinderGanName, final String KidKinderGanClass)
    {
        // Tag used to cancel the request
        String tag_string_req = "teachers_request";

        pDialog.setMessage("Loading teachers data ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "teachers Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // Teachers successfully loaded from MySQL
                        // Now store the Teachers in SQLite
                        JSONArray teachers = jObj.getJSONArray("Teachers");
                        for (int i = 0; i < teachers.length(); i++)
                        {
                            JSONObject teacher = teachers.getJSONObject(i);
                            Nanny.SetID(teacher.getString("ID"));
                            Nanny.SetFirstName(teacher.getString("first_name"));
                            Nanny.SetLastName(teacher.getString("last_name"));
                            Nanny.SetPhone(teacher.getString("phone"));
                            Gan.SetName(teacher.getString("kindergan_name"));
                            Nanny.SetClass(teacher.getString("kindergan_class"));
                            Nanny.SetEmail(teacher.getString("email"));
                            Nanny.SetNotificationTime(teacher.getString("notification_time"));

                            // Inserting row in teachers table
                            db.addTeacher(Nanny, Gan);
                        }

                        // The push up notification if kid isn't arrived to kindergarten
                        Notification();
                    }
                    else
                    {
                        // Error occurred in loading. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onLoadFailed(errorMsg);
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
                onLoadFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get_teachers");
                params.put("ID", ParentID);
                params.put("KinderGan_Name", KidKinderGanName);
                params.put("KinderGan_Class", KidKinderGanClass);
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
     * Function to update kid special request in MySQL,
     * will post all params to login url
     */
    private void UpdateSpecialRequest()
    {
        // Tag used to cancel the request
        String tag_string_req = "special_request";

        pDialog.setMessage("Updating kid special request ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "special request Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (error)
                    {
                        // Error occurred in update. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onLoadFailed(errorMsg);
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
                onLoadFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "update_special_request");
                params.put("parent_id", user.get(db.KEY_ID));
                params.put("special", kid.get(db.KEY_SPECIAL));
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
     * Function to update kid contacts in MySQL,
     * will post all params to login url
     */
    private void UpdateKidContacts()
    {
        // Tag used to cancel the request
        String tag_string_req = "kidcontacts_request";

        pDialog.setMessage("Updating kid pickup list ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "kid contact list Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (error)
                    {
                        // Error occurred in update. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onLoadFailed(errorMsg);
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
                onLoadFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "update_pickup_list");
                params.put("parent_id", user.get(db.KEY_ID));
                params.put("contact1", kid.get(db.KEY_CONTACT1));
                params.put("contact2", kid.get(db.KEY_CONTACT2));
                params.put("contact3", kid.get(db.KEY_CONTACT3));
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
     * Function to upload schedule to MySQL database,
     * will post all params to login url
     * @param TeacherKinderGanName
     * @param TeacherKinderGanCity
     */
    private void uploadSchedule(final String TeacherKinderGanName, final String TeacherKinderGanCity)
    {
        // Tag used to cancel the request
        String tag_string_req = "schedule_request";

        pDialog.setMessage("Uploading schedule ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "schedule Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (error)
                    {
                        // Error occurred in insert. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onLoadFailed(errorMsg);
                    }
                    else
                    {
                        onLoadFailed("The schedule uploaded");
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
                Log.e(TAG, "Update Error: " + error.getMessage());
                onLoadFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "upload_schedule");
                params.put("KinderGan_Name", TeacherKinderGanName);
                params.put("KinderGan_City", TeacherKinderGanCity);
                //Converting Bitmap to String
                String image = bmpHandler.getStringImage(bmpHandler.decodeSampledBitmapFromStream(Uri.parse(Gan.GetSchedule()), 720, 1280));
                params.put("Schedule", image);

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
     * Function to load schedule from MySQL database to SQLite,
     * will post all params to login url
     * @param KinderGanName
     */
    private void LoadSchedule(final String KinderGanName)
    {
        // Tag used to cancel the request
        String tag_string_req = "schedule_request";

        pDialog.setMessage("Loading schedule ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "schedule Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // KinderGan successfully loaded from MySQL
                        // Now store the KinderGan in SQLite
                        JSONObject gan = jObj.getJSONObject("gan");
                        Gan.SetID(gan.getString("uid"));
                        Gan.SetName(gan.getString("name"));
                        Gan.SetClasses(Integer.parseInt(gan.getString("classes")));
                        Gan.SetAddress(gan.getString("address"));
                        Gan.SetCity(gan.getString("city"));
                        Gan.SetPhone(gan.getString("phone"));
                        Gan.SetSchedule(gan.getString("schedule_plan"));

                        // Inserting row in kindergans table
                        db.addKindergan(Gan);

                        // Get the schedule url
                        schedule_planURL = db.getKinderGanSchedule();
                    }
                    else
                    {
                        // Error occurred in loading. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        onLoadFailed(errorMsg);
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
                onLoadFailed(error.getMessage());
                hideDialog();
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get_schedule");
                params.put("KinderGan_Name", KinderGanName);
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
     * This function shows a message to the user that the load has failed.
     */
    public void onLoadFailed(String message)
    {
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
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
     * Load the child's nannies from SQLite
     */
    private void getTeacherNames()
    {
        FULLNAME_LIST = db.getTeacherNames();
        Log.d(TAG, "Nannies List: " + FULLNAME_LIST);
    }

    /**
     * Show alert dialog with the names of Nannies of the kid
     */
    public void ShowAlertDialogNamesWithListview()
    {
        //Create sequence of items
        final CharSequence[] Options = FULLNAME_LIST.toArray(new String[FULLNAME_LIST.size()]);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Names");
        dialogBuilder.setItems(Options, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                // Selected item in alert dialog
                String selectedText = Options[item].toString();
                // Open alert dialog of contacts
                ShowAlertDialogWithListview(selectedText);
            }
        });
        //Create alert dialog object via builder
        AlertDialog alertDialogObject = dialogBuilder.create();
        //Show the dialog
        alertDialogObject.show();
    }

    /**
     * Show alert dialog with options to contact: Call, SMS, Email
     * @param TeacherFullName - The selected teacher name
     */
    public void ShowAlertDialogWithListview(String TeacherFullName)
    {
        List<String> mOptions = new ArrayList<String>();
        mOptions.add("Call");
        mOptions.add("Send SMS");
        mOptions.add("Send email");

        // Split the full name to first name and last name by - sign
        String[] parts = TeacherFullName.split(" - ");
        final String FirstName = parts[0]; // First name
        final String LastName = parts[1]; // Last name

        ArrayList<String> Contact;
        Contact = db.getTeacherContact(FirstName, LastName);
        phone_number = Contact.get(0);
        final String email = Contact.get(1);

        //Create sequence of items
        final CharSequence[] Options = mOptions.toArray(new String[mOptions.size()]);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Contact");
        dialogBuilder.setItems(Options, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                switch (item)
                {
                    // Phone call
                    case 0:
                    {
                        callPhoneNumber(phone_number);
                        break;
                    }
                    // Send sms
                    case 1:
                    {
                        sendSMS(phone_number);
                        break;
                    }
                    // Send email
                    case 2:
                    {
                        sendEmail(email);
                        break;
                    }
                }
            }
        });
        //Create alert dialog object via builder
        AlertDialog alertDialogObject = dialogBuilder.create();
        //Show the dialog
        alertDialogObject.show();
    }

    public void callPhoneNumber(String number)
    {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        // Permission to call doesn't granted
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            // Request permission to call on runtime
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 101);
        }
        // Permission to call granted
        else
        {
            // Start call
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + number));
            startActivity(callIntent);
        }
    }

    @Override
    // Request permission to call on runtime
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode == 101)
        {
            // Permission to call granted
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
            {
                callPhoneNumber(phone_number);
            }
            else
            {
                Log.d(TAG, "Call Permission Not Granted");
            }
        }
    }

    protected void sendSMS(String number)
    {
        Log.i("Send SMS", "");
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        smsIntent.setData(Uri.parse("smsto:"));
        //smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address"  , number);

        try
        {
            startActivity(smsIntent);
            finish();
            Log.i("Finished sending SMS...", "");
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(UserActivity.this,
                    "SMS failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void sendEmail(String recipient)
    {
        Log.i("Send email", "");
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { recipient });

        try
        {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(UserActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Performs the entire alert process
     */
    public void Notification()
    {
        // get notification time from teacher
        String notification_time = db.getNotificationTime();
        if (notification_time != null)
        {
            // Split time to hours and minutes
            String[] parts = notification_time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            // Set notification alarm
            ShowNotificationJob.scheduleExact(hours, minutes, kid.get(db.KEY_PARENT_ID));
        }
    }

    //Check if the file is online or not and if it's a picture or not
    private class MyTask extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con = (HttpURLConnection) new URL(params[0]).openConnection();
                con.setRequestMethod("HEAD");
                System.out.println(con.getResponseCode());
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            boolean bResponse = result;
            if (bResponse==true)
            {
                exist = true;
                // Set download button visible
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                Toast.makeText(UserActivity.this, "File exists online!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                exist = false;
                // Set download button invisible
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.INVISIBLE);
                Toast.makeText(UserActivity.this, "The file doesn't exist, Please make sure the Schedule is Uploaded", Toast.LENGTH_SHORT).show();
            }
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
     */
    //@TargetApi(Build.VERSION_CODES.KITKAT)
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            image_path = data.getData();
            // Save the picture path in Gan object
            Gan.SetSchedule(image_path.toString());

            uploadSchedule(user.get(db.KEY_KINDERGAN_NAME), user.get(db.KEY_CITY));
        }
    }

    private void startDownload(String KinderGanName)
    {
        String url = schedule_planURL;
        new DownloadFileAsync().execute(url, KinderGanName);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading file..");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    class DownloadFileAsync extends AsyncTask<String, String, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl)
        {
            int count;

            try
            {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());

                File storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                final File newFile = new File(storagePath,"KinderGan");
                if (!newFile.exists())
                    newFile.mkdirs();
                OutputStream output = new FileOutputStream(new File(newFile,"Schedule" + aurl[1] + ".png"));

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1)
                {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            }
            catch (Exception e)
            {
                onLoadFailed("Download Failed, Please Make Sure A Schedule Have Been Uploaded");
            }
            return null;
        }

        protected void onProgressUpdate(String... progress)
        {
            Log.d("ANDRO_ASYNC",progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused)
        {
            Toast.makeText(getBaseContext(), "Download Complete, The File Is In DCIM/KinderGan Folder", Toast.LENGTH_LONG).show();
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
    }

    /*
    Verify external read and write permissions and grant them on runtime
    */
    public static void verifyStoragePermissions(Activity activity)
    {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity,
                    PERMISSION_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}
