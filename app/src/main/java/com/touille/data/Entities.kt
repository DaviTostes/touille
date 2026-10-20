package com.touille.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val key: String,
    val name: String,
    val namePt: String,
    val category: String,
    val emoji: String,
    val owned: Boolean = false
)

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String? = null,
    val steps: String = "",
    val prepMinutes: Int = 0,
    val servings: Int = 2,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientKey"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ingredientKey")]
)
data class RecipeIngredientEntity(
    val recipeId: Long,
    val ingredientKey: String,
    val amount: String = ""
)

@Entity(tableName = "shopping_items", indices = [Index("ingredientKey")])
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ingredientKey: String? = null,
    val customName: String = "",
    val amount: String = "",
    val checked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
