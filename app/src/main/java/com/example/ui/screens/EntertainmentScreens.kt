package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.example.model.ShortItem
import com.example.model.UserProfile
import com.example.model.VideoItem
import com.example.viewmodel.MainViewModel

@Composable
fun EntertainmentModeScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Home, 1: Shorts, 2: Library, 3: Profile

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
                    modifier = Modifier.testTag("tab_ent_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = "Shorts") },
                    label = { Text("Shorts") },
                    modifier = Modifier.testTag("tab_ent_shorts")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Library") },
                    label = { Text("Library") },
                    modifier = Modifier.testTag("tab_ent_library")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("tab_ent_profile")
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
                0 -> EntertainmentHomeScreen(viewModel = viewModel)
                1 -> ShortsFeedScreen(viewModel = viewModel, section = "ENTERTAINMENT")
                2 -> EntertainmentLibraryScreen(viewModel = viewModel)
                3 -> EntertainmentProfileScreen(viewModel = viewModel, userProfile = userProfile)
            }
        }
    }
}

@Composable
fun EntertainmentHomeScreen(viewModel: MainViewModel) {
    val videos by viewModel.entertainmentVideos.collectAsState()
    val shorts by viewModel.shorts.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Trending", "News", "Culture", "Music", "Comedy")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title & Search Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NEOL ENTERTAINMENT",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFFFBBF24))
                }
            }

            // New App Version & Features Notification in Entertainment Mode
            if (appSettings.appVersionFeatures.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF831843)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Celebration, contentDescription = null, tint = Color(0xFFF472B6), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New App Update v${appSettings.latestAppVersion}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                            Badge(containerColor = Color(0xFFF472B6)) {
                                Text("Latest Features", color = Color(0xFF831843), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = appSettings.appVersionFeatures,
                            color = Color(0xFFFCE7F3),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search videos, shows, creators...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0284C7),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categories Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }

        // Trending Shorts Rail
        item {
            Text(
                text = "Trending Shorts",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (shorts.isEmpty()) {
                Text("No shorts published yet.", color = Color.Gray, fontSize = 12.sp)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(shorts) { s ->
                        ShortThumbnailCard(shortItem = s)
                    }
                }
            }
        }

        // Main Video Feed
        val filteredVideos = videos.filter {
            (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
            (searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true))
        }

        item {
            Text(
                text = "Recommended Video Stream",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 17.sp
            )
        }

        if (filteredVideos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No entertainment videos match.", color = Color.Gray)
                }
            }
        } else {
            items(filteredVideos) { video ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (video.videoUrl.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                            context.startActivity(intent)
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
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
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
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
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = video.category,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp
                                )
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

@Composable
fun ShortThumbnailCard(shortItem: ShortItem) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(190.dp),
        onClick = {
            if (shortItem.videoUrl.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(shortItem.videoUrl))
                context.startActivity(intent)
            }
        },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (shortItem.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = shortItem.thumbnailUrl,
                    contentDescription = shortItem.title,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF334155)))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = shortItem.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${shortItem.likesCount} ❤️",
                        color = Color(0xFFE2E8F0),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ShortsFeedScreen(viewModel: MainViewModel, section: String) {
    val shorts by viewModel.shorts.collectAsState()
    val filtered = shorts.filter { it.section == section || section == "ALL" }
    val context = LocalContext.current

    if (filtered.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No shorts available in this feed yet.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filtered) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(440.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (item.thumbnailUrl.isNotEmpty()) {
                            AsyncImage(
                                model = item.thumbnailUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)))
                        }

                        // Play Button
                        IconButton(
                            onClick = {
                                if (item.videoUrl.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.videoUrl))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play Short", tint = Color.White, modifier = Modifier.size(40.dp))
                        }

                        // Bottom Overlay
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(16.dp)
                        ) {
                            Text(item.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("By ${item.creatorName}", color = Color(0xFF38BDF8), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.likesCount} Likes", color = Color(0xFFE2E8F0), fontSize = 12.sp)
                                Row {
                                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
fun EntertainmentLibraryScreen(viewModel: MainViewModel) {
    val videos by viewModel.entertainmentVideos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Entertainment Library",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Watch History", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Recently viewed videos and episodes are synced with your Google account.", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Saved Videos & Playlists", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Bookmark educational or entertainment shows for offline-ready viewing.", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun EntertainmentProfileScreen(
    viewModel: MainViewModel,
    userProfile: UserProfile
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                            .background(Color(0xFFE11D48)),
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

                Badge(containerColor = Color(0xFFE11D48)) {
                    Text(
                        text = "Entertainment User",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Preferences & Account", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.selectExperienceMode("STUDENT") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch to Student / Education Mode")
                }

                // If user is Admin, add dedicated Entertainment Admin Panel button
                if (userProfile.role == "ADMIN") {
                    var showEntAdminDialog by remember { mutableStateOf(false) }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showEntAdminDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Entertainment Admin Panel", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    if (showEntAdminDialog) {
                        EntertainmentAdminDialog(
                            viewModel = viewModel,
                            onDismiss = { showEntAdminDialog = false }
                        )
                    }
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
    }
}
