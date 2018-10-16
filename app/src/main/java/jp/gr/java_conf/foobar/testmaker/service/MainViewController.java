/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.gr.java_conf.foobar.testmaker.service;

import android.content.SharedPreferences;
import android.util.Log;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;

import java.util.List;

import jp.gr.java_conf.foobar.testmaker.service.activities.MainActivity;

import static android.content.Context.MODE_PRIVATE;

/**
 * Handles control logic of the BaseGamePlayActivity
 */
public class MainViewController {
    private static final String TAG = "MainViewController";

    public final UpdateListener mUpdateListener;
    private MainActivity mActivity;

    // Tracks if we currently own a premium car
    public MainViewController(MainActivity activity) {
        mUpdateListener = new UpdateListener();
        mActivity = activity;
    }


    /**
     * Handler to billing updates
     */
    private class UpdateListener implements BillingManager.BillingUpdatesListener {
        @Override
        public void onBillingClientSetupFinished() {
            //mActivity.onBillingManagerSetupFinished();
        }

        @Override
        public void onConsumeFinished(String token, @BillingResponse int result) {
            Log.d(TAG, "Consumption finished. Purchase token: " + token + ", result: " + result);

            // Note: We know this is the SKU_GAS, because it's the only one we consume, so we don't
            // check if token corresponding to the expected sku was consumed.
            // If you have more than one sku, you probably need to validate that the token matches
            // the SKU you expect.
            // It could be done by maintaining a map (updating it every time you call consumeAsync)
            // of all tokens into SKUs which were scheduled to be consumed and then looking through
            // it here to check which SKU corresponds to a consumed token.
            if (result == BillingResponse.OK) {
                // Successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                saveData();
            }

        }

        @Override
        public void onPurchasesUpdated(List<Purchase> purchaseList) {

            for (Purchase purchase : purchaseList) {
                switch (purchase.getSku()) {
                    case "removead":
                        Log.d(TAG, "You remove ad! Congratulations!!!");
                        mActivity.sharedPreferenceManager.setRemovedAd(true);

                        mActivity.removeAd();
                        break;
                }
            }

            //レイアウトの更新
        }
    }

    private void saveData() {
        SharedPreferences.Editor spe = mActivity.getPreferences(MODE_PRIVATE).edit();
        spe.apply();
    }
}