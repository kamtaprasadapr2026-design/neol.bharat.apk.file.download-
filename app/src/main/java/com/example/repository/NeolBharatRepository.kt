package com.example.repository

import android.content.Context
import com.example.R
import com.example.model.*
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NeolBharatRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User & Profile ---
    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { it.toObject(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun saveOrUpdateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val uid = requireUserId()
            val path = "users/$uid"
            val data = mapOf(
                "uid" to uid,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "photoUrl" to profile.photoUrl,
                "role" to profile.role,
                "selectedMode" to profile.selectedMode,
                "isBlocked" to profile.isBlocked,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val doc = db.collection("users").document(uid).get().await()
            if (!doc.exists()) {
                val fullData = data + ("createdAt" to FieldValue.serverTimestamp())
                db.collection("users").document(uid).set(fullData).await()
            } else {
                db.collection("users").document(uid).update(data).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/${auth.currentUser?.uid}")
            Result.failure(e)
        }
    }

    suspend fun updateUserSelectedMode(mode: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid).update(
                mapOf(
                    "selectedMode" to mode,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeStudentProfile(userId: String): Flow<StudentProfile?> = flow {
        val path = "studentProfiles/$userId"
        emitAll(
            db.collection("studentProfiles").document(userId)
                .snapshots()
                .map { it.toObject(StudentProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun saveStudentProfile(profile: StudentProfile): Result<Unit> {
        return try {
            val uid = profile.userId.ifEmpty { requireUserId() }
            val path = "studentProfiles/$uid"
            val data = mapOf(
                "userId" to uid,
                "studentId" to profile.studentId,
                "fullName" to profile.fullName,
                "email" to profile.email,
                "gradeClass" to profile.gradeClass,
                "phoneNumber" to profile.phoneNumber,
                "enrolledCourses" to profile.enrolledCourses,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val doc = db.collection("studentProfiles").document(uid).get().await()
            if (!doc.exists()) {
                val fullData = data + ("createdAt" to FieldValue.serverTimestamp())
                db.collection("studentProfiles").document(uid).set(fullData).await()
            } else {
                db.collection("studentProfiles").document(uid).update(data).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Courses ---
    fun observeCourses(): Flow<List<Course>> = flow {
        val path = "courses"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Course::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createOrUpdateCourse(course: Course): Result<String> {
        return try {
            val courseId = course.id.ifEmpty { UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to courseId,
                "title" to course.title,
                "description" to course.description,
                "category" to course.category,
                "thumbnailUrl" to course.thumbnailUrl,
                "price" to course.price,
                "isFree" to course.isFree,
                "isPublished" to course.isPublished,
                "instructorName" to course.instructorName,
                "lessonsCount" to course.lessonsCount,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val doc = db.collection("courses").document(courseId).get().await()
            if (!doc.exists()) {
                val fullData = data + ("createdAt" to FieldValue.serverTimestamp())
                db.collection("courses").document(courseId).set(fullData).await()
            } else {
                db.collection("courses").document(courseId).update(data).await()
            }
            Result.success(courseId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "courses/${course.id}")
            Result.failure(e)
        }
    }

    suspend fun deleteCourse(courseId: String): Result<Unit> {
        return try {
            db.collection("courses").document(courseId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Lessons ---
    fun observeLessons(courseId: String): Flow<List<Lesson>> = flow {
        val path = "courses/$courseId/lessons"
        emitAll(
            db.collection("courses").document(courseId).collection("lessons")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Lesson::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addLesson(courseId: String, lesson: Lesson): Result<String> {
        return try {
            val lessonId = lesson.id.ifEmpty { UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to lessonId,
                "courseId" to courseId,
                "title" to lesson.title,
                "description" to lesson.description,
                "videoUrl" to lesson.videoUrl,
                "pdfUrl" to lesson.pdfUrl,
                "durationMinutes" to lesson.durationMinutes,
                "orderIndex" to lesson.orderIndex,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("courses").document(courseId).collection("lessons").document(lessonId).set(data).await()
            // Increment lessonsCount
            db.collection("courses").document(courseId).update("lessonsCount", FieldValue.increment(1)).await()
            Result.success(lessonId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Videos & Shorts ---
    fun observeVideos(section: String? = null): Flow<List<VideoItem>> = flow {
        val path = "videos"
        val query = if (section != null) db.collection(path).whereEqualTo("section", section) else db.collection(path)
        emitAll(
            query.snapshots()
                .map { snapshot -> snapshot.toObjects(VideoItem::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveVideo(video: VideoItem): Result<String> {
        return try {
            val videoId = video.id.ifEmpty { UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to videoId,
                "title" to video.title,
                "description" to video.description,
                "videoUrl" to video.videoUrl,
                "thumbnailUrl" to video.thumbnailUrl,
                "category" to video.category,
                "section" to video.section,
                "isPaid" to video.isPaid,
                "price" to video.price,
                "requiredCourseId" to video.requiredCourseId,
                "views" to video.views,
                "likes" to video.likes,
                "isPublished" to video.isPublished,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("videos").document(videoId).set(data).await()
            Result.success(videoId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVideo(videoId: String): Result<Unit> {
        return try {
            db.collection("videos").document(videoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeShorts(section: String? = null): Flow<List<ShortItem>> = flow {
        val path = "shorts"
        val query = if (section != null) db.collection(path).whereEqualTo("section", section) else db.collection(path)
        emitAll(
            query.snapshots()
                .map { snapshot -> snapshot.toObjects(ShortItem::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveShort(short: ShortItem): Result<String> {
        return try {
            val shortId = short.id.ifEmpty { UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to shortId,
                "title" to short.title,
                "videoUrl" to short.videoUrl,
                "thumbnailUrl" to short.thumbnailUrl,
                "creatorName" to short.creatorName,
                "section" to short.section,
                "likesCount" to short.likesCount,
                "isPublished" to short.isPublished,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("shorts").document(shortId).set(data).await()
            Result.success(shortId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteShort(shortId: String): Result<Unit> {
        return try {
            db.collection("shorts").document(shortId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Attendance ---
    fun observeStudentAttendance(userId: String): Flow<List<AttendanceRecord>> = flow {
        val path = "attendance"
        emitAll(
            db.collection(path).whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(AttendanceRecord::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllAttendance(): Flow<List<AttendanceRecord>> = flow {
        val path = "attendance"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(AttendanceRecord::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<String> {
        return try {
            val recordId = record.id.ifEmpty { UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to recordId,
                "userId" to record.userId,
                "studentName" to record.studentName,
                "date" to record.date,
                "status" to record.status,
                "notes" to record.notes,
                "recordedAt" to FieldValue.serverTimestamp()
            )
            db.collection("attendance").document(recordId).set(data).await()
            Result.success(recordId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "attendance/${record.id}")
            Result.failure(e)
        }
    }

    // --- Fees ---
    fun observeStudentFees(userId: String): Flow<List<FeeRecord>> = flow {
        val path = "fees"
        emitAll(
            db.collection(path).whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FeeRecord::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllFees(): Flow<List<FeeRecord>> = flow {
        val path = "fees"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FeeRecord::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveFeeRecord(record: FeeRecord): Result<String> {
        return try {
            val feeId = record.id.ifEmpty { UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to feeId,
                "userId" to record.userId,
                "studentName" to record.studentName,
                "month" to record.month,
                "feeType" to record.feeType,
                "feeDescription" to record.feeDescription,
                "totalAmount" to record.totalAmount,
                "paidAmount" to record.paidAmount,
                "pendingAmount" to record.pendingAmount,
                "status" to record.status,
                "dueDate" to record.dueDate,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("fees").document(feeId).set(data).await()
            Result.success(feeId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "fees/${record.id}")
            Result.failure(e)
        }
    }

    suspend fun deleteFeeRecord(feeId: String): Result<Unit> {
        return try {
            db.collection("fees").document(feeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "fees/$feeId")
            Result.failure(e)
        }
    }

    // Admin manually generates a Due/Fee Receipt or Demand Notice directly for a student
    suspend fun adminGenerateFeeReceipt(feeRecord: FeeRecord, receiptStatus: String = "VERIFIED"): Result<String> {
        return try {
            val receiptId = "RCP-" + (System.currentTimeMillis() % 10000000)
            val receiptData = mapOf(
                "id" to receiptId,
                "receiptNumber" to receiptId,
                "paymentId" to feeRecord.id,
                "userId" to feeRecord.userId,
                "studentName" to feeRecord.studentName,
                "studentId" to "NB-${feeRecord.userId.takeLast(6).uppercase()}",
                "userEmail" to "",
                "purpose" to "${feeRecord.feeType} FEE (${feeRecord.month})",
                "targetName" to feeRecord.feeDescription.ifEmpty { "${feeRecord.feeType} Fee - ${feeRecord.month}" },
                "amount" to if (receiptStatus == "DUE") feeRecord.pendingAmount else (if (feeRecord.paidAmount > 0) feeRecord.paidAmount else feeRecord.totalAmount),
                "paymentDate" to FieldValue.serverTimestamp(),
                "status" to receiptStatus,
                "transactionReference" to if (receiptStatus == "DUE") "FEE-DEMAND-DUE" else "ADMIN-OFFLINE-VERIFIED"
            )
            db.collection("receipts").document(receiptId).set(receiptData).await()
            Result.success(receiptId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "receipts")
            Result.failure(e)
        }
    }

    // --- Payments ---
    fun observeStudentPayments(userId: String): Flow<List<PaymentRecord>> = flow {
        val path = "payments"
        emitAll(
            db.collection(path).whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(PaymentRecord::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllPayments(): Flow<List<PaymentRecord>> = flow {
        val path = "payments"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(PaymentRecord::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // Student submits payment proof / UTR transaction reference
    suspend fun submitPayment(record: PaymentRecord): Result<String> {
        return try {
            val uid = requireUserId()
            val paymentId = UUID.randomUUID().toString()
            val data = mapOf(
                "id" to paymentId,
                "userId" to uid,
                "userName" to record.userName,
                "userEmail" to (auth.currentUser?.email ?: ""),
                "amount" to record.amount,
                "purpose" to record.purpose,
                "targetId" to record.targetId,
                "targetName" to record.targetName,
                "transactionReference" to record.transactionReference,
                "paymentMethod" to record.paymentMethod,
                "status" to "PENDING_VERIFICATION", // Always pending verification by server/admin
                "verifiedBy" to "",
                "receiptId" to "",
                "notes" to record.notes,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("payments").document(paymentId).set(data).await()
            Result.success(paymentId)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "payments")
            Result.failure(e)
        }
    }

    // Admin verifies payment: updates status to VERIFIED, generates Receipt, updates Fee or unlocks Course
    suspend fun verifyPayment(paymentId: String, isApproved: Boolean): Result<Unit> {
        return try {
            val adminUid = requireUserId()
            val paymentDoc = db.collection("payments").document(paymentId).get().await()
            val payment = paymentDoc.toObject(PaymentRecord::class.java)
                ?: return Result.failure(IllegalStateException("Payment not found"))

            if (!isApproved) {
                db.collection("payments").document(paymentId).update(
                    mapOf(
                        "status" to "REJECTED",
                        "verifiedBy" to adminUid,
                        "verifiedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                return Result.success(Unit)
            }

            // Create verified Receipt
            val receiptId = "RCP-${System.currentTimeMillis() % 1000000}"
            val receiptData = mapOf(
                "id" to receiptId,
                "receiptNumber" to receiptId,
                "paymentId" to paymentId,
                "userId" to payment.userId,
                "studentName" to payment.userName,
                "studentId" to "NB-${payment.userId.take(6).uppercase()}",
                "userEmail" to payment.userEmail,
                "purpose" to payment.purpose,
                "targetName" to payment.targetName,
                "amount" to payment.amount,
                "paymentDate" to FieldValue.serverTimestamp(),
                "status" to "VERIFIED",
                "transactionReference" to payment.transactionReference
            )
            db.collection("receipts").document(receiptId).set(receiptData).await()

            // Update payment record
            db.collection("payments").document(paymentId).update(
                mapOf(
                    "status" to "VERIFIED",
                    "receiptId" to receiptId,
                    "verifiedBy" to adminUid,
                    "verifiedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // If Course Purchase, enroll student
            if (payment.purpose == "COURSE_PURCHASE" && payment.targetId.isNotEmpty()) {
                db.collection("studentProfiles").document(payment.userId).update(
                    "enrolledCourses", FieldValue.arrayUnion(payment.targetId)
                ).await()
            }

            // If Paid Video Purchase, unlock video for student
            if ((payment.purpose == "VIDEO_PURCHASE" || payment.purpose == "PAID_VIDEO") && payment.targetId.isNotEmpty()) {
                db.collection("studentProfiles").document(payment.userId).update(
                    "unlockedVideos", FieldValue.arrayUnion(payment.targetId)
                ).await()
            }

            // If Fee, mark fee as paid/partially paid
            if (payment.purpose == "MONTHLY_FEE" && payment.targetId.isNotEmpty()) {
                val feeRef = db.collection("fees").document(payment.targetId)
                val feeSnap = feeRef.get().await()
                if (feeSnap.exists()) {
                    val fee = feeSnap.toObject(FeeRecord::class.java)
                    if (fee != null) {
                        val newPaid = fee.paidAmount + payment.amount
                        val newPending = (fee.totalAmount - newPaid).coerceAtLeast(0.0)
                        val status = if (newPending <= 0) "PAID" else "PARTIAL"
                        feeRef.update(
                            mapOf(
                                "paidAmount" to newPaid,
                                "pendingAmount" to newPending,
                                "status" to status,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            }

            // If consolidated total fee payment, mark all student dues as paid
            if (payment.purpose == "TOTAL_FEE" || payment.targetId.startsWith("TOTAL_DUE_")) {
                val studentFees = db.collection("fees").whereEqualTo("userId", payment.userId).get().await()
                for (doc in studentFees.documents) {
                    val fee = doc.toObject(FeeRecord::class.java)
                    if (fee != null && fee.pendingAmount > 0) {
                        doc.reference.update(
                            mapOf(
                                "paidAmount" to fee.totalAmount,
                                "pendingAmount" to 0.0,
                                "status" to "PAID",
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "payments/$paymentId")
            Result.failure(e)
        }
    }

    // --- Receipts ---
    fun observeStudentReceipts(userId: String): Flow<List<Receipt>> = flow {
        val path = "receipts"
        emitAll(
            db.collection(path).whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Receipt::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllReceipts(): Flow<List<Receipt>> = flow {
        val path = "receipts"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Receipt::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    // --- Announcements ---
    fun observeAnnouncements(): Flow<List<Announcement>> = flow {
        val path = "announcements"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Announcement::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveAnnouncement(announcement: Announcement): Result<String> {
        return try {
            val id = announcement.id.ifEmpty { UUID.randomUUID().toString() }
            val data = mapOf(
                "id" to id,
                "title" to announcement.title,
                "content" to announcement.content,
                "targetAudience" to announcement.targetAudience,
                "targetStudentEmail" to announcement.targetStudentEmail,
                "priority" to announcement.priority,
                "scheduledDate" to announcement.scheduledDate,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("announcements").document(id).set(data).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(id: String): Result<Unit> {
        return try {
            db.collection("announcements").document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- AppSettings ---
    fun observeAppSettings(): Flow<AppSettings> = flow {
        val path = "appSettings/global_config"
        emitAll(
            db.collection("appSettings").document("global_config")
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(AppSettings::class.java) ?: AppSettings()
                    } else {
                        AppSettings()
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    emit(AppSettings())
                }
        )
    }

    suspend fun saveAppSettings(settings: AppSettings): Result<Unit> {
        return try {
            val data = mapOf(
                "instituteName" to settings.instituteName,
                "upiId" to settings.upiId,
                "upiPayeeName" to settings.upiPayeeName,
                "qrImageUrl" to settings.qrImageUrl,
                "paymentInstructions" to settings.paymentInstructions,
                "announcementBanner" to settings.announcementBanner,
                "supportPhone" to settings.supportPhone,
                "supportEmail" to settings.supportEmail,
                "customAppIconUrl" to settings.customAppIconUrl,
                "latestAppVersion" to settings.latestAppVersion,
                "appVersionFeatures" to settings.appVersionFeatures,
                "appUpdateUrl" to settings.appUpdateUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("appSettings").document("global_config").set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetOrDeleteAppSettings(): Result<Unit> {
        return try {
            val defaultSettings = AppSettings()
            val data = mapOf(
                "instituteName" to defaultSettings.instituteName,
                "upiId" to defaultSettings.upiId,
                "upiPayeeName" to defaultSettings.upiPayeeName,
                "qrImageUrl" to "",
                "paymentInstructions" to defaultSettings.paymentInstructions,
                "announcementBanner" to defaultSettings.announcementBanner,
                "supportPhone" to defaultSettings.supportPhone,
                "supportEmail" to defaultSettings.supportEmail,
                "customAppIconUrl" to "",
                "latestAppVersion" to defaultSettings.latestAppVersion,
                "appVersionFeatures" to defaultSettings.appVersionFeatures,
                "appUpdateUrl" to defaultSettings.appUpdateUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("appSettings").document("global_config").set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Student & User Management (For Admin) ---
    suspend fun adminCreateStudent(displayName: String, email: String, gradeClass: String, initialPasswordOrPhone: String, photoUrl: String = ""): Result<String> {
        return try {
            val studentUid = "std_" + UUID.randomUUID().toString().take(10)
            val userProfileData = mapOf(
                "uid" to studentUid,
                "email" to email,
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "role" to "STUDENT",
                "selectedMode" to "STUDENT",
                "isBlocked" to false,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(studentUid).set(userProfileData).await()

            val studentProfileData = mapOf(
                "userId" to studentUid,
                "studentId" to "NB-${studentUid.takeLast(6).uppercase()}",
                "fullName" to displayName,
                "email" to email,
                "gradeClass" to gradeClass,
                "phoneNumber" to initialPasswordOrPhone,
                "enrolledCourses" to emptyList<String>(),
                "unlockedVideos" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("studentProfiles").document(studentUid).set(studentProfileData).await()

            Result.success(studentUid)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users")
            Result.failure(e)
        }
    }

    suspend fun adminUpdateStudentProfile(userId: String, displayName: String, email: String, gradeClass: String, phone: String, photoUrl: String = ""): Result<Unit> {
        return try {
            val userUpdates = mutableMapOf<String, Any>(
                "displayName" to displayName,
                "email" to email,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (photoUrl.isNotEmpty()) {
                userUpdates["photoUrl"] = photoUrl
            }
            db.collection("users").document(userId).update(userUpdates).await()

            val studentDoc = db.collection("studentProfiles").document(userId).get().await()
            val studentUpdates = mutableMapOf<String, Any>(
                "fullName" to displayName,
                "email" to email,
                "gradeClass" to gradeClass,
                "phoneNumber" to phone,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (studentDoc.exists()) {
                db.collection("studentProfiles").document(userId).update(studentUpdates).await()
            } else {
                studentUpdates["userId"] = userId
                studentUpdates["studentId"] = "NB-${userId.takeLast(6).uppercase()}"
                studentUpdates["createdAt"] = FieldValue.serverTimestamp()
                db.collection("studentProfiles").document(userId).set(studentUpdates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$userId")
            Result.failure(e)
        }
    }

    suspend fun adminDeleteUser(userId: String): Result<Unit> {
        return try {
            db.collection("users").document(userId).delete().await()
            db.collection("studentProfiles").document(userId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$userId")
            Result.failure(e)
        }
    }

    suspend fun adminToggleBlockUser(userId: String, isBlocked: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(
                mapOf(
                    "isBlocked" to isBlocked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- All Students (For Admin) ---
    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(UserProfile::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }
}
