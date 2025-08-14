package com.innovatex.bulkup.logic

import kotlin.math.ln
import kotlin.math.pow

object HealthCalculators {
	fun bmi(weightKg: Double, heightCm: Double): Double {
		if (weightKg <= 0 || heightCm <= 0) return 0.0
		val hM = heightCm / 100.0
		return weightKg / (hM * hM)
	}

	fun bmiCategory(bmi: Double): String = when {
		bmi <= 0 -> "—"
		bmi < 18.5 -> "Underweight"
		bmi < 24.9 -> "Normal"
		bmi < 29.9 -> "Overweight"
		else -> "Obese"
	}

	// US Navy method approximation
	fun bodyFatPercentage(gender: String, waistCm: Double?, neckCm: Double?, hipCm: Double?, heightCm: Double?): Double? {
		if (heightCm == null || waistCm == null || neckCm == null) return null
		if (gender.lowercase() == "male") {
			return (495.0 / (1.0324 - 0.19077 * ln((waistCm - neckCm)) + 0.15456 * ln(heightCm)) - 450.0)
		}
		if (hipCm == null) return null
		return (495.0 / (1.29579 - 0.35004 * ln((waistCm + hipCm - neckCm)) + 0.22100 * ln(heightCm)) - 450.0)
	}

	fun tdeeMifflinStJeor(gender: String, weightKg: Double, heightCm: Double, age: Int, activityLevel: String): Double {
		val bmr = if (gender.lowercase() == "male") {
			10 * weightKg + 6.25 * heightCm - 5 * age + 5
		} else {
			10 * weightKg + 6.25 * heightCm - 5 * age - 161
		}
		val factor = when (activityLevel) {
			"sedentary" -> 1.2
			"light" -> 1.375
			"moderate" -> 1.55
			"active" -> 1.725
			"very_active" -> 1.9
			else -> 1.55
		}
		return bmr * factor
	}

	data class MacroResult(val calories: Int, val proteinG: Int, val carbsG: Int, val fatsG: Int)

	fun macrosFromPercent(calories: Int, pPercent: Int, cPercent: Int, fPercent: Int): MacroResult {
		val pCal = calories * pPercent / 100
		val cCal = calories * cPercent / 100
		val fCal = calories * fPercent / 100
		return MacroResult(
			calories,
			proteinG = (pCal / 4.0).toInt(),
			carbsG = (cCal / 4.0).toInt(),
			fatsG = (fCal / 9.0).toInt()
		)
	}

	fun macrosFromPerKg(calories: Int, proteinPerKg: Double, weightKg: Double): MacroResult {
		val proteinG = (proteinPerKg * weightKg).toInt()
		val remainingCal = calories - proteinG * 4
		val fatsG = (remainingCal * 0.25 / 9.0).toInt()
		val carbsG = ((remainingCal - fatsG * 9) / 4.0).toInt()
		return MacroResult(calories, proteinG, carbsG, fatsG)
	}
}


