package jp.gr.java_conf.foobar.testmaker.service.view.share.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun ComposeAdView(isRemovedAd: Boolean) {
    if (isRemovedAd) return

    AndroidView(
        factory = {
            AdView(it).apply {
                adSize = AdSize.BANNER
                adUnitId = "ca-app-pub-8942090726462263/8420884238"
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
    )
}