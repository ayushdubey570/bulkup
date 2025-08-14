package com.innovatex.bulkup.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LocalStorage(context: Context) {
	private val prefs: SharedPreferences =
		context.getSharedPreferences("bulkup_prefs", Context.MODE_PRIVATE)
	private val gson = Gson()

	fun loadProfile(): Profile =
		prefs.getString("profile", null)?.let { json ->
			gson.fromJson(json, Profile::class.java)
		} ?: Profile()

	fun saveProfile(profile: Profile) {
		prefs.edit().putString("profile", gson.toJson(profile)).apply()
	}

	fun loadGamification(): GamificationState =
		prefs.getString("game_state", null)?.let { json ->
			gson.fromJson(json, GamificationState::class.java)
		} ?: GamificationState()

	fun saveGamification(state: GamificationState) {
		prefs.edit().putString("game_state", gson.toJson(state)).apply()
	}

	fun loadDailyFoodLog(date: String): DailyFoodLog =
		prefs.getString("food_" + date, null)?.let { json ->
			gson.fromJson(json, DailyFoodLog::class.java)
		} ?: DailyFoodLog(date)

	fun saveDailyFoodLog(log: DailyFoodLog) {
		prefs.edit().putString("food_" + log.date, gson.toJson(log)).apply()
	}

	fun appendWeightEntry(entry: WeightEntry) {
		val list = loadWeightEntries().toMutableList()
		list.removeAll { it.date == entry.date }
		list.add(entry)
		prefs.edit().putString("weights", gson.toJson(list)).apply()
	}

	fun loadWeightEntries(): List<WeightEntry> {
		val json = prefs.getString("weights", null) ?: return emptyList()
		val type = object : TypeToken<List<WeightEntry>>() {}.type
		return gson.fromJson(json, type)
	}
}


