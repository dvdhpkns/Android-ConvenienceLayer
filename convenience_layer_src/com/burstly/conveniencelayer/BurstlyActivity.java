package com.burstly.conveniencelayer;

import android.app.Activity;

/**
 * Wraps Activity events and dispatches them to the Burstly Convencience Layer subsystem
 */
public class BurstlyActivity extends Activity {
    /**
     * Verifies that com.burstly.conveniencelayer.Burstly was initialized and dispatches the onResume event
     */
    @Override
    public void onResume() {
        Burstly.onResumeActivity(this);

        super.onResume();
    }

    /**
     * Verifies that com.burstly.conveniencelayer.Burstly was initialized and dispatches the onPause event
     */
    @Override
    public void onPause() {
        Burstly.onPauseActivity(this);

        super.onPause();
    }

    /**
     * Verifies that com.burstly.conveniencelayer.Burstly was initialized and dispatches the onDestroy event
     */
    @Override
    public void onDestroy() {
        Burstly.onDestroyActivity(this);

        super.onDestroy();
    }
}
