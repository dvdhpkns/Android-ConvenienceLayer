package com.burstly.conveniencelayer.events;

/**
 * Event containing data on the ad that was dismissed
 */
public class AdDismissFullscreenEvent extends AdEvent {
    /**
     * The {@link AdShowEvent} event that caused an a fullscreen ad to be presented fullscreen, and then dismissed
     */
    private final AdShowEvent mMatchingShowEvent;

    /**
     * Was this fullscreen dismiss a result of an expandable being collapsed
     */
    private final boolean mIsCollapseEvent;

    /**
     * Construct a new AdDismissFullscreenEvent
     * @param showEvent the {@link AdShowEvent} that triggered the ad being presented fullscreen, and subsequently dismissed
     * @param isCollapseEvent is this event the result of an expandable collapsing
     */
    public AdDismissFullscreenEvent(final AdShowEvent showEvent, boolean isCollapseEvent) {
        mMatchingShowEvent = showEvent;
        mIsCollapseEvent = isCollapseEvent;
    }

    /**
     * Get the {@link AdShowEvent} that triggered the ad being presented fullscreen, and subsequently dismissed
     * @return The {@link AdShowEvent} that triggered the ad being presented fullscreen, and subsequently dismissed
     */
    public AdShowEvent getMatchingShowEvent() {
        return mMatchingShowEvent;
    }

    /**
     * Was this event triggered by an expandable ad collapsing
     * @return true for expandable collapse, false for an Activity being dismissed
     */
    public boolean isCollapseEvent() {
        return mIsCollapseEvent;
    }
}
