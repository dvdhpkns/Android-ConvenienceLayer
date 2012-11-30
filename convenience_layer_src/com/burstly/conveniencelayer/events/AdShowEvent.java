package com.burstly.conveniencelayer.events;

import java.util.ArrayList;

/**
 * Event containing data on an ad that is being shown
 */
public class AdShowEvent extends AdEvent {
    /**
     * Flag telling whether this ad will be shown in a new {@link android.app.Activity}
     */
    private final boolean mIsActivityInterstitial;

    /**
     * The network providing the creative that was cached
     */
    private final String mLoadedCreativeNetwork;

    /**
     * The list of networks which Burstly attempted to retrieve an ad from but did not return a valid
     */
    private final ArrayList<String> mFailedCreativeNetworks;

    /**
     * Is this ad showing as the result of a refresh. true if it's a refreshed banner, false if it's the first time it is shown
     */
    private final boolean mIsRefresh;

    /**
     * Constructs a new AdShowEvent
     * @param isActivityInterstitial Flag telling whether this ad will be shown in a new {@link android.app.Activity}.  If
     *                               false it will only be visible if the view associated with this ad is attached to the layout.
     * @param loadedNetowrk The network providing the creative that was cached
     * @param failedCreativeNetworks The list of networks which Burstly attempted to retrieve but did not provide a valid ad
     * @param isRefresh Is this ad showing as the result of a refresh. true if it's a refreshed banner, false if it's the first time it is shown
     */
    public AdShowEvent(boolean isActivityInterstitial, final String loadedNetowrk, final ArrayList<String> failedCreativeNetworks, boolean isRefresh) {
        mIsActivityInterstitial = isActivityInterstitial;
        mLoadedCreativeNetwork = loadedNetowrk;
        mFailedCreativeNetworks = failedCreativeNetworks;
        mIsRefresh = isRefresh;
    }

    /**
     * Will the ad be shown in a new {@link android.app.Activity}
     * @return if false shown in current {@link android.app.Activity} and will only be visible if the view associated with
     * this ad is attached to the layout. if true the ad will be shown in a new {@link android.app.Activity}
     */
    public boolean isActivityInterstitial() {
        return mIsActivityInterstitial;
    }

    /**
     * Gets the network which provided the ad that was cached
     * @return The name of the network which provided the cached ad
     */
    public String getLoadedCreativeNetwork() {
        return mLoadedCreativeNetwork;
    }

    /**
     * Gets the list of networks which Burstly attempted to retrieve an ad from but did not provide a valid ad
     * @return The list of networks which Burstly attempted to retrieve an ad from but did not provide a valid ad
     */
    public ArrayList<String> getFailedCreativesNetworks() {
        return mFailedCreativeNetworks;
    }

    /**
     * Gets a flag saying if this AdShowEvent resulted from an ad being refreshed
     * @return true if this was the result of an ad refresh, false otherwise
     */
    public boolean isRefreshedAd() {
        return mIsRefresh;
    }
}
