package com.burstly.conveniencelayer;

import android.support.v4.app.Fragment;

/**
 * IFragmentListener receives callbacks for onPause, onResume, and onDestroy {@link Fragment} events
 */
public interface IFragmentListener {
    /**
     * {@link Fragment} paused
     * @param fragment {@link Fragment} that was paused
     */
    void fragmentPaused(final Fragment fragment);

    /**
     * {@link Fragment} resumed
     * @param fragment {@link Fragment} that was resumed
     */
    void fragmentResumed(final Fragment fragment);

    /**
     * {@link Fragment} destroyed
     * @param fragment {@link Fragment} that was destroyed
     */
    void fragmentDestroyed(final Fragment fragment);
}
