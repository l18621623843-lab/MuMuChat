package com.kk.mumuchat.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private const val CORRECT_CODE = "11111"

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    val context = LocalContext.current
    // 0 = mode selection, 1 = phone registration, 2 = fingerprint
    var currentStep by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1E88E5))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (currentStep) {
            0 -> ModeSelectionContent(
                onPhoneMode = { currentStep = 1 },
                onFingerprintMode = { currentStep = 2 }
            )
            1 -> PhoneRegistrationContent(
                onBack = { currentStep = 0 },
                onSuccess = onRegisterSuccess
            )
            2 -> FingerprintContent(
                onBack = { currentStep = 0 },
                onSuccess = onRegisterSuccess
            )
        }
    }
}

@Composable
private fun ModeSelectionContent(
    onPhoneMode: () -> Unit,
    onFingerprintMode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "创建账户",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "选择你的注册方式",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(48.dp))

        ModeCard(
            icon = Icons.Filled.Phone,
            title = "手机号注册",
            description = "使用手机号和验证码创建账户",
            onClick = onPhoneMode
        )
        Spacer(Modifier.height(16.dp))
        ModeCard(
            icon = Icons.Filled.Fingerprint,
            title = "私密模式",
            description = "使用指纹特征码，无需手机号",
            onClick = onFingerprintMode
        )
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = Color.White.copy(alpha = 0.65f))
        }
    }
}


@Composable
private fun PhoneRegistrationContent(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("手机号注册", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(32.dp))

        // Phone number field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 11) phoneNumber = it.filter { c -> c.isDigit() } },
            label = { Text("手机号") },
            placeholder = { Text("请输入11位手机号") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                cursorColor = Color.White,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Send code button
        AnimatedVisibility(!codeSent) {
            Button(
                onClick = {
                    if (phoneNumber.length == 11) {
                        codeSent = true
                        errorMsg = ""
                        Toast.makeText(context, "验证码已发送", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMsg = "请输入正确的11位手机号"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1565C0)
                )
            ) {
                Text("发送验证码", fontWeight = FontWeight.SemiBold)
            }
        }


        // Verification code input (shown after sending)
        AnimatedVisibility(
            visible = codeSent,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { if (it.length <= 5) verificationCode = it.filter { c -> c.isDigit() } },
                    label = { Text("验证码") },
                    placeholder = { Text("请输入5位验证码") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color.White,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.4f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (verificationCode == CORRECT_CODE) {
                            onSuccess()
                        } else {
                            errorMsg = "验证码错误，请输入 11111"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1565C0)
                    )
                ) {
                    Text("注册", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (errorMsg.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(errorMsg, color = Color(0xFFFF8A80), fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) {
            Text("← 返回选择", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun FingerprintContent(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("点击下方按钮进行指纹验证") }
    var isError by remember { mutableStateOf(false) }

    fun launchBiometric() {
        val activity = context as? FragmentActivity
        if (activity == null) {
            statusText = "当前环境不支持指纹验证"
            isError = true
            return
        }

        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            statusText = when (canAuth) {
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "设备不支持指纹识别"
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "指纹硬件暂不可用"
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "未录入指纹，请先在系统设置中添加"
                else -> "指纹验证不可用"
            }
            isError = true
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                statusText = "验证失败: $errString"
                isError = true
            }
            override fun onAuthenticationFailed() {
                statusText = "指纹不匹配，请重试"
                isError = true
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("MuMuChat 私密模式")
            .setSubtitle("使用指纹验证身份")
            .setNegativeButtonText("取消")
            .build()
        prompt.authenticate(promptInfo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("私密模式", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(12.dp))
        Text(
            "通过指纹特征码创建私密账户\n无需手机号，更加安全",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(40.dp))

        // Fingerprint icon button
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .clickable {
                    isError = false
                    statusText = "验证中..."
                    launchBiometric()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Fingerprint,
                contentDescription = "指纹验证",
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            statusText,
            fontSize = 14.sp,
            color = if (isError) Color(0xFFFF8A80) else Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onBack) {
            Text("← 返回选择", color = Color.White.copy(alpha = 0.7f))
        }
    }
}
