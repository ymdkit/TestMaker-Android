package com.example.ui.core

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdView(
    viewModel: AdViewModel,
    adSize: AdSize = AdSize.BANNER
) {
    val isRemovedAd by viewModel.isRemovedAd.collectAsState()

    if (!isRemovedAd) {
        AndroidView(
            factory = {
                AdView(it).apply {
                    this.adSize = adSize
                    adUnitId = "ca-app-pub-8942090726462263/8420884238"
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier
                .height(adSize.height.dp)
                .fillMaxWidth()
        )
    }
}