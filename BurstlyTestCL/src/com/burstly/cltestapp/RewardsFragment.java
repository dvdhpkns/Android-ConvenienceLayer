/**
 * 
 */
package com.burstly.cltestapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.burstly.conveniencelayer.*;
import com.burstly.conveniencelayer.events.*;
import com.burstly.lib.currency.CurrencyManager;
import com.burstly.lib.feature.currency.ICurrencyListener;

import java.io.IOException;

/**
 *
 */
public class RewardsFragment extends BurstlyFragment implements ICurrencyListener, IBurstlyListener {

    private static Boolean PRE_CACHE_INTERSTITIALS = false;

    //private static String APP_ID = "0BxvZ-YgMUaghWAOZTQaTg";
    //private static String INTERSTITIAL_ZONE_ID = "0656921169102284126";
    private static String INTERSTITIAL_ZONE_ID = "0954195379157264033";
    private static String CURRENCY_LABEL_PREFIX = "Burstly Currency:";
    private static String TAG = "BurstlyCLRewards";

    private CurrencyManager mCurrencyManager;
    private TextView mCurrencyLabel;
    private BurstlyInterstitial mInterstitial;
    private Button mInterstitialButton;

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
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.rewards, container, false);

        // Get reference to currency label.
        mCurrencyLabel = (TextView)layout.findViewById(R.id.currencyLabel);
        // Set default currency text.
        mCurrencyLabel.setText(CURRENCY_LABEL_PREFIX+" 0");

        // Initialize currency manager.
        mCurrencyManager = Burstly.getCurrencyManager();
        // Must add self as a listener to receive ICurrencyListener callbacks.
        mCurrencyManager.addCurrencyListener(this);
        // Check for balance in onResume
        //checkForCurrencyUpdate();
        Burstly.setIntegrationNetwork(BurstlyIntegrationModeAdNetworks.REWARDS_SAMPLE);

        // Create Burstly interstitial.
        mInterstitial = new BurstlyInterstitial(this.getActivity(), INTERSTITIAL_ZONE_ID, "BurstlyInterstitial", false);
        // Add Listener to receive callbacks
        mInterstitial.addBurstlyListener(this);
        // Optionally pre-cache interstitial.
        if (PRE_CACHE_INTERSTITIALS) mInterstitial.cacheAd();

        // Get reference to button.
        mInterstitialButton = (Button)layout.findViewById(R.id.interstitialButton);
        // Handle button clicks.
        mInterstitialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Set button to disabled while ad is being retrieved.
                mInterstitialButton.setText(getString(R.string.retrieve));
                mInterstitialButton.setEnabled(false);
                // Show an interstitial.
                mInterstitial.showAd();
            }
        });


		return layout;
	}

    /**
     * Destroy Burstly stuff. Call this in
     */
    private void destroyBurstlyStuff(){
        // Remove self as listener.
        mCurrencyManager.removeCurrencyListener(this);
        mInterstitial.removeBurstlyListener(this);

        // Remove references.
        mCurrencyManager = null;
        mInterstitial = null;
        mInterstitialButton = null;
        mCurrencyLabel = null;
    }

    @Override
    public void onDestroyView()
    {
        destroyBurstlyStuff();
        super.onDestroy();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Set button enabled and change text to show ad
        mInterstitialButton.setText(getString(R.string.show));
        mInterstitialButton.setEnabled(true);

        checkForCurrencyUpdate();
    }

    // Private methods

    private void checkForCurrencyUpdate()
    {
        try
        {
            mCurrencyManager.checkForUpdate();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Exception thrown while checking for currency update: " + e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "Exception thrown while checking for currency update: " + e.getMessage());
        }
        catch (IllegalStateException e)
        {
            Log.e(TAG, "Exception thrown while checking for currency update: " + e.getMessage());
        }
    }

    // ICurrencyListener methods

    public void didUpdateBalance(com.burstly.lib.currency.event.BalanceUpdateEvent balanceUpdateEvent)
    {
        Log.d(TAG, "didUpdateBalance");

        // Received updated Burstly currency.
        // Update currency label.
        int oldBalance = balanceUpdateEvent.getOldBalance();
        final int newBalance = balanceUpdateEvent.getNewBalance();
        int gain = newBalance - oldBalance;
        if (gain > 0)
        {
            Log.i(TAG, "Gained currency: "+gain);
        }
        else if (gain < 0)
        {
            Log.i(TAG, "Spent currency: "+(-gain));
        }

        //this must be run on the UI thread
        mCurrencyLabel.post(new Runnable() {
            @Override
            public void run() {
                // update display with new balance
                mCurrencyLabel.setText(CURRENCY_LABEL_PREFIX+" " + newBalance);
            }
        });


    }

    public void didFailToUpdateBalance(com.burstly.lib.currency.event.BalanceUpdateEvent balanceUpdateEvent)
    {
        // Failed to retrieve Burstly currency.
        Log.d(TAG, "didFailToUpdateBalance");
        Log.e(TAG, "Failed to update Burstly currency balance.");
    }

    // IBurstlyListener methods

    public void onHide(final BurstlyBaseAd ad, final AdHideEvent event)
    {
        Log.d(TAG, "onHide");
        if (ad == mInterstitial)
        {
            // The interstitial is now hidden.
            // Should resume app activity.
            Log.d(TAG, "Interstitial hidden");
        }
    }

    /**
     * Called when an ad is shown, or on a refresh, when the creative changes
     * @param ad The ad which is showing a creative
     * @param event show event data
     */
    public void onShow(final BurstlyBaseAd ad, final AdShowEvent event)
    {
        Log.d(TAG, "onShow");
        if (ad == mInterstitial)
        {
            // The interstitial is now shown.
            // Should pasue app activity.
            Log.d(TAG, "Interstitial shown");
        }
    }

    /**
     * Called when an ad fails to load when an attempt to precache or display is made
     * @param ad The ad which failed to display a creative when an attempt to show or precache an ad was made
     * @param event fail event data
     */
    public void onFail(final BurstlyBaseAd ad, final AdFailEvent event)
    {
        Log.d(TAG, "onFail: Ad " + ad.getName() + " failed to load.");
        if (ad == mInterstitial)
        {
            // The interstitial failed to load.
            if(event.wasRequestThrottled())
                // Request throttled
                mInterstitialButton.setText(getString(R.string.throttled) + event.getMinTimeUntilNextRequest() + " ms");
            else
                // Request failed
                mInterstitialButton.setText(getString(R.string.failed));
            // Set button to enabled
            mInterstitialButton.setEnabled(true);

        }
    }

    /**
     * Called when a creative is cached
     * @param ad The ad which cached a creative
     * @param event cache event data
     */
    public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event)
    {
        if (ad == mInterstitial)
        {
            // The interstitial has cached.

        }
    }

    /**
     * Called when an ad is clicked on
     * @param ad The ad that was clicked
     * @param event click event data
     */
    public void onClick(final BurstlyBaseAd ad, final AdClickEvent event)
    {
        if (ad == mInterstitial)
        {
            // The interstitial has been clicked.

        }
    }

    /**
     * Called when a new Activity takes over the screen displaying an interstitial
     * @param ad The ad which took over the screen
     * @param event present fullscreen event data
     */
    public void onPresentFullscreen(final BurstlyBaseAd ad, final AdPresentFullscreenEvent event)
    {
        Log.d(TAG, "onPresentFullscreen");
        if (ad == mInterstitial)
        {
            // The interstitial has taken over full screen.
            // Should make sure app activity is paused.

        }
    }

    /**
     * Called when an Activity displaying an interstitial is dismissed
     * @param ad The ad which took over the screen
     * @param event dismiss fullscreen event data
     */
    public void onDismissFullscreen(final BurstlyBaseAd ad, final AdDismissFullscreenEvent event)
    {
        Log.d(TAG, "onDismissFullscreen");
        if (ad == mInterstitial)
        {
            // The interstitial has dismissed from full screen.
            // May want to resume app activity.

            // This is a good time to check for balance updates.
            checkForCurrencyUpdate();
        }
    }
}