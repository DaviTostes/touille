package com.touille.domain

import java.text.Normalizer

private val CombiningMarks = Regex("\\p{Mn}+")

fun String.foldForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(CombiningMarks, "").lowercase()

fun String.containsFolded(term: String): Boolean = foldForSearch().contains(term.foldForSearch())

enum class CookStatus { READY, ALMOST, MISSING }

data class Ingredient(
    val key: String,
    val name: String,
    val namePt: String = name,
    val category: String,
    val emoji: String,
    val owned: Boolean
) {
    fun matches(term: String): Boolean = name.containsFolded(term) || namePt.containsFolded(term)
}

data class RecipeIngredient(
    val ingredient: Ingredient,
    val amount: String
)

data class ShoppingItem(
    val id: Long,
    val ingredient: Ingredient?,
    val customName: String,
    val amount: String,
    val checked: Boolean
) {
    val key: String? get() = ingredient?.key
    val name: String get() = ingredient?.name ?: customName
    val emoji: String get() = ingredient?.emoji ?: "🛒"
    val fromCatalog: Boolean get() = ingredient != null
}

data class Recipe(
    val id: Long,
    val name: String,
    val photoUri: String?,
    val steps: String,
    val prepMinutes: Int,
    val servings: Int,
    val tags: List<String>,
    val ingredients: List<RecipeIngredient>
) {
    val available: List<RecipeIngredient> get() = ingredients.filter { it.ingredient.owned }
    val missing: List<RecipeIngredient> get() = ingredients.filterNot { it.ingredient.owned }
    val status: CookStatus get() = CookMatcher.statusFor(missing.size)
}
