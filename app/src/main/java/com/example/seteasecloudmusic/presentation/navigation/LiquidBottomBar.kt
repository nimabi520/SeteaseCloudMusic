package com.example.seteasecloudmusic.presentation.navigation

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/**
 * LiquidGlass 毛玻璃效果底栏导航
 * 使用 Backdrop 库实现真实的 iOS 风格毛玻璃效果
 */
@Composable
fun LiquidBottomBar(
    backdrop: Backdrop?,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (backdrop == null) return

    Row(
        modifier = modifier
            .safeContentPadding()
            .height(80.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(28.dp) },
                effects = {
                    vibrancy()
                    blur(8f.dp.toPx())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        lens(12f.dp.toPx(), 24f.dp.toPx())
                    }
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.15f)) }
            ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem.items.forEach { item ->
            BottomNavItemView(
                item = item,
                backdrop = backdrop,
                isSelected = currentRoute == item.route,
                onItemClick = { onItemClick(item.route) }
            )
        }
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    backdrop: Backdrop,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale = animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        label = "icon_scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    
    val iconTint = animateColorAsState(
        targetValue = if (isSelected) primaryColor else Color.White.copy(
            alpha = 0.6f
        ),
        label = "icon_tint"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { CircleShape },
                shadow = null,
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        lens(8f.dp.toPx(), 16f.dp.toPx())
                    }
                },
                onDrawSurface = {
                    if (isSelected) {
                        val tint = primaryColor
                        drawRect(tint, blendMode = BlendMode.Multiply)
                        drawRect(tint.copy(alpha = 0.2f))
                    } else {
                        drawRect(Color.White.copy(alpha = 0.1f))
                    }
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onItemClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.contentDescription,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer(
                        scaleX = iconScale.value,
                        scaleY = iconScale.value
                    ),
                tint = iconTint.value
            )
            Text(
                text = item.label,
                fontSize = 10.sp,
                color = iconTint.value,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
