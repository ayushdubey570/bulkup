package com.innovatex.bulkup.data

data class Profile(
	val name: String? = null,
	val age: Int? = null,
	val gender: String = "male", // "male" or "female"
	val weightKg: Double? = null,
	val heightCm: Double? = null,
	val activityLevel: String = "moderate", // sedentary, light, moderate, active, very_active
	val waistCm: Double? = null,
	val neckCm: Double? = null,
	val hipCm: Double? = null,
	val calorieSurplus: Int = 300, // surplus for bulking
	val macroMode: String = "percent", // "percent" or "perkg"
	val macroPercentProtein: Int = 30,
	val macroPercentCarbs: Int = 50,
	val macroPercentFats: Int = 20,
	val proteinPerKg: Double = 2.2
)

data class FoodItem(
	val id: String,
	val name: String,
	val servingSize: String,
	val calories: Double,
	val protein: Double,
	val carbs: Double,
	val fats: Double
)

data class FoodLogEntry(
	val foodId: String,
	val name: String,
	val quantity: Double, // multiplier of serving
	val calories: Double,
	val protein: Double,
	val carbs: Double,
	val fats: Double
)

data class DailyFoodLog(
	val date: String,
	val entries: MutableList<FoodLogEntry> = mutableListOf()
)

data class WeightEntry(
	val date: String,
	val weightKg: Double
)

data class WorkoutExercise(
	val id: String,
	val name: String,
	val muscleGroup: String,
	val equipment: String,
	val description: String
)

data class WorkoutSession(
	val date: String,
	val name: String,
	val exerciseIds: List<String>,
	val completed: Boolean = false
)

data class Tip(
	val id: String,
	val text: String
)

data class AchievementDefinition(
	val id: String,
	val name: String,
	val description: String,
	val type: String, // workout_streak_days, calories_logged_total, first_workout
	val threshold: Int
)

data class GamificationState(
	val lastActionDate: String? = null,
	val currentStreakDays: Int = 0,
	val points: Int = 0,
	val totalCaloriesLogged: Int = 0,
	val badgesUnlocked: MutableSet<String> = mutableSetOf()
)


