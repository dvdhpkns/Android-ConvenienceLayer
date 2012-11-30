package com.burstly.conveniencelayer;

import android.app.Activity;

/**
 * IActivityListener receives callbacks for onPause, onResume, and onDestroy {@link Activity} events
 */
public interface IActivityListener {
    /**
     * {@link Activity} paused
     * @param activity {@link Activity} that was paused
     */
    void activityPaused(final Activity activity);

    /**
     * {@link Activity} resumed
     * @param activity {@link Activity} that was resumed
     */
    void activityResumed(final Activity activity);

    /**
     * {@link Activity} destroyed
     * @param activity {@link Activity} that was destroyed
     */
    void activityDestroyed(final Activity activity);
}
