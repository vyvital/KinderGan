package com.devsoul.dima.kindergarten.app;

import android.app.Application;
import android.os.StrictMode;
import android.text.TextUtils;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.devsoul.dima.kindergarten.helper.jobs.DemoJobCreator;
import com.evernote.android.job.JobManager;

/**
 * Setting up a Singleton for RequestQueue volley object
 * We use here the Volley library
 */
public class AppController extends Application
{
    public static final String TAG = AppController.class.getSimpleName();
    private static AppController mInstance;
    private RequestQueue myRequest;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        // Creating the JobManager instance and add our job to the JobManager
        JobManager.create(this).addJobCreator(new DemoJobCreator());
        // Ignore the URI exposure in Android N with Camera
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public static synchronized AppController getInstance()
    {
        return mInstance;
    }

    public RequestQueue getRequestQueue()
    {
        if (myRequest == null)
        {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            myRequest = Volley.newRequestQueue(getApplicationContext());
        }
        return myRequest;
    }

    public <T> void addToRequestQueue (Request<T> req, String tag)
    {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue (Request<T> req)
    {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag)
    {
        if(myRequest != null)
        {
            myRequest.cancelAll(tag);
        }
    }
}
