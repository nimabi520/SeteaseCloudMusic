package com.example.seteasecloudmusic.presentation.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
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
import kotlin.math.absoluteValue

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
    contentBackdrop: Backdrop,
    mainItemCount: Int,
    selectedIndex: Int,
    dragOffsetX: Float?, // 新增加：拖拽时的横坐标 (px)
    navBarHeight: androidx.compose.ui.unit.Dp,
    mainBarProgress: Float,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    mainSearchGap: androidx.compose.ui.unit.Dp,
    searchButtonWidth: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
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
                .fillMaxHeight()
                .graphicsLayer {
                    // 让滑块在点击状态下的放大程度增大
                    val maxScale = (size.width + 32f.dp.toPx()) / size.width
                    val scale = lerp(1f, maxScale, mainBarProgress)
                    scaleX = scale
                    scaleY = scale
                },
            contentAlignment = Alignment.CenterStart
        ) {
            val slotWidth = if (mainItemCount > 0) maxWidth / mainItemCount.toFloat() else 0.dp
            val activeMainIndex = selectedIndex.takeIf { it in 0 until mainItemCount } ?: 0
            // 设置内边距，使滑块不会产生锯齿或溢出。这里使用 0.dp 让滑块与父容器圆角完全重合并填满分段，不产生突出。
            // 也可改为 2.dp / 4.dp 来实现具有内陷感的嵌套分段控件。
            val thumbPadding = 4.dp // <-- 刚刚要求改成 4.dp
            val targetThumbWidth = (slotWidth - thumbPadding * 2).coerceAtLeast(0.dp)
            val baseOffsetX = slotWidth * activeMainIndex + thumbPadding
            
            val density = LocalDensity.current
            val targetThumbOffsetX = if (dragOffsetX != null) {
                // 如果正在拖拽，滑块中心跟随手指 X 坐标
                val fingerXDp = with(density) { dragOffsetX.toDp() }
                val halfThumb = targetThumbWidth / 2
                (fingerXDp - halfThumb).coerceIn(thumbPadding, maxWidth - targetThumbWidth - thumbPadding)
            } else {
                baseOffsetX
            }

            val tracking = dragOffsetX != null
            val animatedThumbWidth by animateDpAsState(
                targetValue = targetThumbWidth,
                // 根据是否在拖拽，切换弹簧参数实现更顺手的跟随效果
                animationSpec = if (tracking) spring(stiffness = 800f, dampingRatio = 0.8f) else spring(stiffness = 300f, dampingRatio = 0.6f),
                label = "glassThumbWidth"
            )
            val animatedThumbOffsetX by animateDpAsState(
                targetValue = targetThumbOffsetX,
                animationSpec = if (tracking) spring(stiffness = 800f, dampingRatio = 0.8f) else spring(stiffness = 300f, dampingRatio = 0.6f),
                label = "glassThumbOffset"
            )
            val thumbAlpha by animateFloatAsState(
                targetValue = if (selectedIndex in 0 until mainItemCount) 1f else 0f,
                animationSpec = spring(stiffness = 500f, dampingRatio = 0.9f),
                label = "glassThumbAlpha"
            )
            // 增加苹果风格的滑动拖拽弹性（Gooey Stretch Effect）
            // 根据目标与实际的当前动画差值，动态增加滑块的宽度，使其在运动时产生“拉丝”或加速度拉长的效果。
            val offsetDiff = targetThumbOffsetX - animatedThumbOffsetX
            val stretchFactor = 0.35f // 弹性强度
            
            val renderedOffsetX = if (offsetDiff.value < 0f) {
                // 如果是往左边滑，真正的视觉起点应该提前向左探出
                animatedThumbOffsetX + offsetDiff * stretchFactor
            } else {
                // 如果是往右边滑，起点不变，右侧宽度增加即可
                animatedThumbOffsetX
            }
            // 宽度在原有基础上叠加差距的绝对值比例
            val renderedWidth = animatedThumbWidth + offsetDiff.value.absoluteValue.dp * stretchFactor

            val thumbHeight = navBarHeight - thumbPadding * 2
            val innerCornerRadius = cornerRadius - thumbPadding

            Box(
                Modifier
                    .graphicsLayer { alpha = thumbAlpha }
                    .offset(x = renderedOffsetX)
                    .drawBackdrop(
                        backdrop = rememberCombinedBackdrop(backdrop, contentBackdrop),
                        shape = { RoundedRectangle(innerCornerRadius) },
                        effects = {
                            lens(
                                refractionHeight = 6f.dp.toPx(),
                                refractionAmount = 12f.dp.toPx(),
                                chromaticAberration = true
                            )
                        }
                    )
                    .size(animatedThumbWidth, thumbHeight)
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
    
    // 缩小底栏接收点击时的弹跳程度，增加 stiffness 使其更紧致，缩小弹跳幅度
    val animationSpec = remember { spring<Float>(0.8f, 500f, 0.001f) }
    
    val backdrop = rememberLayerBackdrop{
        drawRect(backgroundColor)
        drawContent()
    }
    val navBarContentBackdrop = rememberLayerBackdrop()

    // 记录导航栏宽度和当前手指 X 坐标准备拖拽交互
    var barWidth by remember { mutableFloatStateOf(0f) }
    var dragOffsetX by remember { mutableStateOf<Float?>(null) }

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
    val navBarHeight = 60.dp
    val mainSearchGap = 16.dp
    val searchButtonWidth = navBarHeight
    // 统一形状：使用 RoundedRectangle 实现 G² 连续圆角（squircle），
    // 让玻璃滑块、主导航条、搜索按钮共享一致的圆角半径。
    val cornerRadius = navBarHeight / 2

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
                // 让滑块可以采样到底栏和图标内容，而不仅是背景层。
                .layerBackdrop(navBarContentBackdrop)
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
                        // 按下时整体轻微放大，模拟液态玻璃被“压出张力”的感觉。缩小底栏放大比例。
                        val progress = mainBarProgressAnimation.value
                        val maxScale = (size.width + 8f.dp.toPx()) / size.width
                        val scale = lerp(1f, maxScale, progress)
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(cornerRadius) },
                        effects = {
                            vibrancy()
                            blur(2f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        layerBlock = {
                            val progress = mainBarProgressAnimation.value
                            val maxScale = (size.width + 8f.dp.toPx()) / size.width
                            val scale = lerp(1f, maxScale, progress)
                            scaleX = scale
                            scaleY = scale
                        },
                        // 半透明白色表面让底层色块在模糊后保留一点“磨砂感”。
                        onDrawSurface = { drawRect(Color.White.copy(alpha = 0.5f)) },
                    )
                    .pointerInput(mainBarAnimationScope) {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            var currentX = down.position.x
                            dragOffsetX = currentX

                            val updateSelection = { x: Float ->
                                val slotWidthPx = if (mainNavItems.isNotEmpty()) size.width.toFloat() / mainNavItems.size.toFloat() else 0f
                                if (slotWidthPx > 0f) {
                                    val newIndex = (x / slotWidthPx).toInt().coerceIn(0, mainNavItems.size - 1)
                                    selectedIndex = newIndex
                                }
                            }
                            // 第一次按下时立即响应切换选中状态
                            updateSelection(currentX)

                            mainBarAnimationScope.launch { mainBarProgressAnimation.animateTo(1f, animationSpec) }
                            
                            var inGesture = true
                            while (inGesture) {
                                val event = awaitPointerEvent()
                                val dragEvent = event.changes.firstOrNull()
                                if (dragEvent != null && dragEvent.pressed) {
                                    currentX = dragEvent.position.x
                                    dragOffsetX = currentX
                                    updateSelection(currentX) // 拖拽时动态更新选中项
                                    dragEvent.consume() // 消耗事件防止底层组件响应
                                } else {
                                    inGesture = false
                                }
                            }
                            
                            dragOffsetX = null
                            mainBarAnimationScope.launch { mainBarProgressAnimation.animateTo(0f, animationSpec) }
                        }
                    }
            ) {
                // 主导航栏内部负责均分三个一级入口。
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    mainNavItems.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        val itemColor = if (isSelected) Color(0xFFFA233B) else Color.DarkGray

                        Column(
                            modifier = Modifier
                                .weight(1f) // 使所有导航项等宽，实现 Apple Music 分段滑块风格
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = itemColor,
                                modifier = Modifier.size(26.dp)
                            )
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
                        val maxScale = (size.width + 4f.dp.toPx()) / size.width
                        val scale = lerp(1f, maxScale, progress)
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedRectangle(cornerRadius) },
                        effects = {
                            vibrancy()
                            blur(2f.dp.toPx())
                            lens(16f.dp.toPx(), 32f.dp.toPx())
                        },
                        layerBlock = {
                            val progress = searchProgressAnimation.value
                            val maxScale = (size.width + 4f.dp.toPx()) / size.width
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
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        GlassSlider(
            backdrop = backdrop,
            contentBackdrop = navBarContentBackdrop,
            mainItemCount = mainNavItems.size,
            selectedIndex = selectedIndex,
            dragOffsetX = dragOffsetX,
            navBarHeight = navBarHeight,
            mainBarProgress = mainBarProgressAnimation.value,
            horizontalPadding = horizontalPadding,
            mainSearchGap = mainSearchGap,
            searchButtonWidth = searchButtonWidth,
            cornerRadius = cornerRadius,
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
