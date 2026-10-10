package com.iiitk.gdgevents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iiitk.gdgevents.ui.theme.MyGDGEventsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import android.widget.Toast
import android.app.Activity
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp

interface GDGApiService {
    // Fetches the Upcoming/Live events
    @GET("api/event_slim/for_chapter/1767/?page_size=4&status=Live&order=start_date")
    suspend fun getLiveEvents(): BevyResponse

    // Fetches the Past events (Notice the status=Past and order=-start_date)
    @GET("api/event_slim/for_chapter/1767/?page_size=10&status=Past&order=-start_date")
    suspend fun getPastEvents(): BevyResponse
}

object GDGApi {
    // The base URL must always end with a trailing slash
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gdg.community.dev/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val networkService: GDGApiService by lazy {
        retrofit.create(GDGApiService::class.java)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Read the system theme OUTSIDE the remember block
            val systemTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemTheme) }

            // 2. Pass that state into your theme wrapper
            MyGDGEventsTheme(darkTheme = isDarkTheme) {
                // 3. Pass the state and the updater function down to the app
                AppRoot(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
    }
}

@Composable
fun AppRoot(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var currentRoute by remember { mutableStateOf("main") }
    var isLoggedIn by remember { mutableStateOf(false) } // You can use this later to swap the profile icon to an avatar!

    val context = LocalContext.current
    BackHandler(enabled = currentRoute != "main") {
        currentRoute = "main"
    }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            if (targetState != "main") {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(250)) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(250))
            } else {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(250)) togetherWith
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(250))
            }
        },
        label = "Screen Transition"
    ) { route ->
        when (route) {
            "main" -> MainScreen(
                onNavigateToSettings = { currentRoute = "settings" },
                onNavigateToProfile = { currentRoute = "auth" } // <-- Navigates to Auth
            )
            "auth" -> AuthScreen(
                onBackClick = { currentRoute = "main" },
                onLoginSuccess = {
                    isLoggedIn = true
                    currentRoute = "main" // Sends them back to feed after login
                    Toast.makeText(context, "Successfully Logged In!", Toast.LENGTH_SHORT).show()
                }
            )
            "settings" -> SettingsScreen(
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange,
                onBackClick = { currentRoute = "main" },
                onNavigateToDarkTheme = { currentRoute = "settings_dark_theme" },
                onNavigateToMonet = { currentRoute = "settings_monet" }
            )
            "settings_dark_theme" -> DarkThemeConfigScreen(onBackClick = { currentRoute = "settings" })
            "settings_monet" -> MonetConfigScreen(onBackClick = { currentRoute = "settings" })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun  MainScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit // <--- Make sure this is here!
) {// <-- ADD THIS PARAMETER


    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // --- ADD THIS NEW BACK NAVIGATION LOGIC ---
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // State to control the sliding drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Resets the "double back to exit" timer after 2 seconds
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000L)
            backPressedOnce = false
        }
    }

    // Handles the back gesture priorities
    BackHandler(enabled = true) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() } // Priority 1: Close Drawer
        } else if (selectedTabIndex != 0) {
            selectedTabIndex = 0 // Priority 2: Go to Home Tab
        } else {
            // Priority 3: Double tap to exit
            if (backPressedOnce) {
                (context as? Activity)?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val bottomTabs = listOf("Home", "Events", "Community")
    val bottomIcons = listOf(
        Icons.Default.Home,
        Icons.Default.DateRange,
        Icons.Default.Groups
    )

    // The Sliding Side Panel
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "More Options",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // 1. Settings (Navigates to new page)
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToSettings() // <-- CALL IT HERE
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                // 2. Reminders
                NavigationDrawerItem(
                    label = { Text("Reminders") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        // TODO: Implement Reminders functionality
                    },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Reminders") },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                // 3. Contact Us (Uses the intent we created earlier)
                val context = LocalContext.current
                NavigationDrawerItem(
                    label = { Text("Contact Us") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:dsc.iiitkalyani@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Query regarding GDG IIIT Kalyani")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    },
                    icon = { Icon(Icons.Default.Email, contentDescription = "Contact Us") },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 4. Code of Conduct
                NavigationDrawerItem(
                    label = { Text("Code of Conduct") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Code of Conduct") },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                // 5. Submit Project/Idea
                NavigationDrawerItem(
                    label = { Text("Submit Project Idea") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Submit Idea") },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                // 6. FAQ
                NavigationDrawerItem(
                    label = { Text("FAQ & Help") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "FAQ") },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        // Your existing Scaffold goes inside the Drawer block
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("GDG IIIT Kalyani", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        // The Menu Icon on the Top Left
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) { // <--- Remove the brackets and TODO
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Account")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomTabs.forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = { Icon(imageVector = bottomIcons[index], contentDescription = title) },
                            label = { Text(title) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTabIndex) {
                    0 -> HomeScreen()
                    1 -> EventsScreen()
                    2 -> CommunityScreen() // Replaced the placeholder!
                }
            }
        }
    }
}// 1. The Data Structure
// 1. The Data Structure
data class GDGEvent(
    val title: String,
    val category: String,
    val date: String,
    val time: String,
    val venue: String,
    val imageUrl: String,
    val isPast: Boolean
)

// This represents the top-level JSON response from the server
data class BevyResponse(
    val results: List<BevyEvent>
)

// This represents the individual event inside the JSON "results" array
// The variable names MUST exactly match the JSON keys in the URL
data class BevyEvent(
    val title: String,
    val start_date: String,
    val event_type_title: String?,
    val cropped_picture_url: String?,
    val cropped_banner_url: String?, // Add this line
    val url: String?
)


// 3. The UI Screen

@Composable
fun EventsScreen() {
    var upcomingEvents by remember { mutableStateOf<List<GDGEvent>>(emptyList()) }
    var pastEvents by remember { mutableStateOf<List<GDGEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            // 1. Fetch Upcoming Events (Standard API)
            val liveResponse = GDGApi.networkService.getLiveEvents()
            upcomingEvents = liveResponse.results.map { apiEvent ->
                GDGEvent(
                    title = apiEvent.title,
                    category = apiEvent.event_type_title ?: "Upcoming Event",
                    date = apiEvent.start_date.take(10),
                    time = "TBA",
                    venue = "IIIT Kalyani",
                    imageUrl = apiEvent.cropped_banner_url ?: apiEvent.cropped_picture_url ?: "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?q=80&w=1000",
                    isPast = false
                )
            }

            // 2. Fetch Past Events (Using our Jsoup hybrid function)
            pastEvents = fetchPastEventsWithImages()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: Upcoming Events
            item {
                Text("Upcoming Events", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            if (upcomingEvents.isEmpty()) {
                item {
                    Text("No upcoming events at the moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(upcomingEvents) { event -> EventCard(event) }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // SECTION 2: Past Events
            item {
                Text("Past Events", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            if (pastEvents.isEmpty()) {
                item {
                    Text("No past events found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(pastEvents) { event -> EventCard(event) }
            }
        }
    }
}
@Composable
fun HomeScreen() {
    var liveHeroEvent by remember { mutableStateOf<GDGEvent?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = GDGApi.networkService.getLiveEvents()
            if (response.results.isNotEmpty()) {
                val first = response.results[0]
                liveHeroEvent = GDGEvent(
                    title = first.title,
                    category = first.event_type_title ?: "UPCOMING EVENT",
                    date = first.start_date.take(10),
                    time = "TBA",
                    venue = "TBA",
                    imageUrl = first.cropped_banner_url ?: first.cropped_picture_url ?: "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?q=80&w=1000",
                    isPast = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Hero Event Banner
        item {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (liveHeroEvent != null) {
                HeroEventCard(event = liveHeroEvent!!)
            } else {
                // Modern Android 17 style empty state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(50) // Creates a perfect pill shape
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Caught up",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No upcoming events!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. The Community Poll (Moved here!)
        item { CommunityPollCard() }

        // 3. Featured Media
        item { FeaturedVideoCard() }

        // 4. Community Highlights
        item { DiscussionPreviewCard() }
    }
}

@Composable
fun FeaturedVideoCard() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Featured Media",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: Launch YouTube Intent */ },
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                // Mock Video Thumbnail Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "A Step closer to Rebase || Rebase-01 Opening Ceremony",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GDSC IIIT Kalyani • 44 mins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DiscussionPreviewCard() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Community Highlights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)

        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Author Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Supachai Pholwongsa", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text("9 months ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Post Content
                Text(
                    text = "Impact of Artificial Intelligence on Society",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Artificial Intelligence is becoming increasingly integrated into various aspects of life, from healthcare to transportation. What are your thoughts on the ethical considerations...",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { /* TODO: Navigate to Community Tab */ },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Read full discussion")
                }
            }
        }
    }
}

// 4. The Individual Card UI
@Composable
fun EventCard(event: GDGEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.date, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { /* TODO: Launch RSVP Sheet */ }, modifier = Modifier.fillMaxWidth()) {
                Text(if (event.isPast) "View Details" else "RSVP Now")
            }
        }
    }
}


@Composable
fun HeroEventCard(event: GDGEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Fixed height to show off the banner
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. The Background Image loaded from the Web
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Event Banner",
                contentScale = ContentScale.Crop, // Crops the image to fill the card perfectly
                modifier = Modifier.fillMaxSize()
            )

            // 2. The Dark Gradient Scrim (Protects the white text)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f) // Darkens at the bottom
                            ),
                            startY = 50f
                        )
                    )
            )

            // 3. The Text Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom // Pushes text to the bottom over the dark gradient
            ) {
                Text(
                    text = event.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${event.date} • ${event.time} • ${event.venue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* RSVP Action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text("RSVP Now", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}


// 1. Team Member Data Structure
data class TeamMember(val name: String, val role: String)

val organizers = listOf(
    TeamMember("Saksham Jaiswal", "GDGoC Organizer"),
    TeamMember("Adrisha Biswas", "GDGoC Organizer"),
    TeamMember("Anurag A Pandit", "GDGoC Organizer")
)

// 2. The Community Screen Layout
@Composable
fun CommunityScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section 1: Organizing Team
        item {
            Text(
                text = "Organizing Team",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            // LazyRow makes the cards scroll sideways!
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(organizers) { member ->
                    TeamMemberCard(member)
                }
            }
        }

        // Section 2: Discussions (Reusing the card you already made)
        item {
            DiscussionPreviewCard()
        }
        item { CommunityPollCard() }

        // Section 3: Join Chapter CTA
        item {
            JoinChapterCard()
        }
    }
}

// 3. Helper Composables for the Community Screen
@Composable
fun TeamMemberCard(member: TeamMember) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = member.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun JoinChapterCard() {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Become a Member",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join GDG on Campus IIIT Kalyani to get updates on workshops, swags, and developer opportunities.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gdg.community.dev/gdg-on-campus-indian-institute-of-information-technology-kalyani-india/"))
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Join on Bevy", color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,                  // Add this
    onThemeChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToDarkTheme: () -> Unit,
    onNavigateToMonet: () -> Unit
) {

    var isMonetEnabled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
            Text("Display", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 12.dp))

            SplitSettingItem(
                title = "Use Dark theme",
                subtitle = "Make your screen more comfortable to view",
                isChecked = isDarkTheme,           // Use the hoisted state
                onCheckedChange = onThemeChange,   // Trigger the hoisted update
                onTextClick = onNavigateToDarkTheme
            )

            SplitSettingItem(
                title = "Monet Colours",
                subtitle = "Match app colors to system wallpaper",
                isChecked = isMonetEnabled,
                onCheckedChange = { isMonetEnabled = it },
                onTextClick = onNavigateToMonet
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkThemeConfigScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dark theme") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Options", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))
            // Placeholder for the radio buttons you requested
            Text("O Standard", modifier = Modifier.padding(vertical = 8.dp))
            Text("O Expanded", modifier = Modifier.padding(vertical = 8.dp))
            Text("O Pure black", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonetConfigScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallpaper & style") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Icons, text and more match colours in your wallpaper", textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp))

            // Placeholder for the circular color swatches
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.Cyan))
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.Gray))
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.DarkGray))
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.Blue))
            }
        }
    }
}
@Composable
fun SplitSettingItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTextClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // IntrinsicSize.Min ensures the vertical divider stretches to match the text height
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side (Text area - Clickable to navigate)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTextClick() }
                    .padding(16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // The visible vertical bar
            VerticalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )

            // Right side (The Switch)
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Defines the ✓ and x icons for the thumb
                val thumbIcon: (@Composable () -> Unit)? = if (isChecked) {
                    { Icon(Icons.Filled.Check, contentDescription = "On", modifier = Modifier.size(SwitchDefaults.IconSize)) }
                } else {
                    { Icon(Icons.Filled.Close, contentDescription = "Off", modifier = Modifier.size(SwitchDefaults.IconSize)) }
                }

                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    thumbContent = thumbIcon
                )
            }
        }
    }
}

@Composable
fun CommunityPollCard() {
    // Poll State
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasVoted by remember { mutableStateOf(false) }

    // Mock Data
    val question = "What workshop should we host next month?"
    val options = listOf(
        "Advanced Jetpack Compose",
        "Intro to Machine Learning",
        "Web3 & Blockchain Basics",
        "Resume Building & Open Source"
    )
    // Mock percentages for after they vote
    val results = listOf(0.45f, 0.25f, 0.10f, 0.20f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Poll", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Community Poll", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            options.forEachIndexed { index, option ->
                val isSelected = selectedOption == option

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = if (isSelected && !hasVoted) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(MaterialTheme.colorScheme.background)
                        .selectable(
                            selected = isSelected,
                            onClick = { if (!hasVoted) selectedOption = option }
                        )
                        .padding(12.dp)
                ) {
                    if (!hasVoted) {
                        // Pre-vote: Show Radio Buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null, // Handled by the parent box
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = option, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        // Post-vote: Show Animated Progress Bars
                        val animatedProgress by animateFloatAsState(
                            targetValue = results[index],
                            animationSpec = tween(durationMillis = 1000),
                            label = "progress"
                        )

                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                trackColor = Color.Transparent,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "${(results[index] * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            AnimatedVisibility(visible = !hasVoted) {
                Button(
                    onClick = { hasVoted = true },
                    enabled = selectedOption != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vote")
                }
            }

            AnimatedVisibility(visible = hasVoted) {
                Text(
                    text = "Thanks for voting! 142 total votes.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onBackClick: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("") }, // Empty title for a clean look
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Welcome to GDG Kalyani", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Sign in to access events and community", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoginSuccess,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* Mock Sign Up */ }) {
                Text("Don't have an account? Sign up")
            }
        }
    }
}
suspend fun fetchPastEventsWithImages(): List<GDGEvent> = withContext(Dispatchers.IO) {
    val finalEvents = mutableListOf<GDGEvent>()

    try {
        val url = "https://gdg.community.dev/gdg-on-campus-indian-institute-of-information-technology-kalyani-india/"
        val document = Jsoup.connect(url).get()

        // Select the list items that contain the past events.
        // Note: You may need to inspect the live GDG page to confirm these CSS selectors.
        val pastEventCards = document.select("#past-events .event-card")

        for (card in pastEventCards) {
            val title = card.select(".event-title").text()
            val date = card.select(".event-date").text()
            val category = card.select(".event-type").text()

            // The banner image is sometimes set as a background image on a div
            val imageStyle = card.select(".event-image-bg").attr("style")
            var imageUrl = ""
            if (imageStyle.contains("url(")) {
                // Extract the URL from the style attribute: background-image: url('...');
                imageUrl = imageStyle.substringAfter("url('").substringBefore("')")
                if (imageUrl == imageStyle) { // Fallback if no single quotes
                    imageUrl = imageStyle.substringAfter("url(").substringBefore(")")
                }
            } else {
                imageUrl = card.select("img.event-image").attr("src")
            }

            if (title.isNotEmpty()) {
                finalEvents.add(
                    GDGEvent(
                        title = title,
                        category = category.ifEmpty { "Past Event" },
                        date = date,
                        time = "TBA",
                        venue = "IIIT Kalyani",
                        imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?q=80&w=1000" },
                        isPast = true
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext finalEvents
}