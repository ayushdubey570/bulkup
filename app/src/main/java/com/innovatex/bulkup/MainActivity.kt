package com.innovatex.bulkup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.innovatex.bulkup.ui.theme.BulkupTheme
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import com.innovatex.bulkup.R
import androidx.compose.foundation.layout.fillMaxWidth
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.innovatex.bulkup.data.LocalStorage
import com.innovatex.bulkup.data.GamificationState
import com.innovatex.bulkup.data.AchievementDefinition
import com.innovatex.bulkup.logic.Gamification
import java.time.LocalDate
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this)
        setContent {
            BulkupTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController: NavHostController = rememberNavController()
    val context = LocalContext.current
    val storage = LocalStorage(context)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BannerAdView()
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { HomeScreen(storage = storage, onNavigate = { route -> navController.navigate(route) }) }
                composable("budget") { BudgetBulkScreen() }
                composable("workouts") { WorkoutScreen(storage = storage) }
                composable("food") { com.innovatex.bulkup.ui.FoodLoggerScreenFull(context, storage) }
                composable("progress") { com.innovatex.bulkup.ui.ProgressScreenFull(storage) }
                composable("settings") { com.innovatex.bulkup.ui.SettingsScreenFull(context, storage) }
                composable("badges") { BadgesScreen() }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    BulkupTheme {
        App()
    }
}

@Composable
fun BannerAdView() {
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

@Composable
fun HomeScreen(storage: LocalStorage, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    var tip by remember { mutableStateOf("") }
    val game = remember { storage.loadGamification() }
    LaunchedEffect(Unit) {
        tip = loadTipOfTheDay(context)
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Namaste, Fitness Enthusiast!")
        Spacer(modifier = Modifier.height(6.dp))
        Text("Streak: ${game.currentStreakDays} days • Points: ${game.points}")
        Spacer(modifier = Modifier.height(6.dp))
        Text("Tip: $tip")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { onNavigate("budget") }) { Text("Budget Bulk Mode") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onNavigate("workouts") }) { Text("Workout Library") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onNavigate("food") }) { Text("Food Logger") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onNavigate("progress") }) { Text("Progress Tracker") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onNavigate("settings") }) { Text("Settings & Calculators") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onNavigate("badges") }) { Text("Badges & Achievements") }
    }
}

@Composable
fun BadgesScreen() {
    val context = LocalContext.current
    val storage = LocalStorage(context)
    val state = storage.loadGamification()
    val defs = loadAchievements(context)
    val unlocked = state.badgesUnlocked
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Badges")
        Spacer(modifier = Modifier.height(8.dp))
        defs.forEach { def ->
            val have = unlocked.contains(def.id)
            Text(text = (if (have) "✅ " else "⬜ ") + def.name + " — " + def.description)
            Spacer(modifier = Modifier.height(4.dp))
        }
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

@Composable
fun BudgetBulkScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var items by remember { mutableStateOf<List<CityPriceItem>>(emptyList()) }
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }

    LaunchedEffect(Unit) {
        items = loadCityPricesFromRaw(context, R.raw.delhi_prices)
            .sortedByDescending { cityItem ->
                if (cityItem.price <= 0.0) 0.0 else cityItem.calories / cityItem.price
            }
        RewardedAd.load(
            context,
            "ca-app-pub-3940256099942544/5224354917",
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
                override fun onAdFailedToLoad(adError: LoadAdError) { rewardedAd = null }
            }
        )
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(items) { item ->
            val value = if (item.price <= 0.0) 0.0 else item.calories / item.price
            Text(text = "${item.name} • ${item.unit} • ₹${item.price}")
            Text(text = "Calories: ${item.calories} • Value: ${String.format("%.1f", value)} cal/₹")
            item.sponsoredBy?.let { sponsor ->
                Text(text = "Sponsored: $sponsor")
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                rewardedAd?.show(context as ComponentActivity) { _ -> }
            }) { Text("Watch video to unlock 7-day plan") }
            Spacer(modifier = Modifier.height(16.dp))
        }
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

@Composable
fun WorkoutScreen(storage: LocalStorage) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }

    LaunchedEffect(Unit) {
        InterstitialAd.load(
            context,
            "ca-app-pub-3940256099942544/1033173712",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
                override fun onAdFailedToLoad(adError: LoadAdError) { interstitialAd = null }
            }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Workout Library & Tracker")
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = {
            interstitialAd?.show(context as ComponentActivity)
            interstitialAd = null
            // Gamification: award points and update streak
            val today = LocalDate.now()
            var game = storage.loadGamification()
            game = Gamification.registerAction(today, game, points = 20)
            val defs = loadAchievements(context)
            val unlocked = Gamification.evaluateAchievements(game, defs)
            storage.saveGamification(game.copy(badgesUnlocked = unlocked.toMutableSet()))
        }) { Text("Complete Workout (shows ad)") }
    }
}

@Composable
fun FoodLoggerScreen() { Text("Food Logger & TDEE") }

@Composable
fun ProgressScreen() { Text("Progress Tracking") }

@Composable
fun SettingsScreen() { Text("Settings") }

private fun loadAchievements(context: Context): List<AchievementDefinition> {
    val inputStream = context.resources.openRawResource(R.raw.achievements)
    inputStream.use { stream ->
        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val json = reader.readText()
        val listType = object : com.google.gson.reflect.TypeToken<List<AchievementDefinition>>() {}.type
        return Gson().fromJson(json, listType)
    }
}

private fun loadTipOfTheDay(context: Context): String {
    val inputStream = context.resources.openRawResource(R.raw.tips)
    inputStream.use { stream ->
        val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
        val json = reader.readText()
        val listType = object : com.google.gson.reflect.TypeToken<List<com.innovatex.bulkup.data.Tip>>() {}.type
        val tips: List<com.innovatex.bulkup.data.Tip> = Gson().fromJson(json, listType)
        if (tips.isEmpty()) return ""
        val idx = (java.time.LocalDate.now().dayOfYear - 1) % tips.size
        return tips[idx].text
    }
}