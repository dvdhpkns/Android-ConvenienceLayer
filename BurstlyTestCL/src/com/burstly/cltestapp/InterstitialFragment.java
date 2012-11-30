/**
 * 
 */
package com.burstly.cltestapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.burstly.conveniencelayer.*;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;

/**
 * @author mwho
 *
 */
public class InterstitialFragment extends BurstlyFragment {
    private static final String RETRIEVING = "Retrieving Ad";
    private static final String SHOW = "Show Ad";
    private static final String THROTTLED = "Throttled. Retry in ";
    private static final String FAILED = "Request Failed";

    private BurstlyInterstitial mInterstitial;
    private Button mButton;
    private static final String TAG = "BurstlyInterstitialFragment";

    /* (non-Javadoc)
      * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
      */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
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
                View interView = getInterstitialView(ad);

                //add view to parent
                parentLayout.addView(interView);
            }
        }

		return layout;
	}

    private View getInterstitialView(BurstlyIntegrationModeAdNetworks ad) {
        String adName = ad.getAdName();

        //inflate view from layout file
        View interView;
        LayoutInflater vi = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        interView = vi.inflate(R.layout.interstitial_inflater, null);

        //set adName in TextView
        TextView adNameTV = (TextView) interView.findViewById(R.id.adName);
        adNameTV.setText(adName);
        //get reference to status populate with loading text
        final TextView statusTV = (TextView) interView.findViewById(R.id.adStatus);
        statusTV.setText(getString(R.string.idle));

        //set image for scrollview based on ad network
        ImageView imageView = (ImageView) interView.findViewById(R.id.adNetworkImage);
        switch (ad){
            case HOUSE: imageView.setImageResource(R.drawable.burstly);
                break;
            case MILLENIAL: imageView.setImageResource(R.drawable.millennial);
                break;
            case ADMOB: imageView.setImageResource(R.drawable.admob);
                break;
            case GREYSTRIPE: imageView.setImageResource(R.drawable.greystripe);
                break;
            case INMOBI: imageView.setImageResource(R.drawable.inmobi);
                break;
            default: Log.w(TAG, "Missing case for ad " + ad.getAdName());
        }

        Burstly.setIntegrationNetwork(ad);
        //create interstitial
        final BurstlyInterstitial interstitial = new BurstlyInterstitial( this.getActivity(), "0000000000000000000", adName + " Interstitial", false);
        //add listener
        interstitial.addBurstlyListener(getBurstlyListener(statusTV));

        //Add on click listener to show ad
        Button launchBtn = (Button) interView.findViewById(R.id.launchButton);
        launchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusTV.setText(getString(R.string.loading));
                interstitial.showAd();
            }
        });
        //add onlick listener to precache ad
        Button precacheBtn = (Button) interView.findViewById(R.id.precacheButton);
        precacheBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusTV.setText(getString(R.string.loading));
                interstitial.cacheAd();
            }
        });

        return interView;
    }

    private BurstlyListenerAdapter getBurstlyListener(final TextView status) {
        BurstlyListenerAdapter listener = new BurstlyListenerAdapter() {
            @Override
            public void onShow(final BurstlyBaseAd ad, final AdShowEvent event) {
                status.setText(getString(R.string.idle));
                Log.d(TAG, "Ad has been shown: " + ad.getName());
            }

            @Override
            public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event) {
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

}
