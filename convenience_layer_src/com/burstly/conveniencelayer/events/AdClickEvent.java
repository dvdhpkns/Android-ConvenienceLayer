package com.burstly.conveniencelayer.events;

/**
 * Event containing data on an ad that was clicked
 */
public class AdClickEvent extends AdEvent {
    /**
     * The name of the ad network which provided the ad that was clicked
     */
    private final String mClickedNetwork;

    /**
     * Contsruct a new AdClickEvent
     * @param clickedNetwork The name of the network which provided the ad that was clicked
     */
    public AdClickEvent(final String clickedNetwork) {
        mClickedNetwork = clickedNetwork;
    }

    /**
     * Gets the name of the network which provided the ad that was clicked
     * @return The name of the network which provided the ad that was clicked
     */
    public String getClickedNetwork() {
        return mClickedNetwork;
    }
}
