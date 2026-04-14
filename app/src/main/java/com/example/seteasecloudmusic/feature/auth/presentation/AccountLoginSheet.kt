package com.example.seteasecloudmusic.feature.auth.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seteasecloudmusic.feature.auth.usecase.AuthInputValidator
import kotlinx.coroutines.launch

private enum class AccountPanel {
    METHODS,
    CAPTCHA,
    QR
}

@Composable
fun AccountLoginSheetContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var panel by rememberSaveable { mutableStateOf(AccountPanel.METHODS.name) }
    var phone by rememberSaveable { mutableStateOf("") }
    var captcha by rememberSaveable { mutableStateOf("") }
    var helperText by rememberSaveable { mutableStateOf<String?>(null) }
    var qrHint by rememberSaveable { mutableStateOf("等待扫码登录") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showSnack(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    val activePanel = remember(panel) { AccountPanel.valueOf(panel) }
    val red = Color(0xFFFA233B)
    val secondary = Color(0xFF8D8D93)

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 6.dp)
        ) {
            Header(onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedContent(
                targetState = activePanel,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, delayMillis = 30)) togetherWith
                        fadeOut(animationSpec = tween(140))
                },
                label = "accountPanelTransition"
            ) { current ->
                when (current) {
                    AccountPanel.METHODS -> {
                        MethodSelectionPanel(
                            red = red,
                            secondary = secondary,
                            onCaptchaClick = {
                                helperText = null
                                panel = AccountPanel.CAPTCHA.name
                            },
                            onQrClick = {
                                helperText = null
                                panel = AccountPanel.QR.name
                            },
                            onSettingsClick = {
                                showSnack("音乐设置功能开发中")
                            }
                        )
                    }

                    AccountPanel.CAPTCHA -> {
                        CaptchaPanel(
                            red = red,
                            helperText = helperText,
                            phone = phone,
                            captcha = captcha,
                            onPhoneChange = {
                                helperText = null
                                phone = it.filter(Char::isDigit).take(11)
                            },
                            onCaptchaChange = {
                                helperText = null
                                captcha = it.filter(Char::isDigit).take(6)
                            },
                            onBack = {
                                helperText = null
                                panel = AccountPanel.METHODS.name
                            },
                            onSendCaptcha = {
                                val normalizedPhone = AuthInputValidator.normalizePhone(phone)
                                if (!AuthInputValidator.isValidCnPhone(normalizedPhone)) {
                                    helperText = "请输入正确的手机号"
                                    return@CaptchaPanel
                                }
                                showSnack("验证码发送接口待接入")
                            },
                            onLogin = {
                                val normalizedPhone = AuthInputValidator.normalizePhone(phone)
                                val normalizedCaptcha = AuthInputValidator.normalizeCaptcha(captcha)
                                if (!AuthInputValidator.isValidCnPhone(normalizedPhone)) {
                                    helperText = "请输入正确的手机号"
                                    return@CaptchaPanel
                                }
                                if (!AuthInputValidator.isValidCaptcha(normalizedCaptcha)) {
                                    helperText = "验证码必须是 6 位数字"
                                    return@CaptchaPanel
                                }
                                showSnack("验证码登录接口待接入")
                            }
                        )
                    }

                    AccountPanel.QR -> {
                        QrPanel(
                            red = red,
                            secondary = secondary,
                            qrHint = qrHint,
                            onBack = {
                                panel = AccountPanel.METHODS.name
                            },
                            onRefresh = {
                                qrHint = "二维码已刷新（占位）"
                                showSnack("二维码刷新接口待接入")
                            },
                            onSimulateScan = {
                                qrHint = "等待在手机上确认（占位）"
                                showSnack("二维码轮询接口待接入")
                            }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun Header(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE8EC))
                    .border(1.dp, Color(0xFFFFBAC7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Sms,
                    contentDescription = null,
                    tint = Color(0xFFFA233B),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = "Setease 账户",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF121212)
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFF0F0F4), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "关闭",
                tint = Color(0xFF1C1C1E)
            )
        }
    }
}

@Composable
private fun MethodSelectionPanel(
    red: Color,
    secondary: Color,
    onCaptchaClick: () -> Unit,
    onQrClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        LoginMethodRow(
            text = "手机验证码登录...",
            red = red,
            onClick = onCaptchaClick
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 14.dp),
            color = Color(0xFFE8E8ED),
            thickness = 1.dp
        )

        LoginMethodRow(
            text = "二维码登录",
            red = red,
            onClick = onQrClick
        )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
        text = "账户用于同步你的收藏、播放记录和偏好设置。",
        color = secondary,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSettingsClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(0xFFFFE8EC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null,
                    tint = red,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = "音乐设置",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = red
            )
        }
    }
}

@Composable
private fun LoginMethodRow(
    text: String,
    red: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = red,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CaptchaPanel(
    red: Color,
    helperText: String?,
    phone: String,
    captcha: String,
    onPhoneChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onBack: () -> Unit,
    onSendCaptcha: () -> Unit,
    onLogin: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PanelTitle(
            title = "手机验证码登录",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = phone,
                onValueChange = onPhoneChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                placeholder = { Text("请输入手机号") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFE8E8ED),
                thickness = 1.dp
            )

            TextField(
                value = captcha,
                onValueChange = onCaptchaChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("请输入 6 位验证码") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSendCaptcha,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("发送验证码")
                }

                Button(
                    onClick = onLogin,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("登录")
                }
            }
        }

        if (!helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                color = red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun QrPanel(
    red: Color,
    secondary: Color,
    qrHint: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSimulateScan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PanelTitle(
            title = "二维码登录",
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(Color(0xFFF3F3F7), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode2,
                        contentDescription = null,
                        tint = Color(0xFF1F1F21),
                        modifier = Modifier.size(86.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = qrHint,
                    color = secondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("刷新二维码")
                    }

                    Button(
                        onClick = onSimulateScan,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("我已扫码")
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelTitle(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color(0xFF121212)
            )
        }
        Text(
            text = title,
            color = Color(0xFF121212),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
