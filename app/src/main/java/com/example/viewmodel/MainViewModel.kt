package com.example.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.model.*
import com.example.repository.NeolBharatRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser, val profile: UserProfile?) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class MainViewModel(
    private val repository: NeolBharatRepository
) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _studentProfile = MutableStateFlow<StudentProfile?>(null)
    val studentProfile: StateFlow<StudentProfile?> = _studentProfile.asStateFlow()

    // Data streams
    val courses: StateFlow<List<Course>> = repository.observeCourses()
        .catch { Log.e("MainVM", "Error observing courses", it); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val educationVideos: StateFlow<List<VideoItem>> = repository.observeVideos("EDUCATION")
        .catch { Log.e("MainVM", "Error observing edu videos", it); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entertainmentVideos: StateFlow<List<VideoItem>> = repository.observeVideos("ENTERTAINMENT")
        .catch { Log.e("MainVM", "Error observing entertainment videos", it); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shorts: StateFlow<List<ShortItem>> = repository.observeShorts()
        .catch { Log.e("MainVM", "Error observing shorts", it); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<Announcement>> = repository.observeAnnouncements()
        .catch { Log.e("MainVM", "Error observing announcements", it); emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings> = repository.observeAppSettings()
        .catch { Log.e("MainVM", "Error observing settings", it); emit(AppSettings()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // Student personal streams
    private val _studentAttendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val studentAttendance: StateFlow<List<AttendanceRecord>> = _studentAttendance.asStateFlow()

    private val _studentFees = MutableStateFlow<List<FeeRecord>>(emptyList())
    val studentFees: StateFlow<List<FeeRecord>> = _studentFees.asStateFlow()

    private val _studentPayments = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val studentPayments: StateFlow<List<PaymentRecord>> = _studentPayments.asStateFlow()

    private val _studentReceipts = MutableStateFlow<List<Receipt>>(emptyList())
    val studentReceipts: StateFlow<List<Receipt>> = _studentReceipts.asStateFlow()

    // Admin streams
    val allUsers: StateFlow<List<UserProfile>> = repository.observeAllUsers()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.observeAllAttendance()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFees: StateFlow<List<FeeRecord>> = repository.observeAllFees()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentRecord>> = repository.observeAllPayments()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReceipts: StateFlow<List<Receipt>> = repository.observeAllReceipts()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            loadUserData(user)
        } else {
            _userProfile.value = null
            _studentProfile.value = null
            _authUiState.value = AuthUiState.Idle
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        auth.currentUser?.let { loadUserData(it) }
    }

    private fun loadUserData(user: FirebaseUser) {
        viewModelScope.launch {
            // Check if bootstrap admin
            val isAdminEmail = user.email == "kamtaprasadapr2026@gmail.com"
            repository.observeUserProfile(user.uid).collect { profile ->
                if (profile == null) {
                    // Initialize new profile
                    val newRole = if (isAdminEmail) "ADMIN" else "STUDENT"
                    val newProfile = UserProfile(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = user.displayName ?: "Student",
                        photoUrl = user.photoUrl?.toString() ?: "",
                        role = newRole,
                        selectedMode = "STUDENT"
                    )
                    repository.saveOrUpdateUserProfile(newProfile)
                    _userProfile.value = newProfile
                } else {
                    val updatedProfile = if (isAdminEmail && profile.role != "ADMIN") {
                        profile.copy(role = "ADMIN")
                    } else profile
                    _userProfile.value = updatedProfile
                }
                _authUiState.value = AuthUiState.Authenticated(user, _userProfile.value)
            }
        }

        viewModelScope.launch {
            repository.observeStudentProfile(user.uid).collect { sp ->
                if (sp == null) {
                    val newSp = StudentProfile(
                        userId = user.uid,
                        studentId = "NB-${user.uid.take(6).uppercase()}",
                        fullName = user.displayName ?: "Student",
                        email = user.email ?: "",
                        gradeClass = "General"
                    )
                    repository.saveStudentProfile(newSp)
                    _studentProfile.value = newSp
                } else {
                    _studentProfile.value = sp
                }
            }
        }

        viewModelScope.launch {
            repository.observeStudentAttendance(user.uid).collect {
                _studentAttendance.value = it
            }
        }

        viewModelScope.launch {
            repository.observeStudentFees(user.uid).collect {
                _studentFees.value = it
            }
        }

        viewModelScope.launch {
            repository.observeStudentPayments(user.uid).collect {
                _studentPayments.value = it
            }
        }

        viewModelScope.launch {
            repository.observeStudentReceipts(user.uid).collect {
                _studentReceipts.value = it
            }
        }
    }

    fun selectExperienceMode(mode: String) {
        viewModelScope.launch {
            repository.updateUserSelectedMode(mode)
            _userProfile.value = _userProfile.value?.copy(selectedMode = mode)
        }
    }

    // Google Sign-In interactive
    fun signInWithGoogle(context: Context) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _authUiState.value = AuthUiState.Error("Google Web Client ID missing")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        _authUiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user?.let { loadUserData(it) }
                } else {
                    _authUiState.value = AuthUiState.Error("Unexpected credential type returned")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google sign-in cancelled: ${e.message}", e)
                _authUiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                Log.e("Auth", "Google sign-in failed", e)
                _authUiState.value = AuthUiState.Error(e.localizedMessage ?: "Sign-in failed")
            }
        }
    }

    // Auto-sign-in on startup
    fun attemptAutoSignIn(context: Context) {
        if (auth.currentUser != null) return
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return
        }
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                }
            } catch (e: Exception) {
                // Ignore silent fail, user will see interactive Google button
            }
        }
    }

    fun signOut(context: Context) {
        auth.signOut()
        viewModelScope.launch {
            try {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed clearing credential state", e)
            } finally {
                _currentUser.value = null
                _userProfile.value = null
                _studentProfile.value = null
                _authUiState.value = AuthUiState.Idle
            }
        }
    }

    // Student Actions
    fun submitPayment(amount: Double, purpose: String, targetId: String, targetName: String, utr: String, onComplete: (Boolean, String) -> Unit) {
        val user = _currentUser.value ?: return onComplete(false, "Not authenticated")
        viewModelScope.launch {
            val record = PaymentRecord(
                userId = user.uid,
                userName = user.displayName ?: "Student",
                userEmail = user.email ?: "",
                amount = amount,
                purpose = purpose,
                targetId = targetId,
                targetName = targetName,
                transactionReference = utr,
                paymentMethod = "UPI"
            )
            val result = repository.submitPayment(record)
            if (result.isSuccess) {
                onComplete(true, "Payment request submitted. Verification in progress.")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Submission failed")
            }
        }
    }

    // Admin Actions
    fun adminVerifyPayment(paymentId: String, approve: Boolean, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyPayment(paymentId, approve)
            if (result.isSuccess) {
                onComplete(true, if (approve) "Payment verified and receipt generated." else "Payment marked rejected.")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed verifying payment")
            }
        }
    }

    fun adminSaveAttendance(record: AttendanceRecord, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveAttendanceRecord(record)
            onComplete(result.isSuccess)
        }
    }

    fun adminSaveFee(record: FeeRecord, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveFeeRecord(record)
            onComplete(result.isSuccess)
        }
    }

    fun adminDeleteFee(feeId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteFeeRecord(feeId)
            onComplete(result.isSuccess)
        }
    }

    fun adminGenerateFeeReceipt(feeRecord: FeeRecord, isDueReceipt: Boolean, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val status = if (isDueReceipt) "DUE" else "VERIFIED"
            val result = repository.adminGenerateFeeReceipt(feeRecord, status)
            if (result.isSuccess) {
                val receiptNum = result.getOrNull()
                onComplete(true, "Receipt $receiptNum generated & synced in realtime!")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed generating receipt")
            }
        }
    }

    fun adminSaveCourse(course: Course, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.createOrUpdateCourse(course)
            onComplete(result.isSuccess)
        }
    }

    fun adminDeleteCourse(courseId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteCourse(courseId)
            onComplete(result.isSuccess)
        }
    }

    fun adminAddLesson(courseId: String, lesson: Lesson, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.addLesson(courseId, lesson)
            onComplete(result.isSuccess)
        }
    }

    fun adminSaveVideo(video: VideoItem, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveVideo(video)
            onComplete(result.isSuccess)
        }
    }

    fun adminDeleteVideo(videoId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteVideo(videoId)
            onComplete(result.isSuccess)
        }
    }

    fun adminSaveShort(short: ShortItem, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveShort(short)
            onComplete(result.isSuccess)
        }
    }

    fun adminDeleteShort(shortId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteShort(shortId)
            onComplete(result.isSuccess)
        }
    }

    fun adminSaveAnnouncement(announcement: Announcement, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveAnnouncement(announcement)
            onComplete(result.isSuccess)
        }
    }

    fun adminDeleteAnnouncement(announcementId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteAnnouncement(announcementId)
            onComplete(result.isSuccess)
        }
    }

    fun adminCreateStudent(displayName: String, email: String, gradeClass: String, initialPasswordOrPhone: String, photoUrl: String = "", onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.adminCreateStudent(displayName, email, gradeClass, initialPasswordOrPhone, photoUrl)
            if (result.isSuccess) {
                onComplete(true, "Student profile uploaded & created in Firestore!")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed to create student")
            }
        }
    }

    fun adminUpdateStudentProfile(userId: String, displayName: String, email: String, gradeClass: String, phone: String, photoUrl: String = "", onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.adminUpdateStudentProfile(userId, displayName, email, gradeClass, phone, photoUrl)
            if (result.isSuccess) {
                onComplete(true, "Student profile updated in Firestore!")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed to update student")
            }
        }
    }

    fun adminDeleteUser(userId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.adminDeleteUser(userId)
            if (result.isSuccess) {
                onComplete(true, "Student deleted from database.")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed to delete student")
            }
        }
    }

    fun adminToggleBlockUser(userId: String, isBlocked: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.adminToggleBlockUser(userId, isBlocked)
            onComplete(result.isSuccess)
        }
    }

    fun adminSaveAppSettings(settings: AppSettings, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveAppSettings(settings)
            onComplete(result.isSuccess)
        }
    }

    fun adminResetAppSettings(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.resetOrDeleteAppSettings()
            onComplete(result.isSuccess)
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}
