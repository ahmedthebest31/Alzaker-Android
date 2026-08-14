package com.ahmedsamy.alzaker.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FrontHand
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedsamy.alzaker.ui.components.AppBackground
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.ui.theme.ThemeName
import com.ahmedsamy.alzaker.ui.theme.themeColors
import com.ahmedsamy.alzaker.util.Permissions

/**
 * First-launch wizard mirroring the legacy app/onboarding.tsx: four steps,
 * skip/next navigation, progress dots, and a notification-permission gate on
 * the final step before the app persists the 'hasLaunched' flag and opens the
 * tabs. All copy and accessibility strings match the legacy screen exactly.
 */
@Composable
fun OnboardingScreen(
    themeName: ThemeName,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = themeColors(themeName)
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    val context = LocalContext.current

    var step by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val totalSteps = STEPS.size
    val isLastStep = step == totalSteps - 1

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            onCompleteOnboarding()
        } else {
            isLoading = false
            showPermissionDialog = true
        }
    }

    fun completeOrRequestPermission() {
        if (Permissions.isNotificationGranted(context)) {
            onCompleteOnboarding()
        } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    AppBackground(colors = theme, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isLastStep) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 8.dp, end = 8.dp),
                ) {
                    Text(
                        text = "تخطي",
                        fontSize = (15 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = onCompleteOnboarding,
                            )
                            .padding(8.dp)
                            .semantics {
                                contentDescription = "تخطي مقدمة التطبيق"
                                stateDescription = "ينقلك مباشرةً إلى الشاشة الرئيسية"
                                role = Role.Button
                            },
                    )
                }
            }

            Text(
                text = "${step + 1} / $totalSteps",
                fontSize = (13 * fontSizeMultiplier).sp,
                fontFamily = AmiriFontFamily,
                letterSpacing = 1.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .semantics {
                        contentDescription = "الخطوة ${step + 1} من $totalSteps"
                    },
            )

            Crossfade(
                targetState = step,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                animationSpec = tween(durationMillis = 240),
                label = "onboardingStep",
            ) { currentStep ->
                StepContent(step = STEPS[currentStep], fontSizeMultiplier = fontSizeMultiplier)
            }

            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "مؤشر التقدم: الخطوة ${step + 1} من $totalSteps"
                        liveRegion = LiveRegionMode.Polite
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (index == step) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (index == step) Color.White else Color.White.copy(alpha = 0.35f))
                            .clearAndSetSemantics {},
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(min = 200.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(50.dp), clip = false)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable(enabled = !isLoading) {
                            if (isLastStep) {
                                isLoading = true
                                completeOrRequestPermission()
                            } else {
                                step += 1
                            }
                        }
                        .padding(vertical = 16.dp, horizontal = 40.dp)
                        .semantics {
                            contentDescription =
                                if (isLastStep) "ابدأ رحلتك الإيمانية" else "التالي"
                            stateDescription =
                                if (isLastStep) {
                                    "يطلب الأذونات الضرورية ثم ينقلك إلى التطبيق"
                                } else {
                                    "ينتقل إلى الخطوة ${step + 2} من $totalSteps"
                                }
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = when {
                            isLoading -> "جارٍ التحضير…"
                            isLastStep -> "ابدأ رحلتك الإيمانية"
                            else -> "التالي"
                        },
                        fontSize = (18 * fontSizeMultiplier).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFontFamily,
                        color = theme.primary,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("إذن الإشعارات مطلوب") },
            text = {
                Text(
                    "يحتاج التطبيق إلى إذن الإشعارات لإرسال التذكيرات الصوتية. " +
                        "يرجى تفعيل الإشعارات من إعدادات الجهاز.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        Permissions.openNotificationSettings(context)
                    },
                ) {
                    Text("فتح الإعدادات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("إلغاء") }
            },
        )
    }
}

/**
 * One onboarding step's icon circle, title, description and optional tip. The
 * whole area is a single polite live region whose label merges the legacy
 * `title. description[. tip]` announcement; every inner element is hidden from
 * accessibility so TalkBack announces exactly the merged label.
 */
@Composable
private fun StepContent(step: OnboardingStep, fontSizeMultiplier: Float) {
    val mergedLabel = buildString {
        append(step.title).append(". ").append(step.description)
        step.tip?.let { append(". ").append(it) }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) {
                contentDescription = mergedLabel
                liveRegion = LiveRegionMode.Polite
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(96.dp),
            )
        }
        Text(
            text = step.title,
            fontSize = (28 * fontSizeMultiplier).sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFontFamily,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 36.dp, bottom = 18.dp)
                .clearAndSetSemantics {},
        )
        Text(
            text = step.description,
            fontSize = (17 * fontSizeMultiplier).sp,
            lineHeight = 30.sp,
            fontFamily = AmiriFontFamily,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clearAndSetSemantics {},
        )
        step.tip?.let {
            Text(
                text = it,
                fontSize = (14 * fontSizeMultiplier).sp,
                fontFamily = AmiriFontFamily,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clearAndSetSemantics {},
            )
        }
    }
}

/**
 * The four onboarding steps with the exact legacy strings (app/onboarding.tsx).
 * Material icon substitutions for the legacy Ionicons glyphs:
 * 'sparkles-outline' -> AutoAwesome, 'book-outline' -> AutoMirrored MenuBook,
 * 'hand-right-outline' -> FrontHand, 'notifications-circle-outline' ->
 * NotificationsActive (all verified present in material-icons-extended 1.7.8).
 */
private data class OnboardingStep(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val tip: String? = null,
)

private val STEPS = listOf(
    OnboardingStep(
        icon = Icons.Outlined.AutoAwesome,
        title = "مرحباً بك في الذاكر",
        description = "رفيقك اليومي ليساعدك على ذكر الله والبقاء على اتصال دائم بعبادتك، بطريقة ذكية وعصرية.",
        tip = "اضغط \"التالي\" للانتقال إلى الخطوة القادمة.",
    ),
    OnboardingStep(
        icon = Icons.AutoMirrored.Outlined.MenuBook,
        title = "مكتبة أذكار شاملة",
        description = "تصفّح مئات الأذكار والأدعية الصحيحة مُصنَّفةً حسب المناسبة، مع عداد لكل ذكر لمتابعة تقدّمك.",
    ),
    OnboardingStep(
        icon = Icons.Outlined.FrontHand,
        title = "مسبحة رقمية متطورة",
        description = "استخدم المسبحة الذكية لتسبيحك، مع أهداف قابلة للتخصيص واهتزازات لمساعدتك على التركيز.",
    ),
    OnboardingStep(
        icon = Icons.Outlined.NotificationsActive,
        title = "تذكيرات صوتية ذكية",
        description = "لا تفوّت وِرْدَك اليومي. فعّل التذكيرات الصوتية لتصلك في الأوقات التي تحددها أنت.",
        tip = "سنطلب منك الآن بعض الأذونات الضرورية.",
    ),
)
