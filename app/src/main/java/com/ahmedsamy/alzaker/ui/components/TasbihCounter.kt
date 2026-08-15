package com.ahmedsamy.alzaker.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmedsamy.alzaker.ui.theme.AmiriFontFamily
import com.ahmedsamy.alzaker.ui.theme.Gold
import com.ahmedsamy.alzaker.ui.theme.LocalFontSizeMultiplier
import com.ahmedsamy.alzaker.util.HapticFeedbackType
import com.ahmedsamy.alzaker.util.Haptics
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions

/**
 * Interactive tasbih counter mirroring the legacy TasbihCounter: an optional
 * goal input, an optional fixed dhikr text with its required-repeat label, a
 * 200 dp tap-to-count circle, a gold celebration state when the goal is
 * reached and a reset button. Haptics and toasts match the legacy flow.
 */
@Composable
fun TasbihCounter(
    initialDhikrText: String? = null,
    initialRepeatCount: Int? = null,
    showGoalInput: Boolean = false,
    modifier: Modifier = Modifier,
    hapticsEnabled: Boolean = true,
    externalCount: Int? = null,
    externalGoal: Int? = null,
    onCountChange: ((Int) -> Unit)? = null,
    onGoalChange: ((Int) -> Unit)? = null,
) {
    val context = LocalContext.current
    val fontSizeMultiplier = LocalFontSizeMultiplier.current
    val primaryColor = MaterialTheme.colorScheme.primary

    // In external mode the count and goal are owned by the caller (the tasbih
    // tab binds them to TasbihStore, a goal of 0 meaning "no goal"); otherwise
    // they stay local so DhikrDetails keeps its current behavior.
    val external = externalCount != null || externalGoal != null
    var localCount by remember { mutableStateOf(0) }
    var localGoal by remember { mutableStateOf(initialRepeatCount?.takeIf { it > 0 }) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var celebrationGoal by remember { mutableStateOf<Int?>(null) }

    val count = externalCount ?: localCount
    val tasbihGoal: Int? = if (external) externalGoal?.takeIf { it > 0 } else localGoal

    fun setCountValue(value: Int) {
        if (external) onCountChange?.invoke(value) else localCount = value
    }

    fun setGoalValue(value: Int) {
        if (external) onGoalChange?.invoke(value) else localGoal = if (value > 0) value else null
    }

    val isGoalReached = tasbihGoal != null && count >= (tasbihGoal ?: 0)

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            toastMessage = null
        }
    }

    fun showToast(message: String) {
        toastMessage = message
    }

    fun incrementCount() {
        Haptics.trigger(context, HapticFeedbackType.ImpactMedium, hapticsEnabled)
        val newCount = count + 1
        setCountValue(newCount)
        val goal = tasbihGoal
        if (goal != null && newCount == goal) {
            Haptics.trigger(context, HapticFeedbackType.NotificationSuccess, hapticsEnabled)
            celebrationGoal = goal
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (showGoalInput) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 300.dp)
                        .padding(bottom = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "حدد هدفك:",
                        color = Color.White,
                        fontSize = (20 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .border(width = 1.dp, color = Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicTextField(
                            value = tasbihGoal?.toString() ?: "",
                            onValueChange = { text ->
                                setGoalValue(text.toIntOrNull() ?: 0)
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = (18 * fontSizeMultiplier).sp,
                                fontFamily = AmiriFontFamily,
                                textAlign = TextAlign.Center,
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 15.dp)
                                .semantics {
                                    contentDescription = "أدخل هدف التسبيحات"
                                },
                        )
                        if (tasbihGoal == null) {
                            Text(
                                text = "مثل: 100",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = (18 * fontSizeMultiplier).sp,
                                fontFamily = AmiriFontFamily,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            if (initialDhikrText != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = initialDhikrText,
                        color = Color.White,
                        fontSize = (24 * fontSizeMultiplier).sp,
                        fontFamily = AmiriFontFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    val repeat = initialRepeatCount ?: 0
                    if (repeat > 0) {
                        Text(
                            text = "التكرار المطلوب: $repeat مرة",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = (18 * fontSizeMultiplier).sp,
                            fontFamily = AmiriFontFamily,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            if (isGoalReached) {
                Text(
                    text = "ما شاء الله! لقد وصلت إلى هدفك!",
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = (24 * fontSizeMultiplier).sp,
                    fontFamily = AmiriFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { incrementCount() }
                    .semantics(mergeDescendants = true) { role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = count.toString(),
                    color = if (isGoalReached) Gold else Color.White,
                    fontSize = (120 * fontSizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFontFamily,
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White)
                    .clickable {
                        Haptics.trigger(context, HapticFeedbackType.NotificationWarning, hapticsEnabled)
                        setCountValue(0)
                        showToast("تم إعادة ضبط العداد.")
                    }
                    .padding(vertical = 12.dp, horizontal = 40.dp)
                    .semantics(mergeDescendants = true) { role = Role.Button },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "تصفير",
                    color = primaryColor,
                    fontSize = (20 * fontSizeMultiplier).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFontFamily,
                )
            }
        }

        AppToast(message = toastMessage.orEmpty(), visible = toastMessage != null)
    }

    val celebrationGoalValue = celebrationGoal
    if (celebrationGoalValue != null) {
        AlertDialog(
            onDismissRequest = { celebrationGoal = null },
            title = { Text("أحسنت!") },
            text = { Text("بارك الله فيك لقد وصلت لهدفك ($celebrationGoalValue) 🎉") },
            confirmButton = {
                TextButton(onClick = { celebrationGoal = null }) { Text("تم") }
            },
        )
    }
}
