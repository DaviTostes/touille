package com.touille.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touille.data.TouilleRepository
import com.touille.domain.CookMatcher
import com.touille.domain.CookStatus
import com.touille.domain.Recipe
import com.touille.domain.containsFolded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeFilters(
    val query: String = "",
    val status: CookStatus? = null,
    val tag: String? = null,
    val quickOnly: Boolean = false
)

data class RecipesUiState(
    val filters: RecipeFilters = RecipeFilters(),
    val recipes: List<Recipe> = emptyList(),
    val tags: List<String> = emptyList(),
    val readyCount: Int = 0,
    val totalCount: Int = 0
) {
    val hasActiveFilters: Boolean
        get() = filters.status != null || filters.tag != null || filters.quickOnly
}

class RecipesViewModel(private val repository: TouilleRepository) : ViewModel() {

    private val filters = MutableStateFlow(RecipeFilters())

    val uiState: StateFlow<RecipesUiState> =
        combine(repository.recipes, filters) { recipes, active ->
            val term = active.query.trim()
            RecipesUiState(
                filters = active,
                recipes = CookMatcher.sortForLibrary(recipes.filter { it.matches(active, term) }),
                tags = recipes.flatMap { it.tags }.distinct().sorted(),
                readyCount = recipes.count { it.status == CookStatus.READY },
                totalCount = recipes.size
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesUiState())

    fun onQueryChange(value: String) {
        filters.value = filters.value.copy(query = value)
    }

    fun onStatusSelected(status: CookStatus?) {
        filters.value = filters.value.copy(status = status.takeIf { it != filters.value.status })
    }

    fun onTagSelected(tag: String) {
        filters.value = filters.value.copy(tag = tag.takeIf { it != filters.value.tag })
    }

    fun onQuickToggled() {
        filters.value = filters.value.copy(quickOnly = !filters.value.quickOnly)
    }

    /** Drops status/tag/quick filters but keeps whatever is typed in the search box. */
    fun clearFilters() {
        filters.value = RecipeFilters(query = filters.value.query)
    }

    fun deleteRecipe(id: Long) {
        viewModelScope.launch { repository.deleteRecipe(id) }
    }
}

private const val QUICK_MEAL_MINUTES = 30

private fun Recipe.matches(filters: RecipeFilters, term: String): Boolean {
    if (term.isNotEmpty() && !name.containsFolded(term) &&
        ingredients.none { it.ingredient.matches(term) }
    ) return false
    if (filters.status != null && status != filters.status) return false
    if (filters.tag != null && filters.tag !in tags) return false
    if (filters.quickOnly && (prepMinutes == 0 || prepMinutes > QUICK_MEAL_MINUTES)) return false
    return true
}
