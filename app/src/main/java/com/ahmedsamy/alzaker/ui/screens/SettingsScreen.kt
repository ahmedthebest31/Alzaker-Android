package com.ahmedsamy.alzaker.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedsamy.alzaker.ui.AppViewModel
import com.ahmedsamy.alzaker.ui.components.ActionButton
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.components.AppToast
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.AlzakerThemes
import com.ahmedsamy.alzaker.ui.theme.Gold
import com.ahmedsamy.alzaker.ui.theme.LegacyColors
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors
import com.ahmedsamy.alzaker.util.Battery
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics
import com.ahmedsamy.alzaker.util.IntervalValidation
import com.ahmedsamy.alzaker.util.Permissions
import com.ahmedsamy.alzaker.util.Share
import com.ahmedsamy.alzaker.util.TimeFormat
import com.ahmedsamy.alzaker.util.TimeFormat.Period
import kotlinx.coroutines.delay

// --- App and Donation Links (legacy settings.tsx constants) -----------------
private const val ANDROID_STORE_URL = "https://play.google.com/store/apps/details?id=com.ahmedsamy.alzaker"
private const val PAYPAL_URL = "https://www.paypal.com/paypalme/ahmedthebest31"
private const val INSTAPAY_URL = "https://ipn.eg/S/ahmedthebest/instapay/63TO4s"
private const val DEFAULT_FONT_SIZE_MULTIPLIER = 1.0f
private const val MIN_AUDIO_INTERVAL = IntervalValidation.MIN_AUDIO_INTERVAL
private const val HIKMAH_RESET_INTERVAL = 60

// The legacy screen requested the notification permission exactly once per app
// lifetime (module-level `permissionsRequested`); mirrored with this flag.
private var notificationPermissionRequested = false

private enum class SettingsDialog {
    IntervalInvalid,
    AudioIntervalInvalid,
    AudioInfo,
    QuietTimeInvalid,
    BatteryOptimization,
    NotificationPermission,
    ExactAlarm,
    Donate,
}

/**
 * Settings tab mirroring the legacy app/(tabs)/settings.tsx: theme selector
 * (five circles), font-size slider with reset, audible tadhkir reminder
 * (>= 15 min) and silent hikmah reminder (>= 1 min) toggles with interval
 * inputs, haptics toggle, quiet hours (12h inputs persisted as 24h), and the
 * about section (share / rate / donate). Every label, hint, toast and alert
 * string matches the legacy copy exactly.
 */
@Composable
fun SettingsScreen(
    themeName: ThemeName,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier,
) {
    val theme = themeColors(themeName)
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    val context = LocalContext.current
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val hapticsEnabled = settings.hapticsEnabled

    var toastMessage by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    var tadhkirInterval by remember { mutableStateOf(settings.tadhkirIntervalMinutes.toString()) }
    var hikmahInterval by remember { mutableStateOf(settings.hikmahIntervalMinutes.toString()) }
    var quietStartHour by remember { mutableStateOf(TimeFormat.to12Hour(settings.quietStart).hour) }
    var quietStartMinute by remember { mutableStateOf(TimeFormat.to12Hour(settings.quietStart).minute) }
    var quietStartPeriod by remember { mutableStateOf(TimeFormat.to12Hour(settings.quietStart).period) }
    var quietEndHour by remember { mutableStateOf(TimeFormat.to12Hour(settings.quietEnd).hour) }
    var quietEndMinute by remember { mutableStateOf(TimeFormat.to12Hour(settings.quietEnd).minute) }
    var quietEndPeriod by remember { mutableStateOf(TimeFormat.to12Hour(settings.quietEnd).period) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            toastMessage = null
        }
    }

    // Keep the local inputs in sync with the persisted settings whenever any
    // setting changes (mirrors the legacy loadState-on-mount behaviour).
    LaunchedEffect(settings) {
        tadhkirInterval = settings.tadhkirIntervalMinutes.toString()
        hikmahInterval = settings.hikmahIntervalMinutes.toString()
        val start = TimeFormat.to12Hour(settings.quietStart)
        quietStartHour = start.hour
        quietStartMinute = start.minute
        quietStartPeriod = start.period
        val end = TimeFormat.to12Hour(settings.quietEnd)
        quietEndHour = end.hour
        quietEndMinute = end.minute
        quietEndPeriod = end.period
    }

    val showToast: (String) -> Unit = { toastMessage = it }
    val closeDialog = { dialog = null }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) dialog = SettingsDialog.NotificationPermission
    }

    LaunchedEffect(Unit) {
        if (!notificationPermissionRequested) {
            notificationPermissionRequested = true
            if (!Permissions.isNotificationGranted(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val openUrl: (String, String) -> Unit = { url, failureMessage ->
        val opened = runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            true
        }.getOrDefault(false)
        if (!opened) showToast(failureMessage)
    }

    val handleTadhkirToggle: (Boolean) -> Unit = { value ->
        if (value && !IntervalValidation.isAudioIntervalValid(tadhkirInterval)) {
            dialog = SettingsDialog.AudioIntervalInvalid
            tadhkirInterval = MIN_AUDIO_INTERVAL.toString()
        } else {
            val minutes = tadhkirInterval.toIntOrNull() ?: MIN_AUDIO_INTERVAL
            appViewModel.setTadhkirEnabled(value, minutes)
            if (value) {
                if (Permissions.canScheduleExactAlarms(context)) {
                    dialog = SettingsDialog.BatteryOptimization
                } else {
                    dialog = SettingsDialog.ExactAlarm
                }
                showToast("تم تفعيل التذكير المسموع كل $tadhkirInterval دقيقة.")
            } else {
                showToast("تم تعطيل التذكير المسموع.")
            }
        }
    }

    val handleHikmahToggle: (Boolean) -> Unit = { value ->
        if (value && !IntervalValidation.isValidHikmahInterval(hikmahInterval)) {
            dialog = SettingsDialog.IntervalInvalid
        } else {
            val minutes = hikmahInterval.toIntOrNull() ?: 1
            appViewModel.setHikmahEnabled(value, minutes)
            if (value) {
                if (Permissions.canScheduleExactAlarms(context)) {
                    showToast("تم تفعيل تذكير الحكمة كل $hikmahInterval دقيقة.")
                } else {
                    dialog = SettingsDialog.ExactAlarm
                    showToast("تم تفعيل تذكير الحكمة كل $hikmahInterval دقيقة.")
                }
            } else {
                showToast("تم تعطيل تذكير الحكمة.")
            }
        }
    }

    val validateTadhkirInterval: () -> Unit = {
        if (!IntervalValidation.isAudioIntervalValid(tadhkirInterval)) {
            dialog = SettingsDialog.AudioIntervalInvalid
            tadhkirInterval = MIN_AUDIO_INTERVAL.toString()
            if (settings.isTadhkirEnabled) {
                appViewModel.setTadhkirIntervalMinutes(MIN_AUDIO_INTERVAL)
                showToast("تم ضبط الفاصل الزمني إلى $MIN_AUDIO_INTERVAL دقيقة.")
            }
        } else {
            val minutes = tadhkirInterval.toIntOrNull() ?: MIN_AUDIO_INTERVAL
            appViewModel.setTadhkirIntervalMinutes(minutes)
            if (settings.isTadhkirEnabled) {
                showToast("تم تحديث الفاصل الزمني للتذكير المسموع.")
            }
        }
    }

    val validateHikmahInterval: () -> Unit = {
        if (!IntervalValidation.isValidHikmahInterval(hikmahInterval)) {
            dialog = SettingsDialog.IntervalInvalid
            hikmahInterval = HIKMAH_RESET_INTERVAL.toString()
            if (settings.isHikmahEnabled) {
                appViewModel.setHikmahIntervalMinutes(HIKMAH_RESET_INTERVAL)
                showToast("تم ضبط الفاصل الزمني إلى $HIKMAH_RESET_INTERVAL دقيقة.")
            }
        } else {
            val minutes = hikmahInterval.toIntOrNull() ?: 1
            appViewModel.setHikmahIntervalMinutes(minutes)
            if (settings.isHikmahEnabled) {
                showToast("تم تحديث الفاصل الزمني لتذكير الحكمة.")
            }
        }
    }

    val handleHapticsToggle: (Boolean) -> Unit = { value ->
        appViewModel.setHapticsEnabled(value)
        if (value) {
            Haptics.trigger(context, HapticFeedbackType.NotificationSuccess, enabled = true)
        }
        showToast(if (value) "تم تفعيل الاهتزاز." else "تم تعطيل الاهتزاز.")
    }

    val handleQuietHoursToggle: (Boolean) -> Unit = { value ->
        appViewModel.setQuietHoursEnabled(value)
        showToast(if (value) "تم تفعيل وضع عدم الإزعاج." else "تم تعطيل وضع عدم الإزعاج.")
    }

    val saveQuietHours: () -> Unit = {
        val startValid = TimeFormat.isValidTime12(quietStartHour, quietStartMinute)
        val endValid = TimeFormat.isValidTime12(quietEndHour, quietEndMinute)
        val start24 = TimeFormat.to24Hour(quietStartHour, quietStartMinute, quietStartPeriod)
        val end24 = TimeFormat.to24Hour(quietEndHour, quietEndMinute, quietEndPeriod)
        if (!startValid || !endValid || start24 == null || end24 == null) {
            dialog = SettingsDialog.QuietTimeInvalid
        } else {
            appViewModel.setQuietStart(start24)
            appViewModel.setQuietEnd(end24)
        }
    }

    val toggleStartPeriod: () -> Unit = {
        quietStartPeriod = if (quietStartPeriod == Period.AM) Period.PM else Period.AM
        saveQuietHours()
    }

    val toggleEndPeriod: () -> Unit = {
        quietEndPeriod = if (quietEndPeriod == Period.AM) Period.PM else Period.AM
        saveQuietHours()
    }

    val handleResetFontSize: () -> Unit = {
        appViewModel.setFontSizeMultiplier(DEFAULT_FONT_SIZE_MULTIPLIER)
        Haptics.trigger(context, HapticFeedbackType.NotificationSuccess, hapticsEnabled)
        showToast("تم إعادة حجم الخط للوضع الافتراضي.")
    }

    val handleThemeSelect: (ThemeName) -> Unit = { selected ->
        Haptics.trigger(context, HapticFeedbackType.ImpactLight, hapticsEnabled)
        appViewModel.setThemeName(selected.key)
    }

    val shareApp: () -> Unit = {
        val message = "✨ تطبيق \"الذاكر\" يساعدك على ذكر الله، حمله الآن! ✨\n$ANDROID_STORE_URL"
        val opened = Share.shareText(context, message)
        if (!opened) showToast("فشل في مشاركة التطبيق.")
    }

    val rateApp: () -> Unit = { openUrl(ANDROID_STORE_URL, "فشل في فتح متجر التطبيقات.") }
    val donatePayPal: () -> Unit = { openUrl(PAYPAL_URL, "فشل فتح رابط باي بال.") }
    val donateInstaPay: () -> Unit = { openUrl(INSTAPAY_URL, "فشل فتح رابط انستا باي.") }

    AppBackground(colors = themeColors(themeName), modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .padding(top = 50.dp)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "الإعدادات",
                color = Color.White,
                fontSize = (32 * fontSizeMultiplier).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFontFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
            )

            SettingsSection(
                title = "مظهر التطبيق",
                fontSizeMultiplier = fontSizeMultiplier,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ThemeName.entries.forEach { name ->
                        val colors = AlzakerThemes.getValue(name)
                        val selected = name == themeName
                        val borderColor = when {
                            name == ThemeName.HIGH_CONTRAST -> Gold
                            selected -> Color.White
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                                .border(width = 2.dp, color = borderColor, shape = CircleShape)
                                .then(
                                    if (selected) Modifier.graphicsLayer { scaleX = 1.1f; scaleY = 1.1f }
                                    else Modifier,
                                )
                                .clickable { handleThemeSelect(name) }
                                .semantics {
                                    contentDescription = themeAccessibilityLabel(name)
                                    role = Role.Button
                                    this.selected = selected
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = if (name == ThemeName.HIGH_CONTRAST) Gold else Color.White,
                                )
                            }
                        }
                    }
                }
            }

            SettingsSection(
                title = "حجم خط التطبيق",
                fontSizeMultiplier = fontSizeMultiplier,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "صغير",
                        color = Color.White,
                        fontSize = (14 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                    )
                    Slider(
                        value = settings.fontSizeMultiplier,
                        onValueChange = appViewModel::setFontSizeMultiplier,
                        valueRange = 0.8f..1.5f,
                        steps = 6,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                            .semantics { contentDescription = "حجم خط التطبيق" },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Gold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                        ),
                    )
                    Text(
                        text = "كبير",
                        color = Color.White,
                        fontSize = (20 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(25.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { handleResetFontSize() }
                        .padding(vertical = 10.dp, horizontal = 20.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "إعادة ضبط حجم الخط"
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "إعادة ضبط للحجم الافتراضي",
                        color = Color.White,
                        fontSize = (16 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                    )
                }
            }

            SettingsSection(
                title = "تذكير مسموع (تذكير)",
                fontSizeMultiplier = fontSizeMultiplier,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                SettingRow(
                    label = "تفعيل التذكير المسموع",
                    description = "إشعار دوري مع صوت مخصص لتذكيرك بالذكر.",
                    value = settings.isTadhkirEnabled,
                    onValueChange = handleTadhkirToggle,
                    accessibilityHint = "يسمح لك بتفعيل أو تعطيل التذكيرات الصوتية الدورية.",
                    fontSizeMultiplier = fontSizeMultiplier,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تنبيه كل:",
                                color = Color.White,
                                fontSize = (18 * fontSizeMultiplier).sp,
                                fontFamily = AmiriFontFamily,
                            )
                            StyledTextField(
                                value = tadhkirInterval,
                                onValueChange = { tadhkirInterval = it },
                                onEditingDone = validateTadhkirInterval,
                                fontSizeMultiplier = fontSizeMultiplier,
                                contentDescription = "الفاصل الزمني بالدقائق للتذكير المسموع",
                                modifier = Modifier.padding(start = 12.dp),
                            )
                            Text(
                                text = "دقيقة",
                                color = Color.White,
                                fontSize = (16 * fontSizeMultiplier).sp,
                                fontFamily = AmiriFontFamily,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clickable { dialog = SettingsDialog.AudioInfo }
                                    .padding(4.dp)
                                    .semantics {
                                        contentDescription = "لماذا الحد الأدنى 15 دقيقة؟"
                                        role = Role.Button
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
            }

            SettingsSection(
                title = "إشعارات التذكيرات المقروءة",
                fontSizeMultiplier = fontSizeMultiplier,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                SettingRow(
                    label = "تفعيل تذكير الحكمة",
                    description = "إشعار يحتوي على ذكر أو حكمة عشوائية.",
                    value = settings.isHikmahEnabled,
                    onValueChange = handleHikmahToggle,
                    accessibilityHint = "يسمح لك بتفعيل أو تعطيل إشعارات الحكم والأذكار.",
                    fontSizeMultiplier = fontSizeMultiplier,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f)),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "تنبيه كل:",
                            color = Color.White,
                            fontSize = (18 * fontSizeMultiplier).sp,
                            fontFamily = AmiriFontFamily,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StyledTextField(
                                value = hikmahInterval,
                                onValueChange = { hikmahInterval = it },
                                onEditingDone = validateHikmahInterval,
                                fontSizeMultiplier = fontSizeMultiplier,
                                contentDescription = "الفاصل الزمني بالدقائق لتذكير الحكمة",
                            )
                            Text(
                                text = "دقيقة",
                                color = Color.White,
                                fontSize = (16 * fontSizeMultiplier).sp,
                                fontFamily = AmiriFontFamily,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }

            SettingsSection(
                title = "الاهتزاز",
                fontSizeMultiplier = fontSizeMultiplier,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                SettingRow(
                    label = "تفعيل الاهتزاز",
                    description = "تفعيل الاهتزازات عند الضغط على الأزرار والمسبحة.",
                    value = hapticsEnabled,
                    onValueChange = handleHapticsToggle,
                    accessibilityHint = "يتحكم في الاهتزازات التفاعلية داخل التطبيق.",
                    fontSizeMultiplier = fontSizeMultiplier,
                )
            }

            SettingsSection(
                title = "وضع عدم الإزعاج",
                fontSizeMultiplier = fontSizeMultiplier,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                SettingRow(
                    label = "تفعيل وضع عدم الإزعاج",
                    description = "إيقاف التذكيرات الصوتية خلال فترة محددة.",
                    value = settings.quietHoursEnabled,
                    onValueChange = handleQuietHoursToggle,
                    accessibilityHint = "عند التفعيل، لن يُشغَّل أي صوت خلال النافذة الزمنية المحددة.",
                    fontSizeMultiplier = fontSizeMultiplier,
                )
                if (settings.quietHoursEnabled) {
                    Column(modifier = Modifier.padding(top = 14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.12f)),
                        )
                        QuietTimeRow(
                            label = "وقت البدء",
                            hour = quietStartHour,
                            minute = quietStartMinute,
                            period = quietStartPeriod,
                            onHourChange = { quietStartHour = it },
                            onMinuteChange = { quietStartMinute = it },
                            onPeriodToggle = toggleStartPeriod,
                            hourLabel = "ساعة البدء",
                            minuteLabel = "دقيقة البدء",
                            onEditingDone = saveQuietHours,
                            fontSizeMultiplier = fontSizeMultiplier,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(vertical = 4.dp),
                        )
                        QuietTimeRow(
                            label = "وقت الانتهاء",
                            hour = quietEndHour,
                            minute = quietEndMinute,
                            period = quietEndPeriod,
                            onHourChange = { quietEndHour = it },
                            onMinuteChange = { quietEndMinute = it },
                            onPeriodToggle = toggleEndPeriod,
                            hourLabel = "ساعة الانتهاء",
                            minuteLabel = "دقيقة الانتهاء",
                            onEditingDone = saveQuietHours,
                            fontSizeMultiplier = fontSizeMultiplier,
                        )
                        Text(
                            text = "يدعم النوافذ التي تتجاوز منتصف الليل (مثال: 10 مساءً حتى 6 صباحاً).",
                            color = LegacyColors.LightGray.copy(alpha = 0.8f),
                            fontSize = (13 * fontSizeMultiplier).sp,
                            fontFamily = AmiriFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                        )
                    }
                }
            }

            SettingsSection(
                title = "عن التطبيق",
                fontSizeMultiplier = fontSizeMultiplier,
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionButton(
                        icon = Icons.Filled.Share,
                        contentDescription = "مشاركة التطبيق",
                        onClick = shareApp,
                        hapticsEnabled = hapticsEnabled,
                    )
                    ActionButton(
                        icon = Icons.Filled.Star,
                        contentDescription = "تقييم التطبيق",
                        onClick = rateApp,
                        hapticsEnabled = hapticsEnabled,
                    )
                    ActionButton(
                        icon = Icons.Filled.VolunteerActivism,
                        contentDescription = "دعم التطبيق",
                        onClick = { dialog = SettingsDialog.Donate },
                        hapticsEnabled = hapticsEnabled,
                    )
                }
            }
        }

        AppToast(message = toastMessage.orEmpty(), visible = toastMessage != null)
    }

    when (dialog) {
        null -> Unit

        SettingsDialog.IntervalInvalid -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("قيمة غير صالحة") },
            text = { Text("الحد الأدنى للفاصل الزمني للتذكير هو دقيقة واحدة (1).") },
            confirmButton = {
                TextButton(onClick = closeDialog) { Text("حسنًا") }
            },
        )

        SettingsDialog.AudioIntervalInvalid -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("فاصل زمني غير مدعوم") },
            text = {
                Text(
                    "قيود نظام Android تمنع تشغيل الصوت في الخلفية لأقل من $MIN_AUDIO_INTERVAL دقيقة.\n\n" +
                        "يرجى إدخال $MIN_AUDIO_INTERVAL دقيقة أو أكثر.",
                )
            },
            confirmButton = {
                TextButton(onClick = closeDialog) { Text("حسنًا") }
            },
        )

        SettingsDialog.AudioInfo -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("لماذا 15 دقيقة؟") },
            text = {
                Text(
                    "قيود نظام Android (WorkManager) تمنع تشغيل أي كود في الخلفية لأقل من $MIN_AUDIO_INTERVAL دقيقة.\n\n" +
                        "هذا حد يفرضه النظام نفسه لتوفير البطارية، وليس من التطبيق.\n\n" +
                        "لن يتم احترام أي قيمة أقل من $MIN_AUDIO_INTERVAL دقيقة، وسيعمل التذكير كل $MIN_AUDIO_INTERVAL دقيقة على الأقل بغض النظر عن القيمة التي تدخلها.",
                )
            },
            confirmButton = {
                TextButton(onClick = closeDialog) { Text("حسنًا") }
            },
        )

        SettingsDialog.QuietTimeInvalid -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("وقت غير صالح") },
            text = { Text("يرجى إدخال وقت صحيح.") },
            confirmButton = {
                TextButton(onClick = closeDialog) { Text("حسنًا") }
            },
        )

        SettingsDialog.BatteryOptimization -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("إعدادات البطارية (ضروري للخلفية)") },
            text = {
                Text(
                    "قد تمنع بعض الهواتف تشغيل الصوت في الخلفية (خاصة إذا كان الهاتف مقفولاً).\n\n" +
                        "لضمان عمل التذكير الصوتي في الخلفية:\n\n" +
                        "1. اذهب إلى إعدادات البطارية\n" +
                        "2. اختر \"إدارة طاقة التطبيقات\"\n" +
                        "3. ابحث عن تطبيق \"الذاكر\"\n" +
                        "4. اختر \"بدون تحسين\" أو \"عدم الإيقاف\"\n\n" +
                        "سيؤدي ذلك إلى منع النظام من إيقاف التطبيق في الخلفية.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    Battery.openAppDetailsSettings(context)
                    closeDialog()
                }) { Text("فتح إعدادات البطارية") }
            },
            dismissButton = {
                TextButton(onClick = closeDialog) { Text("حسنًا") }
            },
        )

        SettingsDialog.NotificationPermission -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("إذن الإشعارات مطلوب") },
            text = {
                Text("يحتاج التطبيق إلى إذن الإشعارات لإرسال التذكيرات الصوتية. يرجى تفعيل الإشعارات من إعدادات الجهاز.")
            },
            confirmButton = {
                TextButton(onClick = {
                    Permissions.openNotificationSettings(context)
                    closeDialog()
                }) { Text("فتح الإعدادات") }
            },
            dismissButton = {
                TextButton(onClick = closeDialog) { Text("إلغاء") }
            },
        )

        SettingsDialog.ExactAlarm -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("المنبهات الدقيقة مطلوبة") },
            text = {
                Text(
                    "يسمح النظام على هذا الجهاز للمنبهات الدقيقة فقط بتشغيل التذكير الصوتي في موعده عند إغلاق التطبيق.\n\n" +
                        "منح الإذن يضمن وصول تذكيرك في اللحظة المحددة بدقة.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    Permissions.openExactAlarmSettings(context)
                    closeDialog()
                }) { Text("فتح الإعدادات") }
            },
            dismissButton = {
                TextButton(onClick = closeDialog) { Text("لاحقًا") }
            },
        )

        SettingsDialog.Donate -> AlertDialog(
            onDismissRequest = closeDialog,
            title = { Text("دعم التطبيق") },
            text = { Text("إذا أعجبك التطبيق، يمكنك دعم المطور عبر إحدى الطرق التالية. شكرًا لك!") },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        donatePayPal()
                        closeDialog()
                    }) { Text("PayPal") }
                    TextButton(onClick = {
                        donateInstaPay()
                        closeDialog()
                    }) { Text("InstaPay") }
                }
            },
            dismissButton = {
                TextButton(onClick = closeDialog) { Text("إلغاء") }
            },
        )
    }
}

/** Rounded translucent card holding one settings section, legacy rgba(255,255,255,0.1) / radius 15. */
@Composable
private fun SettingsSection(
    title: String,
    fontSizeMultiplier: Float,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(15.dp))
            .padding(20.dp),
    ) {
        Text(
            text = title,
            color = Gold,
            fontSize = (22 * fontSizeMultiplier).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFontFamily,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
        )
        content()
    }
}

/** One toggle row: the whole row is a switch button with the label right in RTL. */
@Composable
private fun SettingRow(
    label: String,
    description: String?,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    accessibilityHint: String?,
    fontSizeMultiplier: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { onValueChange(!value) }
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                contentDescription = label
                if (accessibilityHint != null) stateDescription = accessibilityHint
                toggleableState = ToggleableState(value)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = (18 * fontSizeMultiplier).sp,
                fontFamily = AmiriFontFamily,
                textAlign = TextAlign.End,
            )
            if (description != null) {
                Text(
                    text = description,
                    color = LegacyColors.LightGray.copy(alpha = 0.8f),
                    fontSize = (14 * fontSizeMultiplier).sp,
                    fontFamily = AmiriFontFamily,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Switch(
            checked = value,
            onCheckedChange = onValueChange,
            modifier = Modifier.clearAndSetSemantics {},
            colors = SwitchDefaults.colors(
                checkedThumbColor = LegacyColors.OffWhite,
                uncheckedThumbColor = LegacyColors.OffWhite,
                checkedTrackColor = LegacyColors.LightBlue,
                uncheckedTrackColor = LegacyColors.MediumGray,
            ),
        )
    }
}

/** Numeric input styled like the legacy TextInput (translucent box, centered text). */
@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onEditingDone: () -> Unit,
    fontSizeMultiplier: Float,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    maxLength: Int = Int.MAX_VALUE,
    width: Dp = 70.dp,
    radius: Dp = 8.dp,
    backgroundAlpha: Float = 0.2f,
    bordered: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .width(width)
            .background(Color.White.copy(alpha = backgroundAlpha), shape)
            .then(
                if (bordered) Modifier.border(width = 1.dp, color = Color.White.copy(alpha = 0.25f), shape = shape)
                else Modifier,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            )
            .onFocusChanged { if (!it.isFocused) onEditingDone() },
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = value,
            onValueChange = { new -> if (new.length <= maxLength && new.all(Char::isDigit)) onValueChange(new) },
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = (18 * fontSizeMultiplier).sp,
                fontFamily = AmiriFontFamily,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** One quiet-hours row: label, hour:minute inputs and the AM/PM period button. */
@Composable
private fun QuietTimeRow(
    label: String,
    hour: String,
    minute: String,
    period: Period,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onPeriodToggle: () -> Unit,
    hourLabel: String,
    minuteLabel: String,
    onEditingDone: () -> Unit,
    fontSizeMultiplier: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = (16 * fontSizeMultiplier).sp,
            fontFamily = AmiriFontFamily,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            StyledTextField(
                value = hour,
                onValueChange = onHourChange,
                onEditingDone = onEditingDone,
                fontSizeMultiplier = fontSizeMultiplier,
                contentDescription = hourLabel,
                maxLength = 2,
                width = 50.dp,
                radius = 10.dp,
                backgroundAlpha = 0.18f,
                bordered = true,
            )
            Text(
                text = ":",
                color = Color.White,
                fontSize = 20.sp,
                fontFamily = AmiriFontFamily,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            StyledTextField(
                value = minute,
                onValueChange = onMinuteChange,
                onEditingDone = onEditingDone,
                fontSizeMultiplier = fontSizeMultiplier,
                contentDescription = minuteLabel,
                maxLength = 2,
                width = 50.dp,
                radius = 10.dp,
                backgroundAlpha = 0.18f,
                bordered = true,
            )
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(8.dp))
                    .clickable { onPeriodToggle() }
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .semantics {
                        contentDescription = if (period == Period.AM) "صباحاً" else "مساءً"
                        role = Role.Button
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (period == Period.AM) "صباحاً" else "مساءً",
                    color = Color.White,
                    fontSize = (16 * fontSizeMultiplier).sp,
                    fontFamily = AmiriFontFamily,
                )
            }
        }
    }
}

/** Screen-reader labels for the five theme circles, legacy getThemeAccessibilityLabel. */
private fun themeAccessibilityLabel(name: ThemeName): String = when (name) {
    ThemeName.DEFAULT -> "الثيم الافتراضي (أزرق غامق)"
    ThemeName.MIDNIGHT -> "ثيم منتصف الليل (أرجواني غامق)"
    ThemeName.NATURE -> "ثيم الطبيعة (أخضر)"
    ThemeName.ROYAL -> "الثيم الملكي (أزرق فاتح)"
    ThemeName.HIGH_CONTRAST -> "ثيم التباين العالي (أسود وأصفر لضعاف البصر)"
}
