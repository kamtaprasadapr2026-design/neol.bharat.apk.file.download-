package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.util.ReceiptPdfGenerator
import com.example.viewmodel.MainViewModel

@Composable
fun StudentModeScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    studentProfile: StudentProfile?,
    onOpenPaymentDialog: (Double, String, String, String) -> Unit,
    onOpenReceipt: (Receipt) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Home, 1: Videos, 2: Courses, 3: Shorts, 4: Profile

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("tab_student_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = "Video") },
                    label = { Text("Video") },
                    modifier = Modifier.testTag("tab_student_video")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.School, contentDescription = "Courses") },
                    label = { Text("Courses") },
                    modifier = Modifier.testTag("tab_student_courses")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = "Shorts") },
                    label = { Text("Shorts") },
                    modifier = Modifier.testTag("tab_student_shorts")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("tab_student_profile")
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF020617))
        ) {
            when (selectedTab) {
                0 -> StudentHomeScreen(
                    viewModel = viewModel,
                    userProfile = userProfile,
                    studentProfile = studentProfile,
                    onNavigateToCourses = { selectedTab = 2 },
                    onNavigateToVideos = { selectedTab = 1 },
                    onPayFee = { fee ->
                        onOpenPaymentDialog(fee.pendingAmount, "MONTHLY_FEE", fee.id, "Fee ${fee.month}")
                    }
                )
                1 -> StudentVideosScreen(
                    viewModel = viewModel,
                    studentProfile = studentProfile,
                    onBuyVideo = { video ->
                        onOpenPaymentDialog(video.price, "VIDEO_PURCHASE", video.id, video.title)
                    }
                )
                2 -> StudentCoursesScreen(
                    viewModel = viewModel,
                    studentProfile = studentProfile,
                    onBuyCourse = { course ->
                        onOpenPaymentDialog(course.price, "COURSE_PURCHASE", course.id, course.title)
                    }
                )
                3 -> ShortsFeedScreen(viewModel = viewModel, section = "EDUCATION")
                4 -> StudentProfileScreen(
                    viewModel = viewModel,
                    userProfile = userProfile,
                    studentProfile = studentProfile,
                    onViewReceipt = onOpenReceipt
                )
            }
        }
    }
}

@Composable
fun StudentHomeScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    studentProfile: StudentProfile?,
    onNavigateToCourses: () -> Unit,
    onNavigateToVideos: () -> Unit,
    onPayFee: (FeeRecord) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val videos by viewModel.educationVideos.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val fees by viewModel.studentFees.collectAsState()
    val attendance by viewModel.studentAttendance.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Institute Name Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (appSettings.customAppIconUrl.isNotEmpty()) {
                        AsyncImage(
                            model = appSettings.customAppIconUrl,
                            contentDescription = "Institute Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appSettings.instituteName.ifEmpty { "NEOL BHARAT INSTITUTE" },
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Admin Portal & Educational Institute • v${appSettings.latestAppVersion}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Top Student Profile Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userProfile.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Namaste, ${userProfile.displayName}!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Student ID: ${studentProfile?.studentId ?: "NB-STUDENT"} • ${studentProfile?.gradeClass ?: "Grade 10"}",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // App Version Update Notice Card (Features & Direct Access)
        if (appSettings.appVersionFeatures.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF134E4A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("New Version ${appSettings.latestAppVersion} Available!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                            Badge(containerColor = Color(0xFF2DD4BF)) {
                                Text("New Features", color = Color(0xFF134E4A), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = appSettings.appVersionFeatures,
                            color = Color(0xFFCCFBF1),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Announcement / Notice Banner
        if (announcements.isNotEmpty() || appSettings.announcementBanner.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = announcements.firstOrNull()?.content ?: appSettings.announcementBanner,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Education Quick Stats: Attendance & Fee Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Attendance Card
                val presentCount = attendance.count { it.status == "PRESENT" }
                val totalDays = attendance.size
                val attendancePercent = if (totalDays > 0) (presentCount * 100) / totalDays else 100
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Attendance", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$attendancePercent%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "$presentCount / $totalDays Recorded Days",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Fee Status Card (Total & Pending Summary)
                val totalFee = fees.sumOf { it.totalAmount }
                val pendingFee = fees.sumOf { it.pendingAmount }
                val paidFee = fees.sumOf { it.paidAmount }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Fee Due", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "₹ ${pendingFee.toInt()}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (pendingFee > 0) Color(0xFFF43F5E) else Color(0xFF10B981)
                        )
                        Text(
                            text = "Total: ₹${totalFee.toInt()} | Paid: ₹${paidFee.toInt()}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        if (pendingFee > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val consolidatedFee = FeeRecord(
                                        id = "TOTAL_DUE_${System.currentTimeMillis()}",
                                        userId = userProfile.uid,
                                        studentName = userProfile.displayName,
                                        month = "Consolidated All Months",
                                        feeType = "TOTAL_DUE",
                                        feeDescription = "Total Outstanding Dues (${fees.filter { it.pendingAmount > 0 }.size} Bills)",
                                        totalAmount = pendingFee,
                                        paidAmount = 0.0,
                                        pendingAmount = pendingFee,
                                        status = "DUE"
                                    )
                                    onPayFee(consolidatedFee)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                Text("Pay Total Due", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Student Month-wise & Category-wise Fees Ledger on Home Screen
        if (fees.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Month-Wise Fee Status / शुल्क विवरण",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Pending: ₹${fees.sumOf { it.pendingAmount }.toInt()} • Total: ₹${fees.sumOf { it.totalAmount }.toInt()}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
                            }
                            if (fees.any { it.pendingAmount > 0 }) {
                                Button(
                                    onClick = {
                                        val totalPending = fees.sumOf { it.pendingAmount }
                                        val consolidatedFee = FeeRecord(
                                            id = "TOTAL_DUE_${System.currentTimeMillis()}",
                                            userId = userProfile.uid,
                                            studentName = userProfile.displayName,
                                            month = "All Pending Months",
                                            feeType = "CONSOLIDATED",
                                            feeDescription = "Pay All Outstanding Fees",
                                            totalAmount = totalPending,
                                            paidAmount = 0.0,
                                            pendingAmount = totalPending,
                                            status = "DUE"
                                        )
                                        onPayFee(consolidatedFee)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Pay Total Fee", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        fees.forEach { feeItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (feeItem.status == "PAID") Color(0xFF1E293B) else Color(0xFF2D1B28)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${feeItem.month} (${feeItem.feeType})",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = feeItem.feeDescription.ifEmpty { "Tuition / Educational Fee" },
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "Due: ₹${feeItem.pendingAmount.toInt()} / ₹${feeItem.totalAmount.toInt()} (Paid: ₹${feeItem.paidAmount.toInt()}) • Due by ${feeItem.dueDate.ifEmpty { "10th" }}",
                                            color = if (feeItem.pendingAmount > 0) Color(0xFFFCA5A5) else Color(0xFF86EFAC),
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (feeItem.pendingAmount > 0) {
                                        Button(
                                            onClick = { onPayFee(feeItem) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Pay Fee", fontSize = 12.sp)
                                        }
                                    } else {
                                        Badge(containerColor = Color(0xFF16A34A)) {
                                            Text("PAID", color = Color.White, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Free Courses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Free Courses / मुफ़्त कोर्स",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                TextButton(onClick = onNavigateToCourses) {
                    Text("View All", color = Color(0xFF38BDF8))
                }
            }
            val freeCourses = courses.filter { it.isFree }
            if (freeCourses.isEmpty()) {
                Text(
                    text = "No free courses currently uploaded.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(freeCourses) { course ->
                        CourseHorizontalCard(course = course)
                    }
                }
            }
        }

        // Paid Courses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Featured Paid Courses",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                TextButton(onClick = onNavigateToCourses) {
                    Text("Explore", color = Color(0xFF38BDF8))
                }
            }
            val paidCourses = courses.filter { !it.isFree }
            if (paidCourses.isEmpty()) {
                Text(
                    text = "No paid courses available yet.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(paidCourses) { course ->
                        CourseHorizontalCard(course = course)
                    }
                }
            }
        }

        // Video Lectures Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Video Lectures / वीडियो कक्षाएं",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                TextButton(onClick = onNavigateToVideos) {
                    Text("More Videos", color = Color(0xFF38BDF8))
                }
            }
            if (videos.isEmpty()) {
                Text(
                    text = "No video lectures available.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(videos) { video ->
                        VideoLectureCard(video = video)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun CourseHorizontalCard(course: Course) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(210.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                if (course.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = course.thumbnailUrl,
                        contentDescription = course.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                // Free/Paid Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (course.isFree) Color(0xFF16A34A) else Color(0xFF0284C7))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (course.isFree) "FREE" else "₹${course.price.toInt()}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
                Text(
                    text = course.category,
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${course.lessonsCount} Lessons • ${course.instructorName}",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun VideoLectureCard(video: VideoItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp),
        onClick = {
            if (video.videoUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                context.startActivity(intent)
            }
        },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                if (video.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = video.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(36.dp)
                )
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${video.views} views",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun StudentVideosScreen(
    viewModel: MainViewModel,
    studentProfile: StudentProfile?,
    onBuyVideo: (VideoItem) -> Unit
) {
    val videos by viewModel.educationVideos.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Educational Video Lectures",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No videos uploaded by admin yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(videos) { video ->
                    val isUnlocked = !video.isPaid ||
                            (studentProfile?.unlockedVideos?.contains(video.id) == true) ||
                            (video.requiredCourseId.isNotEmpty() && studentProfile?.enrolledCourses?.contains(video.requiredCourseId) == true)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (isUnlocked) {
                                if (video.videoUrl.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                                    context.startActivity(intent)
                                }
                            } else {
                                onBuyVideo(video)
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (video.thumbnailUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = video.thumbnailUrl,
                                        contentDescription = video.title,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUnlocked) Icons.Default.PlayArrow else Icons.Default.Lock,
                                        contentDescription = if (isUnlocked) "Play" else "Locked",
                                        tint = if (isUnlocked) Color.White else Color(0xFFFBBF24),
                                        modifier = Modifier.size(34.dp)
                                    )
                                }

                                // Paid / Free Badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isUnlocked) Color(0xFF16A34A) else Color(0xFFDC2626))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isUnlocked) (if (video.isPaid) "UNLOCKED" else "FREE") else "₹${video.price.toInt()} PAY TO UNLOCK",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = video.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = video.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Category: ${video.category}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp
                                    )
                                    if (!isUnlocked) {
                                        Button(
                                            onClick = { onBuyVideo(video) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Unlock ₹${video.price.toInt()}", fontSize = 11.sp)
                                        }
                                    } else {
                                        Text(
                                            text = "${video.views} views • ${video.likes} likes",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCoursesScreen(
    viewModel: MainViewModel,
    studentProfile: StudentProfile?,
    onBuyCourse: (Course) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Enrolled/Free, 2: Premium

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Courses & Curriculum",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All Courses") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Free / Enrolled") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Premium") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        val enrolledList = studentProfile?.enrolledCourses ?: emptyList()
        val filtered = when (selectedTab) {
            1 -> courses.filter { it.isFree || enrolledList.contains(it.id) }
            2 -> courses.filter { !it.isFree }
            else -> courses
        }

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No courses found in this category.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filtered) { course ->
                    val isUnlocked = course.isFree || enrolledList.contains(course.id)
                    CourseDetailedCard(
                        course = course,
                        isUnlocked = isUnlocked,
                        onBuy = { onBuyCourse(course) }
                    )
                }
            }
        }
    }
}

@Composable
fun CourseDetailedCard(
    course: Course,
    isUnlocked: Boolean,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                if (course.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = course.thumbnailUrl,
                        contentDescription = course.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isUnlocked) Color(0xFF16A34A) else Color(0xFFEA580C))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "UNLOCKED" else "LOCKED (₹${course.price.toInt()})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = course.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${course.lessonsCount} Lessons • ${course.instructorName}",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp
                    )
                    if (!isUnlocked) {
                        Button(
                            onClick = onBuy,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unlock Course")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { /* Open lessons */ },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Start Learning", color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile,
    studentProfile: StudentProfile?,
    onViewReceipt: (Receipt) -> Unit
) {
    val context = LocalContext.current
    val receipts by viewModel.studentReceipts.collectAsState()
    val attendance by viewModel.studentAttendance.collectAsState()
    val fees by viewModel.studentFees.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (userProfile.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile.photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile.displayName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 32.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = userProfile.displayName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = userProfile.email,
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Badge(containerColor = Color(0xFF0284C7)) {
                        Text(
                            text = "Student ID: ${studentProfile?.studentId ?: "NB-STUDENT"}",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Attendance Record Summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "My Attendance History",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (attendance.isEmpty()) {
                        Text("No attendance recorded yet.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        attendance.take(5).forEach { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(rec.date, color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                val statusColor = when (rec.status) {
                                    "PRESENT" -> Color(0xFF10B981)
                                    "ABSENT" -> Color(0xFFEF4444)
                                    else -> Color(0xFFF59E0B)
                                }
                                Text(rec.status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Fee History
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Monthly Fees Ledger",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (fees.isEmpty()) {
                        Text("No fee records registered.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        fees.forEach { fee ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(fee.month, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("Total: ₹${fee.totalAmount.toInt()} | Paid: ₹${fee.paidAmount.toInt()}", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(
                                    text = fee.status,
                                    color = if (fee.status == "PAID") Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Receipts Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verified Payment Receipts / रसीदें",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (receipts.isEmpty()) {
                        Text("No verified receipts yet. Generated automatically after admin payment confirmation.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        receipts.forEach { receipt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(receipt.receiptNumber, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${receipt.purpose}: ₹${receipt.amount.toInt()}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { onViewReceipt(receipt) }) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Download Receipt PDF",
                                            tint = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Switch Mode or Logout Options
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Preferences & Account",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.selectExperienceMode("ENTERTAINMENT") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch to Entertainment Mode")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.signOut(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
