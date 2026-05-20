package com.example.memory_matching

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memory_matching.ui.theme.MEMORY_MATCHINGTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen {
    object Dashboard : Screen()
    data class Game(val mode: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MEMORY_MATCHINGTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

                Crossfade(targetState = currentScreen, label = "navigation") { screen ->
                    when (screen) {
                        is Screen.Dashboard -> DashboardScreen(
                            onModeSelected = { mode -> currentScreen = Screen.Game(mode) }
                        )
                        is Screen.Game -> MemoryGameApp(
                            mode = screen.mode,
                            onBack = { currentScreen = Screen.Dashboard }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onModeSelected: (String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var userName by remember { mutableStateOf("Shraban Sah") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Stats") },
                    label = { Text("Stats") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier.padding(innerPadding),
            label = "tab_content"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> HomeContent(userName, onModeSelected, onProfileClick = { selectedTab = 2 })
                1 -> StatsContent()
                2 -> ProfileContent(userName, onNameChange = { userName = it })
            }
        }
    }
}

@Composable
fun HomeContent(userName: String, onModeSelected: (String) -> Unit, onProfileClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { HeaderSection(userName, onProfileClick) }
        item { QuickStatsSection() }
        item {
            Text(
                text = "Game Modes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item { GameModesRow(onModeSelected) }
        item {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        items(recentActivities) { activity ->
            ActivityItem(activity)
        }
    }
}

@Composable
fun StatsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Statistics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(Modifier.weight(1f), "Games Played", "452", Icons.Default.VideogameAsset, Color(0xFF2196F3))
            StatCard(Modifier.weight(1f), "Win Rate", "78%", Icons.Default.PieChart, Color(0xFFE91E63))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Best Times", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                BestTimeRow("Classic", "0:42")
                BestTimeRow("Expert", "2:15")
                BestTimeRow("Time Trial", "0:38")
            }
        }
    }
}

@Composable
fun BestTimeRow(mode: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(mode, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(time, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProfileContent(userName: String, onNameChange: (String) -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(userName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("shraban.s@example.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, null)
            Spacer(Modifier.width(8.dp))
            Text("Edit Profile")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Settings, null)
            Spacer(Modifier.width(8.dp))
            Text("Game Settings")
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile Name") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("User Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onNameChange(tempName)
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Game Settings") },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sound Effects")
                        var soundEnabled by remember { mutableStateOf(true) }
                        Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dark Mode")
                        var darkMode by remember { mutableStateOf(false) }
                        Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun HeaderSection(userName: String, onProfileClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("Welcome back,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, "Profile", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun QuickStatsSection() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(modifier = Modifier.weight(1f), "Total Wins", "128", Icons.Default.EmojiEvents, Color(0xFFFFD700))
        StatCard(modifier = Modifier.weight(1f), "Rank", "Gold II", Icons.Default.TrendingUp, Color(0xFF4CAF50))
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun GameModesRow(onModeSelected: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        item {
            ModeCard(
                title = "Classic",
                subtitle = "Standard 4x4",
                icon = Icons.Default.GridView,
                gradient = listOf(Color(0xFF6200EE), Color(0xFFBB86FC)),
                onClick = { onModeSelected("Classic") }
            )
        }
        item {
            ModeCard(
                title = "Time Trial",
                subtitle = "Beat the clock",
                icon = Icons.Default.Timer,
                gradient = listOf(Color(0xFFFF5722), Color(0xFFFF9800)),
                onClick = { onModeSelected("Time Trial") }
            )
        }
        item {
            ModeCard(
                title = "Expert",
                subtitle = "Hard 6x6",
                icon = Icons.Default.Psychology,
                gradient = listOf(Color(0xFF009688), Color(0xFF80CBC4)),
                onClick = { onModeSelected("Expert") }
            )
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(width = 160.dp, height = 200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient))
                .padding(20.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// --- Memory Game Implementation ---

data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameApp(mode: String, onBack: () -> Unit) {
    val symbols = remember(mode) {
        val baseSymbols = listOf("🍎", "🍌", "🍇", "🍉", "🍓", "🍒", "🍍", "🥝", "🥑", "🥥", "🍋", "🍊", "🍑", "🍐", "🫐", "🍈", "🍅", "🥦")
        val count = if (mode == "Expert") 18 else 8
        baseSymbols.take(count)
    }

    val cards = remember {
        mutableStateListOf<MemoryCard>().apply {
            addAll((symbols + symbols).shuffled().mapIndexed { index, symbol ->
                MemoryCard(id = index, symbol = symbol)
            })
        }
    }

    var indexOfSingleSelectedCard by remember { mutableStateOf<Int?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var moveCount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val isWin = cards.isNotEmpty() && cards.all { it.isMatched }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mode) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Moves: $moveCount", style = MaterialTheme.typography.titleMedium)
            
            if (isWin) {
                Text("You Win! 🎉", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(if (mode == "Expert") 6 else 4),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(cards) { index, card ->
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                if (isProcessing || card.isFlipped || card.isMatched) return@clickable
                                cards[index] = card.copy(isFlipped = true)
                                if (indexOfSingleSelectedCard == null) {
                                    indexOfSingleSelectedCard = index
                                } else {
                                    moveCount++
                                    val firstIndex = indexOfSingleSelectedCard!!
                                    if (cards[firstIndex].symbol == cards[index].symbol) {
                                        cards[firstIndex] = cards[firstIndex].copy(isMatched = true)
                                        cards[index] = cards[index].copy(isMatched = true)
                                    } else {
                                        isProcessing = true
                                        scope.launch {
                                            delay(800)
                                            cards[firstIndex] = cards[firstIndex].copy(isFlipped = false)
                                            cards[index] = cards[index].copy(isFlipped = false)
                                            isProcessing = false
                                        }
                                    }
                                    indexOfSingleSelectedCard = null
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (card.isFlipped || card.isMatched) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (card.isFlipped || card.isMatched) {
                                Text(card.symbol, fontSize = if(mode == "Expert") 18.sp else 28.sp)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    cards.clear()
                    cards.addAll((symbols + symbols).shuffled().mapIndexed { index, s -> MemoryCard(index, s) })
                    moveCount = 0
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Restart Game")
            }
        }
    }
}

@Composable
fun ActivityItem(activity: RecentActivity) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(activity.color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(activity.icon, null, tint = activity.color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(activity.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(activity.time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class RecentActivity(val title: String, val time: String, val icon: ImageVector, val color: Color)
val recentActivities = listOf(
    RecentActivity("Won Classic Mode", "2 hours ago", Icons.Default.CheckCircle, Color(0xFF4CAF50)),
    RecentActivity("New Record: 45s", "Yesterday", Icons.Default.History, Color(0xFF2196F3)),
    RecentActivity("Daily Streak: 5 Days", "2 days ago", Icons.Default.Whatshot, Color(0xFFFF5722))
)

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    MEMORY_MATCHINGTheme {
        DashboardScreen { _ -> }
    }
}
