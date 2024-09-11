package com.example.muse.ui.muse.HelperUI

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val color = Color(0xFF1D1D1D)
val color2 = Color(0xFF2D2D2D)


//val GradientColors = listOf(Cyan, Color.Blue, Color.Red )
val GradientColors = listOf(color, color2, color)



val GradientTextStyle = TextStyle(
    brush = Brush.linearGradient(
        colors = GradientColors
    )
)

@Composable
fun IconWithText(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit = {}
) {

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
    ) {

        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, style = MaterialTheme.typography.bodyLarge, fontSize = 16.sp)

    }

}