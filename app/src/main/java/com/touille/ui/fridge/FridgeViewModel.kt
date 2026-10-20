package com.touille.ui.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touille.data.TouilleRepository
import com.touille.di.LanguageStore
import com.touille.domain.Ingredient
import com.touille.ui.Language
import com.touille.ui.displayName
import com.touille.ui.stringsFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FridgeCategory(
    val name: String,
    val ingredients: List<Ingredient>,
    val ownedCount: Int,
    val collapsed: Boolean
)

data class FridgeUiState(
    val query: String = "",
    val categories: List<FridgeCategory> = emptyList(),
    val ownedTotal: Int = 0,
    val catalogTotal: Int = 0,
    val language: Language = Language.EN
)

class FridgeViewModel(
    private val repository: TouilleRepository,
    private val languageStore: LanguageStore
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val expanded = MutableStateFlow(emptySet<String>())

    val uiState: StateFlow<FridgeUiState> =
        combine(
            repository.ingredients,
            query,
            expanded,
            languageStore.language
        ) { ingredients, search, open, language ->
            val term = search.trim()
            val labels = stringsFor(language)
            val visible = if (term.isEmpty()) ingredients else ingredients.filter { it.matches(term) }
            FridgeUiState(
                query = search,
                categories = visible.groupBy { it.category }
                    .map { (category, items) ->
                        FridgeCategory(
                            name = category,
                            ingredients = items.sortedBy { it.displayName(language) },
                            ownedCount = items.count { it.owned },
                            collapsed = term.isEmpty() && category !in open
                        )
                    }
                    .sortedBy { labels.categoryLabel(it.name) },
                ownedTotal = ingredients.count { it.owned },
                catalogTotal = ingredients.size,
                language = language
            )
        }.flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FridgeUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun toggleCategory(category: String) {
        expanded.value = expanded.value.let { if (category in it) it - category else it + category }
    }

    fun toggleIngredient(ingredient: Ingredient) {
        viewModelScope.launch { repository.setOwned(ingredient.key, !ingredient.owned) }
    }

    fun clearFridge() {
        viewModelScope.launch { repository.clearFridge() }
    }
}
