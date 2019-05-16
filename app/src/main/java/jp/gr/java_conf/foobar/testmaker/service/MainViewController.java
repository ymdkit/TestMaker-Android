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

import android.util.Log;

import com.android.billingclient.api.Purchase;

import java.util.List;

import jp.gr.java_conf.foobar.testmaker.service.activities.MainActivity;

public class MainViewController {
    private static final String TAG = "MainViewController";

    public final UpdateListener mUpdateListener;
    private MainActivity mActivity;

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
        }
    }
}