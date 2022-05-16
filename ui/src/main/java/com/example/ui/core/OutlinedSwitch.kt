package jp.gr.java_conf.foobar.testmaker.service.view.share.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OutlinedSwitch(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = {
            onCheckedChange(!checked)
        },
        border = BorderStroke(
            ButtonDefaults.OutlinedBorderSize,
            MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
        )
    )
    {
        Text(
            label,
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.weight(weight = 1f, fill = true))
        Switch(
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
            })
    }
}