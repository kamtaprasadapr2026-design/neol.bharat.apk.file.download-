package com.example.model

import com.google.firebase.Timestamp

enum class UserRole {
    ADMIN,
    STUDENT,
    ENTERTAINMENT
}

enum class UserMode {
    STUDENT,
    ENTERTAINMENT
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: String = "STUDENT",
    val selectedMode: String = "STUDENT",
    val isBlocked: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class StudentProfile(
    val userId: String = "",
    val studentId: String = "",
    val fullName: String = "",
    val email: String = "",
    val gradeClass: String = "",
    val phoneNumber: String = "",
    val enrolledCourses: List<String> = emptyList(),
    val unlockedVideos: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class Course(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "General",
    val thumbnailUrl: String = "",
    val price: Double = 0.0,
    val isFree: Boolean = true,
    val isPublished: Boolean = true,
    val instructorName: String = "Neol Bharat Faculty",
    val lessonsCount: Long = 0,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class Lesson(
    val id: String = "",
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val pdfUrl: String = "",
    val durationMinutes: Long = 15,
    val orderIndex: Long = 1,
    val createdAt: Timestamp? = null
)

data class VideoItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val category: String = "General",
    val section: String = "EDUCATION", // EDUCATION or ENTERTAINMENT
    val isPaid: Boolean = false,
    val price: Double = 0.0,
    val requiredCourseId: String = "",
    val views: Long = 0,
    val likes: Long = 0,
    val isPublished: Boolean = true,
    val createdAt: Timestamp? = null
)

data class ShortItem(
    val id: String = "",
    val title: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val creatorName: String = "Neol Bharat",
    val section: String = "ENTERTAINMENT", // EDUCATION or ENTERTAINMENT
    val likesCount: Long = 0,
    val isPublished: Boolean = true,
    val createdAt: Timestamp? = null
)

data class AttendanceRecord(
    val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val date: String = "", // YYYY-MM-DD
    val status: String = "PRESENT", // PRESENT, ABSENT, LEAVE
    val notes: String = "",
    val recordedAt: Timestamp? = null
)

data class FeeRecord(
    val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val month: String = "", // e.g. "October 2026"
    val feeType: String = "MONTHLY", // MONTHLY, ADMISSION, EXAM, LAB, LIBRARY, SPORTS, TRANSPORT, OTHER
    val feeDescription: String = "", // Custom note/label e.g. "Annual Exam Fee", "Computer Lab"
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val status: String = "DUE", // PAID, PARTIAL, DUE
    val dueDate: String = "",
    val updatedAt: Timestamp? = null
)

data class PaymentRecord(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val amount: Double = 0.0,
    val purpose: String = "COURSE_PURCHASE", // COURSE_PURCHASE or MONTHLY_FEE
    val targetId: String = "", // Course ID or Fee Record ID
    val targetName: String = "",
    val transactionReference: String = "",
    val paymentMethod: String = "UPI",
    val status: String = "PENDING_VERIFICATION", // PENDING_VERIFICATION, VERIFIED, REJECTED
    val verifiedBy: String = "",
    val receiptId: String = "",
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val verifiedAt: Timestamp? = null
)

data class Receipt(
    val id: String = "",
    val receiptNumber: String = "",
    val paymentId: String = "",
    val userId: String = "",
    val studentName: String = "",
    val studentId: String = "",
    val userEmail: String = "",
    val purpose: String = "",
    val targetName: String = "",
    val amount: Double = 0.0,
    val paymentDate: Timestamp? = null,
    val status: String = "VERIFIED",
    val transactionReference: String = ""
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val targetAudience: String = "ALL", // ALL, STUDENT, ENTERTAINMENT, SELECTED
    val targetStudentEmail: String = "", // Optional single student / selected student
    val priority: String = "NORMAL", // NORMAL, URGENT
    val scheduledDate: String = "", // e.g. "2026-10-15" or schedule info
    val createdAt: Timestamp? = null
)

data class AppSettings(
    val instituteName: String = "Neol Bharat Institute",
    val upiId: String = "neolbharat@upi",
    val upiPayeeName: String = "Neol Bharat Education",
    val qrImageUrl: String = "",
    val paymentInstructions: String = "Pay directly via UPI App (GPay, PhonePe, Paytm). Complete payment and click Confirm Payment.",
    val announcementBanner: String = "Welcome to Neol Bharat: Learn, Explore & Elevate!",
    val supportPhone: String = "+91 98765 43210",
    val supportEmail: String = "support@neolbharat.org",
    val customAppIconUrl: String = "",
    val latestAppVersion: String = "2.0.0",
    val appVersionFeatures: String = "• Direct One-Click UPI Payment\n• New Admin Suchna & Announcements tab\n• Instant Month-wise Fee Ledger\n• Enhanced Video Player & Entertainment Mode",
    val appUpdateUrl: String = "https://github.com",
    val updatedAt: Timestamp? = null
)
