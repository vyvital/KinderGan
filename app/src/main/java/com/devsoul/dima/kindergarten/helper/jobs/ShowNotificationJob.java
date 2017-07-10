package com.devsoul.dima.kindergarten.helper.jobs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.devsoul.dima.kindergarten.R;
import com.devsoul.dima.kindergarten.activities.MissingActivity;
import com.devsoul.dima.kindergarten.app.AppConfig;
import com.devsoul.dima.kindergarten.app.AppController;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Helper class of notification job
 */
public class ShowNotificationJob extends Job
{
    static final String TAG = "ShowNotificationJob";;
    private String ParentID;

    @NonNull
    @Override
    protected Result onRunJob(Params params)
    {
        Log.i(TAG, "onRunJob:" + params.getId());

        ParentID = getParams().getExtras().getString("ParentID", null);
        Log.i(TAG, "ID:" + ParentID);

        if (ParentID != null)
            // Get presence from MySQL
            GetPresence(ParentID);

        return Result.RESCHEDULE;
    }

    /*
    Reschedule every day (every 24 hours)
     */
    @Override
    protected void onReschedule(int newJobId)
    {
        super.onReschedule(newJobId);

        Log.i(TAG, "onReschedule:" + newJobId);

        // Run periodic job every day
        schedulePeriodic(ParentID);
    }

    /**
     * Create the notification that the child is missing from the kindergarten
     */
    private void createNotification()
    {
        // Prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(getContext(), MissingActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getContext(), (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        Notification notification = new Notification.Builder(getContext())
                .setContentTitle("Missing child")
                .setContentText("Your child is absent today from kindergarten")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[] { 1000, 3000 })
                .setLights(Color.RED, 3000, 3000)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);
    }

    /**
     * Schedule exact job on the time that was given
     * @param hour
     * @param minutes
     */
    public static void scheduleExact(int hour, int minutes, String ParentID)
    {
        long time = getRequiredTime(hour, minutes) - System.currentTimeMillis();

        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("ParentID", ParentID);

        new JobRequest.Builder(ShowNotificationJob.TAG)
                      .setExact(time)
                      .setPersisted(true)
                      .setUpdateCurrent(true)
                      .setExtras(extras)
                      .build()
                      .schedule();
    }

    /**
     * Schedule periodic job every 24 hours
     */
    public static void schedulePeriodic(String ParentID)
    {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("ParentID", ParentID);

        new JobRequest.Builder(ShowNotificationJob.TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1))
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setExtras(extras)
                .build()
                .schedule();
    }

    /**
     * Schedule periodic job every 15 minutes
     * for debug (testing)
     */
    public static void schedulePeriodic1(String ParentID)
    {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("ParentID", ParentID);

        new JobRequest.Builder(ShowNotificationJob.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setExtras(extras)
                .build()
                .schedule();
    }

    private static long getRequiredTime(int hour, int minutes)
    {
        Calendar now = Calendar.getInstance();
        Calendar required_time = Calendar.getInstance();

        required_time.set(Calendar.HOUR_OF_DAY, hour);
        required_time.set(Calendar.MINUTE, minutes);

        // Alarm in the same day
        if (now.getTimeInMillis() < required_time.getTimeInMillis())
        {
            return required_time.getTimeInMillis();
        }

        // Alarm in next day
        required_time.add(Calendar.DATE, 1);
        return required_time.getTimeInMillis();
    }

    /**
     * Function to get presence of the kid from MySQL with Parent ID as parameter,
     * will post all params to login url
     * @param ParentID - The ID of the parent of the kid that we want his presence.
     */
    private void GetPresence(final String ParentID)
    {
        // Tag used to cancel the request
        String tag_string_req = "presence_request";

        // Making the volley http request
        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.LOGIN_URL, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                Log.d(TAG, "presence Response: " + response.toString());

                try
                {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error)
                    {
                        // Presence successfully loaded from MySQL
                        // Now store the Presence
                        JSONObject kid = jObj.getJSONObject("kid");
                        int presence =  Integer.parseInt(kid.getString("presence"));

                        Log.i(TAG, "presence:" + Integer.toString(presence));
                        // kid doesn't presence at kindergarten
                        if (presence == 0)
                            createNotification();
                    }
                    else
                    {
                        // Error occurred in retrieval. Get the error message
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
            }
        })
        {
            @Override
            protected Map<String, String> getParams()
            {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get_presence");
                params.put("Parent_ID", ParentID);
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
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Cancel the job
     * @param jobId
     */
    private void cancelJob(int jobId)
    {
        JobManager.instance().cancel(jobId);
    }
}
