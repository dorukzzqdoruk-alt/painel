package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GalaxyConstellationCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 3 Functions State - Iniciam desativadas
    var isRecoilReduced by remember { mutableStateOf(false) }
    var isInputLagRemoved by remember { mutableStateOf(false) }
    var isTouchOptimized by remember { mutableStateOf(false) }

    val freeFireDeeplink = "freefire://open"
    var showDeeplinkDispatchedDialog by remember { mutableStateOf(false) }
    var deeplinkStatusTitle by remember { mutableStateOf("Painel Ativado") }
    var deeplinkStatusMessage by remember { mutableStateOf("") }

    fun playFeedbackSound(isToggleOn: Boolean) {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            val toneType = if (isToggleOn) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_PROP_NACK
            toneGen.startTone(toneType, 50)
        } catch (_: Exception) {}
    }

    fun vibrateShort() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    fun launchFreeFireDirectly() {
        playFeedbackSound(true)
        vibrateShort()

        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage("com.dts.freefireth")
            ?: pm.getLaunchIntentForPackage("com.dts.freefiremax")

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            context.startActivity(launchIntent)
            return
        }

        // Se não encontrar o pacote direto, tenta o deeplink
        try {
            val deeplinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(freeFireDeeplink)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(deeplinkIntent)
            return
        } catch (_: Exception) {}

        // Redireciona diretamente para a página do Free Fire na Google Play Store
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.dts.freefireth")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dts.freefireth")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            } catch (_: Exception) {
                deeplinkStatusTitle = "Free Fire"
                deeplinkStatusMessage = "O aplicativo Free Fire não foi encontrado neste dispositivo. Instale o Free Fire no seu aparelho para abrir diretamente."
                showDeeplinkDispatchedDialog = true
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GalaxyConstellationCanvas(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top App Bar / Header: Sasuke Avatar + CAZIEL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xE60A142E))
                    .border(1.dp, Color(0xFF1E3A8A), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF00E5FF), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_user_avatar),
                        contentDescription = "Avatar Sasuke",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CAZIEL",
                        color = Color(0xFF00E5FF),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Card containing the 3 Functions and Launch Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF00E5FF))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF1E3A8A), Color(0xFF0F172A))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF208122B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // ================= FUNÇÃO 1: REDUZIR RECUO =================
                    FunctionCardItem(
                        iconRes = R.drawable.ic_func_recoil,
                        title = "Reduzir Recuo",
                        subtitle = "Estabilização dinâmica de dispersão das armas",
                        isActive = isRecoilReduced,
                        onToggle = {
                            isRecoilReduced = it
                            vibrateShort()
                            playFeedbackSound(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ================= FUNÇÃO 2: RETIRAR INPUT LAG =================
                    FunctionCardItem(
                        iconRes = R.drawable.ic_func_lag,
                        title = "Retirar input lag",
                        subtitle = "Zero delay e resposta instantânea do motor de jogo",
                        isActive = isInputLagRemoved,
                        onToggle = {
                            isInputLagRemoved = it
                            vibrateShort()
                            playFeedbackSound(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ================= FUNÇÃO 3: OTIMIZAR TOUCH =================
                    FunctionCardItem(
                        iconRes = R.drawable.ic_func_touch,
                        title = "Otimizar Touch",
                        subtitle = "Amostragem de 240Hz e suavidade de arraste de mira",
                        isActive = isTouchOptimized,
                        onToggle = {
                            isTouchOptimized = it
                            vibrateShort()
                            playFeedbackSound(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(22.dp))
                    HorizontalDivider(color = Color(0xFF1E3A8A), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(22.dp))

                    // Action Button: Direct Free Fire Launch
                    Button(
                        onClick = { launchFreeFireDirectly() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("launch_free_fire_button")
                            .shadow(14.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF021526)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_free_fire),
                                contentDescription = "Ícone Free Fire",
                                tint = Color(0xFF021526),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ABRIR FREE FIRE DIRETAMENTE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        // Result Dialog
        if (showDeeplinkDispatchedDialog) {
            AlertDialog(
                onDismissRequest = { showDeeplinkDispatchedDialog = false },
                containerColor = Color(0xFF0B1736),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_free_fire),
                            contentDescription = "Status",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = deeplinkStatusTitle,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Text(
                        text = deeplinkStatusMessage,
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showDeeplinkDispatchedDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text(text = "ENTENDI", color = Color(0xFF021526), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun FunctionCardItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF00E5FF).copy(alpha = 0.7f) else Color(0xFF1E3A8A),
        label = "border_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF060F24))
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onToggle(!isActive) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) Color(0xFF0E305C) else Color(0xFF0B172E))
                    .border(1.dp, if (isActive) Color(0xFF00E5FF) else Color(0xFF1E3A8A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = if (isActive) Color(0xFF00E5FF) else Color(0xFF64748B),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = if (isActive) Color(0xFFFFFFFF) else Color(0xFF94A3B8),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Live badge status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActive) Color(0xFF022C22) else Color(0xFF1E293B))
                            .border(0.8.dp, if (isActive) Color(0xFF10B981) else Color(0xFF475569), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) Color(0xFF10B981) else Color(0xFF64748B))
                                    .alpha(if (isActive) dotAlpha else 1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isActive) "ATIVO" else "OFF",
                                color = if (isActive) Color(0xFF34D399) else Color(0xFF94A3B8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    color = if (isActive) Color(0xFF38BDF8) else Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }

        Switch(
            checked = isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF021526),
                checkedTrackColor = Color(0xFF00E5FF),
                uncheckedThumbColor = Color(0xFF94A3B8),
                uncheckedTrackColor = Color(0xFF0B172E),
                uncheckedBorderColor = Color(0xFF1E3A8A)
            )
        )
    }
}
