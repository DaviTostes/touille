package com.touille.data

import com.touille.domain.Ingredient
import com.touille.domain.Recipe
import com.touille.domain.RecipeIngredient
import com.touille.domain.ShoppingItem
import com.touille.domain.foldForSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class TouilleRepository(private val dao: TouilleDao) {

    val ingredients: Flow<List<Ingredient>> =
        dao.observeIngredients()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.Default)

    val recipes: Flow<List<Recipe>> =
        combine(dao.observeRecipes(), dao.observeIngredients()) { recipes, catalog ->
            val byKey = catalog.associateBy { it.key }
            recipes.map { it.toDomain(byKey) }
        }.flowOn(Dispatchers.Default)

    val shopping: Flow<List<ShoppingItem>> =
        combine(dao.observeShopping(), dao.observeIngredients()) { items, catalog ->
            val byKey = catalog.associateBy { it.key }
            items.map { it.toDomain(byKey) }
        }.flowOn(Dispatchers.Default)

    fun recipe(id: Long): Flow<Recipe?> =
        combine(dao.observeRecipe(id), dao.observeIngredients()) { recipe, catalog ->
            val byKey = catalog.associateBy { it.key }
            recipe?.toDomain(byKey)
        }.flowOn(Dispatchers.Default)

    suspend fun setOwned(ingredientKey: String, owned: Boolean) = dao.setOwned(ingredientKey, owned)

    suspend fun clearFridge() = dao.clearFridge()

    suspend fun saveRecipe(
        id: Long,
        name: String,
        photoUri: String?,
        steps: String,
        prepMinutes: Int,
        servings: Int,
        tags: List<String>,
        items: List<Pair<String, String>>
    ): Long = dao.saveRecipe(
        RecipeEntity(
            id = id,
            name = name.trim(),
            photoUri = photoUri,
            steps = steps,
            prepMinutes = prepMinutes,
            servings = servings,
            tags = tags.joinToString(",")
        ),
        items.map { (ingredientKey, amount) -> RecipeIngredientEntity(id, ingredientKey, amount) }
    )

    suspend fun deleteRecipe(id: Long) = dao.deleteRecipe(id)

    /**
     * Adds something to the shopping list. When [ingredientKey] is given the item is linked to the
     * catalog, so it can later jump straight into the fridge. Existing entries are never duplicated:
     * they are simply brought back to "to buy".
     */
    suspend fun addShoppingItem(name: String, ingredientKey: String? = null, amount: String = "") {
        val trimmedName = name.trim()
        val trimmedAmount = amount.trim()
        if (ingredientKey == null && trimmedName.isEmpty()) return
        val existing = dao.shoppingOnce().firstOrNull { item ->
            if (ingredientKey != null) {
                item.ingredientKey == ingredientKey
            } else {
                item.ingredientKey == null && item.customName.foldForSearch() == trimmedName.foldForSearch()
            }
        }
        if (existing != null) {
            if (existing.checked) dao.setShoppingChecked(existing.id, false)
            if (existing.amount.isBlank() && trimmedAmount.isNotEmpty()) {
                dao.setShoppingAmount(existing.id, trimmedAmount)
            }
            return
        }
        dao.insertShoppingItem(
            ShoppingItemEntity(
                ingredientKey = ingredientKey,
                customName = if (ingredientKey == null) trimmedName else "",
                amount = trimmedAmount
            )
        )
    }

    suspend fun addToList(ingredients: List<Ingredient>) {
        ingredients.forEach { addShoppingItem(name = it.name, ingredientKey = it.key) }
    }

    suspend fun addMissingToList(recipe: Recipe) {
        recipe.missing.forEach { line ->
            addShoppingItem(
                name = line.ingredient.name,
                ingredientKey = line.ingredient.key,
                amount = line.amount
            )
        }
    }

    suspend fun toggleShoppingItem(id: Long, checked: Boolean) = dao.setShoppingChecked(id, checked)

    suspend fun removeShoppingItem(id: Long) = dao.deleteShoppingItems(listOf(id))

    suspend fun clearCheckedShopping() = dao.clearCheckedShopping()

    /** Marks every checked catalog item as owned and takes it off the list. Returns how many moved. */
    suspend fun moveCheckedToFridge(): Int {
        val moveable = dao.shoppingOnce().filter { it.checked && it.ingredientKey != null }
        if (moveable.isEmpty()) return 0
        dao.markOwned(moveable.mapNotNull { it.ingredientKey })
        dao.deleteShoppingItems(moveable.map { it.id })
        return moveable.size
    }
}

private fun IngredientEntity.toDomain() = Ingredient(key, name, namePt, category, emoji, owned)

private fun ShoppingItemEntity.toDomain(catalog: Map<String, IngredientEntity>) = ShoppingItem(
    id = id,
    ingredient = ingredientKey?.let { catalog[it] }?.toDomain(),
    customName = customName,
    amount = amount,
    checked = checked
)

private fun RecipeWithIngredients.toDomain(catalog: Map<String, IngredientEntity>) = Recipe(
    id = recipe.id,
    name = recipe.name,
    photoUri = recipe.photoUri,
    steps = recipe.steps,
    prepMinutes = recipe.prepMinutes,
    servings = recipe.servings,
    tags = recipe.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    ingredients = items.mapNotNull { item ->
        catalog[item.ingredientKey]?.let { RecipeIngredient(it.toDomain(), item.amount) }
    }.sortedBy { it.ingredient.name }
)
