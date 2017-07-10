package com.devsoul.dima.kindergarten.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.devsoul.dima.kindergarten.R;
import com.devsoul.dima.kindergarten.helper.BitmapHandler;
import com.devsoul.dima.kindergarten.helper.SessionManager;
import com.devsoul.dima.kindergarten.model.KinderGan;
import com.devsoul.dima.kindergarten.model.Teacher;

/**
 * The Signup teacher Activity enables the user that is a teacher to create an account in the application,
 * and is generally displayed via the link on the Registration Activity.
 */
public class SignupTeacherActivity extends Activity
{
    private static final String TAG = SignupTeacherActivity.class.getSimpleName();

    private static final int PICK_IMAGE_REQUEST = 1; // To get Image from gallery

    @InjectView(R.id.input_id)    EditText inputID;
    @InjectView(R.id.input_FName) EditText inputFirstName;
    @InjectView(R.id.input_LName) EditText inputLastName;
    @InjectView(R.id.input_phone) EditText inputPhone;
    @InjectView(R.id.link_login)  TextView btnLinkToLogin;

    private ImageButton btnImgChoose;    // Choose the image
    private ImageView imageView;         // To show the selected image

    private ImageButton img_btnNext;

    private SessionManager session;
    private BitmapHandler bmpHandler;

    private Bundle extras;

    private Teacher Nanny;
    private KinderGan Gan;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_teacher);

        // Image choose button
        btnImgChoose = (ImageButton) findViewById(R.id.btn_NannyPic);
        // View of the image
        imageView = (ImageView) findViewById(R.id.imageView);

        // Next button
        img_btnNext = (ImageButton) findViewById(R.id.img_btn_next);

        // Inject the ButterKnife design
        ButterKnife.inject(this);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Bitmap handler
        bmpHandler = new BitmapHandler(getApplicationContext());

        // Create nanny object
        Nanny = new Teacher();

        // Check if user is already logged in or not
        if (session.isLoggedIn())
        {
            // User is already logged in. Take him to user activity
            Intent intent = new Intent(SignupTeacherActivity.this, UserActivity.class);
            startActivity(intent);
            finish();
        }

        // To retrieve objects (Teacher, KinderGan) in current activity
        extras = getIntent().getExtras();
        if (extras != null)
        // Has objects from previous activity (SignUpKinderGan Activity)
        {
            //Obtaining data
            Nanny = (Teacher) getIntent().getSerializableExtra("teacher");
            Gan = (KinderGan) getIntent().getSerializableExtra("kindergan");
            // Load fields of Nanny
            LoadFields();
        }

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

        // Next page Button Click event
        img_btnNext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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

                    Nanny.SetID(ID);
                    Nanny.SetFirstName(FName);
                    Nanny.SetLastName(LName);
                    Nanny.SetPhone(Phone);

                    // Go to next page of registration (KinderGan Info)
                    Intent i = new Intent(SignupTeacherActivity.this, SignupTeacherGanActivity.class);
                    // To pass object of Nanny to next activity
                    i.putExtra("teacher", Nanny);
                    // Has object of KinderGan
                    if (extras != null)
                    {
                        // To pass object of Gan to next activity
                        i.putExtra("kindergan", Gan);
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
                Intent i = new Intent(SignupTeacherActivity.this, LoginActivity.class);
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

        // Nanny Picture validation
        if (bmpHandler.GetBitmap() == null)
        {
            onValidationFailed("Enter your picture !");
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
     * This function load the fields of nanny in current activity
     */
    public void LoadFields()
    {
        inputID.setText(Nanny.GetID());
        inputFirstName.setText(Nanny.GetFirstName());
        inputLastName.setText(Nanny.GetLastName());
        inputPhone.setText(Nanny.GetPhone());

        // Load the image
        bmpHandler.loadBitmap(Uri.parse(Nanny.GetPicture()), imageView);
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
            Uri image_path = data.getData();
            try
            {
                //final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                //getContentResolver().takePersistableUriPermission(image_path, takeFlags);

                // Decode the image and set on the image view
                bmpHandler.loadBitmap(image_path, imageView);

                // Save the picture path in nanny object
                Nanny.SetPicture(image_path.toString());
            }
            catch (Exception e)
            {
                //handle exception
                e.printStackTrace();
            }
        }
    }
}
