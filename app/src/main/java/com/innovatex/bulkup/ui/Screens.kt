package com.innovatex.bulkup.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.innovatex.bulkup.R
import com.innovatex.bulkup.data.*
import com.innovatex.bulkup.logic.HealthCalculators
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

private fun <T> loadRawList(context: Context, resId: Int, typeToken: TypeToken<T>): T {
	val inputStream = context.resources.openRawResource(resId)
	inputStream.use { stream ->
		val reader = BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8))
		val json = reader.readText()
		return Gson().fromJson(json, typeToken.type)
	}
}

@Composable
fun SettingsScreenFull(context: Context, storage: LocalStorage) {
	var profile by remember { mutableStateOf(storage.loadProfile()) }
	var bmiVal by remember { mutableStateOf(0.0) }
	var bmiCat by remember { mutableStateOf("—") }
	var bodyFat by remember { mutableStateOf<Double?>(null) }
	var tdee by remember { mutableStateOf(0) }
	var macros by remember { mutableStateOf(HealthCalculators.MacroResult(0,0,0,0)) }

	fun recalc() {
		profile.heightCm?.let { h ->
			profile.weightKg?.let { w ->
				bmiVal = HealthCalculators.bmi(w, h)
				bmiCat = HealthCalculators.bmiCategory(bmiVal)
			}
		}
		bodyFat = HealthCalculators.bodyFatPercentage(
			profile.gender,
			profile.waistCm,
			profile.neckCm,
			profile.hipCm,
			profile.heightCm
		)
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

	Column(modifier = Modifier.padding(16.dp)) {
		Text("Profile & Calculators")
		Spacer(modifier = Modifier.height(8.dp))
		Row(modifier = Modifier.fillMaxWidth()) {
			OutlinedTextField(
				value = (profile.heightCm?.toString() ?: ""),
				onValueChange = { v -> profile = profile.copy(heightCm = v.toDoubleOrNull()); recalc() },
				label = { Text("Height (cm)") },
				modifier = Modifier.weight(1f)
			)
			Spacer(modifier = Modifier.height(8.dp))
			OutlinedTextField(
				value = (profile.weightKg?.toString() ?: ""),
				onValueChange = { v -> profile = profile.copy(weightKg = v.toDoubleOrNull()); recalc() },
				label = { Text("Weight (kg)") },
				modifier = Modifier.weight(1f)
			)
		}
		Spacer(modifier = Modifier.height(8.dp))
		Text("BMI: ${String.format("%.1f", bmiVal)} ($bmiCat)")
		Text("Body Fat (est.): ${bodyFat?.let { String.format("%.1f%%", it) } ?: "—"}")
		Spacer(modifier = Modifier.height(8.dp))
		Text("TDEE (with surplus): $tdee kcal")
		Text("Macros -> Protein: ${macros.proteinG}g, Carbs: ${macros.carbsG}g, Fats: ${macros.fatsG}g")
		Spacer(modifier = Modifier.height(8.dp))
		Button(onClick = { storage.saveProfile(profile) }) { Text("Save Profile") }
	}
}

@Composable
fun FoodLoggerScreenFull(context: Context, storage: LocalStorage) {
	val foods: List<FoodItem> = loadRawList(
		context,
		R.raw.foods,
		object : TypeToken<List<FoodItem>>() {}
	)
	val today = java.time.LocalDate.now().toString()
	var log by remember { mutableStateOf(storage.loadDailyFoodLog(today)) }
	var selected by remember { mutableStateOf<FoodItem?>(null) }
	var qty by remember { mutableStateOf("1.0") }

	fun add() {
		val f = selected ?: return
		val q = qty.toDoubleOrNull() ?: 1.0
		log.entries.add(
			FoodLogEntry(
				foodId = f.id,
				name = f.name,
				quantity = q,
				calories = f.calories * q,
				protein = f.protein * q,
				carbs = f.carbs * q,
				fats = f.fats * q
			)
		)
		storage.saveDailyFoodLog(log)
	}

	Column(modifier = Modifier.padding(16.dp)) {
		Text("Food Logger")
		Spacer(modifier = Modifier.height(8.dp))
		LazyColumn(modifier = Modifier.height(160.dp)) {
			items(foods) { f ->
				Button(onClick = { selected = f }) { Text(f.name) }
			}
		}
		Spacer(modifier = Modifier.height(8.dp))
		Row(modifier = Modifier.fillMaxWidth()) {
			OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Qty") })
			Spacer(modifier = Modifier.height(8.dp))
			Button(onClick = { add() }) { Text("Add") }
		}
		Spacer(modifier = Modifier.height(8.dp))
		val totals = log.entries.fold(doubleArrayOf(0.0,0.0,0.0,0.0)) { acc, e ->
			acc[0]+=e.calories; acc[1]+=e.protein; acc[2]+=e.carbs; acc[3]+=e.fats; acc
		}
		Text("Today Total: kcal ${totals[0].toInt()} • P ${totals[1].toInt()}g • C ${totals[2].toInt()}g • F ${totals[3].toInt()}g")
		Spacer(modifier = Modifier.height(8.dp))
		LazyColumn { items(log.entries) { e -> Text("${e.name} x${e.quantity} -> ${e.calories.toInt()} kcal") } }
	}
}

@Composable
fun ProgressScreenFull(storage: LocalStorage) {
	var weight by remember { mutableStateOf("") }
	val entries = storage.loadWeightEntries()
	Column(modifier = Modifier.padding(16.dp)) {
		Text("Progress Tracking")
		Spacer(modifier = Modifier.height(8.dp))
		OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") })
		Spacer(modifier = Modifier.height(8.dp))
		Button(onClick = {
			weight.toDoubleOrNull()?.let { w ->
				storage.appendWeightEntry(WeightEntry(java.time.LocalDate.now().toString(), w))
			}
		}) { Text("Add Weight") }
		Spacer(modifier = Modifier.height(8.dp))
		LazyColumn { items(entries) { e -> Text("${e.date}: ${e.weightKg} kg") } }
	}
}


