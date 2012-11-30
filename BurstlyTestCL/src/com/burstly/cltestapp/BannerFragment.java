/**
 * 
 */
package com.burstly.cltestapp;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.burstly.conveniencelayer.*;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;

/**
 *
 */
public class BannerFragment extends BurstlyFragment {
    private static final String TAG = "BannerFragment";
    private static final int SWIPE_MIN_DISTANCE = 40;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int REFRESH_TIME = 20;

    /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
      */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.scrollview, container, false);
        LinearLayout parentLayout = (LinearLayout) layout.findViewById(R.id.adNetworkParentLayout);

        for(BurstlyIntegrationModeAdNetworks ad : BurstlyIntegrationModeAdNetworks.values()){
            if(ad != BurstlyIntegrationModeAdNetworks.REWARDS_SAMPLE && ad != BurstlyIntegrationModeAdNetworks.DISABLED){
                //get banner view containing status, banner, etc.
                View bannerView = getBannerView(ad);

                //add view to parent
                parentLayout.addView(bannerView);
            }
        }
        return layout;
    }

    private View getBannerView(BurstlyIntegrationModeAdNetworks ad){
        //String zone = ad.getBannerZone();
        String adName = ad.getAdName();

        //inflate view from layout file
        View bannerView;
        LayoutInflater vi = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bannerView = vi.inflate(R.layout.banner_inflater, null);

        //set adName in TextView
        TextView bannerAdName = (TextView)bannerView.findViewById(R.id.adName);
        bannerAdName.setText(adName);
        //get reference to status populate with loading text
        final TextView status = (TextView)bannerView.findViewById(R.id.adStatus);
        status.setText(getString(R.string.loading));
        //initialize progressbar
        LinearLayout progressBar = (LinearLayout)bannerView.findViewById(R.id.progressBar);
        ProgressBar pB = new ProgressBar(REFRESH_TIME*1000, 10, progressBar);

        //reference to the layout that contains only the add
        LinearLayout bannerParent = (LinearLayout)bannerView.findViewById(R.id.bannerParent);

        Burstly.setIntegrationNetwork(ad);
        //Add BurstlyBanner to layout
        final BurstlyBanner banner = new BurstlyBanner(this,
                bannerParent,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT),
                "0000000000000000000",
                adName,
                REFRESH_TIME);

        //add listener that will update status and start progress bar
        banner.addBurstlyListener(getBurstlyListener(status, pB));
        //display ad
        banner.showAd();

        //create onclick listener to refresh ad
        ImageView refreshButton = (ImageView)bannerView.findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText(getString(R.string.loading));
                banner.showAd();
            }
        });
        return bannerView;
    }

    private BurstlyListenerAdapter getBurstlyListener(final TextView status, final ProgressBar pB) {
        BurstlyListenerAdapter listener = new BurstlyListenerAdapter() {
            @Override
            public void onShow(final BurstlyBaseAd ad, final AdShowEvent event) {
                status.setText(getString(R.string.idle));
                pB.start();
                Log.d(TAG, "Ad has been shown: " + ad.getName());
            }

            @Override
            public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event) {
                //should not be seen
                status.setText(getString(R.string.precached));
            }

            @Override
            public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {

                if(event.wasRequestThrottled())
                    status.setText(getString(R.string.throttled));
                else
                    status.setText(getString(R.string.failed));
                    Log.d(TAG, "onFail: " + event.toString());
            }
        };

        return listener;
    }

    /**
     * Class used to create ad progress bar that lasts the duration of ad refresh
     */
    class ProgressBar extends CountDownTimer {
        private LinearLayout progressBar;
        private View timeDown;
        private View timeRemaining;
        private long millisInFuture;

        public ProgressBar(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public ProgressBar(long millisInFuture, long countDownInterval, LinearLayout progressBar) {
            super(millisInFuture, countDownInterval);
            this.millisInFuture= millisInFuture;
            this.progressBar = progressBar;
            timeDown = (View)progressBar.findViewById(R.id.timeDown);
            timeRemaining = (View)progressBar.findViewById(R.id.timeRemaining);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            timeDown.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (millisInFuture-millisUntilFinished)));
            timeRemaining.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, (millisUntilFinished)));
        }

        @Override
        public void onFinish() {
            timeDown.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            timeRemaining.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }
    }
}
