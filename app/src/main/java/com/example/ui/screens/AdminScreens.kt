package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    onCloseAdmin: () -> Unit
) {
    var adminSection by remember { mutableIntStateOf(0) }
    // 0: Dashboard, 1: Students, 2: Education & Videos, 3: Attendance, 4: Fees, 5: Payments, 6: Settings & Branding

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NEOL BHARAT ADMIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Authorized as ${userProfile.email}",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                },
                actions = {
                    // Quick Switch to Student Mode
                    TextButton(
                        onClick = {
                            viewModel.selectExperienceMode("STUDENT")
                            onCloseAdmin()
                        }
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Student App", color = Color(0xFF38BDF8), fontSize = 11.sp)
                    }

                    // Quick Switch to Entertainment Mode
                    TextButton(
                        onClick = {
                            viewModel.selectExperienceMode("ENTERTAINMENT")
                            onCloseAdmin()
                        }
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Entertainment", color = Color(0xFFF59E0B), fontSize = 11.sp)
                    }

                    IconButton(onClick = onCloseAdmin) {
                        Icon(Icons.Default.Close, contentDescription = "Close Admin Panel", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        bottomBar = {
            ScrollableTabRow(
                selectedTabIndex = adminSection,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                edgePadding = 12.dp
            ) {
                Tab(selected = adminSection == 0, onClick = { adminSection = 0 }, text = { Text("Dashboard") })
                Tab(selected = adminSection == 1, onClick = { adminSection = 1 }, text = { Text("Suchna (सूचना)") })
                Tab(selected = adminSection == 2, onClick = { adminSection = 2 }, text = { Text("Students") })
                Tab(selected = adminSection == 3, onClick = { adminSection = 3 }, text = { Text("Video & Education") })
                Tab(selected = adminSection == 4, onClick = { adminSection = 4 }, text = { Text("Attendance") })
                Tab(selected = adminSection == 5, onClick = { adminSection = 5 }, text = { Text("Fees") })
                Tab(selected = adminSection == 6, onClick = { adminSection = 6 }, text = { Text("Payments") })
                Tab(selected = adminSection == 7, onClick = { adminSection = 7 }, text = { Text("Settings & App") })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF020617))
        ) {
            when (adminSection) {
                0 -> AdminDashboardSection(
                    viewModel = viewModel,
                    onNavigateToSection = { adminSection = it }
                )
                1 -> AdminSuchnaSection(viewModel = viewModel)
                2 -> AdminStudentsSection(viewModel = viewModel)
                3 -> AdminEducationAndVideoSection(viewModel = viewModel)
                4 -> AdminAttendanceSection(viewModel = viewModel)
                5 -> AdminFeesSection(viewModel = viewModel)
                6 -> AdminPaymentsSection(viewModel = viewModel)
                7 -> AdminSettingsAndBrandingSection(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminDashboardSection(
    viewModel: MainViewModel,
    onNavigateToSection: (Int) -> Unit = {}
) {
    val users by viewModel.allUsers.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val videos by viewModel.educationVideos.collectAsState()
    val entertainmentVideos by viewModel.entertainmentVideos.collectAsState()
    val allVideos = videos + entertainmentVideos
    val payments by viewModel.allPayments.collectAsState()
    val fees by viewModel.allFees.collectAsState()

    val totalCollected = payments.filter { it.status == "VERIFIED" }.sumOf { it.amount }
    val pendingFees = fees.sumOf { it.pendingAmount }

    // Dialog state for clicking stat cards
    var activeDetailModal by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Operational Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                    text = "Tap any card to view details",
                    fontSize = 11.sp,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Total Users (Click to view)",
                    value = "${users.size}",
                    icon = Icons.Default.People,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "USERS" }
                )
                AdminStatCard(
                    title = "Total Courses (Click to view)",
                    value = "${courses.size}",
                    icon = Icons.Default.School,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "COURSES" }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Fee Collected (Click to view)",
                    value = "₹${totalCollected.toInt()}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "COLLECTED_FEES" }
                )
                AdminStatCard(
                    title = "Outstanding Fees (Click to view)",
                    value = "₹${pendingFees.toInt()}",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "OUTSTANDING_FEES" }
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatCard(
                    title = "Total Videos (Click to view)",
                    value = "${allVideos.size}",
                    icon = Icons.Default.PlayCircle,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { activeDetailModal = "VIDEOS" }
                )
                val pendingPayments = payments.count { it.status == "PENDING_VERIFICATION" }
                AdminStatCard(
                    title = "Pending Verifications",
                    value = "$pendingPayments",
                    icon = Icons.Default.PendingActions,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToSection(6) }
                )
            }
        }
    }

    // Modal dialogs showing one-by-one detail on clicking cards
    activeDetailModal?.let { modal ->
        AlertDialog(
            onDismissRequest = { activeDetailModal = null },
            title = {
                Text(
                    text = when (modal) {
                        "USERS" -> "Total Registered Users (${users.size})"
                        "COURSES" -> "All Courses Details (${courses.size})"
                        "COLLECTED_FEES" -> "Fee Collected Details (₹${totalCollected.toInt()})"
                        "OUTSTANDING_FEES" -> "Outstanding Dues Details (₹${pendingFees.toInt()})"
                        "VIDEOS" -> "All Videos Details (${allVideos.size})"
                        else -> "Details"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                    when (modal) {
                        "USERS" -> {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(users) { u ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(u.displayName, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Email: ${u.email}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                            Text("Role: ${u.role} • Mode: ${u.selectedMode} • Status: ${if (u.isBlocked) "Blocked" else "Active"}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        "COURSES" -> {
                            if (courses.isEmpty()) {
                                Text("No courses available.", color = Color.Gray)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(courses) { c ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(c.title, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text(c.description, color = Color(0xFF94A3B8), fontSize = 12.sp, maxLines = 2)
                                                Text("Category: ${c.category} • ${if (c.isFree) "Free" else "Price: ₹${c.price.toInt()}"}", color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "COLLECTED_FEES" -> {
                            val verifiedPayments = payments.filter { it.status == "VERIFIED" }
                            if (verifiedPayments.isEmpty()) {
                                Text("No verified fee payments yet.", color = Color.Gray)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(verifiedPayments) { p ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(p.userName, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text("₹${p.amount.toInt()}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                                }
                                                Text("Purpose: ${p.purpose} • Target: ${p.targetName}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                                Text("Ref / Receipt: ${p.receiptId.ifEmpty { p.transactionReference }}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "OUTSTANDING_FEES" -> {
                            val dueFees = fees.filter { it.pendingAmount > 0 }
                            if (dueFees.isEmpty()) {
                                Text("No outstanding fees! All student accounts are clear.", color = Color(0xFF10B981))
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(dueFees) { f ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(f.studentName, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text("Due: ₹${f.pendingAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = Color(0xFFEF4444))
                                                }
                                                Text("Month: ${f.month} • Type: ${f.feeType}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                                Text("Total: ₹${f.totalAmount.toInt()} | Paid: ₹${f.paidAmount.toInt()} | Status: ${f.status}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "VIDEOS" -> {
                            if (allVideos.isEmpty()) {
                                Text("No videos uploaded.", color = Color.Gray)
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(allVideos) { v ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(v.title, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("Section: ${v.section} • Category: ${v.category} • ${if (v.isPaid) "₹${v.price.toInt()}" else "Free"}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                                Text(v.videoUrl, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDetailModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(title, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun AdminStudentsSection(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val context = LocalContext.current

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var studentName by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var studentGrade by remember { mutableStateOf("10th Standard") }
    var studentPhone by remember { mutableStateOf("") }
    var studentPhotoUrl by remember { mutableStateOf("") }

    // Edit student profile state
    var editingUser by remember { mutableStateOf<UserProfile?>(null) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editGrade by remember { mutableStateOf("10th Standard") }
    var editPhone by remember { mutableStateOf("") }
    var editPhotoUrl by remember { mutableStateOf("") }

    var studentToDelete by remember { mutableStateOf<UserProfile?>(null) }

    // Android Photo Picker for Student Profile Photo Upload
    val studentPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageLocally(context, uri, "std_avatar_${System.currentTimeMillis()}.png")
            if (savedPath != null) {
                if (editingUser != null) {
                    editPhotoUrl = savedPath
                } else {
                    studentPhotoUrl = savedPath
                }
                Toast.makeText(context, "Student profile photo selected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student & User Records",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Button(
                    onClick = {
                        showAddStudentDialog = true
                        studentPhotoUrl = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Student Profile")
                }
            }
        }

        // Add Student Form Card
        if (showAddStudentDialog) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Upload & Create New Student Profile", fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(onClick = { showAddStudentDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        // Photo Picker Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (studentPhotoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = studentPhotoUrl,
                                    contentDescription = "Profile Preview",
                                    modifier = Modifier.size(52.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            OutlinedButton(
                                onClick = { studentPhotoPicker.launch("image/*") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (studentPhotoUrl.isEmpty()) "Upload Student Photo" else "Change Photo")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = studentEmail,
                            onValueChange = { studentEmail = it },
                            label = { Text("Student Google/Contact Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = studentGrade,
                            onValueChange = { studentGrade = it },
                            label = { Text("Class / Course Grade") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = studentPhone,
                            onValueChange = { studentPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                if (studentName.isNotEmpty() && studentEmail.isNotEmpty()) {
                                    viewModel.adminCreateStudent(studentName, studentEmail, studentGrade, studentPhone, studentPhotoUrl) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) {
                                            showAddStudentDialog = false
                                            studentName = ""
                                            studentEmail = ""
                                            studentPhone = ""
                                            studentPhotoUrl = ""
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Name and Email are required", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Create & Sync Student Profile")
                        }
                    }
                }
            }
        }

        items(users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (user.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(42.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF0284C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(user.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(user.email, color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Text("Role: ${user.role} • Mode: ${user.selectedMode}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Edit Student Profile Button
                        IconButton(onClick = {
                            editingUser = user
                            editName = user.displayName
                            editEmail = user.email
                            editGrade = "10th Standard"
                            editPhone = ""
                            editPhotoUrl = user.photoUrl
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFF38BDF8))
                        }

                        // Block/Unblock Button
                        IconButton(onClick = {
                            viewModel.adminToggleBlockUser(user.uid, !user.isBlocked) {
                                Toast.makeText(context, if (!user.isBlocked) "User blocked" else "User unblocked", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = if (user.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                                contentDescription = "Block or Unblock",
                                tint = if (user.isBlocked) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                            )
                        }

                        // Delete Student Button
                        IconButton(onClick = {
                            studentToDelete = user
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Student Profile", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    // Edit Student Profile Dialog
    editingUser?.let { target ->
        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text("Edit Student Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (editPhotoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = editPhotoUrl,
                                contentDescription = "Edit Preview",
                                modifier = Modifier.size(50.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        OutlinedButton(
                            onClick = { studentPhotoPicker.launch("image/*") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Upload/Replace Photo")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editGrade, onValueChange = { editGrade = it }, label = { Text("Class / Grade") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotEmpty() && editEmail.isNotEmpty()) {
                            viewModel.adminUpdateStudentProfile(target.uid, editName, editEmail, editGrade, editPhone, editPhotoUrl) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) editingUser = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save & Sync Profile")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirmation dialog for deleting student
    studentToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Delete Student Profile?") },
            text = { Text("Are you sure you want to delete ${target.displayName} (${target.email})? This permanently deletes their profile from Firestore.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeleteUser(target.uid) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            studentToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminSuchnaSection(viewModel: MainViewModel) {
    val announcements by viewModel.announcements.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingAnnouncement by remember { mutableStateOf<Announcement?>(null) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("ALL") } // ALL, STUDENT, ENTERTAINMENT, SELECTED
    var selectedStudentEmail by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("NORMAL") } // NORMAL, URGENT
    var scheduledDate by remember { mutableStateOf("") }

    var announcementToDelete by remember { mutableStateOf<Announcement?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Suchna / Announcements (सूचना)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    Text("Create, schedule, broadcast or send to selected students", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        editingAnnouncement = null
                        title = ""
                        content = ""
                        targetAudience = "ALL"
                        selectedStudentEmail = ""
                        priority = "NORMAL"
                        scheduledDate = ""
                        showCreateDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Suchna")
                }
            }
        }

        if (announcements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No active announcements. Tap '+ Create Suchna' above to post a notice.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(announcements) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (notice.priority == "URGENT") Icons.Default.Warning else Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = if (notice.priority == "URGENT") Color(0xFFEF4444) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(notice.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Edit Announcement
                                IconButton(onClick = {
                                    editingAnnouncement = notice
                                    title = notice.title
                                    content = notice.content
                                    targetAudience = notice.targetAudience
                                    selectedStudentEmail = notice.targetStudentEmail
                                    priority = notice.priority
                                    scheduledDate = notice.scheduledDate
                                    showCreateDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Suchna", tint = Color(0xFF38BDF8))
                                }

                                // Delete Announcement
                                IconButton(onClick = { announcementToDelete = notice }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Suchna", tint = Color(0xFFEF4444))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(notice.content, color = Color(0xFFE2E8F0), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Badge(containerColor = Color(0xFF1E293B)) {
                                    Text("Audience: ${notice.targetAudience}", color = Color(0xFF38BDF8), fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                                }
                                if (notice.targetStudentEmail.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFF1E293B)) {
                                        Text("For: ${notice.targetStudentEmail}", color = Color(0xFFFBBF24), fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }
                            if (notice.scheduledDate.isNotEmpty()) {
                                Text("Schedule: ${notice.scheduledDate}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Announcement Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(if (editingAnnouncement == null) "Create New Suchna" else "Edit Suchna", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / शीर्षक") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Notice Message / सूचना विवरण") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Target Students / Audience:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = targetAudience == "ALL",
                            onClick = { targetAudience = "ALL" },
                            label = { Text("All Students") }
                        )
                        FilterChip(
                            selected = targetAudience == "STUDENT",
                            onClick = { targetAudience = "STUDENT" },
                            label = { Text("Student Mode") }
                        )
                        FilterChip(
                            selected = targetAudience == "SELECTED",
                            onClick = { targetAudience = "SELECTED" },
                            label = { Text("Selected Student") }
                        )
                    }

                    if (targetAudience == "SELECTED") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = selectedStudentEmail,
                            onValueChange = { selectedStudentEmail = it },
                            label = { Text("Student Email (e.g., student@gmail.com)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Schedule & Timing:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = scheduledDate,
                        onValueChange = { scheduledDate = it },
                        label = { Text("Scheduled Date / Time (e.g. 2026-10-15 or Immediate)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = priority == "URGENT",
                            onCheckedChange = { priority = if (it) "URGENT" else "NORMAL" }
                        )
                        Text("Mark as High Priority / Urgent Notice", color = Color.White, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty()) {
                            val notice = Announcement(
                                id = editingAnnouncement?.id ?: "",
                                title = title,
                                content = content,
                                targetAudience = targetAudience,
                                targetStudentEmail = if (targetAudience == "SELECTED") selectedStudentEmail else "",
                                priority = priority,
                                scheduledDate = scheduledDate
                            )
                            viewModel.adminSaveAnnouncement(notice) { success ->
                                if (success) {
                                    showCreateDialog = false
                                    Toast.makeText(context, "Suchna published successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Title and message content are required", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text(if (editingAnnouncement == null) "Broadcast Suchna" else "Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation
    announcementToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { announcementToDelete = null },
            title = { Text("Delete Suchna?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete announcement '${target.title}'? This will remove it from all student feeds.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminDeleteAnnouncement(target.id) { success ->
                            if (success) {
                                Toast.makeText(context, "Announcement deleted", Toast.LENGTH_SHORT).show()
                                announcementToDelete = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { announcementToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminEducationAndVideoSection(viewModel: MainViewModel) {
    val courses by viewModel.courses.collectAsState()
    val educationVideos by viewModel.educationVideos.collectAsState()
    val entertainmentVideos by viewModel.entertainmentVideos.collectAsState()
    val allVideos = educationVideos + entertainmentVideos
    val context = LocalContext.current

    var subTab by remember { mutableIntStateOf(0) } // 0: Videos Editor, 1: Courses Catalog

    // Video creation / editor states
    var showVideoDialog by remember { mutableStateOf(false) }
    var editingVideo by remember { mutableStateOf<VideoItem?>(null) }
    var videoTitle by remember { mutableStateOf("") }
    var videoDescription by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var videoThumbnailUrl by remember { mutableStateOf("") }
    var videoCategory by remember { mutableStateOf("General") }
    var videoSection by remember { mutableStateOf("EDUCATION") }
    var isPaidVideo by remember { mutableStateOf(false) }
    var videoPrice by remember { mutableStateOf("0") }

    // Filter section for admin list: ALL, EDUCATION, ENTERTAINMENT
    var filterVideoSection by remember { mutableStateOf("ALL") }

    // Course creation state
    var showCreateCourse by remember { mutableStateOf(false) }
    var courseTitle by remember { mutableStateOf("") }
    var courseDesc by remember { mutableStateOf("") }
    var courseCategory by remember { mutableStateOf("Science") }
    var coursePrice by remember { mutableStateOf("0") }
    var isFreeCourse by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TabRow(
                selectedTabIndex = subTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Videos (Upload/Edit/Delete)") })
                Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Courses Management") })
            }
        }

        if (subTab == 0) {
            // VIDEOS MANAGER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Video Platform Catalog", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                        Text("Manage Education & Entertainment Videos", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            editingVideo = null
                            videoTitle = ""
                            videoDescription = ""
                            videoUrl = ""
                            videoThumbnailUrl = ""
                            videoCategory = if (filterVideoSection == "ENTERTAINMENT") "Entertainment" else "Education"
                            videoSection = if (filterVideoSection == "ENTERTAINMENT") "ENTERTAINMENT" else "EDUCATION"
                            isPaidVideo = false
                            videoPrice = "0"
                            showVideoDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Video")
                    }
                }
            }

            // Section Filter for Videos
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All Videos", "EDUCATION" to "Education Videos", "ENTERTAINMENT" to "Entertainment Videos").forEach { (sec, label) ->
                        FilterChip(
                            selected = filterVideoSection == sec,
                            onClick = { filterVideoSection = sec },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // Upload / Edit Video Card
            if (showVideoDialog) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (editingVideo == null) "Upload New Video" else "Edit Video Title & Details",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                IconButton(onClick = { showVideoDialog = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = videoTitle,
                                onValueChange = { videoTitle = it },
                                label = { Text("Video Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoDescription,
                                onValueChange = { videoDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoUrl,
                                onValueChange = { videoUrl = it },
                                label = { Text("Video Stream URL (MP4 / HLS / Web)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoThumbnailUrl,
                                onValueChange = { videoThumbnailUrl = it },
                                label = { Text("Thumbnail Image URL") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = videoCategory,
                                onValueChange = { videoCategory = it },
                                label = { Text("Category (e.g., Mathematics, Culture, News, Comedy)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Target Platform Mode / Section:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FilterChip(
                                    selected = videoSection == "EDUCATION",
                                    onClick = { videoSection = "EDUCATION" },
                                    label = { Text("Education Mode") }
                                )
                                FilterChip(
                                    selected = videoSection == "ENTERTAINMENT",
                                    onClick = { videoSection = "ENTERTAINMENT" },
                                    label = { Text("Entertainment Mode") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isPaidVideo, onCheckedChange = { isPaidVideo = it })
                                Text("Premium / Paid Video (Requires Student Payment)", color = Color.White, fontSize = 13.sp)
                            }
                            if (isPaidVideo) {
                                OutlinedTextField(
                                    value = videoPrice,
                                    onValueChange = { videoPrice = it },
                                    label = { Text("Video Unlock Price (₹)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (videoTitle.isNotEmpty() && videoUrl.isNotEmpty()) {
                                        val newOrUpdated = VideoItem(
                                            id = editingVideo?.id ?: "",
                                            title = videoTitle,
                                            description = videoDescription,
                                            videoUrl = videoUrl,
                                            thumbnailUrl = videoThumbnailUrl,
                                            category = videoCategory,
                                            section = videoSection,
                                            isPaid = isPaidVideo,
                                            price = if (isPaidVideo) videoPrice.toDoubleOrNull() ?: 0.0 else 0.0,
                                            views = editingVideo?.views ?: 0,
                                            likes = editingVideo?.likes ?: 0,
                                            isPublished = true
                                        )
                                        viewModel.adminSaveVideo(newOrUpdated) { success ->
                                            if (success) {
                                                showVideoDialog = false
                                                Toast.makeText(context, "Video synced to Firestore!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Title and Video URL are required", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text(if (editingVideo == null) "Upload Video" else "Save Video Changes")
                            }
                        }
                    }
                }
            }

            val displayVideos = when (filterVideoSection) {
                "EDUCATION" -> allVideos.filter { it.section == "EDUCATION" }
                "ENTERTAINMENT" -> allVideos.filter { it.section == "ENTERTAINMENT" }
                else -> allVideos
            }

            if (displayVideos.isEmpty()) {
                item {
                    Text("No videos uploaded in this section yet. Tap '+ Upload Video' above.", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                items(displayVideos) { v ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(v.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Mode: ${v.section} • ${v.category}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                    if (v.isPaid) {
                                        Badge(containerColor = Color(0xFFEF4444)) {
                                            Text("₹${v.price.toInt()}", color = Color.White, fontSize = 9.sp)
                                        }
                                    } else {
                                        Badge(containerColor = Color(0xFF10B981)) {
                                            Text("FREE", color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                                Text(v.videoUrl, color = Color(0xFF94A3B8), fontSize = 11.sp, maxLines = 1)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Edit Video Title & details
                                IconButton(onClick = {
                                    editingVideo = v
                                    videoTitle = v.title
                                    videoDescription = v.description
                                    videoUrl = v.videoUrl
                                    videoThumbnailUrl = v.thumbnailUrl
                                    videoCategory = v.category
                                    videoSection = v.section
                                    isPaidVideo = v.isPaid
                                    videoPrice = v.price.toInt().toString()
                                    showVideoDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Video", tint = Color(0xFF38BDF8))
                                }

                                // Delete Video
                                IconButton(onClick = {
                                    viewModel.adminDeleteVideo(v.id) {
                                        Toast.makeText(context, "Video deleted", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Video", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // COURSES CATALOG
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Courses Catalog", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
                    Button(
                        onClick = { showCreateCourse = !showCreateCourse },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (showCreateCourse) "Cancel" else "+ Add Course")
                    }
                }
            }

            if (showCreateCourse) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("New Course Details", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = courseTitle, onValueChange = { courseTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = courseDesc, onValueChange = { courseDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isFreeCourse, onCheckedChange = { isFreeCourse = it })
                                Text("Free Course", color = Color.White)
                            }
                            if (!isFreeCourse) {
                                OutlinedTextField(value = coursePrice, onValueChange = { coursePrice = it }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (courseTitle.isNotEmpty()) {
                                        val newCourse = Course(
                                            title = courseTitle,
                                            description = courseDesc,
                                            category = courseCategory,
                                            isFree = isFreeCourse,
                                            price = if (isFreeCourse) 0.0 else coursePrice.toDoubleOrNull() ?: 0.0,
                                            isPublished = true
                                        )
                                        viewModel.adminSaveCourse(newCourse) { success ->
                                            if (success) {
                                                showCreateCourse = false
                                                courseTitle = ""
                                                courseDesc = ""
                                                Toast.makeText(context, "Course published!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Publish Course to Firestore")
                            }
                        }
                    }
                }
            }

            items(courses) { course ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(course.title, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(if (course.isFree) "FREE" else "Price: ₹${course.price.toInt()}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.adminDeleteCourse(course.id) {} }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAttendanceSection(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val attendance by viewModel.allAttendance.collectAsState()
    val context = LocalContext.current

    var selectedUserId by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("2026-10-07") }
    var selectedStatus by remember { mutableStateOf("PRESENT") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Attendance Tracking & Corrections", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mark / Correct Student Attendance", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Student Email/UID:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    users.forEach { u ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedUserId == u.uid,
                                onClick = { selectedUserId = u.uid }
                            )
                            Text(u.displayName + " (${u.email})", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("PRESENT", "ABSENT", "LEAVE").forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (selectedUserId.isNotEmpty()) {
                                val student = users.find { it.uid == selectedUserId }
                                val record = AttendanceRecord(
                                    userId = selectedUserId,
                                    studentName = student?.displayName ?: "Student",
                                    date = selectedDate,
                                    status = selectedStatus
                                )
                                viewModel.adminSaveAttendance(record) { success ->
                                    if (success) Toast.makeText(context, "Attendance updated!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Save Attendance Record")
                    }
                }
            }
        }

        item {
            Text("Recent Attendance Logs", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
        }

        items(attendance.take(15)) { rec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(rec.studentName, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(rec.date, color = Color.Gray, fontSize = 11.sp)
                    }
                    Text(rec.status, color = if (rec.status == "PRESENT") Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminFeesSection(viewModel: MainViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val fees by viewModel.allFees.collectAsState()
    val context = LocalContext.current

    var selectedUser by remember { mutableStateOf("") }
    var feeMonth by remember { mutableStateOf("October 2026") }
    var feeType by remember { mutableStateOf("MONTHLY") }
    var feeDescription by remember { mutableStateOf("") }
    var feeAmount by remember { mutableStateOf("1500") }
    var dueDate by remember { mutableStateOf("10th of Month") }

    // State for Editing an existing Fee Record
    var editingFee by remember { mutableStateOf<FeeRecord?>(null) }
    var editFeeAmount by remember { mutableStateOf("") }
    var editPaidAmount by remember { mutableStateOf("") }
    var editMonth by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("DUE") }

    // Filter by student / fee status
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, DUE, PAID, OTHER
    var feeSearchQuery by remember { mutableStateOf("") }

    val feeTypeOptions = listOf("MONTHLY", "ADMISSION", "EXAM", "LAB", "LIBRARY", "SPORTS", "TRANSPORT", "OTHER")

    val totalAllFees = fees.sumOf { it.totalAmount }
    val totalPaidFees = fees.sumOf { it.paidAmount }
    val totalPendingFees = fees.sumOf { it.pendingAmount }

    val filteredFees = fees.filter { fee ->
        val matchesStatus = when (filterStatus) {
            "DUE" -> fee.pendingAmount > 0 || fee.status == "DUE"
            "PAID" -> fee.status == "PAID"
            "OTHER" -> fee.feeType != "MONTHLY"
            else -> true
        }
        val matchesQuery = feeSearchQuery.isEmpty() ||
                fee.studentName.contains(feeSearchQuery, ignoreCase = true) ||
                fee.month.contains(feeSearchQuery, ignoreCase = true) ||
                fee.feeType.contains(feeSearchQuery, ignoreCase = true)
        matchesStatus && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Student Fee Management (Month-Wise & Category Records)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
        }

        // Summary Metric Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Total Billed", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalAllFees.toInt()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${fees.size} Bills", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Paid Fees", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalPaidFees.toInt()}", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${fees.count { it.status == "PAID" }} Paid", color = Color(0xFF10B981), fontSize = 10.sp)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pending Fees", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text("₹${totalPendingFees.toInt()}", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${fees.count { it.pendingAmount > 0 }} Due", color = Color(0xFFEF4444), fontSize = 10.sp)
                    }
                }
            }
        }

        // Create / Demand Fee Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Create Fee Bill / Fee Demand Notice", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Fee Category / Type:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(feeTypeOptions) { ft ->
                            FilterChip(
                                selected = feeType == ft,
                                onClick = { feeType = ft },
                                label = { Text(ft) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feeMonth,
                        onValueChange = { feeMonth = it },
                        label = { Text("Fee Month / Period (e.g. October 2026, Q3, Term 1)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feeDescription,
                        onValueChange = { feeDescription = it },
                        label = { Text("Fee Purpose / Description (e.g. Science Lab Fee, Tuition)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feeAmount,
                        onValueChange = { feeAmount = it },
                        label = { Text("Total Fee Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (e.g. 15th October 2026)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Target Student:", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    users.forEach { u ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedUser == u.uid, onClick = { selectedUser = u.uid })
                            Text(u.displayName + " (${u.email})", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (selectedUser.isNotEmpty()) {
                                val student = users.find { it.uid == selectedUser }
                                val amt = feeAmount.toDoubleOrNull() ?: 1000.0
                                val fee = FeeRecord(
                                    userId = selectedUser,
                                    studentName = student?.displayName ?: "Student",
                                    month = feeMonth,
                                    feeType = feeType,
                                    feeDescription = feeDescription.ifEmpty { "$feeType Fee ($feeMonth)" },
                                    totalAmount = amt,
                                    paidAmount = 0.0,
                                    pendingAmount = amt,
                                    status = "DUE",
                                    dueDate = dueDate
                                )
                                viewModel.adminSaveFee(fee) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Fee bill created for student!", Toast.LENGTH_SHORT).show()
                                        feeDescription = ""
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Select student first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Create Fee Bill in Firestore")
                    }
                }
            }
        }

        // Filter & Search Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Month-Wise Fee Records Filter:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                OutlinedTextField(
                    value = feeSearchQuery,
                    onValueChange = { feeSearchQuery = it },
                    placeholder = { Text("Search by Student, Month, or Fee Type") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All Fees", "DUE" to "Pending Due", "PAID" to "Paid Fees", "OTHER" to "Other Fees").forEach { (key, label) ->
                        FilterChip(
                            selected = filterStatus == key,
                            onClick = { filterStatus = key },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        if (filteredFees.isEmpty()) {
            item {
                Text("No fee records found for the selected filter.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            items(filteredFees) { fee ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(fee.studentName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text(
                                    text = "${fee.feeType}: ${fee.month} • Due: ₹${fee.pendingAmount.toInt()} / ₹${fee.totalAmount.toInt()} (Paid: ₹${fee.paidAmount.toInt()})",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp
                                )
                                if (fee.feeDescription.isNotEmpty()) {
                                    Text(fee.feeDescription, color = Color.LightGray, fontSize = 11.sp)
                                }
                                Text("Due Date: ${fee.dueDate.ifEmpty { "N/A" }}", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(
                                    containerColor = if (fee.status == "PAID") Color(0xFF16A34A) else Color(0xFFDC2626)
                                ) {
                                    Text(fee.status, color = Color.White, modifier = Modifier.padding(3.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                // Edit Fee Button
                                IconButton(onClick = {
                                    editingFee = fee
                                    editFeeAmount = fee.totalAmount.toInt().toString()
                                    editPaidAmount = fee.paidAmount.toInt().toString()
                                    editMonth = fee.month
                                    editDescription = fee.feeDescription
                                    editStatus = fee.status
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Fee", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }
                                // Delete Fee Button
                                IconButton(onClick = {
                                    viewModel.adminDeleteFee(fee.id) { success ->
                                        Toast.makeText(context, if (success) "Fee deleted!" else "Failed to delete fee", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Fee", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Generate Due/Demand Notice Receipt
                            OutlinedButton(
                                onClick = {
                                    viewModel.adminGenerateFeeReceipt(fee, isDueReceipt = true) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Due Receipt", fontSize = 11.sp)
                            }

                            // Generate Paid Fee Receipt
                            Button(
                                onClick = {
                                    viewModel.adminGenerateFeeReceipt(fee, isDueReceipt = false) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paid Receipt", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Editing Fee Record
    editingFee?.let { targetFee ->
        AlertDialog(
            onDismissRequest = { editingFee = null },
            title = { Text("Edit Student Fee Record", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student: ${targetFee.studentName}", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editMonth,
                        onValueChange = { editMonth = it },
                        label = { Text("Fee Month/Period") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Fee Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editFeeAmount,
                        onValueChange = { editFeeAmount = it },
                        label = { Text("Total Fee (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPaidAmount,
                        onValueChange = { editPaidAmount = it },
                        label = { Text("Paid Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DUE", "PARTIAL", "PAID").forEach { st ->
                            FilterChip(
                                selected = editStatus == st,
                                onClick = { editStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalAmt = editFeeAmount.toDoubleOrNull() ?: targetFee.totalAmount
                        val paidAmt = editPaidAmount.toDoubleOrNull() ?: targetFee.paidAmount
                        val pendingAmt = (totalAmt - paidAmt).coerceAtLeast(0.0)
                        val updated = targetFee.copy(
                            month = editMonth,
                            feeDescription = editDescription,
                            totalAmount = totalAmt,
                            paidAmount = paidAmt,
                            pendingAmount = pendingAmt,
                            status = if (pendingAmt <= 0) "PAID" else editStatus
                        )
                        viewModel.adminSaveFee(updated) { success ->
                            if (success) {
                                Toast.makeText(context, "Fee record updated!", Toast.LENGTH_SHORT).show()
                                editingFee = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingFee = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminPaymentsSection(viewModel: MainViewModel) {
    val payments by viewModel.allPayments.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Payment Approvals & Verification", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
            Text(
                "Only Admin verification marks fees paid, creates receipts, and unlocks paid courses.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        items(payments) { p ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(p.userName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text(
                            text = p.status,
                            color = when (p.status) {
                                "VERIFIED" -> Color(0xFF10B981)
                                "REJECTED" -> Color(0xFFEF4444)
                                else -> Color(0xFFF59E0B)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Amount: ₹${p.amount.toInt()} • Purpose: ${p.purpose}", color = Color(0xFF38BDF8), fontSize = 13.sp)
                    Text("Target: ${p.targetName}", color = Color.LightGray, fontSize = 12.sp)
                    Text("Transaction Reference/UTR: ${p.transactionReference}", color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                    if (p.status == "PENDING_VERIFICATION") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.adminVerifyPayment(p.id, true) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Verify & Approve")
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.adminVerifyPayment(p.id, false) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reject", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsAndBrandingSection(viewModel: MainViewModel) {
    val settings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current

    var instituteName by remember { mutableStateOf(settings.instituteName) }
    var upiId by remember { mutableStateOf(settings.upiId) }
    var payeeName by remember { mutableStateOf(settings.upiPayeeName) }
    var qrImageUrl by remember { mutableStateOf(settings.qrImageUrl) }
    var customIconUrl by remember { mutableStateOf(settings.customAppIconUrl) }
    var instructions by remember { mutableStateOf(settings.paymentInstructions) }
    var banner by remember { mutableStateOf(settings.announcementBanner) }
    var supportPhone by remember { mutableStateOf(settings.supportPhone) }
    var supportEmail by remember { mutableStateOf(settings.supportEmail) }
    var latestAppVersion by remember { mutableStateOf(settings.latestAppVersion) }
    var appVersionFeatures by remember { mutableStateOf(settings.appVersionFeatures) }
    var appUpdateUrl by remember { mutableStateOf(settings.appUpdateUrl) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Android Photo Picker for Gallery QR Image selection (zero-permission)
    val qrImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageLocally(context, uri, "payment_qr_${System.currentTimeMillis()}.png")
            if (savedPath != null) {
                qrImageUrl = savedPath
                Toast.makeText(context, "QR image selected from gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Android Photo Picker for App Icon / Branding image
    val appIconPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageLocally(context, uri, "app_icon_${System.currentTimeMillis()}.png")
            if (savedPath != null) {
                customIconUrl = savedPath
                Toast.makeText(context, "App branding icon selected from gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(settings) {
        instituteName = settings.instituteName
        upiId = settings.upiId
        payeeName = settings.upiPayeeName
        qrImageUrl = settings.qrImageUrl
        customIconUrl = settings.customAppIconUrl
        instructions = settings.paymentInstructions
        banner = settings.announcementBanner
        supportPhone = settings.supportPhone
        supportEmail = settings.supportEmail
        latestAppVersion = settings.latestAppVersion
        appVersionFeatures = settings.appVersionFeatures
        appUpdateUrl = settings.appUpdateUrl
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Payment QR, App Icon & Branding Controls", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)

        // 1. UPI QR Code Gallery Upload Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Upload Gallery QR Payment Photo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Upload or replace the UPI QR code photo shown to students on the payment checkout screen.", color = Color(0xFF94A3B8), fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                if (qrImageUrl.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            AsyncImage(
                                model = qrImageUrl,
                                contentDescription = "Active Payment QR",
                                modifier = Modifier.fillMaxSize().padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Current Payment QR", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextButton(onClick = { qrImageUrl = "" }) {
                                Text("Remove QR", color = Color(0xFFEF4444))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { qrImagePickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (qrImageUrl.isEmpty()) "Upload QR from Gallery" else "Replace QR Photo")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qrImageUrl,
                    onValueChange = { qrImageUrl = it },
                    label = { Text("Or Enter QR Direct Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Admin Change App Icon & In-App Branding Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Change In-App Brand Icon / Emblem", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Upload a new logo/emblem from gallery or web URL. This dynamically updates the Neol Bharat brand header for all users.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (customIconUrl.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = customIconUrl,
                            contentDescription = "Custom App Icon Preview",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Active In-App Icon", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextButton(onClick = { customIconUrl = "" }) {
                                Text("Reset to Default Emblem", color = Color(0xFFEF4444))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { appIconPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (customIconUrl.isEmpty()) "Pick Icon from Gallery" else "Replace App Icon")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customIconUrl,
                    onValueChange = { customIconUrl = it },
                    label = { Text("Or Enter Icon Web Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. UPI Details & Global Sync Card (All App Settings: Edit, Change, Update & Reset/Delete)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Institute Name & Global App Configuration", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text("Update institute branding, payments, support info, and push new version updates to all users.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = instituteName, onValueChange = { instituteName = it }, label = { Text("Institute Name (Shown on Student Home)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("UPI ID (VPA)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = payeeName, onValueChange = { payeeName = it }, label = { Text("Payee Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Payment Instructions") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = banner, onValueChange = { banner = it }, label = { Text("Announcement Banner") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = supportPhone, onValueChange = { supportPhone = it }, label = { Text("Support Phone Number") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = supportEmail, onValueChange = { supportEmail = it }, label = { Text("Support Email Address") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(14.dp))

                // App Version & Feature Broadcast
                Text("Admin Push New App Version & Features", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 14.sp)
                Text("Broadcast latest features to Student Mode and Entertainment Mode users.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = latestAppVersion, onValueChange = { latestAppVersion = it }, label = { Text("App Version (e.g. 2.1.0)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = appVersionFeatures,
                    onValueChange = { appVersionFeatures = it },
                    label = { Text("New Version Features (Broadcast to All Students & Users)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = appUpdateUrl, onValueChange = { appUpdateUrl = it }, label = { Text("App Update Link / GitHub URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val newSettings = settings.copy(
                                instituteName = instituteName,
                                upiId = upiId,
                                upiPayeeName = payeeName,
                                qrImageUrl = qrImageUrl,
                                customAppIconUrl = customIconUrl,
                                paymentInstructions = instructions,
                                announcementBanner = banner,
                                supportPhone = supportPhone,
                                supportEmail = supportEmail,
                                latestAppVersion = latestAppVersion,
                                appVersionFeatures = appVersionFeatures,
                                appUpdateUrl = appUpdateUrl
                            )
                            viewModel.adminSaveAppSettings(newSettings) { success ->
                                if (success) Toast.makeText(context, "All app settings & new version updated globally!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update Settings")
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset / Delete")
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset / Delete All Custom App Settings?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear all custom QR photos, reset branding icons, and revert all payment & support information back to default factory settings across the app.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminResetAppSettings { success ->
                            showResetDialog = false
                            if (success) {
                                Toast.makeText(context, "All custom app settings reset & deleted!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Local file save helper for gallery uploads
private fun saveImageLocally(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val file = File(context.filesDir, fileName)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
        fos.flush()
        fos.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
