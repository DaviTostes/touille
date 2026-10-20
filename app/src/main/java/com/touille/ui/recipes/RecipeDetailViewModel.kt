package com.touille.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touille.data.TouilleRepository
import com.touille.domain.Ingredient
import com.touille.domain.Recipe
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val repository: TouilleRepository,
    private val recipeId: Long
) : ViewModel() {

    val recipe: StateFlow<Recipe?> = repository.recipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleOwned(ingredient: Ingredient) {
        viewModelScope.launch { repository.setOwned(ingredient.key, !ingredient.owned) }
    }

    fun addMissingToList(recipe: Recipe) {
        viewModelScope.launch { repository.addMissingToList(recipe) }
    }

    fun delete() {
        viewModelScope.launch { repository.deleteRecipe(recipeId) }
    }
}
