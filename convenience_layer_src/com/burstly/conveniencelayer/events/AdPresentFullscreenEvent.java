package com.burstly.conveniencelayer.events;

/**
 * Event containing details of an ad Activity being presented using the fullscreen
 */
public class AdPresentFullscreenEvent extends AdEvent {
    /**
     * Was this fullscreen take over the result of an expandable unit being expanded?
     */
    private boolean mIsExpandEvent;

    /**
     * Construct a new AdPresentFullscreenEvent
     * @param isExpandEvent is this event the result of an expandable expanding
     */
    public AdPresentFullscreenEvent(boolean isExpandEvent) {
        mIsExpandEvent = isExpandEvent;
    }

    /**
     * Was this event triggered by an expandable expanding
     * @return true for expandable expand, false for a new Activity being shown
     */
    public boolean isExpandEvent() {
        return mIsExpandEvent;
    }
}
