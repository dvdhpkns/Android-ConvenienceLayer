package com.burstly.conveniencelayer.events;

/**
 * Event containing details on an ad being hidden
 */
public class AdHideEvent extends AdEvent {
    /**
     * Flag telling if this is a refresh operation and will be followed immediately by a show event
     */
    private boolean mIsARefresh;

    /**
     * The {@link AdShowEvent} which showed the ad which is being hidden now
     */
    private final AdShowEvent mMatchingShowEvent;

    /**
     * Cunstruct a new AdHideEvent
     * @param isARefresh Flag telling if this is a refresh operation and will be followed immediately by a show event
     * @param showEvent the {@link AdShowEvent} which showed the ad which is being hidden now
     */
    public AdHideEvent(boolean isARefresh, final AdShowEvent showEvent) {
        mIsARefresh = isARefresh;
        mMatchingShowEvent = showEvent;
    }

    /**
     *
     * @return the {@link AdShowEvent} which showed the ad which is being hidden now
     */
    public AdShowEvent getMatchingShowEvent() {
        return mMatchingShowEvent;
    }

    /**
     * Is this a refresh operation and will be followed immediately by a show event, or is the view being hidden
     * @return true if this is a refresh operation, false if the view is being hidden and a new ad will not show until triggered
     */
    public boolean isARefresh() {
        return mIsARefresh;
    }
}
