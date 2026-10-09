package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Receipt
import com.example.repository.NeolBharatRepository
import com.example.ui.components.OnlinePaymentDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ReceiptPdfGenerator
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = NeolBharatRepository(applicationContext)
        viewModel = MainViewModel(repository)

        // Attempt silent auto-sign-in on startup
        viewModel.attemptAutoSignIn(this)

        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF020617))
                ) {
                    AppRootContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppRootContent(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val studentProfile by viewModel.studentProfile.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()

    var showAdminPanel by remember { mutableStateOf(false) }

    // Payment Dialog state
    var paymentDialogData by remember {
        mutableStateOf<PaymentModalState?>(null)
    }

    if (currentUser == null) {
        // Auth Gate: Google Sign In
        AuthGateScreen(
            viewModel = viewModel,
            authUiState = authUiState
        )
    } else {
        val profile = userProfile
        if (profile == null) {
            // Loading profile from Firestore
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = Color(0xFF0284C7))
            }
        } else if (profile.selectedMode.isEmpty()) {
            // First time experience choice
            ModeSelectionDialog(
                onSelectMode = { mode ->
                    viewModel.selectExperienceMode(mode)
                }
            )
        } else {
            // If user is ADMIN, they have access to open the Admin Panel
            val isAdmin = profile.role == "ADMIN" || profile.email == "kamtaprasadapr2026@gmail.com"

            if (showAdminPanel && isAdmin) {
                AdminPanelScreen(
                    viewModel = viewModel,
                    userProfile = profile,
                    onCloseAdmin = { showAdminPanel = false }
                )
            } else {
                when (profile.selectedMode) {
                    "STUDENT" -> StudentModeScreen(
                        viewModel = viewModel,
                        userProfile = profile,
                        studentProfile = studentProfile,
                        onOpenPaymentDialog = { amt, purpose, id, name ->
                            paymentDialogData = PaymentModalState(amt, purpose, id, name)
                        },
                        onOpenReceipt = { receipt ->
                            val pdfFile = ReceiptPdfGenerator.generateA4ReceiptPdf(context, receipt)
                            if (pdfFile != null && pdfFile.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        pdfFile
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Open Receipt PDF"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Receipt PDF generated at ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Unable to generate PDF receipt", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    else -> EntertainmentModeScreen(
                        viewModel = viewModel,
                        userProfile = profile
                    )
                }

                // If user is Admin, show floating Admin Panel button in their session
                if (isAdmin) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 90.dp, end = 16.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        FloatingActionButton(
                            onClick = { showAdminPanel = true },
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Admin Panel"
                            )
                        }
                    }
                }
            }
        }
    }

    // Active Payment Dialog
    paymentDialogData?.let { data ->
        OnlinePaymentDialog(
            amount = data.amount,
            purpose = data.purpose,
            targetId = data.targetId,
            targetName = data.targetName,
            appSettings = appSettings,
            viewModel = viewModel,
            onDismiss = { paymentDialogData = null }
        )
    }
}

data class PaymentModalState(
    val amount: Double,
    val purpose: String,
    val targetId: String,
    val targetName: String
)
