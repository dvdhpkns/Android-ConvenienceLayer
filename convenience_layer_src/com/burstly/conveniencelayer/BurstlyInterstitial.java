package com.burstly.conveniencelayer;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;
import com.burstly.lib.ui.BurstlyView;

/**
 * BurstlyInterstitial should be used with zones marked as interstitial zones in the burstly.com UI and will be launched
 * in their own activity.  The {@link Activity} launched will depend on who the ad provider is and the type of ad. For
 * Burstly house and direct ads, as well as server side feeds the BurstlyFullscreenActivity.  For other providers
 * whose SDKs are integrated into the Burstly SDK there own Activities will be used.  See the documentation for the
 * necessary manifest entries
 */
public class BurstlyInterstitial extends BurstlyBaseAd implements ICacheable {
    /**
     * Does this placement take care of caching an ad in the background before it's needed
     */
    private boolean mAutoCached;

    /**
     * Tracks whether show has been triggered while an ad is still being precached
     */
    private boolean mShowTriggered;

    /**
     * Constructs a BurstlyInterstitial used for retrieving and triggering an interstitial shown in a new {@link Activity}
     * @param activity The {@link Activity} where the interstitials are requested from and will be launched from
     * @param zoneId The zoneId for these interstitials
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param autoCache Do you want the caching of ads automatically managed for you?
     */
    public BurstlyInterstitial(final Activity activity, final String zoneId, final String viewName, boolean autoCache) {
        super(activity);
        initNewBurstlyInterstitial(activity, zoneId, viewName);

        mAutoCached = autoCache;
    }

    /**
     * Constructs a BurstlyInterstitial used for retrieving and triggering an interstitial shown in a new {@link Fragment}
     * @param fragment The {@link Fragment} where the interstitials are requested from and will be launched from
     * @param zoneId The zoneId for these interstitials
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param autoCache Do you want the caching of ads automatically managed for you?
     */
    public BurstlyInterstitial(final Fragment fragment, final String zoneId, final String viewName, boolean autoCache) {
        super(fragment);
        initNewBurstlyInterstitial(fragment.getActivity(), zoneId, viewName);

        mAutoCached = autoCache;
    }

    /**
     * Constructs a BurstlyInterstitial used for retrieving and triggering an interstitial shown in a new {@link Activity}
     * @param activity The {@link Activity} where the interstitials are requested from and will be launched from
     * @param zoneId The zoneId for these interstitials
     * @param viewName The name of this view which will be used to identify it in teh logs
     */
    public void initNewBurstlyInterstitial(final Activity activity, final String zoneId, final String viewName) {
        final BurstlyView burstlyView = new BurstlyView(activity);
        burstlyView.setPublisherId( Burstly.getAppID() );
        burstlyView.setZoneId(zoneId);
        burstlyView.setBurstlyViewId(viewName);

        setBurstlyView(burstlyView, AdType.Interstitial);
    }

    /* REMOVED REQUIRES API LEVEL 12

    / **
     * Callback received if the associated BurstlyView is attached to a parent.  This is not allowed for interstitials
     * and will throw an exception.
     * /
    protected void burstlyViewAttachedToWindow() {
        throw new RuntimeException("Interstitial BurstlyView should not be attached to the window.");
    }

    / **
     * Callback received if the associated BurstlyView is detached from its parent.  This should never happen
     * /
    protected void burstlyViewDetachedFromWindow() {}
    */

    /**
     * Called by the convenience layer when the activity or fragment associated with this ad is paused.
     */
    @Override
    protected void resumed() {
        super.resumed();

        mShowTriggered = false;

        if(mAutoCached && !hasCachedAd())
            super.baseCacheAd();
    }

    /**
     * Shows an ad.  If an ad is already precached it will show immediately.  If an ad has not been
     * precached it will send a request for a new ad.  If an ad is precaching it will wait until it
     * completes before sending a request
     */
    @Override
    public void showAd() {
        if(mAutoCached) {
            if(mCachingState == CachingState.Retrieving) {
                mShowTriggered = true;
            }
            else if(mCachingState == CachingState.Retrieved) {
                super.showAd();
            }
            else {
                //If Idle while auto caching is enabled it's because the last request failed and it's waiting for a
                //retry treat as a failure.
                super.onFail(new AdFailEvent(mFailedCreativesList, getBurstlyView(), false));
            }
        }
        else {
            super.showAd();
        }
    }

    /**
     * caches an ad to be shown later
     */
    public void cacheAd() {
        if(mAutoCached)
            throw new RuntimeException("Automatic caching enabled for " + getName() + ". Do not attempt to manually cache an ad also");

        super.baseCacheAd();
    }

    /**
     * Gets whether there is a cached ad ready to be shown
     * @return true if a cached ad is available to be shown. False otherwise.
     */
    public boolean hasCachedAd() {
        return super.baseHasCachedAd();
    }

    /**
     * Gets whether an ad is being retrieved and cached currently
     * @return true if currently retrieving an ad to cache. False otherwise.
     */
    public boolean isCachingAd() {
        return super.baseIsCachingAd();
    }


    /**
     * Override onFail in order to catch failures due to request throttling
     * @param event {@link AdFailEvent} contains data related to the failure including the minimum amount of time
     *                                  until the next request can be made
     */
    @Override
    protected void onFail(final AdFailEvent event) {
        if(mAutoCached && !hasCachedAd()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    if(!hasCachedAd())
                        BurstlyInterstitial.super.baseCacheAd();
                }
            }, event.getMinTimeUntilNextRequest());
        }

        mShowTriggered = false;
        mCachingState = CachingState.Idle;

        //No callbacks for autocaching failures
        if( !(mAutoCached && event.wasFailureResultOfCachingAttempt()) ) {
            for(final IBurstlyListener listener:mListeners) {
                listener.onFail(this, event);
            }
        }
    }

    /**
     * Override onCache to show ad if it's been triggered already. Called when an ad is cached
     * @param event {@link com.burstly.conveniencelayer.events.AdCacheEvent} containing data on the cached ad
     */
    protected void onCache(final AdCacheEvent event) {
        super.onCache(event);

        if(mShowTriggered) {
            mShowTriggered = false;

            showAd();
        }
    }

    /**
     * An ad was loaded and will display
     * @param event {@link AdShowEvent} containing data on the ad shown
     */
    @Override
    protected void onShow(final AdShowEvent event) {
        if(!event.isActivityInterstitial())
            throw new RuntimeException("BurstlyInterstitial being used with a non interstitial zone");
        
        super.onShow(event);
    }
}
