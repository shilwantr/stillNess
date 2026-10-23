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
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun TherapistScreen(
    viewModel: WellnessViewModel,
    onNavigateToCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userRole by viewModel.userRole.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val therapists by viewModel.therapists.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val context = LocalContext.current
    var selectedTherapistForBooking by remember { mutableStateOf<Therapist?>(null) }
    var selectedSlot by remember { mutableStateOf("") }
    var selectedBookingDate by remember { mutableStateOf("Today") }
    var selectedBookingTime by remember { mutableStateOf("04:00 PM") }

    val calendar = remember { Calendar.getInstance() }
    val timePickerDialog = remember(context) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val hour12 = when {
                    hourOfDay == 0 -> 12
                    hourOfDay > 12 -> hourOfDay - 12
                    else -> hourOfDay
                }
                val formatted = String.format("%d:%02d %s", hour12, minute, amPm)
                selectedBookingTime = formatted
                selectedSlot = "$selectedBookingDate $formatted"
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
    }

    val datePickerDialog = remember(context) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d", month + 1, dayOfMonth)
                selectedBookingDate = formattedDate
                selectedSlot = "$formattedDate $selectedBookingTime"
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (userRole == UserRole.CLIENT) "T H E R A P Y" else "P R A C T I C E   P O R T A L",
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 8.sp
                    )
                    Text(
                        text = if (userRole == UserRole.CLIENT) "Verified Licensed Practitioners & Consultations" else "Licensed Practitioner Dashboard • Encrypted Sessions",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Appointments Section
            Text(
                text = if (userRole == UserRole.CLIENT) "My Scheduled Sessions" else "Client Consultation Queue & Requests",
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
                        text = if (userRole == UserRole.CLIENT)
                            "No appointments scheduled yet. Select a practitioner below to book a confidential consultation."
                        else
                            "No client consultations waiting. When a client books or starts a consultation with you, it will appear here in real time.",
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
                                        text = if (userRole == UserRole.CLIENT) apt.therapistName else "Client: ${apt.clientName.ifBlank { "Client Consultation" }}",
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (userRole == UserRole.CLIENT) "${apt.dateSlot} at ${apt.timeSlot} • $${apt.fee.toInt()}" else "Fee: $${apt.fee.toInt()} • ${apt.dateSlot} ${apt.timeSlot}",
                                        color = CyanBright,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Inverted Call & Waiting Room Controls
                                if (userRole == UserRole.CLIENT) {
                                    // CLIENT SIDE: Clients cannot initiate raw calls; they enter the waiting room
                                    when (apt.status) {
                                        "UPCOMING" -> {
                                            GlassButton(
                                                text = "Enter Waiting Room",
                                                onClick = { viewModel.enterWaitingRoom(apt) },
                                                isActive = true,
                                                modifier = Modifier.height(36.dp),
                                                testTag = "btn_enter_waiting_${apt.id}"
                                            )
                                        }
                                        "WAITING" -> {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(CyanGlow.copy(alpha = 0.25f))
                                                    .border(1.dp, GlassBorderActiveCyan, RoundedCornerShape(12.dp))
                                                    .clickable { viewModel.enterWaitingRoom(apt) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "In Waiting Room",
                                                    color = CyanBright,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        "IN_CALL" -> {
                                            GlassButton(
                                                text = "Join Ongoing Call",
                                                onClick = {
                                                    viewModel.startCallWithAppointment(apt)
                                                    onNavigateToCall()
                                                },
                                                isActive = true,
                                                modifier = Modifier.height(36.dp),
                                                testTag = "btn_join_active_${apt.id}"
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = apt.status,
                                                color = TextSubtle,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    // THERAPIST SIDE: Therapist has the authority to Admit & Call Clients
                                    when (apt.status) {
                                        "WAITING" -> {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "WAITING",
                                                        color = Color(0xFF34D399),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                GlassButton(
                                                    text = "Admit & Call",
                                                    onClick = {
                                                        viewModel.startVideoCall(apt)
                                                        onNavigateToCall()
                                                    },
                                                    isActive = true,
                                                    modifier = Modifier.height(36.dp),
                                                    testTag = "btn_admit_client_${apt.id}"
                                                )
                                            }
                                        }
                                        "UPCOMING" -> {
                                            GlassButton(
                                                text = "Start Call",
                                                onClick = {
                                                    viewModel.startVideoCall(apt)
                                                    onNavigateToCall()
                                                },
                                                isActive = false,
                                                modifier = Modifier.height(36.dp),
                                                testTag = "btn_therapist_start_${apt.id}"
                                            )
                                        }
                                        "IN_CALL" -> {
                                            GlassButton(
                                                text = "Reconnect",
                                                onClick = {
                                                    viewModel.startVideoCall(apt)
                                                    onNavigateToCall()
                                                },
                                                isActive = true,
                                                modifier = Modifier.height(36.dp),
                                                testTag = "btn_reconnect_${apt.id}"
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = apt.status,
                                                color = TextSubtle,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            if (apt.status == "UPCOMING" || apt.status == "WAITING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (apt.status == "WAITING") "Client is in waiting room ready for consultation" else "Agora End-to-End Encrypted Session Scheduled",
                                        color = if (apt.status == "WAITING") Color(0xFF34D399) else TextSubtle,
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

            if (userRole == UserRole.CLIENT) {
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

            if (therapists.isEmpty()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 18.dp
                ) {
                    Text(
                        text = "No practitioners registered yet. Therapists can register under the Profile tab.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
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
                                    if (therapist.photoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = therapist.photoUrl,
                                            contentDescription = therapist.name,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, VioletBright, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
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
                                    selectedSlot = "$selectedBookingDate $selectedBookingTime"
                                },
                                isActive = true,
                                modifier = Modifier.height(38.dp),
                                testTag = "btn_book_${therapist.id}"
                            )
                        }
                    }
                }
                }
            }

            } else {
                // -------------------------------------------------------------
                // THERAPIST DASHBOARD OVERVIEW (NO OTHER THERAPISTS LISTED)
                // -------------------------------------------------------------
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Practitioner Overview & Status",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentUser?.displayName ?: "Licensed Practitioner",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Encrypted Peer-to-Peer Teletherapy Ready",
                                    color = CyanBright,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.20f))
                                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "ONLINE",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Your practice is live in the Client directory. When clients initiate a call or book a consultation, it connects automatically to your room and appears in your queue above in real time.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(90.dp))
        }

        // Booking & Payment Confirmation Modal with Interactive Clock Time Picker
        val bookingTherapist = selectedTherapistForBooking
        if (bookingTherapist != null) {
            AlertDialog(
                onDismissRequest = { selectedTherapistForBooking = null },
                containerColor = Color(0xFF1E2D44),
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text(
                        text = "Book Consultation",
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

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose Date:",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Date Picker Row: Today, Tomorrow, or Pick Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Today", "Tomorrow").forEach { dateOption ->
                                val isSelected = selectedBookingDate == dateOption
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) CyanGlow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, if (isSelected) GlassBorderActiveCyan else FrostedGlassBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedBookingDate = dateOption
                                            selectedSlot = "$dateOption $selectedBookingTime"
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dateOption,
                                        color = if (isSelected) TextWhite else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            // Calendar Date Picker Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selectedBookingDate !in listOf("Today", "Tomorrow")) CyanGlow.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, if (selectedBookingDate !in listOf("Today", "Tomorrow")) GlassBorderActiveCyan else FrostedGlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { datePickerDialog.show() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedBookingDate !in listOf("Today", "Tomorrow")) selectedBookingDate else "Pick Date",
                                    color = if (selectedBookingDate !in listOf("Today", "Tomorrow")) TextWhite else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedBookingDate !in listOf("Today", "Tomorrow")) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose Time (Interactive Clock):",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Interactive Clock Card (Tapping opens TimePickerDialog)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, GlassBorderActiveCyan, RoundedCornerShape(14.dp))
                                .clickable { timePickerDialog.show() }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Clock",
                                        tint = CyanBright,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Scheduled Session Time",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "$selectedBookingDate at $selectedBookingTime",
                                            color = TextWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CyanBright.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Set Clock",
                                        color = CyanBright,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Quick Time Presets
                        Text(
                            text = "Quick Presets:",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("10:00 AM", "02:00 PM", "04:30 PM", "06:00 PM").forEach { preset ->
                                val isSel = selectedBookingTime == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
                                        .border(0.8.dp, if (isSel) GlassBorderActiveCyan else FrostedGlassBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedBookingTime = preset
                                            selectedSlot = "$selectedBookingDate $preset"
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset,
                                        color = if (isSel) TextWhite else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
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
                                slot = "$selectedBookingDate $selectedBookingTime",
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
