package com.burstly.conveniencelayer;

import android.support.v4.app.Fragment;

/**
 */
public class BurstlyFragment extends Fragment {
    @Override
    public void onResume() {
        Burstly.onResumeFragment(this);

        super.onResume();

    }

    @Override
    public void onPause() {
        Burstly.onPauseFragment(this);

        super.onPause();

    }

    @Override
    public void onDestroyView() {
        Burstly.onDestroyFragment(this);

        super.onDestroyView();
    }
}
