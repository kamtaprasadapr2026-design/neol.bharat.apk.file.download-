package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppSettings
import com.example.viewmodel.MainViewModel

@Composable
fun OnlinePaymentDialog(
    amount: Double,
    purpose: String,
    targetId: String,
    targetName: String,
    appSettings: AppSettings,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("payment_modal"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Secure UPI Payment",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Summary Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Payee: ${appSettings.upiPayeeName}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        Text("UPI ID: ${appSettings.upiId}", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("For: $targetName", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Amount: ₹${amount.toInt()}", color = Color(0xFF10B981), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pay Now Button (Deep link to UPI apps)
                Button(
                    onClick = {
                        val upiUri = Uri.parse(
                            "upi://pay?pa=${appSettings.upiId}&pn=${Uri.encode(appSettings.upiPayeeName)}&am=${amount}&cu=INR&tn=${Uri.encode("Neol Bharat $purpose")}"
                        )
                        val intent = Intent(Intent.ACTION_VIEW, upiUri)
                        try {
                            context.startActivity(Intent.createChooser(intent, "Pay via UPI App"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "No UPI App found. Copy UPI ID and pay via banking app.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pay Now via Installed UPI App")
                }

                // If QR code image is uploaded by Admin, display it
                if (appSettings.qrImageUrl.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Or Scan Official QR Code:",
                        color = Color(0xFFE2E8F0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier
                            .size(160.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        coil.compose.AsyncImage(
                            model = appSettings.qrImageUrl,
                            contentDescription = "Official UPI Payment QR",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "After completing payment via your UPI app, click below to confirm. Your payment will be verified by the admin:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isSubmitting = true
                        val autoRef = "UPI-DIR-${System.currentTimeMillis() % 100000000}"
                        viewModel.submitPayment(
                            amount = amount,
                            purpose = purpose,
                            targetId = targetId,
                            targetName = targetName,
                            utr = autoRef
                        ) { success, msg ->
                            isSubmitting = false
                            Toast.makeText(context, if (success) "Payment request submitted for Admin verification!" else msg, Toast.LENGTH_LONG).show()
                            if (success) onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Payment / भुगतान की पुष्टि करें", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
