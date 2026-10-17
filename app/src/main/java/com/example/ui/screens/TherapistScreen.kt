package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AppointmentEntity
import com.example.domain.model.Therapist
import com.example.domain.model.UserRole
import com.example.ui.components.GlassButton
import com.example.ui.glass.GlassCard
import com.example.ui.components.GlassIconButton
import com.example.ui.theme.CyanBright
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.FrostedGlassBorder
import com.example.ui.theme.GlassBorderActiveCyan
import com.example.ui.theme.GlassBorderGradient
import com.example.ui.theme.PinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VioletBright
import com.example.ui.theme.VioletGlow
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun TherapistScreen(
    viewModel: WellnessViewModel,
    onNavigateToCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userRole by viewModel.userRole.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val therapists = remember { viewModel.repository.getCuratedTherapists() }

    var selectedTherapistForBooking by remember { mutableStateOf<Therapist?>(null) }
    var selectedSlot by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("therapist_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "T H E R A P Y",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 8.sp
                    )
                    Text(
                        text = if (userRole == UserRole.CLIENT) "Verified Licensed Practitioners" else "Therapist Provider Portal",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                // Role Switcher Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .border(0.8.dp, FrostedGlassBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            val newRole = if (userRole == UserRole.CLIENT) UserRole.THERAPIST else UserRole.CLIENT
                            viewModel.setUserRole(newRole)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (userRole == UserRole.CLIENT) "Client Mode" else "Therapist Mode",
                        color = CyanBright,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Appointments Section
            Text(
                text = if (userRole == UserRole.CLIENT) "My Scheduled Sessions" else "Upcoming Client Appointments",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (appointments.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp
                ) {
                    Text(
                        text = "No appointments scheduled yet. Select a practitioner below to book a confidential consultation.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                appointments.forEach { apt ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        cornerRadius = 20.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = apt.therapistName,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${apt.dateSlot} at ${apt.timeSlot} • $${apt.fee.toInt()}",
                                        color = CyanBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                if (apt.status == "UPCOMING") {
                                    GlassIconButton(
                                        onClick = {
                                            viewModel.startCallWithAppointment(apt)
                                            onNavigateToCall()
                                        },
                                        size = 44.dp,
                                        isActive = apt.callEligible,
                                        testTag = "btn_join_call_${apt.id}"
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Join Voice Session",
                                            tint = TextWhite,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = apt.status,
                                        color = TextSubtle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (apt.status == "UPCOMING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "High-Fidelity 48kHz Voice Call ready",
                                        color = TextSubtle,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Cancel",
                                        color = PinkGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.clickable { viewModel.cancelAppointment(apt.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Practitioner Directory
            Text(
                text = "Discover Licensed Specialists",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Rigorous background verification • Encrypted one-on-one sessions",
                color = TextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            therapists.forEach { therapist ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    cornerRadius = 22.dp,
                    testTag = "card_therapist_${therapist.id}"
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(VioletGlow.copy(alpha = 0.35f))
                                        .border(1.dp, VioletBright, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = therapist.name,
                                        tint = TextWhite,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = therapist.name,
                                            color = TextWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (therapist.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified Specialist",
                                                tint = CyanBright,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = therapist.title,
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = therapist.credentials,
                                        color = TextSubtle,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$${therapist.hourlyRate.toInt()}",
                                    color = CyanBright,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "/ session",
                                    color = TextSubtle,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = therapist.bio,
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        // Rating & Booking Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${therapist.rating} (${therapist.reviewCount} reviews)",
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            GlassButton(
                                text = "Book Session",
                                onClick = {
                                    selectedTherapistForBooking = therapist
                                    selectedSlot = therapist.availableSlots.firstOrNull() ?: "Today 4:00 PM"
                                },
                                isActive = true,
                                modifier = Modifier.height(38.dp),
                                testTag = "btn_book_${therapist.id}"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // Booking & Payment Confirmation Modal
        val bookingTherapist = selectedTherapistForBooking
        if (bookingTherapist != null) {
            AlertDialog(
                onDismissRequest = { selectedTherapistForBooking = null },
                containerColor = Color(0xFF1E2D44),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "Confirm Consultation",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Practitioner: ${bookingTherapist.name}",
                            color = CyanBright,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Specialty: ${bookingTherapist.specialty}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select Availability Slot:",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        bookingTherapist.availableSlots.forEach { slot ->
                            val isSel = slot == selectedSlot
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color.White.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.08f))
                                    .border(0.8.dp, if (isSel) GlassBorderActiveCyan else FrostedGlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { selectedSlot = slot }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = slot,
                                    color = if (isSel) TextWhite else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Fee:", color = TextMuted, fontSize = 13.sp)
                            Text("$${bookingTherapist.hourlyRate.toInt()}", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "Secured via Google Pay • End-to-End Encrypted Call",
                            color = TextSubtle,
                            fontSize = 10.sp
                        )
                    }
                },
                confirmButton = {
                    GlassButton(
                        text = "Pay & Confirm",
                        onClick = {
                            viewModel.bookAppointment(
                                therapist = bookingTherapist,
                                slot = selectedSlot,
                                paymentMethod = "Google Pay (Verified)"
                            )
                            selectedTherapistForBooking = null
                        },
                        isActive = true
                    )
                },
                dismissButton = {
                    TextButton(onClick = { selectedTherapistForBooking = null }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}
