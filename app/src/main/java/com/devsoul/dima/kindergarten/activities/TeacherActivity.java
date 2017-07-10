package com.devsoul.dima.kindergarten.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.devsoul.dima.kindergarten.fabbo.FabOptions;
import com.devsoul.dima.kindergarten.fragments.TimePickerFragment;
import com.devsoul.dima.kindergarten.helper.CSVWriter;
import com.devsoul.dima.kindergarten.helper.GridViewAdapter;
import com.devsoul.dima.kindergarten.helper.SQLiteHandler;
import com.devsoul.dima.kindergarten.model.Attendance;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Teacher Main Activity screen
 */
public class TeacherActivity extends AppCompatActivity implements TimePickerFragment.TimeDialogListener
{
    private static final String TAG = TeacherActivity.class.getSimpleName();
    private static final String DIALOG_TIME = "TeacherActivity.TimeDialog";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSION_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
                                                  Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String FILE_DIRECTORY = "KinderGan";   // File directory of csv and photo file
    private static final String CSV_NAME = "attendance.csv";    // csv file name

    private TextView txtGan;
    private TextView txtCls;
    private FabOptions mFabOptions;

    private SQLiteHandler db;
    private ProgressDialog pDialog;

    private Attendance attendance;

    private HashMap<String, String> teacher;
    private HashMap<String, String> kid;

    private String KinderGan_Name, KinderGan_Class;

    //An ArrayLists for storing kids path pictures
    private ArrayList<String> KIDPICS_LIST;
    //An ArrayLists for storing kids presence
    private ArrayList<Integer> KIDSPRESENCE_LIST;

    private GridView gridView;

    private String phone_number;
    private File imageFile;

    private Calendar calendar;
    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        verifyStoragePermissions(this);

        txtGan = (TextView) findViewById(R.id.ganName);
        txtCls = (TextView) findViewById(R.id.clsNum);

        mFabOptions = (FabOptions) findViewById(R.id.fab_options);

        final ImageView frame = (ImageView) findViewById(R.id.frame);
        final ImageView BtnClose = (ImageView) findViewById(R.id.BtnClose);
        final ImageView BtnCall = (ImageView) findViewById(R.id.BtnCall);
        final ImageView BtnSms = (ImageView) findViewById(R.id.BtnSms);
        final ImageView BtnDetails = (ImageView) findViewById(R.id.BtnDetails);
        final ImageView missBtn = (ImageView) findViewById(R.id.missBtn);
        final ImageView arrvBtn = (ImageView) findViewById(R.id.arrvBtn);

        attendance = new Attendance();

        //Initializing the ArrayLists
        KIDPICS_LIST = new ArrayList<String>();
        KIDSPRESENCE_LIST = new ArrayList<Integer>();

        calendar = Calendar.getInstance();
        // Get today date day from calendar
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        db = new SQLiteHandler(getApplicationContext());

        // Fetching teacher details from SQLite teachers table
        teacher = db.getTeacherDetails();
        KinderGan_Name = teacher.get(db.KEY_KINDERGAN_NAME);
        KinderGan_Class = teacher.get(db.KEY_KINDERGAN_CLASS).toString();
        // Set teacher Gan name as label
        txtGan.setText("Gan " + KinderGan_Name);
        // Set teacher Gan class as label
        txtCls.setText("Class " + KinderGan_Class);

        // Load kids pictures from SQLite
        KIDPICS_LIST = db.getKidsPictures();
        // Load kids presence from SQLite
        KIDSPRESENCE_LIST = db.getKidsPresence();

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new GridViewAdapter(this, KIDPICS_LIST, KIDSPRESENCE_LIST));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id)
            {
                // Open menu
                frame.setVisibility(View.VISIBLE);
                BtnClose.setVisibility(View.VISIBLE);
                BtnCall.setVisibility(View.VISIBLE);
                BtnSms.setVisibility(View.VISIBLE);
                BtnDetails.setVisibility(View.VISIBLE);

                // Get the item
                final CircleImageView item = (CircleImageView) v.findViewById(R.id.grid_item_image);

                // Get image path
                final String image_path = KIDPICS_LIST.get(position);

                // Item frame with red color
                if (item.getBorderColor()== getResources().getColor(R.color.color2))
                {
                    missBtn.setVisibility(View.GONE);
                    arrvBtn.setVisibility(View.VISIBLE);
                }
                // Item frame with green color
                else
                {
                    missBtn.setVisibility(View.VISIBLE);
                    arrvBtn.setVisibility(View.GONE);
                }

                // Miss Button
                missBtn.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        missBtn.setVisibility(View.GONE);
                        arrvBtn.setVisibility(View.VISIBLE);
                        item.setBorderColor(getResources().getColor(R.color.color2));
                        int presence = 0;
                        String ParentID = db.getParentID(image_path);

                        // Update in SQLite presence column in kid table
                        db.UpdateKidPresence(ParentID, presence);
                        // Update column in MySQL in kids table and attendance table
                        UpdatePresence(ParentID, day, presence);
                        Toast.makeText(TeacherActivity.this,"Presence updated successfully",Toast.LENGTH_SHORT).show();
                    }
                });

                // Arrive Button
                arrvBtn.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v){
                        missBtn.setVisibility(View.VISIBLE);
                        arrvBtn.setVisibility(View.GONE);
                        item.setBorderColor(getResources().getColor(R.color.green));
                        int presence = 1;
                        String ParentID = db.getParentID(image_path);

                        // Update in SQLite presence column in kid table
                        db.UpdateKidPresence(ParentID, presence);
                        // Update column in MySQL in kids table and attendance table
                        UpdatePresence(ParentID, day, presence);
                        Toast.makeText(TeacherActivity.this,"Presence updated successfully",Toast.LENGTH_SHORT).show();
                    }
                });

                // Call Button
                BtnCall.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        phone_number = db.getParentPhone(image_path);
                        callPhoneNumber(phone_number);
                    }
                });

                // SMS Button
                BtnSms.setOnClickListener(new View.OnClickListener()
                {
                    public void onClick(View v)
                    {
                        phone_number = db.getParentPhone(image_path);
                        sendSMS(phone_number);
                    }
                });

                // Details Button
                BtnDetails.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(TeacherActivity.this);
                        View mView = getLayoutInflater().inflate(R.layout.dialog_details,null);
                        final TextView KidName = (TextView) mView.findViewById(R.id.Kid_Name);
                        final TextView ParentFulName = (TextView) mView.findViewById(R.id.Parent_FullName);
                        final TextView BirthDate = (TextView) mView.findViewById(R.id.Kid_BirthDate);
                        final TextView Address = (TextView) mView.findViewById(R.id.Address);
                        final TextView Requests = (TextView) mView.findViewById(R.id.Special_Requests);
                        final TextView Contact1 = (TextView) mView.findViewById(R.id.Contact1);
                        final TextView Contact2 = (TextView) mView.findViewById(R.id.Contact2);
                        final TextView Contact3 = (TextView) mView.findViewById(R.id.Contact3);

                        // Get details of kid and his parent
                        kid = db.getDetails(image_path);

                        // Set details to text views
                        KidName.setText("Kid Name: " + kid.get(db.KEY_NAME));
                        ParentFulName.setText("Parent Full Name: " + kid.get(db.KEY_FIRST_NAME) + " " + kid.get(db.KEY_LAST_NAME));
                        BirthDate.setText("Birth Date: " + kid.get(db.KEY_BIRTHDATE));
                        Address.setText("Address: " + kid.get(db.KEY_ADDRESS));

                        // Have already a special request
                        if (!kid.get(db.KEY_SPECIAL).contentEquals("null") && kid.get(db.KEY_SPECIAL).length() != 0)
                        {
                            Requests.setVisibility(View.VISIBLE);
                            Requests.setText("Special Requests: " + kid.get(db.KEY_SPECIAL));
                        }

                        // Have already contact1
                        if (!kid.get(db.KEY_CONTACT1).contentEquals("null") && kid.get(db.KEY_CONTACT1).length() != 0)
                        {
                            Contact1.setVisibility(View.VISIBLE);
                            Contact1.setText("Contact: " + kid.get(db.KEY_CONTACT1));
                        }
                        // Have already contact2
                        if (!kid.get(db.KEY_CONTACT2).contentEquals("null") && kid.get(db.KEY_CONTACT2).length() != 0)
                        {
                            Contact2.setVisibility(View.VISIBLE);
                            Contact2.setText("Contact: " + kid.get(db.KEY_CONTACT2));
                        }
                        // Have already contact3
                        if (!kid.get(db.KEY_CONTACT3).contentEquals("null") && kid.get(db.KEY_CONTACT3).length() != 0)
                        {
                            Contact3.setVisibility(View.VISIBLE);
                            Contact3.setText("Contact: " + kid.get(db.KEY_CONTACT3));
                        }

                        mBuilder.setView(mView);
                        AlertDialog dialogz = mBuilder.create();
                        dialogz.show();
                    }
                });
            }
        });

        // Close button
        BtnClose.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                frame.setVisibility(View.GONE);
                BtnClose.setVisibility(View.GONE);
                BtnCall.setVisibility(View.GONE);
                BtnSms.setVisibility(View.GONE);
                BtnDetails.setVisibility(View.GONE);
                missBtn.setVisibility(View.GONE);
                arrvBtn.setVisibility(View.GONE);
            }
        });

        // Menu options
        mFabOptions.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch (view.getId()) {
                    case R.id.faboptions_notification:
                        TimePickerFragment dialog = new TimePickerFragment();
                        dialog.show(getSupportFragmentManager(), DIALOG_TIME);
                        break;
                    case R.id.faboptions_camera:
                        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        File newFile = new File(file, FILE_DIRECTORY);
                        if (!newFile.exists())
                            newFile.mkdirs();
                        Camera();
                        break;
                    case R.id.faboptions_download:
                        // Delete table of attendance from SQLite
                        if (db.getRowCount(SQLiteHandler.TABLE_ATTENDANCE) > 0)
                            db.deleteTable(SQLiteHandler.TABLE_ATTENDANCE);

                        GetAttendance(KinderGan_Name, KinderGan_Class);
                        openCSVFile(CSV_NAME);
                        break;
                    case R.id.faboptions_share:
                        ArrayList<String> emails = db.getEmails();
                        String[] emailsArray = new String[emails.size()];
                        emailsArray = emails.toArray(emailsArray);
                        sendEmail(emailsArray);
                        break;
                    default:
                }
            }
        });
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
            Toast.makeText(TeacherActivity.this,
                    "SMS failed, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void sendEmail(String[] recipient)
    {
        Log.i("Send email", "");
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipient);

        try
        {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
            Log.i("Finished sending email", "");
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(TeacherActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open the camera and save picture in media store if required
     */
    protected void Camera()
    {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+ "/" + FILE_DIRECTORY, imageFileName);
        Uri tempuri = Uri.fromFile(imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,tempuri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
        startActivityForResult(intent,0);
    }

    /**
     * Save the file captured by camera in internal storage
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode==0)
        {
            switch (resultCode)
            {
                case Activity.RESULT_OK:
                    if (imageFile.exists())
                    {
                        Toast.makeText(this,"The file was saved at"+imageFile.getAbsolutePath(),Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(this,"There was an error",Toast.LENGTH_LONG).show();
                    break;
                case Activity.RESULT_CANCELED:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Function to update kid presence in MySQL,
     * update kids and attendance tables.
     * Will post all params to login url
     * @param ParentID
     * @param Day - The day to update the presence
     * @param Presence - 1 Arrived, 0 Missing
     */
    private void UpdatePresence(final String ParentID, final int Day, final int Presence)
    {
        // Tag used to cancel the request
        String tag_string_req = "presence_request";

        pDialog.setMessage("Updating kid presence ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "kid presence Response: " + response.toString());
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
                params.put("tag", "update_presence");
                params.put("parent_id", ParentID);
                params.put("day", Integer.toString(Day));
                params.put("presence", Integer.toString(Presence));
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
     * Function to update teacher notification time in MySQL,
     * will post all params to login url
     * @param TeacherKinderGanName
     * @param TeacherKinderGanClass
     * @param TeacherTime - Notification time
     */
    private void UpdateNotificationTime(final String TeacherKinderGanName, final String TeacherKinderGanClass, final String TeacherTime)
    {
        // Tag used to cancel the request
        String tag_string_req = "notification_request";

        pDialog.setMessage("Updating notification time ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "teacher notification time Response: " + response.toString());
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
                params.put("tag", "update_notification");
                params.put("KinderGan_Name", TeacherKinderGanName);
                params.put("KinderGan_Class", TeacherKinderGanClass);
                params.put("Notification_Time", TeacherTime);
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
     * onFinishDialog of alarm time picker
     * @param time - the time that was chosen by the teacher for the notification
     */
    @Override
    public void onFinishDialog(String time)
    {
        Log.d("Selected Time: "+ time, "");

        // Update teacher notification time
        teacher.put(db.KEY_NOTIFICATION_TIME, time);
        // Update in SQLite notification time column in teacher table
        db.UpdateTeacherNotificationTime(time);
        // Update column in MySQL
        UpdateNotificationTime(teacher.get(db.KEY_KINDERGAN_NAME), teacher.get(db.KEY_KINDERGAN_CLASS), time);
        Toast.makeText(TeacherActivity.this, "Notification alarm were updated successfully: " + time, Toast.LENGTH_SHORT).show();
    }

    /**
     * Function to get attendance from MySQL to SQLite,
     * will post all params to login url
     * @param KinderGan_Name
     * @param KinderGan_Class
     */
    private void GetAttendance(final String KinderGan_Name, final String KinderGan_Class)
    {
        // Tag used to cancel the request
        String tag_string_req = "attendance_request";

        pDialog.setMessage("Loading kids attendance ...");
        showDialog();

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "kids attendance Response: " + response.toString());
                hideDialog();

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // Kids attendance successfully loaded from MySQL
                        // Now store the attendance in SQLite
                        JSONArray attendanceArr = jObj.getJSONArray("attendance");
                        for (int i = 0; i < attendanceArr.length(); i++)
                        {
                            JSONObject record = attendanceArr.getJSONObject(i);
                            attendance.SetUID(record.getString("uid"));
                            attendance.SetParentID(record.getString("parent_id"));
                            attendance.SetName(record.getString("name"));
                            attendance.SetFirst(record.getString("1st"));
                            attendance.SetSecond(record.getString("2nd"));
                            attendance.SetThird(record.getString("3rd"));
                            attendance.SetFourth(record.getString("4th"));
                            attendance.SetFifth(record.getString("5th"));
                            attendance.SetSixth(record.getString("6th"));
                            attendance.SetSeventh(record.getString("7th"));
                            attendance.SetEighth(record.getString("8th"));
                            attendance.SetNinth(record.getString("9th"));
                            attendance.SetTenth(record.getString("10th"));
                            attendance.SetEleventh(record.getString("11th"));
                            attendance.SetTwelfth(record.getString("12th"));
                            attendance.SetThirteenth(record.getString("13th"));
                            attendance.SetFourteenth(record.getString("14th"));
                            attendance.SetFifteenth(record.getString("15th"));
                            attendance.SetSixteenth(record.getString("16th"));
                            attendance.SetSeventeenth(record.getString("17th"));
                            attendance.SetEighteenth(record.getString("18th"));
                            attendance.SetNineteenth(record.getString("19th"));
                            attendance.SetTwentieth(record.getString("20th"));
                            attendance.SetTwentiethFirst(record.getString("21st"));
                            attendance.SetTwentiethSecond(record.getString("22nd"));
                            attendance.SetTwentiethThird(record.getString("23rd"));
                            attendance.SetTwentiethFourth(record.getString("24th"));
                            attendance.SetTwentiethFifth(record.getString("25th"));
                            attendance.SetTwentiethSixth(record.getString("26th"));
                            attendance.SetTwentiethSeventh(record.getString("27th"));
                            attendance.SetTwentiethEighth(record.getString("28th"));
                            attendance.SetTwentiethNinth(record.getString("29th"));
                            attendance.SetThirtieth(record.getString("30th"));
                            attendance.SetThirtiethFirst(record.getString("31st"));

                            // Inserting row in attendance table
                            db.addAttendance(attendance);
                        }

                        exportDB(CSV_NAME);
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
                params.put("tag", "get_attendance");
                params.put("KinderGan_Name", KinderGan_Name);
                params.put("KinderGan_Class", KinderGan_Class);
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
     * Export SQLite table to csv file
     */
    private void exportDB(String fileName)
    {
        File exportDir = new File(Environment.getExternalStorageDirectory(), FILE_DIRECTORY);
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, fileName);
        try
        {
            file.createNewFile();
            OutputStream os = new FileOutputStream(file.getAbsolutePath());
            // UTF-8 Encoding
            os.write(239);
            os.write(187);
            os.write(191);

            CSVWriter csvWrite = new CSVWriter(new OutputStreamWriter(os, "UTF-8"));
            db.getAttendanceDetails(csvWrite, day);
            csvWrite.flush();
            csvWrite.close();
        }
        catch (Exception sqlEx)
        {
            Log.e(TAG, sqlEx.getMessage(), sqlEx);
        }
    }

    /*
    Open the csv file
     */
    private void openCSVFile(String fileName)
    {
        File file = new File(Environment.getExternalStorageDirectory(), FILE_DIRECTORY + "/" + fileName);
        Intent csvIntent = new Intent(Intent.ACTION_VIEW);
        Uri path = Uri.fromFile(file);
        csvIntent.setDataAndType(path, "text/csv");
        //csvIntent.setDataAndType(FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", finalFile), "text/csv");
        csvIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        csvIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(csvIntent);
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
