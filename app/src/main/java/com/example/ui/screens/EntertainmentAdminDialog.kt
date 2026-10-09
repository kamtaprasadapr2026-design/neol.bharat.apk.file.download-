package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoItem
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntertainmentAdminDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val videos by viewModel.entertainmentVideos.collectAsState()
    val context = LocalContext.current

    var showEditor by remember { mutableStateOf(false) }
    var editingVideo by remember { mutableStateOf<VideoItem?>(null) }
    var videoTitle by remember { mutableStateOf("") }
    var videoDescription by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var videoThumbnailUrl by remember { mutableStateOf("") }
    var videoCategory by remember { mutableStateOf("Entertainment") }
    var isPrivateVideo by remember { mutableStateOf(false) }
    var isPaidVideo by remember { mutableStateOf(false) }
    var videoPrice by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Entertainment Admin Panel",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Upload Video, Edit Title, Set Private/Public & Delete",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar (Upload Button & Stats)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Badge(containerColor = Color(0xFF1E293B)) {
                        Text(
                            text = "${videos.size} Videos in Catalog",
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            editingVideo = null
                            videoTitle = ""
                            videoDescription = ""
                            videoUrl = ""
                            videoThumbnailUrl = ""
                            videoCategory = "Entertainment"
                            isPrivateVideo = false
                            isPaidVideo = false
                            videoPrice = "0"
                            showEditor = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Upload Video", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editor Form Card
                if (showEditor) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (editingVideo == null) "Upload New Entertainment Video" else "Edit Video Title & Settings",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                IconButton(onClick = { showEditor = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close editor", tint = Color.Gray)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = videoTitle,
                                onValueChange = { videoTitle = it },
                                label = { Text("Video Title") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = videoDescription,
                                onValueChange = { videoDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = videoUrl,
                                onValueChange = { videoUrl = it },
                                label = { Text("Video Stream URL (MP4 / Web / YouTube)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = videoThumbnailUrl,
                                onValueChange = { videoThumbnailUrl = it },
                                label = { Text("Thumbnail Image URL") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = videoCategory,
                                onValueChange = { videoCategory = it },
                                label = { Text("Category (Comedy, Music, Shows, Trending)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Private Video Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isPrivateVideo = !isPrivateVideo }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isPrivateVideo,
                                    onCheckedChange = { isPrivateVideo = it }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Private Video (Admin only / Hidden from Feed)",
                                        color = if (isPrivateVideo) Color(0xFFEF4444) else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (isPrivateVideo) "Status: PRIVATE (Unpublished)" else "Status: PUBLIC (Visible to users)",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Paid Video Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isPaidVideo = !isPaidVideo }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isPaidVideo,
                                    onCheckedChange = { isPaidVideo = it }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Paid / Premium Video (Pay to unlock)",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (isPaidVideo) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = videoPrice,
                                    onValueChange = { videoPrice = it },
                                    label = { Text("Price (₹)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (videoTitle.isNotBlank() && videoUrl.isNotBlank()) {
                                        val newOrUpdated = VideoItem(
                                            id = editingVideo?.id ?: "",
                                            title = videoTitle.trim(),
                                            description = videoDescription.trim(),
                                            videoUrl = videoUrl.trim(),
                                            thumbnailUrl = videoThumbnailUrl.trim(),
                                            category = videoCategory.trim().ifEmpty { "Entertainment" },
                                            section = "ENTERTAINMENT",
                                            isPaid = isPaidVideo,
                                            price = if (isPaidVideo) videoPrice.toDoubleOrNull() ?: 0.0 else 0.0,
                                            isPublished = !isPrivateVideo, // private video sets isPublished = false
                                            views = editingVideo?.views ?: 0,
                                            likes = editingVideo?.likes ?: 0
                                        )
                                        viewModel.adminSaveVideo(newOrUpdated) { success ->
                                            if (success) {
                                                showEditor = false
                                                Toast.makeText(context, "Video synced to Firebase!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to save video", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Title and Video URL are required", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (editingVideo == null) "Upload to Firebase" else "Save Changes & Update",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Video List
                if (videos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No entertainment videos yet.\nTap '+ Upload Video' to add the first video.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(videos) { v ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = v.title,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Badge(containerColor = Color(0xFF334155)) {
                                                    Text(v.category, color = Color(0xFF38BDF8), fontSize = 10.sp)
                                                }
                                                // Private / Public Status Badge
                                                if (!v.isPublished) {
                                                    Badge(containerColor = Color(0xFFEF4444)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text("PRIVATE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                } else {
                                                    Badge(containerColor = Color(0xFF10B981)) {
                                                        Text("PUBLIC", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (v.isPaid) {
                                                    Badge(containerColor = Color(0xFFF59E0B)) {
                                                        Text("₹${v.price.toInt()}", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Badge(containerColor = Color(0xFF0284C7)) {
                                                        Text("FREE", color = Color.White, fontSize = 9.sp)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = v.videoUrl,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }

                                        // Actions: Toggle Private, Edit Title, Delete
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Toggle Private / Public
                                            IconButton(
                                                onClick = {
                                                    val updated = v.copy(isPublished = !v.isPublished)
                                                    viewModel.adminSaveVideo(updated) { success ->
                                                        if (success) {
                                                            val statusMsg = if (updated.isPublished) "Video set to PUBLIC" else "Video set to PRIVATE"
                                                            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (!v.isPublished) Icons.Default.Lock else Icons.Default.LockOpen,
                                                    contentDescription = "Toggle Private/Public",
                                                    tint = if (!v.isPublished) Color(0xFFEF4444) else Color(0xFF10B981)
                                                )
                                            }

                                            // Edit Title & Details
                                            IconButton(
                                                onClick = {
                                                    editingVideo = v
                                                    videoTitle = v.title
                                                    videoDescription = v.description
                                                    videoUrl = v.videoUrl
                                                    videoThumbnailUrl = v.thumbnailUrl
                                                    videoCategory = v.category
                                                    isPrivateVideo = !v.isPublished
                                                    isPaidVideo = v.isPaid
                                                    videoPrice = v.price.toInt().toString()
                                                    showEditor = true
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Video Title", tint = Color(0xFF38BDF8))
                                            }

                                            // Delete Video
                                            IconButton(
                                                onClick = {
                                                    viewModel.adminDeleteVideo(v.id) { success ->
                                                        if (success) {
                                                            Toast.makeText(context, "Video deleted from Firebase!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, "Failed to delete video", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Video", tint = Color(0xFFEF4444))
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
    }
}
