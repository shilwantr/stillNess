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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.UserRole
import com.example.ui.components.GlassButton
import com.example.ui.glass.GlassCard
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DustyRose
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.PinkGlow
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
    val currentUser by viewModel.currentUser.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val therapistSaveSuccess by viewModel.therapistSaveSuccess.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val todayMetric by viewModel.todayMetric.collectAsState()

    var hydrationRemindersEnabled by remember { mutableStateOf(true) }
    var mindfulnessAlertsEnabled by remember { mutableStateOf(true) }

    // Minimal Therapist profile form fields
    var therapistTitle by remember { mutableStateOf("") }
    var therapistSpecialty by remember { mutableStateOf("") }
    var therapistHourlyRate by remember { mutableStateOf("") }
    var therapistBio by remember { mutableStateOf("") }

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

            // Authentication & Account Section
            val user = currentUser
            if (user == null) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    borderBrush = GlassBorderActiveCyan,
                    testTag = "card_auth_signin"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CyanGlow.copy(alpha = 0.2f))
                                .border(1.dp, GlassBorderActiveCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Sign in",
                                tint = CyanBright,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Sign In with Google",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sign in to register as a Client or Therapist.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isAuthLoading) {
                            CircularProgressIndicator(
                                color = CyanBright,
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            GlassButton(
                                text = "Continue with Google",
                                onClick = { viewModel.signInWithGoogle(context) },
                                isActive = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (!authError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = authError ?: "",
                                color = PinkGlow,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Logged-in User Profile Header
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    testTag = "card_user_profile"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val photo = user.photoUrl?.toString()
                            if (!photo.isNullOrBlank()) {
                                AsyncImage(
                                    model = photo,
                                    contentDescription = user.displayName ?: "User",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, GlassBorderActiveCyan, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
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
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName ?: "User",
                                    color = TextWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.email ?: "",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (userRole == UserRole.THERAPIST) "Therapist Account" else "Client Account",
                                    color = CyanBright,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "Sign Out",
                                color = PinkGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.signOut() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Registration Type Selector
                Text(
                    text = "Registration Type",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isClient = userRole == UserRole.CLIENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isClient) CyanGlow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                            .border(1.dp, if (isClient) GlassBorderActiveCyan else FrostedGlassBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setUserRole(UserRole.CLIENT) }
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Client",
                                color = if (isClient) TextWhite else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Seeking Support",
                                color = if (isClient) CyanBright else TextSubtle,
                                fontSize = 10.sp
                            )
                        }
                    }

                    val isTherapist = userRole == UserRole.THERAPIST
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isTherapist) VioletGlow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                            .border(1.dp, if (isTherapist) SolidColor(VioletBright) else FrostedGlassBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setUserRole(UserRole.THERAPIST) }
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Therapist",
                                color = if (isTherapist) TextWhite else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Practitioner",
                                color = if (isTherapist) VioletBright else TextSubtle,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (userRole == UserRole.CLIENT) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Client Registration Active",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You are registered as a Client. You can discover specialists and book sessions in the Therapy tab.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    // Minimal Therapist Registration Form
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Therapist Registration",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Complete your profile to appear in the Therapy tab directory.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            OutlinedTextField(
                                value = therapistTitle,
                                onValueChange = { therapistTitle = it },
                                label = { Text("Title (e.g. Clinical Psychologist)", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = FrostedGlassBorder.value,
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                )
                            )

                            OutlinedTextField(
                                value = therapistSpecialty,
                                onValueChange = { therapistSpecialty = it },
                                label = { Text("Specialty (e.g. Anxiety, Stress)", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = FrostedGlassBorder.value,
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                )
                            )

                            OutlinedTextField(
                                value = therapistHourlyRate,
                                onValueChange = { therapistHourlyRate = it },
                                label = { Text("Hourly Rate ($ / session)", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = FrostedGlassBorder.value,
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                )
                            )

                            OutlinedTextField(
                                value = therapistBio,
                                onValueChange = { therapistBio = it },
                                label = { Text("Bio", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = CyanBright,
                                    unfocusedBorderColor = FrostedGlassBorder.value,
                                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                                )
                            )

                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    color = CyanBright,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.CenterHorizontally),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                GlassButton(
                                    text = "Save Therapist Profile",
                                    onClick = {
                                        viewModel.registerTherapistProfile(
                                            title = therapistTitle,
                                            specialty = therapistSpecialty,
                                            hourlyRate = therapistHourlyRate.toDoubleOrNull() ?: 75.0,
                                            bio = therapistBio
                                        )
                                    },
                                    isActive = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (!therapistSaveSuccess.isNullOrBlank()) {
                                Text(
                                    text = therapistSaveSuccess ?: "",
                                    color = Color(0xFF10B981),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (!authError.isNullOrBlank()) {
                                Text(
                                    text = authError ?: "",
                                    color = PinkGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
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
