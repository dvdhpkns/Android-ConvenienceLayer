package com.burstly.conveniencelayer;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity;

import java.lang.ref.WeakReference;

/**
 * Simple decorator which adds a close button to the {@link BurstlyFullscreenActivity} which is used to display house
 * and direct ads.
 */
public class DefaultDecorator implements BurstlyFullscreenActivity.IDecorator, View.OnClickListener {
    /**
     * The close button drawable
     */
    private Drawable mCloseDrawable;

    /**
     * {@link WeakReference} to the {@link Activity} of the interstitial
     */
    private WeakReference<Activity> mInterstitialActivity;

    /**
     * private constructor
     * @param context {@link Context} used to get the assets
     */
    public DefaultDecorator(final Context context) {
        try {
            AssetManager assetMgr = context.getAssets();
            final Bitmap bitmap = BitmapFactory.decodeStream(assetMgr.open("closebutton.png"));
            mCloseDrawable = new BitmapDrawable(bitmap);
        }
        catch(Exception ignore) {
            Burstly.logE("Couldn't load close button image.");
            mCloseDrawable = null;
        }
    }

    /**
     * {@link com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity.IDecorator} method to decorate the interstital
     *
     * @param view {@link View} showing the interstitial content
     * @param activity interstitial {@link Activity}
     * @return the {@link View} which will be attached to the activity
     */
    public View decorate(final View view, final Activity activity)
    {
        if(mCloseDrawable == null) {
            return view;
        }
        else {
            mInterstitialActivity = new WeakReference<Activity>(activity);

            final ImageView closeButton = new ImageView(mInterstitialActivity.get());
            closeButton.setImageDrawable(mCloseDrawable);
            closeButton.setOnClickListener(this);

            final RelativeLayout relativeLayout = new RelativeLayout(mInterstitialActivity.get());
            final RelativeLayout.LayoutParams topRightParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            topRightParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            topRightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            final RelativeLayout.LayoutParams centeredParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            centeredParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 1);
            centeredParams.addRule(RelativeLayout.CENTER_VERTICAL, 1);

            relativeLayout.addView(view, centeredParams);
            relativeLayout.addView(closeButton, topRightParams);

            return relativeLayout;
        }
    }

    /**
     * {@link View.OnClickListener} method closes the active interstitial
     * @param view {@link View} that was clicked
     */
    public synchronized void onClick(final View view) {
        Activity fullscreenActivity;

        if(mInterstitialActivity != null && (fullscreenActivity = mInterstitialActivity.get()) != null) {
            fullscreenActivity.finish();
            mInterstitialActivity = null;
        }
    }
}
