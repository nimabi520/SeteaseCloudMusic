package com.example.seteasecloudmusic.presentation.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.launch

/**
 * `presentation.navigation` 模块说明：
 *
 * 这一层属于表现层，主要负责：
 * 1. 组织页面入口与页面切换结构。
 * 2. 承载纯 UI 的状态，例如当前选中的导航项、按压动画进度。
 * 3. 把视觉效果和交互组合成最终可展示的界面。
 *
 * 当前文件是应用的导航外壳，暂时还没有接入真正的 NavHost，
 * 因此它更像“首页容器 + 底部导航栏”的组合入口。
 */

/**
 * 底部导航栏的数据模型
 *
 * @property title 导航项显示的文字标题
 * @property icon 导航项显示的图标资源
 */
data class BottomNavItem(val title: String, val icon: ImageVector)

//底栏上方的玻璃滑块
@Composable
fun GlassSlider(
    backdrop: Backdrop,
    mainItemCount: Int,
    navBarHeight: androidx.compose.ui.unit.Dp,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    mainSearchGap: androidx.compose.ui.unit.Dp,
    searchButtonWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxWidth()
            .height(navBarHeight),
        horizontalArrangement = Arrangement.spacedBy(mainSearchGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            val trackBackdrop = rememberLayerBackdrop()
            val slotWidth = if (mainItemCount > 0) maxWidth / mainItemCount.toFloat() else 0.dp
            val thumbWidth = maxOf(slotWidth * 0.72f, 40.dp)
            val thumbHeight = navBarHeight
            val centerIndex = (mainItemCount - 1).coerceAtLeast(0) / 2
            val thumbOffsetX = slotWidth * centerIndex + (slotWidth - thumbWidth) / 2f

            Box(
                Modifier
                    .offset(x = thumbOffsetX)
                    .drawBackdrop(
                        backdrop = rememberCombinedBackdrop(backdrop, trackBackdrop),
                        shape = { CircleShape },
                        effects = {
                            lens(
                                refractionHeight = 12f.dp.toPx(),
                                refractionAmount = 16f.dp.toPx(),
                                chromaticAberration = true
                            )
                        }
                    )
                    .size(thumbWidth, thumbHeight)
            )
        }

        // 占位搜索按钮宽度，确保滑块计算区域与主导航条完全一致。
        Spacer(modifier = Modifier.width(searchButtonWidth))
    }
}

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
    // 背景底色会参与毛玻璃采样，决定整个导航栏的基础明度。
    val backgroundColor = Color.White
    
    // 主导航栏按下/抬起时的缩放动画进度。
    val mainBarAnimationScope = rememberCoroutineScope()
    val mainBarProgressAnimation = remember { Animatable(0f) }
    
    // 右侧独立搜索按钮使用单独的动画状态，避免和主导航条互相影响。
    val searchAnimationScope = rememberCoroutineScope()
    val searchProgressAnimation = remember { Animatable(0f) }
    
    // 统一的弹簧动画参数，让导航条和搜索按钮保持一致的按压反馈。
    val animationSpec = remember { spring<Float>(0.5f, 300f, 0.001f) }
    
    val backdrop = rememberLayerBackdrop{
        drawRect(backgroundColor)
        drawContent()
    }

    // 记录当前选中的导航项：
    // 0~2 对应左侧主导航条，3 对应右侧搜索按钮。
    var selectedIndex by remember { mutableIntStateOf(0) }

    // 左侧主导航条目前承载三个一级入口。
    val mainNavItems = listOf(
        BottomNavItem("主页", Icons.Filled.Home),
        BottomNavItem("电台", Icons.Filled.Radio),
        BottomNavItem("我的", Icons.Filled.Person)
    )

    // 抽成共享布局参数，保证底栏和滑块按同一套比例计算。
    val horizontalPadding = 24.dp
    val navBarHeight = 64.dp
    val mainSearchGap = 16.dp
    val searchButtonWidth = navBarHeight

    // 2. 【舞台】：整个屏幕的根容器，使用 Box 以支持 Z 轴方向的层叠排列
    Box(modifier = Modifier.fillMaxSize()) {
        
        // --- 底层内容区域 ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 这里是毛玻璃的“取景层”。
                // 被包进来的内容会先渲染到底层纹理，再提供给上面的导航栏做模糊采样。
                .layerBackdrop(backdrop)
        ) {
            // 当前先只放背景，后续接入页面内容时也可以放在这一层一起参与采样。
            LiquidGlassBackground()
        }

        // --- 顶层悬浮导航栏及独立搜索按钮 ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // 1. 先避开系统导航栏（小白条）
                .windowInsetsPadding(WindowInsets.navigationBars)
                // 2. 手动添加上下左右的间距，左右缩进，底部悬浮
                .padding(horizontal = horizontalPadding, vertical = 24.dp)
                .fillMaxWidth()
                // 保持 Apple Music 的视觉高度
                .height(navBarHeight),
            // 使用 spacedBy 在主导航条和搜索小圆之间留出间距
            horizontalArrangement = Arrangement.spacedBy(mainSearchGap)
        ) {
            
            // ============ 左侧主导航条 (包含主页、电台、我的) ============
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .graphicsLayer {
                        // 按下时整体轻微放大，模拟液态玻璃被“压出张力”的感觉。
                        val progress = mainBarProgressAnimation.value
                        val maxScale = (size.width + 16f.dp.toPx()) / size.width
                        val scale = lerp(1f, maxScale, progress)
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
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
                        // 半透明白色表面让底层色块在模糊后保留一点“磨砂感”。
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
                // 主导航栏内部负责均分三个一级入口。
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
                                    selectedIndex = index
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
                    .fillMaxHeight() // 高度跟随导航栏，视觉上保持独立悬浮。
                    .aspectRatio(1f) // 让容器保持 1:1，从而形成正圆按钮。
                    .graphicsLayer {
                        val progress = searchProgressAnimation.value
                        // 搜索按钮更小，所以放大量也略小于主导航条。
                        val maxScale = (size.width + 8f.dp.toPx()) / size.width
                        val scale = lerp(1f, maxScale, progress)
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
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
                        indication = null
                    ) {
                        selectedIndex = 3
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索",
                    tint = searchColor,
                    // 搜索入口是独立按钮，因此只保留图标，不再额外显示文字。
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // 放在 Row 之后绘制，让玻璃滑块视觉上悬浮在导航栏上层。
        GlassSlider(
            backdrop = backdrop,
            mainItemCount = mainNavItems.size,
            navBarHeight = navBarHeight,
            horizontalPadding = horizontalPadding,
            mainSearchGap = mainSearchGap,
            searchButtonWidth = searchButtonWidth,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp) // 24dp(底部导航栏间距) + 64dp(导航栏高度) + 16dp(悬浮间距)
        )
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
    // 将 dp 尺寸统一换成 px，方便直接在 Canvas 中计算位置。
    val density = LocalDensity.current
    
    // 这些参数控制背景色块的大小、密度和整体留白。
    val tileSize = with(density) { 75.dp.toPx() }
    val tileGap = with(density) { 22.dp.toPx() }
    val cornerRadius = with(density) { 24.dp.toPx() }
    val topPadding = with(density) { 30.dp.toPx() }
    val sidePadding = with(density) { 30.dp.toPx() }

    // 调色板决定了毛玻璃采样后的色彩倾向。
    val palette = listOf(
        Color(0xFFE9425D),
        Color(0xFFE9923F),
        Color(0xFF65BE66),
        Color(0xFF5AB4C0)
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // 先铺一层浅灰底，避免纯白背景下毛玻璃层次过弱。
        drawRect(color = Color(0xFFE6E6E6))

        val step = tileSize + tileGap
        val rowCount = ((size.height - topPadding) / step).toInt() + 2

        // 用规则矩阵生成整块背景，效果稳定，也便于后续替换成更复杂的动态背景。
        for (row in 0 until rowCount) {
            val y = topPadding + row * step
            for (column in 0 until palette.size) {
                val x = sidePadding + column * step
                drawRoundRect(
                    // 这里每列直接取对应色值，形成稳定的纵向色带。
                    color = palette[column],
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(tileSize, tileSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
        }
    }
}
