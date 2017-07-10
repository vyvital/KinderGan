package com.devsoul.dima.kindergarten.helper.jobs;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * JobCreator acts like a factory to provide a Job based on a job tag.
 * Your concrete JobCreator class must implement the JobCreator interface and override the create method.
 */
public class DemoJobCreator implements JobCreator
{
    @Override
    public Job create(String tag)
    {
        switch (tag)
        {
            case ShowNotificationJob.TAG:
                return new ShowNotificationJob();
            default:
                return null;
        }
    }
}
