package com.touille.data

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val items: List<RecipeIngredientEntity>
)

@Dao
interface TouilleDao {

    @Query("SELECT * FROM ingredients ORDER BY category, name")
    fun observeIngredients(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients ORDER BY category, name")
    suspend fun ingredientsOnce(): List<IngredientEntity>

    @Query("UPDATE ingredients SET owned = :owned WHERE `key` = :key")
    suspend fun setOwned(key: String, owned: Boolean)

    @Query("UPDATE ingredients SET owned = 1 WHERE `key` IN (:keys)")
    suspend fun markOwned(keys: List<String>)

    @Query("UPDATE ingredients SET owned = 0")
    suspend fun clearFridge()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredients(items: List<IngredientEntity>)

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun observeRecipes(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipe(id: Long): Flow<RecipeWithIngredients?>

    @Upsert
    suspend fun upsertRecipe(recipe: RecipeEntity): Long

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun clearRecipeIngredients(recipeId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(items: List<RecipeIngredientEntity>)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    @Transaction
    suspend fun saveRecipe(recipe: RecipeEntity, items: List<RecipeIngredientEntity>): Long {
        val id = upsertRecipe(recipe).let { if (it == -1L) recipe.id else it }
        clearRecipeIngredients(id)
        insertRecipeIngredients(items.map { it.copy(recipeId = id) })
        return id
    }

    @Query("SELECT * FROM shopping_items ORDER BY createdAt DESC, id DESC")
    fun observeShopping(): Flow<List<ShoppingItemEntity>>

    @Query("SELECT * FROM shopping_items ORDER BY createdAt DESC, id DESC")
    suspend fun shoppingOnce(): List<ShoppingItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItemEntity): Long

    @Query("UPDATE shopping_items SET checked = :checked WHERE id = :id")
    suspend fun setShoppingChecked(id: Long, checked: Boolean)

    @Query("UPDATE shopping_items SET amount = :amount WHERE id = :id")
    suspend fun setShoppingAmount(id: Long, amount: String)

    @Query("DELETE FROM shopping_items WHERE id IN (:ids)")
    suspend fun deleteShoppingItems(ids: List<Long>)

    @Query("DELETE FROM shopping_items WHERE checked = 1")
    suspend fun clearCheckedShopping()
}
