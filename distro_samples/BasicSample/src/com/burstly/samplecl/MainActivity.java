package com.burstly.samplecl;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.burstly.conveniencelayer.*;
import com.burstly.conveniencelayer.events.AdFailEvent;

/**
 * This basic sample shows how to add a simple banner and interstitial.
 *
 * The banner is anchored to the top of the screen and the will auto rotate every 30 seconds.
 * The interstitial is triggered when the button at the bottom of the screen is clicked and will launch in a new activity
 */
public class MainActivity extends BurstlyActivity implements View.OnClickListener {
    /**
     * The button used to trigger our interstitial
     */
    private Button mButton;

    /**
     * The BurstlyInterstitial instance used to show interstitials
     */
    private BurstlyInterstitial mInterstitial;

    /**
     * A listener which only listens for ad failure. We will add this listener to the mInterstitial to receive event
     * callbacks.  When an interstitial fails to load we will re-enable the button and allow the user to retry.
     */
    private BurstlyListenerAdapter mListener = new BurstlyListenerAdapter(){
        @Override
        public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {
            mButton.setEnabled(true);
            mButton.setText("Show Ad");
        }
    };

    /**
     * In our main activity we need to call Burstly.init which we do here (This call would not be made in sub activities).
     *
     * In all activities where we are showing ads we will initialize the ad objects after the layout is inflated (done via
     * setContentView in this case).
     */
    @Override
    public void onCreate(Bundle savedInstanceData) {
        super.onCreate(savedInstanceData);

        Burstly.init(this, "O3t03IGCnEG5Dqzo5hTSRA");

        setContentView(R.layout.main);

        //Banner added to the layout via the main.xml layout file.
        final BurstlyBanner banner = new BurstlyBanner(this, R.id.bannerview);
        banner.showAd();

        mInterstitial = new BurstlyInterstitial(this, "0854162579083234312", "MainMenuInterstitial", false);
        mInterstitial.addBurstlyListener(mListener);

        mButton = (Button)findViewById(R.id.button);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mButton.setEnabled(true);
        mButton.setText("Show Ad");
    }

    @Override
    public void onClick(View view) {
        mButton.setEnabled(false);
        mButton.setText("Retrieving Ad");

        mInterstitial.showAd();
    }
}
