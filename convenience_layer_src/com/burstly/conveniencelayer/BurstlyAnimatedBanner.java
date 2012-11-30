package com.burstly.conveniencelayer;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import com.burstly.conveniencelayer.events.AdCacheEvent;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.conveniencelayer.events.AdHideEvent;
import com.burstly.conveniencelayer.events.AdShowEvent;
import com.burstly.lib.ui.BurstlyView;

/**
 * AnimatedBanner is used to handle the use case of a banner which is not always visible.  This type of banner can be
 * hidden and shown.  When hideAd and showAd are called they will use the animations provided in setAnims.  If no anims
 * are ever set then the banner will pop on and off when hidden and shown.
 */
public class BurstlyAnimatedBanner extends BurstlyBaseAd implements ICacheable {
    /**
     * The state of the ad
     */
    public enum State {
        Offscreen,
        ShowTriggered,
        IntroAnim,
        OnScreen,
        OutroAnim
    }

    /**
     * Listener for animation events
     */
    public interface IAnimationListener {
        /**
         * Called when the AnimatedBanner which was shown finishes it's intro animation
         * @param banner The {@link BurstlyAnimatedBanner} which was shown
         */
        void onIntroAnimEnd(final BurstlyAnimatedBanner banner);

        /**
         * Called when the AnimatedBanner which was hidden finishes it's outro animation
         * @param banner The {@link BurstlyAnimatedBanner} which was hidden
         */
        void onOutroAnimEnd(final BurstlyAnimatedBanner banner);
    }

    /**
     * AnimationListener used to listen for intro and outro animations to end
     */
    protected class BannerAnimationListener implements Animation.AnimationListener {
        public void onAnimationStart(final Animation animation) {}
        public void onAnimationRepeat(final Animation animation) {}

        /**
         * callback received when animation ends
         * @param animation The {@link Animation} which ended
         */
        public void onAnimationEnd(final Animation animation)  {
            if(animation == mInAnim) {
                if(mState != State.IntroAnim)
                    Burstly.logW("Intro anim finished but no longer in intro anim state");

                mState = State.OnScreen;

                if(mAnimationCallbacks != null)
                    mAnimationCallbacks.onIntroAnimEnd(BurstlyAnimatedBanner.this);
            }
            else {
                if(mState != State.OutroAnim)
                    Burstly.logW("Outro anim finished but no longer in outro anim state");

                mState = State.Offscreen;
                getBurstlyView().setVisibility(View.GONE);

                if(mAnimationCallbacks != null)
                    mAnimationCallbacks.onOutroAnimEnd(BurstlyAnimatedBanner.this);

                BurstlyAnimatedBanner.this.onHide(new AdHideEvent(false, mLastShow));
            }
        }
    }

    /**
     * Instance of the AnimationListener which will be set on each anim
     */
    private BannerAnimationListener mAnimationListener = new BannerAnimationListener();

    /**
     * The state of the current animated banner
     */
    private State mState;

    /**
     * The animation used to transition the banner onto the screen
     */
    private Animation mInAnim;

    /**
     * The animation used to transition the banner off of the screen
     */
    private Animation mOutAnim;

    /**
     * The listener that receives animation complete callbacks
     */
    private IAnimationListener mAnimationCallbacks;

    /**
     * The refresh rate set on the banner initially
     */
    private int mRefreshRate;

    /**
     * When an ad is throttled, the minimum time until the next request can be made
     */
    private int mThrottleTime;

    /**
     * Does this placement take care of caching an ad in the background before it's needed
     */
    private boolean mAutoCached;

    /**
     * Constructor for an AnimatedBanner built from a {@link BurstlyView} that has been attached to a layout file
     * @param activity The {@link Activity} associated with the banner
     * @param id The id for the BurstlyView in the layout
     * @param autoCache Do you want the caching of ads automatically managed for you?
     */
    public BurstlyAnimatedBanner(final Activity activity, int id, boolean autoCache) {
        super(activity);

        final BurstlyView burstlyView = (BurstlyView)activity.findViewById(id);

        if(burstlyView == null)
            throw new RuntimeException("Invalid view Id.  A view with this Id could not be found with the activity's current layout");

        setBurstlyView(burstlyView, AdType.Banner);
        init();

        mAutoCached = autoCache;
    }

    /**
     * Constructor for an AnimatedBanner built from a {@link BurstlyView} that has been attached to a layout file
     * @param fragment The {@link Fragment} associated with the banner
     * @param id The id for the BurstlyView in the layout
     * @param autoCache Do you want the caching of ads automatically managed for you?
     */
    public BurstlyAnimatedBanner(final Fragment fragment, int id, boolean autoCache) {
        super(fragment);

        View rootView = fragment.getView();

        if(rootView == null)
            throw new RuntimeException("fragment.getView returned null. Has this fragment inflated a layout?");

        final BurstlyView burstlyView = (BurstlyView)rootView.findViewById(id);

        if(burstlyView == null)
            throw new RuntimeException("Invalid view Id.  A view with this Id could not be found in the current layout");

        setBurstlyView(burstlyView, AdType.Banner);
        init();

        mAutoCached = autoCache;
    }

    /**
     * Constructs an AnimatedBanner in code and attaches it to a ViewGroup
     * @param activity The {@link Activity} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param refreshRate While the ad is visible the frequency at which it updates.  0 to manually update
     * @param autoCache Do you want the caching of ads automatically managed for you?
     */
    public BurstlyAnimatedBanner(final Activity activity, final ViewGroup group, final ViewGroup.LayoutParams params, final String zoneId, final String viewName, int refreshRate, boolean autoCache) {
        super(activity);
        initNewAnimatedBanner(activity, group, params, zoneId, viewName);
        mRefreshRate = refreshRate;

        mAutoCached = autoCache;
    }

    /**
     * Constructs an AnimatedBanner in code and attaches it to a ViewGroup
     * @param fragment The {@link Fragment} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param refreshRate While the ad is visible the frequency at which it updates.  0 to manually update
     * @param autoCache Do you want the caching of ads automatically managed for you?
     */
    public BurstlyAnimatedBanner(final Fragment fragment, final ViewGroup group, final ViewGroup.LayoutParams params, final String zoneId, final String viewName, int refreshRate, boolean autoCache) {
        super(fragment);
        initNewAnimatedBanner(fragment.getActivity(), group, params, zoneId, viewName);
        mRefreshRate = refreshRate;

        mAutoCached = autoCache;
    }

    /**
     * Initializes an AnimatedBanner in code and attaches it to a ViewGroup
     * @param activity The {@link Activity} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     */
    private void initNewAnimatedBanner(final Activity activity, final ViewGroup group, final ViewGroup.LayoutParams params, final String zoneId, final String viewName) {
        final BurstlyView burstlyView  = new BurstlyView(activity);
        burstlyView.setPublisherId(Burstly.getAppID());
        burstlyView.setZoneId(zoneId);
        burstlyView.setBurstlyViewId(viewName);

        setBurstlyView(burstlyView, AdType.Banner);
        init();

        if(params != null)
            group.addView(burstlyView, params);
        else
            group.addView(burstlyView);
    }

    /**
     * Initialize internal variables
     */
    private void init() {
        mState = State.Offscreen;
        mThrottleTime = -1;
        mRefreshRate = getBurstlyView().getDefaultSessionLife();
    }

    /**
     * Ad caching callback
     * @param event {@link AdCacheEvent} containing info on the successfully cached ad
     */
    @Override
    protected void onCache(final AdCacheEvent event) {
        super.onCache(event);

        if(mState == State.ShowTriggered) {
            super.showAd();
        }
        else if(mState != State.Offscreen) {
            Burstly.logE("Unexpected state. Banner in state " + mState.toString() + " when precache finished.");
        }
    }

    /**
     * An ad was loaded and will display
     * @param event {@link AdShowEvent} containing info on the successfully shown ad
     */
    @Override
    protected void onShow(final AdShowEvent event) {
        if(event.isActivityInterstitial())
            throw new RuntimeException("Trying tor run an interstitial zone Id using BurstlyBanner");

        if(mState == State.ShowTriggered) {
            super.onShow(event);

            getBurstlyView().setVisibility(View.VISIBLE);

            if(mInAnim != null) {
                mState = State.IntroAnim;
                getBurstlyView().startAnimation(mInAnim);
            }
            else {
                mState = State.OnScreen;

                if(mAnimationCallbacks != null)
                    mAnimationCallbacks.onIntroAnimEnd(this);
            }
        }
        else if(mState == State.OnScreen) { //this is a refresh
            super.onShow(event);
        }
        else {
            Burstly.logW("Received onLoad callback when banner was in state " + mState.toString());
        }
    }

    @Override
    protected void onHide(final AdHideEvent event) {
        super.onHide(event);

        if(mAutoCached) {
            if(!event.isARefresh() && !event.getMatchingShowEvent().isActivityInterstitial()) {
                super.baseCacheAd();
            }
        }
    }

    /**
     * Override onFail in order to catch failures due to request throttling
     * @param event {@link AdFailEvent} contains data related to the failure including the minimum amount of time
     *                                  until the next request can be made
     */
    @Override
    protected void onFail(final AdFailEvent event) {
        if(event.wasRequestThrottled())
            mThrottleTime = event.getMinTimeUntilNextRequest();
        else
            mThrottleTime = 0;

        if(mAutoCached && !hasCachedAd() && !isVisible()) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                public void run() {
                    if(!hasCachedAd())
                        BurstlyAnimatedBanner.super.baseCacheAd();
                }
            }, mThrottleTime);
        }

        if(mState != State.OnScreen)
            mState = State.Offscreen;

        mCachingState = CachingState.Idle;

        //No callbacks for autocaching failures
        if( !(mAutoCached && event.wasFailureResultOfCachingAttempt()) ) {
            for(final IBurstlyListener listener:mListeners) {
                listener.onFail(this, event);
            }
        }
    }

    /**
     * Set the listener that will receive callbacks when the intro and outro animations end
     * @param listener The {@link IAnimationListener} that will receive callbacks
     */
    public void setAnimatedBannerListener(final IAnimationListener listener) {
        mAnimationCallbacks = listener;
    }

    /**
     * Called by the convenience layer when the activity or fragment associated with this ad is paused.
     */
    @Override
    protected void resumed() {
        if(!isVisible())
            getBurstlyView().resetDefaultSessionLife();

        super.resumed();

        if(mAutoCached && !hasCachedAd())
            super.baseCacheAd();
    }

    /* REMOVED.  REQUIRES API LEVEL 12

    /**
     * {@link BurstlyView} attached to a parent
     * /
    protected void burstlyViewAttachedToWindow() {
        Burstly.logI(getName() + " attached to parent.");
    }

    /**
     * {@link BurstlyView} removed from it's parent
     * /
    protected void burstlyViewDetachedFromWindow() {
        Burstly.logE(getName() + " is being removed from it's parent. Use an AnimatedBanner if you wish to be able to hide and show an ad");
    }
    */

    /**
     * Set the animations used to transition the banner on and off screen.  If null is used the banner will pop on 
     * and off without an animation.
     * @param inAnim {@link Animation} triggered by showAd
     * @param outAnim {@link Animation} triggered by hideAd
     */
    public void setAnims(final Animation inAnim, final Animation outAnim) {
        if(mState == State.IntroAnim || mState == State.OutroAnim)
            throw new RuntimeException("Attempting to change anims while currently animating");

        if(mInAnim != null)
            mInAnim.setAnimationListener(null);

        if(mOutAnim != null)
            mOutAnim.setAnimationListener(null);

        mInAnim = inAnim;
        mOutAnim = outAnim;

        if(mInAnim != null)
            mInAnim.setAnimationListener(mAnimationListener);

        if(mOutAnim != null)
            mOutAnim.setAnimationListener(mAnimationListener);
    }

    /**
     * Shows an ad.  If an ad is already precached it will begin the intro animation immediately.  If an ad has not been
     * precached it will wait for the ad to finish loading and then begin the intro animation.
     */
    @Override
    public void showAd() {
        throwIfNotOnMainThread();

        if(mState.ordinal() >= State.IntroAnim.ordinal()) {
            super.showAd();
        }
        else if(mState == State.ShowTriggered) {
            Burstly.logW("Trying to show an ad when show has already been called");
        }
        else {
            if(mCachingState != CachingState.Idle) {
                Burstly.logW("calling");
                boolean cached = hasCachedAd();
                Burstly.logW("completed");
                if(cached) {
                    mState = State.ShowTriggered;

                    if(mRefreshRate > 0)
                        getBurstlyView().setDefaultSessionLife(mRefreshRate);

                    super.showAd();
                }
                else if(mCachingState == CachingState.Retrieving /*|| mCachingState == CachingState.CacheRequestThrottled*/) {
                    mState = State.ShowTriggered;
                    Burstly.logW("Attempting to show banner before it finished precaching");
                }
                else {
                    Burstly.logE("Attempting to show precached banner while in idle state");
                }
            }
            else {
                mThrottleTime = 0;

                if(mRefreshRate > 0)
                    getBurstlyView().setDefaultSessionLife(mRefreshRate);

                super.showAd();

                if(mThrottleTime == 0)
                    mState = State.ShowTriggered;
            }
        }
    }

    /**
     * Returns true if the banner is animating onto, displaying on, or animating off of the screen
     * @return whether the ad is visible or not
     */
    public boolean isVisible() {
        return (mState.ordinal() >= State.IntroAnim.ordinal());
    }

    /**
     * Get the state of the animated banner
     * @return The {@link BurstlyAnimatedBanner.State} of the animated banner
     */
    public State getState() {
        return mState;
    }

    /**
     * Hides the animated banner ad
     */
    public void hideAd() {
        throwIfNotOnMainThread();

        if(mState.ordinal() < State.ShowTriggered.ordinal()) {
            Burstly.logW("Calling hide without calling show.");
        }
        else if(mState == State.ShowTriggered) {
            Burstly.logE("Hiding an ad immediately after trying to show it before it made it to the screen.  Impression will be tracked but not shown.");
            mState = State.Offscreen;
        }
        else if(mState == State.OutroAnim) {
            Burstly.logW("Calling hide multiple times");
        }
        else {
            if(mState == State.IntroAnim)
                Burstly.logW("Calling hide before intro transition was finished");

            getBurstlyView().resetDefaultSessionLife();

            if(mOutAnim == null) {
                mState = State.Offscreen;
                getBurstlyView().setVisibility(View.GONE);

                if(mAnimationCallbacks != null)
                    mAnimationCallbacks.onOutroAnimEnd(this);

                onHide(new AdHideEvent(false, mLastShow));
            }
            else {
                mState = State.OutroAnim;
                getBurstlyView().startAnimation(mOutAnim);
            }
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
}
