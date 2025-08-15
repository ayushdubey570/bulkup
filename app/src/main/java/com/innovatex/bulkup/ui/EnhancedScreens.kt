package com.innovatex.bulkup.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.innovatex.bulkup.data.*
import com.innovatex.bulkup.ui.theme.*
import com.innovatex.bulkup.logic.HealthCalculators
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import com.innovatex.bulkup.R
import java.time.LocalDate
import com.google.android.gms.ads.rewarded.RewardedAd

// Enhanced Home Screen
@Composable
fun EnhancedHomeScreen(
    storage: LocalStorage,
    onNavigate: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tip by remember { mutableStateOf("") }
    val game = remember { storage.loadGamification() }
    val profile = remember { storage.loadProfile() }
    
    LaunchedEffect(Unit) {
        tip = loadTipOfTheDay(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 80.dp)
    ) {
        item {
            // Hero Banner
            HeroBanner(
                title = "Hello, ${profile.name ?: "User"}!",
                subtitle = "Ready to crush your goals today?",
                quote = "The pain you feel today will be the strength you feel tomorrow."
            )
        }

        item {
            // Stats Section
            Text(
                text = "Your Stats",
                style = MaterialTheme.typography.headlineSmall,
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            // Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                StatsCard(
                    title = "Weight",
                    value = "${profile.weightKg?.toInt() ?: 0} kg",
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "BMI",
                    value = "${profile.weightKg?.let { w -> profile.heightCm?.let { h -> 
                        (w / ((h / 100) * (h / 100))).toInt() 
                    } } ?: 0}",
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Streak",
                    value = "${game.currentStreakDays} days",
                    isHighlighted = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Recent Activity Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GreenPrimary,
                    modifier = Modifier.clickable { onNavigate("progress") }
                )
            }
        }

        item {
            // Activity Cards
            ActivityCard(
                icon = Icons.Default.Star,
                title = "Chest & Triceps",
                subtitle = "Yesterday, 45 mins",
                onClick = { onNavigate("workout") }
            )
            ActivityCard(
                icon = Icons.Default.Favorite,
                title = "2,350 kcal consumed",
                subtitle = "P: 160g, C: 260g, F: 75g",
                onClick = { onNavigate("food") }
            )
        }

        item {
            // Quick Actions Section
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.headlineSmall,
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            // Quick Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Star,
                    title = "Start\nWorkout",
                    onClick = { onNavigate("workout") }
                )
                QuickActionButton(
                    icon = Icons.Default.Favorite,
                    title = "Log\nMeal",
                    onClick = { onNavigate("food") }
                )
                QuickActionButton(
                    icon = Icons.Default.List,
                    title = "Meal\nPlans",
                    onClick = { onNavigate("budget") }
                )
            }
        }
    }
}

// Enhanced Food Diary Screen
@Composable
fun EnhancedFoodDiaryScreen(
    context: Context,
    storage: LocalStorage,
    onNavigate: (String) -> Unit
) {
    val foods: List<FoodItem> = loadRawList(context, R.raw.foods, object : TypeToken<List<FoodItem>>() {}) as List<FoodItem>
    val today = LocalDate.now().toString()
    var log by remember { mutableStateOf(storage.loadDailyFoodLog(today)) }
    var selectedMeal by remember { mutableStateOf("breakfast") }
    var searchQuery by remember { mutableStateOf("") }

    val mealTypes = listOf("breakfast", "lunch", "dinner", "snacks")
    val mealEntries = log.entries.filter { it.mealType == selectedMeal }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 80.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate("home") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkOnSurface
                )
            }
            Text(
                text = "Food Diary",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search for food"
        )

        // Meal Type Tabs
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(mealTypes) { meal ->
                val isSelected = selectedMeal == meal
                Card(
                    modifier = Modifier.clickable { selectedMeal = meal },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GreenPrimary else DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = meal.capitalize(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) DarkOnSurface else DarkOnSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Food Entries for Selected Meal
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(mealEntries) { entry ->
                FoodItemCard(
                    name = entry.name,
                    calories = entry.calories.toInt(),
                    protein = entry.protein,
                    carbs = entry.carbs,
                    fats = entry.fats
                )
            }

            item {
                // Add Food Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, DarkSurface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Food",
                            tint = DarkOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Food",
                            color = DarkOnSurfaceVariant
                        )
                    }
                }
            }
        }

        // Daily Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Summary",
                        style = MaterialTheme.typography.headlineSmall,
                        color = DarkOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ProgressBar(
                    current = log.entries.sumOf { it.calories },
                    goal = 2000.0,
                    label = "Calories"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroSummary("Protein", log.entries.sumOf { it.protein }, "g")
                    MacroSummary("Carbs", log.entries.sumOf { it.carbs }, "g")
                    MacroSummary("Fats", log.entries.sumOf { it.fats }, "g")
                }
            }
        }
    }
}

@Composable
fun MacroSummary(label: String, value: Double, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DarkOnSurfaceVariant
        )
        Text(
            text = "${value.toInt()}$unit",
            style = MaterialTheme.typography.bodyLarge,
            color = DarkOnSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

// Enhanced Budget Meal Plan Screen
@Composable
fun EnhancedBudgetMealPlanScreen(
    context: Context,
    onNavigate: (String) -> Unit
) {
    var items by remember { mutableStateOf<List<BudgetMealItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        items = loadCityPricesFromRaw(context, R.raw.delhi_prices)
            .map { cityItem ->
                BudgetMealItem(
                    id = cityItem.name,
                    name = cityItem.name,
                    unit = cityItem.unit,
                    price = cityItem.price,
                    calories = cityItem.calories,
                    protein = cityItem.protein,
                    carbs = cityItem.carbs,
                    fats = cityItem.fats,
                    sponsoredBy = cityItem.sponsoredBy,
                    caloriesPerRupee = if (cityItem.price > 0) cityItem.calories / cityItem.price else 0.0
                )
            }
            .sortedByDescending { it.caloriesPerRupee }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 80.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate("home") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkOnSurface
                )
            }
            Text(
                text = "Budget Meal Plan",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Generated Plan Title
        Text(
            text = "Generated Plan",
            style = MaterialTheme.typography.headlineSmall,
            color = DarkOnSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Meal Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(items.take(5)) { item ->
                BudgetMealItemCard(
                    name = item.name,
                    unit = item.unit,
                    price = item.price,
                    calories = item.calories,
                    caloriesPerRupee = item.caloriesPerRupee,
                    isSponsored = item.sponsoredBy != null
                )
            }
        }

        // Unlock Button
        Button(
            onClick = { /* Show rewarded ad */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = DarkOnSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Unlock 7-Day Plan (Watch Ad)",
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Enhanced Workout Screen
@Composable
fun EnhancedWorkoutScreen(
    context: Context,
    storage: LocalStorage,
    onNavigate: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val exercises: List<WorkoutExercise> = loadRawList(context, R.raw.exercises, object : TypeToken<List<WorkoutExercise>>() {}) as List<WorkoutExercise>
    val filteredExercises = exercises.filter { exercise ->
        exercise.name.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 80.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate("home") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkOnSurface
                )
            }
            Text(
                text = "Exercises",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search exercises"
        )

        // Filter Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterButton(
                text = "Muscle Groups",
                onClick = { /* Show muscle group filter */ },
                modifier = Modifier.weight(1f)
            )
            FilterButton(
                text = "Equipment",
                onClick = { /* Show equipment filter */ },
                modifier = Modifier.weight(1f)
            )
        }

        // All Exercises Title
        Text(
            text = "All Exercises",
            style = MaterialTheme.typography.headlineSmall,
            color = DarkOnSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Exercise Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredExercises) { exercise ->
                ExerciseCard(
                    name = exercise.name,
                    muscleGroups = exercise.muscleGroup,
                    equipment = exercise.equipment,
                    onAddClick = { /* Add to workout */ }
                )
            }
        }
    }
}

// Enhanced Progress Tracking Screen
@Composable
fun EnhancedProgressTrackingScreen(
    storage: LocalStorage,
    onNavigate: (String) -> Unit
) {
    var selectedTimeRange by remember { mutableStateOf("1M") }
    val entries = storage.loadWeightEntries()
    val timeRanges = listOf("1M", "3M", "6M", "1Y")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 80.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate("home") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkOnSurface
                )
            }
            Text(
                text = "Progress Tracking",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        // Weight Trend Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weight Trend",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkOnSurface,
                    fontWeight = FontWeight.Bold
                )
                
                // Time Range Buttons
                Row {
                    timeRanges.forEach { range ->
                        val isSelected = selectedTimeRange == range
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .clickable { selectedTimeRange = range },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = range,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (isSelected) GreenPrimary else DarkOnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Weight Graph Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Weight Trend Graph",
                        color = DarkOnSurfaceVariant
                    )
                }
            }

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard("Change (1M)", "+2.5 kg", isPositive = true, modifier = Modifier.weight(1f))
                SummaryCard("Average Weight", "78.5 kg", modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryCard("Goal Weight", "85 kg", modifier = Modifier.weight(1f))
                SummaryCard("To Goal", "5 kg", modifier = Modifier.weight(1f))
            }
        }

        // Body Metrics Log Section
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Body Metrics Log",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { /* Add weight */ },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = DarkOnSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Weight",
                        color = DarkOnSurface
                    )
                }
            }

            // Weight Entries
            LazyColumn {
                items(entries.take(3)) { entry ->
                    WeightEntryCard(entry)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    isPositive: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isPositive) GreenPrimary else DarkOnSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeightEntryCard(entry: WeightEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${entry.weightKg} kg",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = { /* Edit weight */ }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = DarkOnSurfaceVariant
                    )
                }
                IconButton(onClick = { /* Delete weight */ }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = RedAccent
                    )
                }
            }
        }
    }
}

// Enhanced Settings Screen
@Composable
fun EnhancedSettingsScreen(
    context: Context,
    storage: LocalStorage,
    onNavigate: (String) -> Unit
) {
    var profile by remember { mutableStateOf(storage.loadProfile()) }
    var bmiVal by remember { mutableStateOf(0.0) }
    var bmiCat by remember { mutableStateOf("—") }
    var tdee by remember { mutableStateOf(0) }
    var macros by remember { mutableStateOf(HealthCalculators.MacroResult(0,0,0,0)) }

    fun recalc() {
        profile.heightCm?.let { h ->
            profile.weightKg?.let { w ->
                bmiVal = HealthCalculators.bmi(w, h)
                bmiCat = HealthCalculators.bmiCategory(bmiVal)
            }
        }
        if (profile.weightKg != null && profile.heightCm != null && profile.age != null) {
            val base = HealthCalculators.tdeeMifflinStJeor(
                profile.gender,
                profile.weightKg!!,
                profile.heightCm!!,
                profile.age!!,
                profile.activityLevel
            )
            tdee = (base + profile.calorieSurplus).toInt()
            macros = if (profile.macroMode == "percent") {
                HealthCalculators.macrosFromPercent(
                    calories = tdee,
                    pPercent = profile.macroPercentProtein,
                    cPercent = profile.macroPercentCarbs,
                    fPercent = profile.macroPercentFats
                )
            } else {
                HealthCalculators.macrosFromPerKg(
                    calories = tdee,
                    proteinPerKg = profile.proteinPerKg,
                    weightKg = profile.weightKg ?: 0.0
                )
            }
        }
    }

    LaunchedEffect(Unit) { recalc() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(bottom = 80.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate("home") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkOnSurface
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = DarkOnSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // Profile Section
            item {
                Text(
                    text = "PROFILE",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkOnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Picture Placeholder
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = GreenPrimary.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name ?: "Rohan Sharma",
                                style = MaterialTheme.typography.bodyLarge,
                                color = DarkOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "View and edit your profile",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkOnSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View Profile",
                            tint = DarkOnSurfaceVariant
                        )
                    }
                }
            }

            // Preferences Section
            item {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkOnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Units",
                    subtitle = "Metric / Imperial",
                    value = profile.units.capitalize(),
                    onClick = { /* Show units selection */ }
                )
            }

            item {
                SettingsItem(
                    title = "Language",
                    subtitle = "English / हिन्दी",
                    value = profile.language.capitalize(),
                    onClick = { /* Show language selection */ }
                )
            }

            item {
                SettingsItem(
                    title = "Ad Settings",
                    subtitle = "Manage ad preferences",
                    onClick = { /* Show ad settings */ }
                )
            }

            // Data Management Section
            item {
                Text(
                    text = "DATA MANAGEMENT",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkOnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Data Export",
                    subtitle = "Export workout and nutrition data",
                    onClick = { /* Export data */ }
                )
            }

            item {
                SettingsItem(
                    title = "Storage Optimization",
                    subtitle = "Manage cache and old data",
                    onClick = { /* Storage optimization */ }
                )
            }

            // About Section
            item {
                Text(
                    text = "ABOUT",
                    style = MaterialTheme.typography.labelLarge,
                    color = DarkOnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                SettingsItem(
                    title = "App Version",
                    value = "1.0.0"
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkOnSurface,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkOnSurfaceVariant
                    )
                }
            }
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DarkOnSurface
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Navigate",
                    tint = DarkOnSurfaceVariant
                )
            }
        }
    }
}

// Helper functions
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun loadRawList(context: Context, resId: Int, typeToken: TypeToken<*>): List<*> {
    val inputStream = context.resources.openRawResource(resId)
    inputStream.use { stream ->
        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val json = reader.readText()
        return Gson().fromJson(json, typeToken.type)
    }
}

private fun loadCityPricesFromRaw(context: Context, resId: Int): List<CityPriceItem> {
    val inputStream = context.resources.openRawResource(resId)
    inputStream.use { stream ->
        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val json = reader.readText()
        val listType = object : TypeToken<List<CityPriceItem>>() {}.type
        return Gson().fromJson(json, listType)
    }
}

private fun loadTipOfTheDay(context: Context): String {
    val inputStream = context.resources.openRawResource(R.raw.tips)
    inputStream.use { stream ->
        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val json = reader.readText()
        val listType = object : TypeToken<List<Tip>>() {}.type
        val tips: List<Tip> = Gson().fromJson(json, listType)
        if (tips.isEmpty()) return ""
        val idx = (LocalDate.now().dayOfYear - 1) % tips.size
        return tips[idx].text
    }
}

data class CityPriceItem(
    val name: String,
    val unit: String,
    val price: Double,
    val calories: Double,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fats: Double? = null,
    val sponsoredBy: String? = null
)
