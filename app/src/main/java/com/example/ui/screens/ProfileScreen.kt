package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassButton
import com.example.ui.glass.GlassCard
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.OrangeFlame
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.DustyRose
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import com.example.ui.theme.VioletGlow
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun ProfileScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val payments by viewModel.payments.collectAsState()
    val todayMetric by viewModel.todayMetric.collectAsState()

    var hydrationRemindersEnabled by remember { mutableStateOf(true) }
    var mindfulnessAlertsEnabled by remember { mutableStateOf(true) }
    var isSubscribed by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "P R O F I L E",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 8.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // User Profile Header Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                testTag = "card_user_profile"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(CyanGlow.copy(alpha = 0.5f), VioletGlow.copy(alpha = 0.3f))
                                )
                            )
                            .border(1.5.dp, GlassBorderActiveCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = TextWhite,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alex Mercer",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "STILL Member • Daily Pacer",
                            color = CyanBright,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Connected with Step Sensor & Audio Engine",
                            color = TextSubtle,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Membership & Subscriptions
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                borderBrush = GlassBorderActiveCyan,
                testTag = "card_subscription_tier"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Premium",
                                tint = CyanBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STILL Premium Sanctuary",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanGlow.copy(alpha = 0.3f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Active", color = CyanBright, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Unlimited guided meditations, high-frequency soundscapes, encrypted therapist voice consultations, and predictive energy analytics.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$9.99 / month • Renews Oct 28, 2026",
                        color = TextSubtle,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notifications & Reminders
            Text(
                text = "Notifications & Reminders",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hydration Reminders", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Gentle reminders to reach your 2.5L goal", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = hydrationRemindersEnabled,
                            onCheckedChange = { hydrationRemindersEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyanGlow
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mindfulness & Evening Wind-Down", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Breathwork & relaxation cues before bedtime", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = mindfulnessAlertsEnabled,
                            onCheckedChange = { mindfulnessAlertsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyanGlow
                            )
                        )
                    }

                    // Test Real Notification Button
                    GlassButton(
                        text = "Send Test Reminder Notification",
                        onClick = { viewModel.sendTestHydrationReminder() },
                        isActive = false,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_test_notification"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verified Payment Ledger History
            Text(
                text = "Billing & Payment History",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (payments.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "No payments recorded yet.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                payments.forEach { pay ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pay.title, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(pay.paymentMethod, color = TextSubtle, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$${pay.amount.toInt()}", color = CyanBright, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(pay.status, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Emergency Mental Health Crisis & Hotline Card (MANDATORY Safety Guideline)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                glowColor = DustyRose,
                borderBrush = SolidColor(DustyRose.copy(alpha = 0.5f)),
                testTag = "card_crisis_support"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Crisis Hotline",
                            tint = DustyRose,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "24/7 Crisis & Immediate Support",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "If you or someone you know is in immediate crisis, confidential free support is available 24 hours a day, 7 days a week.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    GlassButton(
                        text = "Call or Text 988 (Suicide & Crisis Lifeline)",
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:988")
                            }
                            context.startActivity(intent)
                        },
                        isActive = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_call_crisis_lifeline"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Health Disclaimer Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Disclaimer",
                    tint = TextSubtle,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STILL is a mindfulness, breathing and activity companion. Calorie and step values are mathematical estimates. Breathing exercises and guided meditation are not a substitute for professional medical or mental healthcare diagnosis.",
                    color = TextSubtle,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
