package com.innovatex.bulkup.logic

import com.innovatex.bulkup.data.GamificationState
import com.innovatex.bulkup.data.AchievementDefinition
import java.time.LocalDate

object Gamification {
	fun registerAction(today: LocalDate, state: GamificationState, points: Int): GamificationState {
		val lastDate = state.lastActionDate?.let { LocalDate.parse(it) }
		val newStreak = when {
			lastDate == null -> 1
			lastDate == today.minusDays(1) -> state.currentStreakDays + 1
			lastDate == today -> state.currentStreakDays
			else -> 1
		}
		return state.copy(
			lastActionDate = today.toString(),
			currentStreakDays = newStreak,
			points = state.points + points
		)
	}

	fun updateCaloriesTotal(state: GamificationState, addCalories: Int): GamificationState =
		state.copy(totalCaloriesLogged = state.totalCaloriesLogged + addCalories)

	fun evaluateAchievements(state: GamificationState, defs: List<AchievementDefinition>): Set<String> {
		val unlocked = state.badgesUnlocked.toMutableSet()
		defs.forEach { def ->
			val meets = when (def.type) {
				"workout_streak_days" -> state.currentStreakDays >= def.threshold
				"calories_logged_total" -> state.totalCaloriesLogged >= def.threshold
				"first_workout" -> state.points >= def.threshold // simple proxy
				else -> false
			}
			if (meets) unlocked.add(def.id)
		}
		return unlocked
	}
}


