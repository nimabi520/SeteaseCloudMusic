package com.example.seteasecloudmusic.presentation.navigation

import android.R.attr.onClick
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.launch

/**
 * 底部导航栏的数据模型
 *
 * @property title 导航项显示的文字标题
 * @property icon 导航项显示的图标资源
 */
data class BottomNavItem(val title: String, val icon: ImageVector)

/**
 * 应用的主导航入口组件
 *
 * 此组件构建了整个应用的基础布局结构，包含：
 * 1. 底层的 [LiquidGlassBackground] 背景
 * 2. 中间的内容区域（目前与背景层合并）
 * 3. 顶层悬浮的毛玻璃效果底部导航栏
 *
 * 核心使用了 [com.kyant.backdrop] 库来实现高性能的实时模糊与透镜效果。
 */
@Composable
fun AppNavigation() {
    // 1. 【准备胶片】：创建一个 layerBackdrop 状态来保存底层渲染的实时画面
    val backgroundColor = Color.White
    
    // 主导航栏的动画状态
    val mainBarAnimationScope = rememberCoroutineScope()
    val mainBarProgressAnimation = remember { Animatable(0f) }
    
    // 搜索按钮的单独动画状态
    val searchAnimationScope = rememberCoroutineScope()
    val searchProgressAnimation = remember { Animatable(0f) }
    
    // 定义弹簧动画参数：DampingRatio = 0.5f (阻尼比，越小越弹), Stiffness = 300f (刚度，越大越快)
    val animationSpec = remember { spring<Float>(0.5f, 300f, 0.001f) }
    
    val backdrop = rememberLayerBackdrop{
        drawRect(backgroundColor)
                drawContent()
    }
    // 记录当前选中的导航项索引，0~2是主导航的三个按钮，3是搜索按钮
    var selectedIndex by remember { mutableIntStateOf(0) }

    // 定义左侧主导航栏的三个主要功能入口
    val mainNavItems = listOf(
        BottomNavItem("主页", Icons.Filled.Home),
        BottomNavItem("电台", Icons.Filled.Radio),
        BottomNavItem("我的", Icons.Filled.Person) // 第三项 (索引2)
    )

    // 2. 【舞台】：整个屏幕的根容器，使用 Box 以支持 Z 轴方向的层叠排列
    Box(modifier = Modifier.fillMaxSize()) {
        
        // --- 底层内容区域 ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 3. 【架设摄像机】：将此 Box 标记为 backdrop 的捕获源
                // 只要放在这个 Box 里的东西（背景、页面内容），都会被实时渲染并送进 backdrop
                .layerBackdrop(backdrop)
        ) {
            // 绘制多彩的液态背景，因为被包裹在 layerBackdrop 中，底栏可以捕获到它的色彩
            LiquidGlassBackground()
        }

        // --- 顶层悬浮导航栏及独立搜索按钮 ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // 1. 先避开系统导航栏（小白条）
                .windowInsetsPadding(WindowInsets.navigationBars)
                // 2. 手动添加上下左右的间距，左右缩进，底部悬浮
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .fillMaxWidth()
                // 保持 Apple Music 的视觉高度
                .height(64.dp),
            // 使用 spacedBy 在主导航条和搜索小圆之间留出间距
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // ============ 左侧主导航条 (包含主页、电台、我的) ============
            Box(
                modifier = Modifier
                    .weight(1f) // 占据剩余的所有宽度
                    .fillMaxHeight()
                    .graphicsLayer {
                        val progress = mainBarProgressAnimation.value
                        val maxScale = (size.width + 16f.dp.toPx()) / size.width
                        val scale = lerp(1f, maxScale, progress)
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape }, // 胶囊形状
                        effects = {
                            vibrancy()
                            blur(2f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        layerBlock = {
                            val progress = mainBarProgressAnimation.value
                            val maxScale = (size.width + 16f.dp.toPx()) / size.width
                            val scale = lerp(1f, maxScale, progress)
                            scaleX = scale
                            scaleY = scale
                        },
                        onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {}
                    .pointerInput(mainBarAnimationScope) {
                        awaitEachGesture {
                            awaitFirstDown()
                            mainBarAnimationScope.launch { mainBarProgressAnimation.animateTo(1f, animationSpec) }
                            waitForUpOrCancellation()
                            mainBarAnimationScope.launch { mainBarProgressAnimation.animateTo(0f, animationSpec) }
                        }
                    }
            ) {
                // 主导航栏内部按钮布局
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    mainNavItems.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        val itemColor = if (isSelected) Color(0xFFFA233B) else Color.DarkGray
                        val interactionSource = remember { MutableInteractionSource() }

                        LaunchedEffect(interactionSource) {
                            interactionSource.interactions.collect { interaction ->
                                when (interaction) {
                                    is PressInteraction.Press -> {
                                        mainBarAnimationScope.launch { mainBarProgressAnimation.animateTo(1f, animationSpec) }
                                    }
                                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                                        mainBarAnimationScope.launch { mainBarProgressAnimation.animateTo(0f, animationSpec) }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null // 禁用默认水波纹
                                ) {
                                    selectedIndex = index // 主导航栏索引为 0, 1, 2
                                }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = itemColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                color = itemColor,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // ============ 右侧独立的搜索按钮 ============
            val isSearchSelected = selectedIndex == 3
            val searchColor = if (isSearchSelected) Color(0xFFFA233B) else Color.DarkGray
            val searchInteractionSource = remember { MutableInteractionSource() }
            
            LaunchedEffect(searchInteractionSource) {
                searchInteractionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> {
                            searchAnimationScope.launch { searchProgressAnimation.animateTo(1f, animationSpec) }
                        }
                        is PressInteraction.Release, is PressInteraction.Cancel -> {
                            searchAnimationScope.launch { searchProgressAnimation.animateTo(0f, animationSpec) }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f) // 保持1:1宽高比，保证是正圆形
                    .graphicsLayer {
                        val progress = searchProgressAnimation.value
                        // 稍微调整圆形的绽放比例，因为底宽比较小
                        val maxScale = (size.width + 8f.dp.toPx()) / size.width
                        val scale = lerp(1f, maxScale, progress)
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape }, // 正圆形
                        effects = {
                            vibrancy()
                            blur(2f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        layerBlock = {
                            val progress = searchProgressAnimation.value
                            val maxScale = (size.width + 8f.dp.toPx()) / size.width
                            val scale = lerp(1f, maxScale, progress)
                            scaleX = scale
                            scaleY = scale
                        },
                        onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) },
                    )
                    .clickable(
                        interactionSource = searchInteractionSource,
                        indication = null // 禁用默认水波纹
                    ) {
                        selectedIndex = 3 // 对应搜索页面的索引
                    },
                // 将搜索图标放置在正中央
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索",
                    tint = searchColor,
                    // Apple Music 的独立小圆圈没有文字，通常只有一个大一点的加粗图标
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }


}

/**
 * 绘制液态玻璃背景组件
 *
 * 使用原生 Canvas 绘制一系列多彩的圆角矩形，作为整个应用的背景底图。
 * 这些色块配合上层的毛玻璃效果，能产生非常漂亮的漫反射光影。
 *
 * @param modifier 修饰符
 */
@Composable
private fun LiquidGlassBackground(modifier: Modifier = Modifier) {
    // 获取屏幕密度，用于将 dp 转换为 px
    val density = LocalDensity.current
    
    // 定义各种尺寸参数
    val tileSize = with(density) { 90.dp.toPx() } // 色块大小
    val tileGap = with(density) { 22.dp.toPx() }   // 色块间距
    val cornerRadius = with(density) { 24.dp.toPx() } // 色块圆角
    val topPadding = with(density) { 30.dp.toPx() }  // 顶部起始偏移
    val sidePadding = with(density) { 30.dp.toPx() }  // 侧边起始偏移

    // 定义背景配色板
    val palette = listOf(
        Color(0xFFE9425D), // 红色系
        Color(0xFFE9923F), // 橙色系
        Color(0xFF65BE66), // 绿色系
        Color(0xFF5AB4C0)  // 蓝青色系
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. 绘制灰白色底色
        drawRect(color = Color(0xFFE6E6E6))

        // 计算步长和行数
        val step = tileSize + tileGap
        // 根据屏幕高度动态计算需要绘制多少行
        val rowCount = ((size.height - topPadding) / step).toInt() + 2

        // 2. 循环绘制色块矩阵
        for (row in 0 until rowCount) {
            val y = topPadding + row * step
            for (column in 0 until palette.size) {
                // 计算当前色块的 X 坐标
                val x = sidePadding + column * step
                // 绘制圆角矩形
                drawRoundRect(
                    color = palette[column], // 循环使用配色板中的颜色（此时其实是按索引越界循环，但在 Compose 中 list get 需要注意越界，这里原本逻辑可能隐含假设 palette 足够涵盖 column，或者原本逻辑只是示例）
                    // 修正：上面的原始逻辑 palette[column] 如果 column >= palette.size 会崩溃。
                    // 实际上看步长，屏幕宽度通常只够放2-3个，这里假设 column 不会超过 palette.size。
                    // 为了健壮性，建议取模： palette[column % palette.size]
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(tileSize, tileSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
        }
    }
}