package com.burstly.conveniencelayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.burstly.lib.BurstlySdk;
import com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity;
import com.burstly.lib.currency.CurrencyManager;
import com.burstly.lib.util.LoggerExt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Burstly is a singleton which takes care of initializing and shutting down the SDK as well as keeping a list
 * of BurstlyViews associated with each activity so that they can be notified of activity pause and resume events.
 */
public class Burstly {
    /**
     * Log tag
     */
    public static final String TAG = "Burstly Convenience Layer";

    /**
     * Has the system been initialized
     */
    protected static boolean sIsInitialized = false;

    /**
     * Map of all ads associated with an Activity
     */
    private static final HashMap<Activity, ArrayList<IActivityListener>> sActivityListeners = new HashMap<Activity, ArrayList<IActivityListener>>();

    /**
     * Map of all ads associated with a Fragment
     */
    private static final HashMap<Fragment, ArrayList<IFragmentListener>> sFragmentListeners = new HashMap<Fragment, ArrayList<IFragmentListener>>();

    /**
     * Map of BurstlyView names to the pubs and zones they are associated with to make sure a BurstlyView name isn't used
     * with 2 different pub/zone combinations
     */
    private static final HashMap<String, String> sViewMap = new HashMap<String, String>();

    /**
     * Flag keeping track of whether logging is enabled or disabled
     */
    private static boolean sLoggingEnabled;

    /**
     * App ID used by this title
     */
    private static String sAppID;

    /**
     * The currency manager for the associated AppId
     */
    private static CurrencyManager sCurrencyManager;

    /**
     * Should be the main activity for your title
     */
    private static Context sContext;

    /**
     * The device ID for the current device
     */
    private static String sDeviceId;

    /**
     *
     */
    private static String[] sIntegrationDeviceIDs;

    private static boolean sIntegrationModeEnabled = false;

    private static BurstlyIntegrationModeAdNetworks sIntegrationNetwork = BurstlyIntegrationModeAdNetworks.HOUSE;

    /**
     * Flag used to determine whether the test mode alert has been shown
     */
    private static boolean sTestModeAlertShown = false;

    /**
     * Initialize the BurstlySdk and the Burstly convenience layer
     *
     * @param context Application {@link Context} or default {@link Activity} used to initialize the BurstlySdk
     * @return reference to the static instance
     */
    public static synchronized void init(final Context context, final String appID)
    {
        init(context, appID, new DefaultDecorator(context));
    }

    /**
     * Initialize the BurstlySdk and the Burstly convenience layer
     * 
     * @param context Application {@link Context} or default {@link Activity} used to initialize the BurstlySdk
     * @param decorator The {@link com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity.IDecorator} that will be used to decorate your image interstitials
     * @return reference to the static instance
     */
    public static synchronized void init(final Context context, final String appID, final BurstlyFullscreenActivity.IDecorator decorator) {
        if(!sIsInitialized) {
            sIsInitialized = true;
            sAppID = appID;
            sContext = context;
            sIntegrationModeEnabled = false;
            sIntegrationDeviceIDs = null;
            sIntegrationNetwork = BurstlyIntegrationModeAdNetworks.HOUSE;

            initDeviceId();
            initBurstly(context, decorator);

            sCurrencyManager = new CurrencyManager();
            sCurrencyManager.initManager(sContext, sAppID);
        }
        else {
            logW("Burstly already initialized");
        }
    }

    /**
     * Static deinitializer cleans up the SDK and convenience layer
     */
    public static void deinit() {
        if(!sIsInitialized) {
            throw new RuntimeException("Burstly.init never called or Burstly.deinit already called.");
        }
        else {
            if(sActivityListeners.size() != 0) {
                logE("Deinitializing Burstly conveniencelayer system before everything is destroyed");
            }

            BurstlyFullscreenActivity.removeDecorator("burstlyImage");
            BurstlySdk.shutdown(sContext);

            sIsInitialized = false;
        }
    }


    /**
     * Initializer makes calls to initialize the BurstlySdk, and initializes data structures for the convenience
     * layer to manage the BurstlyView's associated with each page
     * @param context {@link Context} or default {@link Activity} used to initialize the Burstly SDK
     * @param decorator {@link com.burstly.lib.component.networkcomponent.burstly.BurstlyFullscreenActivity.IDecorator} used to initialize the SDK
     */
    protected static void initBurstly(final Context context, final BurstlyFullscreenActivity.IDecorator decorator) {
        sLoggingEnabled = true;
        BurstlySdk.init(context);

        sActivityListeners.clear();

        if(decorator != null)
            BurstlyFullscreenActivity.addDecorator("burstlyImage", decorator);
        else
            logW("No decorator spcified. Interstitials will not have a close button.  Pass an instance of com.burstly.convenience.DefaultDecorator into Convenience.init to add the default close button.");
    }

    /**
     * Passes activity onPause events on to necessary BurstlyView objects
     * @param activity The {@link Activity} being paused
     */
    public static void onPauseActivity(final Activity activity) {
        if(sActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = sActivityListeners.get(activity);

            for(IActivityListener listener : listeners) {
                listener.activityPaused(activity);
            }
        }
    }

    /**
     * Passes activity onResume events on to the necessary BurstlyView objects
     * @param activity The {@link Activity} being resumed
     */
    public static void onResumeActivity(final Activity activity) {
        if(sActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = sActivityListeners.get(activity);

            for(IActivityListener listener : listeners) {
                listener.activityResumed(activity);
            }
        }
    }

    /**
     * Passes activity onDestroy events on to necessary BurstlyView objects
     * @param activity The {@link Activity} being destroyed
     */
    public static void onDestroyActivity(final Activity activity) {
        if(sActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = sActivityListeners.get(activity);

            for(int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).activityDestroyed(activity);
                listeners.remove(i);
            }

            sActivityListeners.remove(activity);
        }
    }

    /**
     * Add an {@link IActivityListener} to receive callbacks when the activity is paused, resumed, or destroyed
     * @param activity The {@link Activity} associated with the ad
     * @param activityListener The new {@link IActivityListener} being added
     */
    public static void addActivityListener(final Activity activity, final IActivityListener activityListener) {
        if(activityListener instanceof BurstlyBaseAd) {       
            final BurstlyBaseAd burstlyAd = (BurstlyBaseAd)activityListener;
            final String name = burstlyAd.getName();
            final String appZone = burstlyAd.getAppId() + ":" + burstlyAd.getZoneId();
    
            if(sViewMap.containsKey(name)) {
                if(!sViewMap.get(name).equals(appZone)) {
                    throw new RuntimeException("Attempting to reuse the view Id " + name + " with a different app/zone combination.  Use a new view Id for each pub and zone");
                }
            }
            else {
                sViewMap.put(name, appZone);
            }
        }

        if(sActivityListeners.containsKey(activity)) {
            sActivityListeners.get(activity).add(activityListener);
        }
        else {
            final ArrayList<IActivityListener> listeners = new ArrayList<IActivityListener>();
            listeners.add(activityListener);

            sActivityListeners.put(activity, listeners);
        }
    }

    /**
     * Removes an {@link IActivityListener} from the list of listeners receiving pause, resume, and destroy
     * callbacks for the specified activity
     * @param activity {@link Activity} having the listener removed
     * @param activityListener {@link IActivityListener} being removed from the list of listeners
     */
    public static void removeActivityListener(final Activity activity, final IActivityListener activityListener) {
        if(sActivityListeners.containsKey(activity)) {
            final ArrayList<IActivityListener> listeners = sActivityListeners.get(activity);

            if(listeners.contains(activityListener))
                listeners.remove(activityListener);
        }
    }

    /**
     * Removes {@link IActivityListener} from all lists of listeners receiving pause, resumee, and destroyed callbacks
     * @param activityListener {@link IActivityListener} being removed
     */
    public static void removeActivityListener(final IActivityListener activityListener) {
        final Object[] listenerLists = sActivityListeners.values().toArray();
        
        for(Object listenersObj : listenerLists) {
            ArrayList<IActivityListener> listeners = (ArrayList<IActivityListener>)listenersObj;
            
            if(listeners.contains(activityListener))
                listeners.remove(activityListener);
        }
    }

    /**
     * Passes fragment onPause events on to necessary BurstlyView objects
     * @param fragment The {@link Fragment} being paused
     */
    public static void onPauseFragment(final Fragment fragment) {
        if(sFragmentListeners.containsKey(fragment)) {
            final ArrayList<IFragmentListener> listeners = sFragmentListeners.get(fragment);

            for(IFragmentListener listener : listeners) {
                listener.fragmentPaused(fragment);
            }
        }
    }

    /**
     * Passes fragment onResume events on to the necessary BurstlyView objects
     * @param fragment The {@link Fragment} being resumed
     */
    public static void onResumeFragment(final Fragment fragment) {
        if(sFragmentListeners.containsKey(fragment)) {
            final ArrayList<IFragmentListener> listeners = sFragmentListeners.get(fragment);

            for(IFragmentListener listener : listeners) {
                listener.fragmentResumed(fragment);
            }
        }
    }

    /**
     * Passes fragment onDestroy events on to necessary BurstlyView objects
     * @param fragment The {@link Fragment} being destroyed
     */
    public static void onDestroyFragment(final Fragment fragment) {
        if(sFragmentListeners.containsKey(fragment)) {
            final ArrayList<IFragmentListener> listeners = sFragmentListeners.get(fragment);

            for(int i = listeners.size() - 1; i >= 0; i--) {
                listeners.get(i).fragmentDestroyed(fragment);
                listeners.remove(i);
            }

            sFragmentListeners.remove(fragment);
        }
    }

    /**
     * Add an {@link IFragmentListener} to receive callbacks when the fragment is paused, resumed, or destroyed
     * @param fragment The {@link Fragment} associated with the ad
     * @param fragmentListener The new {@link IFragmentListener} being added
     */
    public static void addFragmentListener(final Fragment fragment, final IFragmentListener fragmentListener) {
        if(fragmentListener instanceof BurstlyBaseAd) {
            final BurstlyBaseAd burstlyAd = (BurstlyBaseAd)fragmentListener;
            final String name = burstlyAd.getName();
            final String appZone = burstlyAd.getAppId() + ":" + burstlyAd.getZoneId();

            if(sViewMap.containsKey(name)) {
                if(!sViewMap.get(name).equals(appZone)) {
                    throw new RuntimeException("Attempting to reuse the view Id " + name + " with a different app/zone combination.  Use a new view Id for each pub and zone");
                }
            }
            else {
                sViewMap.put(name, appZone);
            }
        }

        if(sFragmentListeners.containsKey(fragment)) {
            sFragmentListeners.get(fragment).add(fragmentListener);
        }
        else {
            final ArrayList<IFragmentListener> listeners = new ArrayList<IFragmentListener>();
            listeners.add(fragmentListener);

            sFragmentListeners.put(fragment, listeners);
        }
    }

    /**
     * Removes an {@link IFragmentListener} from the list of listeners receiving pause, resume, and destroy
     * callbacks for the specified fragment
     * @param fragment {@link Fragment} having the listener removed
     * @param fragmentListener {@link IFragmentListener} being removed from the list of listeners
     */
    public static void removeFragmentListener(final Fragment fragment, final IFragmentListener fragmentListener) {
        if(sFragmentListeners.containsKey(fragment)) {
            final ArrayList<IFragmentListener> listeners = sFragmentListeners.get(fragment);

            if(listeners.contains(fragmentListener))
                listeners.remove(fragmentListener);
        }
    }

    /**
     * Removes {@link IFragmentListener} from all lists of listeners receiving pause, resumee, and destroyed callbacks
     * @param fragmentListener {@link IFragmentListener} being removed
     */
    public static void removeFragmentListener(final IFragmentListener fragmentListener) {
        final Object[] listenerLists = sFragmentListeners.values().toArray();

        for(Object listenersObj : listenerLists) {
            ArrayList<IFragmentListener> listeners = (ArrayList<IFragmentListener>)listenersObj;

            if(listeners.contains(fragmentListener))
                listeners.remove(fragmentListener);
        }
    }

    /**
     * Enables and disables logging.
     * @param enabled true if enabling, false if disabling
     */
    public static void setLoggingEnabled(boolean enabled) {
        if(enabled)
            LoggerExt.setLogLevel(LoggerExt.DEBUG_LEVEL);
        else
            LoggerExt.setLogLevel(LoggerExt.NONE_LEVEL);

        sLoggingEnabled = enabled;
    }

    /**
     * Gets the state of logging
     * @return true if enabled, false if disabled
     */
    public static boolean isLoggingEnabled() {
        return sLoggingEnabled;
    }

    /**
     * Print a debug level line to the log
     * @param s string to print to log
     */
    static void logD(final String s) {
        if(isLoggingEnabled())
            Log.d(TAG, s);
    }

    /**
     * Print a debug level line to the log
     * @param s string to print to log
     */
    static void logI(final String s) {
        if(isLoggingEnabled())
            Log.i(TAG, s);
    }

    /**
     * Print a debug level line to the log
     * @param s string to print to log
     */
    static void logW(final String s) {
        if(isLoggingEnabled())
            Log.w(TAG, s);
    }

    /**
     * Print a error level line to the log
     * @param s string to print to log
     */
    static void logE(final String s) {
        //always print error level log entries to the log
        Log.e(TAG, s);
    }

    /**
     * Get the App Id
     * @return the App ID associated with this application
     */
    static String getAppID()
    {
        if(!sIsInitialized)
            throw new RuntimeException("Burstly.init never called or Burstly.deinit already called.");

        return sAppID;
    }

    /**
     * Gets the currency manager
     * @return
     */
    public static CurrencyManager getCurrencyManager()
    {
        if(!sIsInitialized)
            throw new RuntimeException("Burstly.init never called or Burstly.deinit already called.");

        return sCurrencyManager;
    }


    public static void enableIntegrationMode(final String[] integrationDeviceIDs)
    {
        if(!sIsInitialized)
            throw new RuntimeException("Burstly.init never called or Burstly.deinit already called.");

        if(integrationDeviceIDs != null)
            sIntegrationDeviceIDs = integrationDeviceIDs;

        sIntegrationModeEnabled = true;
    }

    public static void setIntegrationNetwork(BurstlyIntegrationModeAdNetworks network)
    {
        if(!sIsInitialized)
            throw new RuntimeException("Burstly.init never called or Burstly.deinit already called.");

        sIntegrationNetwork = network;
    }

    public static BurstlyIntegrationModeAdNetworks getIntegrationNetwork()
    {
        if(!sIsInitialized)
            throw new RuntimeException("Burstly.init never called or Burstly.deinit already called.");

        return sIntegrationNetwork;
    }

    /**
     * Get device id. This is the same waterfall process used in the Burstly SDK.
     */
    static synchronized void initDeviceId() {
        boolean invalidDeviceId = false;
        final TelephonyManager tManager = (TelephonyManager)sContext.getSystemService(Context.TELEPHONY_SERVICE);
        sDeviceId = tManager.getDeviceId();
        logD("TelephonyManager: deviceID - {0}" + sDeviceId);
        invalidDeviceId = !isDeviceIdValid(sDeviceId);

        // Is there no IMEI or MEID? Is this at least Android 2.3+? Then let's get the serial.
        if (invalidDeviceId && Build.VERSION.SDK_INT >= 9) {
            logD("Trying to get serial of 2.3+ device..."); // THIS CLASS IS ONLY LOADED FOR ANDROID 2.3+
            sDeviceId = Build.SERIAL;
            logD("SERIAL: deviceID - {0}" + sDeviceId);
            invalidDeviceId = !isDeviceIdValid(sDeviceId);
        }

        if(invalidDeviceId)
            sDeviceId = null;
    }

    /**
     * Check device id for validity.
     *
     * @param deviceId {@link String} current device id
     * @return {@code boolean} true if device id is valid
     */
    private static boolean isDeviceIdValid(final String deviceId) {
        // Is the device ID null or empty?
        if (deviceId == null) {
            logE("Device id is null.");
            return false;
        }
        // Is this an emulator device ID?
        final boolean validDeviceId = (deviceId.length() != 0 && !deviceId.equals("000000000000000") &&
                                      !deviceId.equals("0") && !deviceId.equals("unknown"));
        if (!validDeviceId) {
            logE("Device id is empty or an emulator.");
        }

        return validDeviceId;
    }

    /**
     * Show alert in application if device is using a test pub and zone
     *
     * @param context
     */
    static synchronized void showTestModeAlert(Context context) {
        if(sTestModeAlertShown != true){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

            // set dialog content
            alertDialogBuilder
                    .setTitle("Burstly Integration Mode")
                    .setMessage("You will only see sample ads from specified networks on this device. Disable Integration Mode to see live ads.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
            sTestModeAlertShown = true;
        }
    }

    public static boolean isIntegrationModeEnabledForThisDevice()
    {
        if(!sIsInitialized)
            throw new RuntimeException("Burstly.init never called or Burstly.deinit already called.");

        if(sIntegrationModeEnabled)
            return ( sIntegrationDeviceIDs == null || (sDeviceId != null && Arrays.asList(sIntegrationDeviceIDs).contains(sDeviceId)) );
        else
            return false;
    }
}
