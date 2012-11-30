package com.burstly.conveniencelayer;

import android.app.Activity;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.View;
import com.burstly.conveniencelayer.events.*;
import com.burstly.lib.ui.AdSize;
import com.burstly.lib.ui.BurstlyView;
import com.burstly.lib.ui.IBurstlyAdListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The BurstlyBaseAd is the base convenience layer wrapper class for all ads.
 */
public abstract class BurstlyBaseAd {
    /**
     * The general type of ad unit
     */
    protected enum AdType
    {
        Banner,
        Interstitial
    }

    /**
     * The state of having a ad cached
     */
    protected enum CachingState {
        Idle,
        //CacheRequestThrottled,
        Retrieving,
        Retrieved
    }

    /**
     * Throw an exception if the current thread is not the UI thread
     */
    protected static void throwIfNotOnMainThread() {
        if(Thread.currentThread() != Looper.getMainLooper().getThread())
            throw new RuntimeException("com.burstly.convenience.Banner must be called from the ui thread");
    }

    /**
     * The {@link BurstlyView} associated with this ad
     */
    private BurstlyView mBurstlyView;

    /**
     * The {@link Activity} this ad is associated with (null if this is attached to a fragment)
     */
    protected Activity mActivity;

    /**
     * The {@link Fragment} this ad is associated with (null if this is attached directly to an activity)
     */
    protected Fragment mFragment;

    /**
     * The {@link IBurstlyListener} which will receive callbacks when ad events occur
     */
    protected final List<IBurstlyListener> mListeners = new ArrayList<IBurstlyListener>();

    /**
     * The list of failed creatives that is built every time a request is made
     */
    protected ArrayList<String> mFailedCreativesList;

    /**
     * The current creative being shown.
     */
    protected String mCurrentCreative;

    /**
     * The state of the current ads cache
     */
    protected CachingState mCachingState = CachingState.Idle;

    /**
     * Event data for the last cached ad
     */
    protected AdCacheEvent mLastCache;

    /**
     * Event data for the last shown ad
     */
    protected AdShowEvent mLastShow;

    /**
     * The general type of ad (Banner or interstitial)
     */
    protected AdType mAdType;

    /**
     * Listener receiving callbacks from the {@link BurstlyView}
     */
    private IBurstlyAdListener mBurstlyAdListener = new IBurstlyAdListener() {
        /**
         * Called when a single network fails to load.
         * @param network {@link String} loaded network name
         */
        public void failedToLoad(String network) {
            singleCreativeFailed(network);
        }

        /**
         * An ad was loaded and will display
         * @param network {@link String} loaded network name
         * @param isInterstitial {@code boolean} defines whether the ad will be loaded in a new Activity
         */
        public void didLoad(final String network, boolean isInterstitial) {
            boolean isRefresh = (mLastShow != null);
            if(isRefresh)
                BurstlyBaseAd.this.onHide(new AdHideEvent(true, mLastShow));

            mLastShow = new AdShowEvent(isInterstitial, network, mFailedCreativesList, isRefresh);
            BurstlyBaseAd.this.onShow(mLastShow);
        }

        /**
         * An ad was cached
         * @param network {@link String} loaded network name
         */
        public void didPrecacheAd(String network) {
            mLastCache = new AdCacheEvent(network, mFailedCreativesList);
            onCache(mLastCache);
        }

        /**
         * Beginning a request to the server
         */
        public void startRequestToServer() {
            requestStarted();
        }

        /**
         * Attempting to load a creative that was returned by the server
         * @param network {@link String} network which we are trying to load
         */
        public void attemptingToLoad(String network) {
            tryToLoadCreative(network);
        }

        /**
         * Ad was clicked on
         * @param network {@link String} network which was clicked
         */
        public void adNetworkWasClicked(String network) {
            BurstlyBaseAd.this.onClick(new AdClickEvent(network));
        }

        /**
         * Request throttled
         * @param timeInMsec {@code int} minimum amount of time until a new request can be made
         */
        public void requestThrottled(int timeInMsec) {
            boolean precaching = (mCachingState == CachingState.Retrieving);
            BurstlyBaseAd.this.onFail(new AdFailEvent(timeInMsec, precaching));
        }

        /**
         * Failed to load any of the creatives in the assigned zones
         */
        public void failedToDisplayAds() {
            boolean precaching = (mCachingState == CachingState.Retrieving);
            BurstlyBaseAd.this.onFail(new AdFailEvent(mFailedCreativesList, mBurstlyView, precaching));
        }

        /**
         * A fullscreen activity interstitial was dismissed
         * @param network {@link String} name of network which was dismissed
         */
        public void adNetworkDismissFullScreen(String network) {
            BurstlyBaseAd.this.onDismissFullscreen(new AdDismissFullscreenEvent(mLastShow, false));
            mLastShow = null;
        }

        /**
         * A fullscreen activity interstitial was dismissed
         * @param network {@link String} name of network which was dismissed
         */
        public void adNetworkPresentFullScreen(String network) {
            BurstlyBaseAd.this.onPresentFullscreen(new AdPresentFullscreenEvent(false));
        }

        /**
         * Expandable ad expanded
         * @param isFullscreen if true means new container size consumes full screen
         */
        public void onExpand(boolean isFullscreen) {
            BurstlyBaseAd.this.onPresentFullscreen(new AdPresentFullscreenEvent(true));
        }

        /**
         * Expandable ad was collapsed
         */
        public void onCollapse() {
            BurstlyBaseAd.this.onDismissFullscreen(new AdDismissFullscreenEvent(mLastShow, true));
        }

        /**
         * Ignored events
         */
        public void finishRequestToServer() {}
        public void viewDidChangeSize(AdSize newSize, AdSize oldSize) {}
        public void onHide() {}
        public void onShow() {}
    };

    /* REMOVED BECAUSE IT REQUIRES API LEVEL 12

    /**
     * abstract method must be overridder by subclasses to monitor ViewGroup attachment state
     * /
    protected abstract void burstlyViewAttachedToWindow();

    /**
     * abstract method must be overridder by subclasses to monitor ViewGroup attachment state
     * /
    protected abstract void burstlyViewDetachedFromWindow();

    /**
     * private listener monitors whether the {@link BurstlyView} has been attached to a ViewGroup
     * /
    private View.OnAttachStateChangeListener mAttachListener = new View.OnAttachStateChangeListener() {
        public void onViewAttachedToWindow(View view) {
            burstlyViewAttachedToWindow();
        }

        public void onViewDetachedFromWindow(View view) {
            burstlyViewDetachedFromWindow();
        }
    };*/

    //private listener receives callbacks when an activity state changes
    private IActivityListener mActivityListener = new IActivityListener() {
        /**
         * Called by the convenience layer when the activity associated with this ad is paused.
         */
        public void activityPaused(final Activity activity) {
            paused();
        }

        /**
         * Called by the convenience layer when the activity associated with this ad is resumed.
         */
        public void activityResumed(final Activity activity) {
            resumed();
        }

        /**
         * Called by the convenience layer when the activity associated with this ad is destroyed.
         */
        public void activityDestroyed(final Activity activity) {
            destroyed();
        }
    };

    //private listener receives calls when a fragment state changes
    private IFragmentListener mFragmentListener = new IFragmentListener() {
        /**
         * Called by the convenience layer when the fragment associated with this ad is paused.
         */
        public void fragmentPaused(final Fragment fragment) {
            paused();
        }

        /**
         * Called by the convenience layer when the fragment associated with this ad is resumed.
         */
        public void fragmentResumed(final Fragment fragment) {
            resumed();
        }

        /**
         * Called by the convenience layer when the fragment associated with this ad is destroyed.
         */
        public void fragmentDestroyed(final Fragment fragment) {
            destroyed();
        }
    };

    /**
     * Called by the convenience layer when the fragment or activity associated with this ad is resumed.
     */
    protected void resumed() {
        throwIfNotOnMainThread();

        mBurstlyView.onShowActivity();
    }

    /**
     * Called by the convenience layer when the fragment or activity associated with this ad is paused.
     */
    protected void paused() {
        throwIfNotOnMainThread();

        mBurstlyView.onHideActivity();

        if(mCachingState == CachingState.Retrieving /*|| mCachingState == CachingState.CacheRequestThrottled*/)
            mCachingState = CachingState.Idle;
    }

    /**
     * Called by the convenience layer when the fragment or activity associated with this ad is destroyed.
     */
    protected void destroyed() {
        throwIfNotOnMainThread();

        mBurstlyView.destroy();
    }

    /**
     * protected constructor for abstract class
     * @param activity {@link Activity} associated with this ad
     */
    protected BurstlyBaseAd(final Activity activity) {
        mActivity = activity;
    }

    /**
     * protected constructor for abstract class
     * @param fragment {@link Fragment} associated with the ad
     */
    protected BurstlyBaseAd(final Fragment fragment) {
        mFragment = fragment;
    }

    /**
     * sets the {@link BurstlyView} used by this ad if in integration mode will override appId and zone Id
     * @param burstlyView The {@link BurstlyView} instance used to access the BurstlySDK and show content
     */
    protected void setBurstlyView(final BurstlyView burstlyView, AdType type) {
        throwIfNotOnMainThread();

        if(mBurstlyView != null)
            throw new RuntimeException("BurstlyView cannot be changed.");

        mBurstlyView = burstlyView;
        mBurstlyView.setPublisherId(Burstly.getAppID());
        //mBurstlyView.addOnAttachStateChangeListener(mAttachListener); //Requires Android API level 12
        mBurstlyView.setBurstlyAdListener(mBurstlyAdListener);

        BurstlyIntegrationModeAdNetworks network = Burstly.getIntegrationNetwork();

        if(Burstly.isIntegrationModeEnabledForThisDevice()){
            Burstly.logD("Device is Test Device.");

            if(network != BurstlyIntegrationModeAdNetworks.DISABLED) {
                Burstly.logD("Ad " + this.getName() + " will display sample ads from specified ad network.");

                mBurstlyView.setZoneId(type == AdType.Banner ? network.getBannerZone() : network.getInterstitialZone());
                mBurstlyView.setPublisherId(BurstlyIntegrationModeAdNetworks.getAppId());
            }

            if(mActivity != null)
                Burstly.showTestModeAlert(mActivity);
            else if(mFragment != null)
                Burstly.showTestModeAlert(mFragment.getActivity());

        } else {
            Burstly.logD("Device is not a test device. Using default pub and zone.");
        }

        if(mActivity != null)
            Burstly.addActivityListener(mActivity, mActivityListener);
        else
            Burstly.addFragmentListener(mFragment, mFragmentListener);
    }

    /**
     * retrieve the {@link BurstlyView} used by this ad     *
     * @return The {@link BurstlyView} used by this ad
     */
    protected BurstlyView getBurstlyView() {
        return mBurstlyView;
    }

    /**
     * Called when an ad is hidden which can occur by itself or just before a show event in the case of a refresh
     * @param event {@link AdHideEvent} containing data on the ad that was hidden
     */
    protected void onHide(final AdHideEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onHide(this, event);
        }

        mLastShow = null;
    }

    /**
     * Called when an ad will be shown
     * @param event {@link AdShowEvent} containing data on the ad shown
     */
    protected void onShow(final AdShowEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onShow(this, event);
        }
    }

    /**
     * Called when an ad is cached
     * @param event {@link AdCacheEvent} containing data on the cached ad
     */
    protected void onCache(final AdCacheEvent event) {
        mCachingState =  CachingState.Retrieved;

        for(final IBurstlyListener listener:mListeners) {
            listener.onCache(this, event);
        }
    }

    /**
     * Called when an ad is clicked
     * @param event {@link AdClickEvent} containing data on the ad that was clicked
     */
    protected void onClick(final AdClickEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onClick(this, event);
        }
    }

    /**
     * Called when a request for an ad fails to fill
     * @param event {@link AdFailEvent} containing data on the failure
     */
    protected void onFail(final AdFailEvent event) {
        mCachingState = CachingState.Idle;

        for(final IBurstlyListener listener:mListeners) {
            listener.onFail(this, event);
        }
    }

    /**
     * Called when an interstitial, which was launched in it's own activity, was dismissed
     * @param event {@link AdDismissFullscreenEvent} containing info on the dismissed ad
     */
    protected void onDismissFullscreen(final AdDismissFullscreenEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onDismissFullscreen(this, event);
        }
    }

    /**
     * Called when a new {@link Activity} will launch to display an interstitial
     * @param event {@link AdPresentFullscreenEvent} containing info on the ad which will display
     */
    protected void onPresentFullscreen(final AdPresentFullscreenEvent event) {
        for(final IBurstlyListener listener:mListeners) {
            listener.onPresentFullscreen(this, event);
        }
    }

    /**
     * Base functionality for a new request clears the failed creatives list
     */
    protected void requestStarted() {
        mFailedCreativesList = new ArrayList<String>();
    }

    /**
     * Base functionality for a failure to load a creative
     * @param network The network which failed to load
     */
    protected void singleCreativeFailed(final String network) {
        mFailedCreativesList.add(network);
    }

    /**
     * Base functionality for an attempt to load a creative
     * @param network The network being attempted
     */
    protected void tryToLoadCreative(final String network) {
        mCurrentCreative = network;
    }

    /**
     * show an ad
     */
    public void showAd() {
        throwIfNotOnMainThread();

        mCachingState = CachingState.Idle;
        mBurstlyView.sendRequestForAd();
    }

    /**
     * caches an ad to be shown later 
     */
    protected void baseCacheAd() {
        Burstly.logW("cacheAd start");
        if(baseHasCachedAd()) {
            Burstly.logW(getName() + ": Ad already cached.");
            onCache(mLastCache);
        }
        else {
            throwIfNotOnMainThread();
            mCachingState = CachingState.Retrieving;
            mBurstlyView.precacheAd();
        }
        Burstly.logW("cacheAd end");
    }

    /**
     * Gets whether there is a cached ad ready to be shown
     * @return true if a cached ad is available to be shown. False otherwise.
     */
    protected boolean baseHasCachedAd() {
        return (mCachingState == CachingState.Retrieved && !mBurstlyView.isCachedAdExpired());
    }

    /**
     * Gets whether an ad is being retrieved and cached currently
     * @return true if currently retrieving an ad to cache. False otherwise.
     */
    protected boolean baseIsCachingAd() {
        return (mCachingState == CachingState.Retrieving);
    }

    /**
     * Add a {@link IBurstlyListener} which will receive callbacks when ad events occur
     * @param listener A {@link IBurstlyListener} which will receive callbacks when ad events occur
     */
    public synchronized void addBurstlyListener(final IBurstlyListener listener) {
        mListeners.add(listener);
    }

    /**
     * Removes a {@link IBurstlyListener} from the list of liseteners
     * @param listener {@link IBurstlyListener} to be removed 
     */
    public synchronized void removeBurstlyListener(final IBurstlyListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Get the name associated with this ad.  This is the ad which is used to identify this ad in the logs.
     * @return The name of this ad space
     */
    public String getName() {
        return mBurstlyView.getBurstlyViewId();
    }

    /**
     * Get the App Id used by this ad
     * @return The App Id used in requests by this ad
     */
    public String getAppId() {
        return mBurstlyView.getPublisherId();
    }

    /**
     * Get the Zone Id used by this ad
     * @return The Zone Id used in requests by this ad
     */
    public String getZoneId() {
        return mBurstlyView.getZoneId();
    }

    /**
     * Set a comma delimited list of key value pairs in the format key=value (i.e. key1=value1,key2=value2,...,keyN=valueN)
     * These parameters can be used to setup custom targeting in the Burstly.com UI
     * @param targetingParams String containing comma delimited list of custom targeting key value pairs
     */
    public void setTargetingParameters(final String targetingParams) {
        mBurstlyView.setPubTargetingParams(targetingParams);
    }

    /**
     * Get the comma delimited list of custom targeting key value pairs.
     * @return String containing comma delimited list of custom targeting key value pairs
     */
    public String getTargetingParameters() {
        return mBurstlyView.getPubTargetingParams();
    }

    /**
     * A comma delimited list of key value pairs which are passed as parameters into ads.
     * @param adParameters String containing comma delimited list of key value pairs which are passed as parameters into ads.
     */
    public void setAdParameters(final String adParameters) {
        mBurstlyView.setCrParms(adParameters);
    }

    /**
     * Get the comma delimited list of key value pairs which are being passed as parameters into ads.
     * @return String containing comma delimited list of key value pairs which are passed as parameters into ads
     */
    public String getAdParameters() {
        return mBurstlyView.getCrParms();
    }
}
