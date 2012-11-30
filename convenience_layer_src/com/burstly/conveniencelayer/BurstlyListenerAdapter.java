package com.burstly.conveniencelayer;

import com.burstly.conveniencelayer.events.*;

/**
 */
public class BurstlyListenerAdapter implements IBurstlyListener{
    /**
     * Called when an ad is removed or, on a refresh, before the ad is changed
     * @param ad The ad which is having a creative hidden
     * @param event hide event data
     */
    public void onHide(BurstlyBaseAd ad, AdHideEvent event) {}

    /**
     * Called when an ad is shown, or on a refresh, when the creative changes
     * @param ad The ad which is showing a creative
     * @param event show event data
     */
    public void onShow(BurstlyBaseAd ad, AdShowEvent event) {}

    /**
     * Called when an ad fails to load when an attempt to precache or display is made
     * @param ad The ad which failed to display a creative when an attempt to show or precache an ad was made
     * @param event fail event data
     */
    public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {}

    /**
     * Called when a creative is cached
     * @param ad The ad which cached a creative
     * @param event cache event data
     */
    public void onCache(final BurstlyBaseAd ad, final AdCacheEvent event) {}

    /**
     * Called when an ad is clicked on
     * @param ad The ad that was clicked
     * @param event click event data
     */
    public void onClick(final BurstlyBaseAd ad, final AdClickEvent event) {}

    /**
     * Called when a new Activity takes over the screen displaying an interstitial, or an expandable expands
     * @param ad The ad which took over the screen
     * @param event present fullscreen event data
     */
    public void onPresentFullscreen(final BurstlyBaseAd ad, final AdPresentFullscreenEvent event) {}

    /**
     * Called when an Activity displaying an interstitial is dismissed, or an expandable collapses
     * @param ad The ad which took over the screen
     * @param event dismiss fullscreen event data
     */
    public void onDismissFullscreen(final BurstlyBaseAd ad, final AdDismissFullscreenEvent event) {}
}
