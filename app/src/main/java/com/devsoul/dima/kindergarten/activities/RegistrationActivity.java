package com.devsoul.dima.kindergarten.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import com.devsoul.dima.kindergarten.R;

/**
 * The Registration Activity is the activity where the user choose his category: Parent or Nanny
 */
public class RegistrationActivity extends Activity
{
    private static final String TAG = RegistrationActivity.class.getSimpleName();

    private ImageButton imgbtnParent, imgbtnNanny;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        imgbtnParent = (ImageButton) findViewById(R.id.img_btn_parent);
        imgbtnNanny = (ImageButton) findViewById(R.id.img_btn_nanny);

        // parent click event
        imgbtnParent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Start the parent sign up activity
                Intent intent = new Intent(RegistrationActivity.this, SignupParentActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Nanny click event
        imgbtnNanny.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Start the teacher sign up activity
                Intent intent = new Intent(RegistrationActivity.this, SignupTeacherActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
