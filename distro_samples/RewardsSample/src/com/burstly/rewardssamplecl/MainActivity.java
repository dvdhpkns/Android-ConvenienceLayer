package com.burstly.rewardssamplecl;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.burstly.conveniencelayer.*;
import com.burstly.conveniencelayer.events.AdFailEvent;
import com.burstly.lib.currency.CurrencyManager;
import com.burstly.lib.currency.event.BalanceUpdateEvent;
import com.burstly.lib.feature.currency.ICurrencyListener;

/**
 * The Burstly rewards shows how to display an offerwall and manage currency.
 */
public class MainActivity extends BurstlyActivity implements View.OnClickListener, ICurrencyListener{
    private static final String TAG = "RewardsSample";

    /**
     * The App ID used by the sample.  You will use your own App ID
     */
    private static final String APP_ID = "0BxvZ-YgMUaghWAOZTQaTg";

    /**
     * The Zone ID for the OFFERWALL within this app.  You will use your own zone ID
     */
    private static final String OFFERWALL_ZONE_ID = "0656921169102284126";

    /**
     * Button used to trigger an interstitial
     */
    private Button mWallButton;

    /**
     * Button used to subtract currency
     */
    private Button mSubtractButton;

    /**
     * Text to display currency value
     */
    private TextView mCurrencyTextView;

    /**
     * The interstitial class which we initialize and use to interact with interstitials
     */
    private BurstlyInterstitial mWallInterstitial;

    /**
     * Currency manager
     */
    private CurrencyManager mCurrencyManger;

    /**
     * Our listener which receives all event callbacks related to the interstitial
     */
    private BurstlyListenerAdapter mListener = new BurstlyListenerAdapter() {
        /**
         * In the case that our attempt to show an interstitial ad fails we need to dismiss the progress dialog
         *
         * @param ad The ad which failed to display a creative when an attempt to show or precache an ad was made
         * @param event fail event data
         */
        @Override
        public void onFail(final BurstlyBaseAd ad, final AdFailEvent event) {
            dismissProgressDialog();
        }
    };

    /**
     * Progress dialog that is shown while offerwall is loaded
     */
    private ProgressDialog mProgressDialog;

    /**
     * In our main activity we need to call Burstly.init which we do here.  In this example all ads are added via code without
     * a layout file.
     */
    @Override
    public void onCreate(Bundle savedData) {
        super.onCreate(savedData);
        Burstly.init(this, APP_ID);

        setContentView(R.layout.main);

        mCurrencyTextView = (TextView)findViewById(R.id.currencyText);

        initWallButton(this);
        initSubButton();
        initAddButton();
        initRefreshButton();


        mWallInterstitial = new BurstlyInterstitial(this, OFFERWALL_ZONE_ID, "Interstitial", false);
        mWallInterstitial.addBurstlyListener(mListener);

        mCurrencyManger = Burstly.getCurrencyManager();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mCurrencyManger.addCurrencyListener(this);
        checkForUpdatedBalance();
        //dismiss progress dialog if one exists on app resume
        dismissProgressDialog();
    }

    /**
     * Our add button calls CurrencyManager.increaseBalance
     */
    private void initWallButton(final Context context) {
        mWallButton = (Button)findViewById(R.id.wallButton);
        mWallButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Show progress dialog to indicate to the user that offer wall is being loaded
                mProgressDialog = ProgressDialog.show(context, "Working..", "Finding Offers");
                mWallInterstitial.showAd();
            }
        });
    }

    /**
     * Our add button calls CurrencyManager.increaseBalance
     */
    private void initAddButton() {
        Button button = (Button)findViewById(R.id.addButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCurrencyManger.increaseBalance(5);
            }
        });
    }

    /**
     * Our subtract button calls CurrencyManager.decreaseBalance
     */
    private void initSubButton() {
        mSubtractButton = (Button)findViewById(R.id.subtractButton);
        mSubtractButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCurrencyManger.decreaseBalance(10);
            }
        });
    }

    /**
     * Our refresh button calls checkForUpdatedBalance which calls CurrencyManager.checkForUpdate and logs errors
     */
    private void initRefreshButton() {
        Button button = (Button)findViewById(R.id.refreshButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                checkForUpdatedBalance();
            }
        });
    }

    /**
     * Check for updated balance when application is resumed
     */
    private void checkForUpdatedBalance() {
        try {
            mCurrencyManger.checkForUpdate();
        } catch (Exception e) {
            Log.e("Exception while checking for updated balance", e.toString());
        }
    }

    /**
     * Click event to launch offerwall. Show a progress dialog while offerwall is loading.
     * @param view
     */
    @Override
    public void onClick(View view) {
        // Show progress dialog to indicate to the user that offer wall is being loaded
        mProgressDialog = ProgressDialog.show(this, "Working..", "Finding Offers");
        mWallInterstitial.showAd();
    }

    /**
     * Dismiss the Progress Dialog (if one exists) on the UI thread
     */
    private void dismissProgressDialog(){
        if(mProgressDialog != null){
            runOnUiThread(new Runnable() {
                public void run() {
                    mProgressDialog.dismiss();
                }
            });
        }
    }


    /**
     * Callback for ICurrencyListener. Update balance text.
     * @param e BalanceUpdateEvent used to get balance
     */
    public void didUpdateBalance(final BalanceUpdateEvent e) {
        setBalance(e.getNewBalance());
    }


    /**
     * Callback for ICurrencyListener for failed attempt. Set balance text to old balance.
     * @param e BalanceUpdateEvent used to get balance
     */
    public void didFailToUpdateBalance(final BalanceUpdateEvent e) {
        // Failed to retrieve Burstly currency.
        Log.d(TAG, "didFailToUpdateBalance");
        Log.e(TAG, "Failed to update Burstly currency balance.");
        setBalance(e.getOldBalance());
    }

    /**
     * Set balance text. This must be donw from the UI thread
     * @param currentBalance
     */
    private void setBalance(final int currentBalance) {
        runOnUiThread(new Runnable() {
            public void run() {
                mCurrencyTextView.setText("Currency: " + currentBalance);
                mSubtractButton.setEnabled(currentBalance >= 10);
            }
        });
    }
}
