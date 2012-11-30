package com.burstly.conveniencelayer;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import com.burstly.lib.ui.BurstlyView;
import android.support.v4.app.Fragment;

/**
 * A default static banner ad placement which is attached to an activity and left visible and active during the life
 * of the Activity
 */
public class BurstlyBanner extends BurstlyBaseAd {
    /**
     * Constructor for a BurstlyBanner built from a {@link BurstlyView} that has been attached to a layout file
     * @param activity The {@link Activity} associated with the banner
     * @param id The id for the BurstlyView in the layout
     */
    public BurstlyBanner(final Activity activity, int id) {
        super(activity);

        final BurstlyView burstlyView = (BurstlyView)activity.findViewById(id);
        setBurstlyView(burstlyView, AdType.Banner);
    }

    /**
     * Constructor for a BurstlyBanner built from a {@link BurstlyView} that has been attached to a layout file
     * @param fragment The {@link Fragment} associated with the banner
     * @param id The id for the BurstlyView in the layout
     */
    public BurstlyBanner(final Fragment fragment, int id) {
        super(fragment);

        View rootView = fragment.getView();

        if(rootView == null)
            throw new RuntimeException("fragment.getView returned null. Has this fragment inflated a layout?");

        final BurstlyView burstlyView = (BurstlyView)rootView.findViewById(id);

        if(burstlyView == null)
            throw new RuntimeException("Invalid view Id.  A view with this Id could not be found in the current layout");

        setBurstlyView(burstlyView, AdType.Banner);
    }

    /**
     * Constructs a BurstlyBanner in code and attaches it to a ViewGroup
     * @param activity The {@link Activity} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param refreshRate The number of seconds between banner refreshes (Minimum 10 seconds)
     */
    public BurstlyBanner(final Activity activity, final ViewGroup group, final ViewGroup.LayoutParams params, final String zoneId, final String viewName, int refreshRate) {
        super(activity);
        initBurstlyBanner(activity, group, params, zoneId, viewName, refreshRate);
    }

    /**
     * Constructs a BurstlyBanner in code and attaches it to a ViewGroup
     * @param fragment The {@link Fragment} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param refreshRate The number of seconds between banner refreshes (Minimum 10 seconds)
     */
    public BurstlyBanner(final Fragment fragment, final ViewGroup group, final ViewGroup.LayoutParams params, final String zoneId, final String viewName, int refreshRate) {
        super(fragment);
        initBurstlyBanner(fragment.getActivity(), group, params, zoneId, viewName, refreshRate);
    }

    /**
     * Constructs a BurstlyBanner in code and attaches it to a ViewGroup
     * @param activity The {@link Activity} associated with this ad
     * @param group The {@link ViewGroup} this ad will be attached to
     * @param params The {@link ViewGroup.LayoutParams} used to attach an ad to the ViewGroup
     * @param zoneId The zoneId for this banner
     * @param viewName The name of this view which will be used to identify it in teh logs
     * @param refreshRate The number of seconds between banner refreshes (Minimum 10 seconds)
     */
    public void initBurstlyBanner(final Activity activity, final ViewGroup group, final ViewGroup.LayoutParams params, final String zoneId, final String viewName, int refreshRate) {
        final BurstlyView burstlyView  = new BurstlyView(activity);
        burstlyView.setPublisherId( Burstly.getAppID() );
        burstlyView.setZoneId(zoneId);
        burstlyView.setBurstlyViewId(viewName);
        burstlyView.setDefaultSessionLife(refreshRate);

        setBurstlyView(burstlyView, AdType.Banner);

        if(params != null)
            group.addView(burstlyView, params);
        else
            group.addView(burstlyView);
    }

    /* REMOVED.  REQUIRES API LEVEL 12

    / **
     * {@link BurstlyView} attached to a parent
     * /
    protected void burstlyViewAttachedToWindow() {
        Burstly.logI(getName() + " attached to parent.");
    }

    / **
     * {@link BurstlyView} removed from it's parent
     * /
    protected void burstlyViewDetachedFromWindow() {
        Burstly.logE(getName() + " is being removed from it's parent. Use an AnimatedBanner if you wish to be able to hide and show an ad");
    }
    */
}
