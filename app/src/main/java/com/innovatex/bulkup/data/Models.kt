package com.innovatex.bulkup.data

import java.time.LocalDate

data class Profile(
	val name: String? = null,
	val age: Int? = null,
	val gender: String = "male", // "male" or "female"
	val weightKg: Double? = null,
	val heightCm: Double? = null,
	val activityLevel: String = "moderate", // sedentary, light, moderate, active, very_active
	val fitnessGoal: String = "weight_loss", // weight_loss, maintenance, muscle_gain
	val waistCm: Double? = null,
	val neckCm: Double? = null,
	val hipCm: Double? = null,
	val calorieSurplus: Int = 300, // surplus for bulking
	val macroMode: String = "percent", // "percent" or "perkg"
	val macroPercentProtein: Int = 30,
	val macroPercentCarbs: Int = 50,
	val macroPercentFats: Int = 20,
	val proteinPerKg: Double = 2.2,
	val units: String = "metric", // metric or imperial
	val language: String = "english" // english or hindi
)

data class FoodItem(
	val id: String,
	val name: String,
	val servingSize: String,
	val calories: Double,
	val protein: Double,
	val carbs: Double,
	val fats: Double,
	val category: String = "general" // breakfast, lunch, dinner, snacks
)

data class FoodLogEntry(
	val id: String = java.util.UUID.randomUUID().toString(),
	val foodId: String,
	val name: String,
	val quantity: Double, // multiplier of serving
	val calories: Double,
	val protein: Double,
	val carbs: Double,
	val fats: Double,
	val mealType: String = "breakfast", // breakfast, lunch, dinner, snacks
	val timestamp: Long = System.currentTimeMillis()
)

data class DailyFoodLog(
	val date: String,
	val entries: MutableList<FoodLogEntry> = mutableListOf(),
	val totalCalories: Double = 0.0,
	val totalProtein: Double = 0.0,
	val totalCarbs: Double = 0.0,
	val totalFats: Double = 0.0
)

data class WeightEntry(
	val id: String = java.util.UUID.randomUUID().toString(),
	val date: String,
	val weightKg: Double,
	val notes: String? = null,
	val timestamp: Long = System.currentTimeMillis()
)

data class WorkoutExercise(
	val id: String,
	val name: String,
	val muscleGroup: String,
	val equipment: String,
	val description: String,
	val imageUrl: String? = null,
	val instructions: List<String> = emptyList(),
	val difficulty: String = "beginner" // beginner, intermediate, advanced
)

data class WorkoutSession(
	val id: String = java.util.UUID.randomUUID().toString(),
	val date: String,
	val name: String,
	val exerciseIds: List<String>,
	val completed: Boolean = false,
	val duration: Int = 0, // in minutes
	val caloriesBurned: Int = 0,
	val notes: String? = null
)

data class WorkoutSet(
	val id: String = java.util.UUID.randomUUID().toString(),
	val exerciseId: String,
	val reps: Int,
	val weight: Double? = null,
	val duration: Int? = null, // for time-based exercises
	val restTime: Int = 60, // rest time in seconds
	val completed: Boolean = false
)

data class Tip(
	val id: String,
	val text: String,
	val category: String = "general", // nutrition, workout, motivation
	val imageUrl: String? = null
)

data class AchievementDefinition(
	val id: String,
	val name: String,
	val description: String,
	val type: String, // workout_streak_days, calories_logged_total, first_workout, weight_goal
	val threshold: Int,
	val icon: String = "🏆",
	val color: String = "#4CAF50"
)

data class GamificationState(
	val lastActionDate: String? = null,
	val currentStreakDays: Int = 0,
	val longestStreakDays: Int = 0,
	val points: Int = 0,
	val totalCaloriesLogged: Int = 0,
	val totalWorkoutsCompleted: Int = 0,
	val badgesUnlocked: MutableSet<String> = mutableSetOf(),
	val level: Int = 1,
	val experiencePoints: Int = 0
)

data class BudgetMealPlan(
	val id: String = java.util.UUID.randomUUID().toString(),
	val name: String,
	val items: List<BudgetMealItem>,
	val totalCost: Double,
	val totalCalories: Double,
	val totalProtein: Double,
	val totalCarbs: Double,
	val totalFats: Double,
	val city: String,
	val budget: Double,
	val goal: String // lean_bulk, standard_bulk
)

data class BudgetMealItem(
	val id: String,
	val name: String,
	val unit: String,
	val price: Double,
	val calories: Double,
	val protein: Double? = null,
	val carbs: Double? = null,
	val fats: Double? = null,
	val sponsoredBy: String? = null,
	val caloriesPerRupee: Double = 0.0
)

data class DailyProgress(
	val date: String,
	val caloriesConsumed: Double = 0.0,
	val caloriesGoal: Double = 2000.0,
	val proteinConsumed: Double = 0.0,
	val proteinGoal: Double = 150.0,
	val carbsConsumed: Double = 0.0,
	val carbsGoal: Double = 250.0,
	val fatsConsumed: Double = 0.0,
	val fatsGoal: Double = 80.0,
	val workoutsCompleted: Int = 0,
	val weightLogged: Boolean = false
)

data class NotificationSettings(
	val workoutReminders: Boolean = true,
	val mealReminders: Boolean = true,
	val dailyTips: Boolean = true,
	val progressReminders: Boolean = true,
	val reminderTime: String = "08:00" // HH:mm format
)


