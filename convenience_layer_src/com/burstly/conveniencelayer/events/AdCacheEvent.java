package com.burstly.conveniencelayer.events;

import java.util.ArrayList;

/**
 * Event containing data on an ad that was cached
 */
public class AdCacheEvent extends AdEvent{
    /**
     * The network providing the creative that was cached
     */
    private final String mLoadedCreativeNetwork;

    /**
     * The list of networks which Burstly attempted to retrieve an ad from but did not return a valid
     */
    private final ArrayList<String> mFailedCreativeNetworks;

    /**
     * Constructs a new AdCacheEvent
     * @param loadedNetowrk The network providing the creative that was cached
     * @param failedCreativeNetworks The list of networks which Burstly attempted to retrieve but did not provide a valid ad
     */
    public AdCacheEvent(final String loadedNetowrk, final ArrayList<String> failedCreativeNetworks) {
        mLoadedCreativeNetwork = loadedNetowrk;
        mFailedCreativeNetworks = failedCreativeNetworks;
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
}
