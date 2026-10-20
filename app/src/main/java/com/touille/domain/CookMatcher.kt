package com.touille.domain

object CookMatcher {

    const val ALMOST_THRESHOLD = 2

    fun statusFor(missingCount: Int): CookStatus = when {
        missingCount == 0 -> CookStatus.READY
        missingCount <= ALMOST_THRESHOLD -> CookStatus.ALMOST
        else -> CookStatus.MISSING
    }

    fun missingFor(recipe: Recipe, ownedKeys: Set<String>): List<Ingredient> =
        recipe.ingredients.map { it.ingredient }.filterNot { it.key in ownedKeys }

    fun statusOf(recipe: Recipe, ownedKeys: Set<String>): CookStatus =
        statusFor(missingFor(recipe, ownedKeys).size)

    fun sortForLibrary(recipes: List<Recipe>): List<Recipe> =
        recipes.sortedWith(compareBy({ it.status.ordinal }, { it.missing.size }, { it.name.lowercase() }))
}
