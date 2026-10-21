package com.touille.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touille.data.TouilleRepository
import com.touille.di.LanguageStore
import com.touille.domain.Ingredient
import com.touille.domain.ShoppingItem
import com.touille.ui.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShoppingUiState(
    val items: List<ShoppingItem> = emptyList(),
    val catalog: List<Ingredient> = emptyList(),
    val query: String = "",
    val language: Language = Language.EN
) {
    val toBuy: List<ShoppingItem> get() = items.filterNot { it.checked }
    val done: List<ShoppingItem> get() = items.filter { it.checked }
    val catalogKeys: Set<String> get() = items.mapNotNull { it.key }.toSet()
    val moveableCount: Int get() = done.count { it.fromCatalog }
}

class ShoppingViewModel(
    private val repository: TouilleRepository,
    languageStore: LanguageStore
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<ShoppingUiState> = combine(
        repository.shopping,
        repository.ingredients,
        languageStore.language,
        query
    ) { shopping, catalog, language, search ->
        ShoppingUiState(
            items = shopping,
            catalog = catalog,
            query = search,
            language = language
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingUiState())

    /**
     * Adds whatever is typed: a catalog ingredient when the name matches one exactly, otherwise a
     * free-form item. The catalog keeps its link so checked items can jump into the fridge.
     */
    fun submitSearch() {
        val text = query.value.trim()
        if (text.isEmpty()) return
        val match = uiState.value.catalog.firstOrNull { it.matchesExactly(text) }
        query.value = ""
        viewModelScope.launch {
            if (match != null) {
                repository.addShoppingItem(name = match.name, ingredientKey = match.key)
            } else {
                repository.addShoppingItem(name = text)
            }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun resetQuery() {
        query.value = ""
    }

    fun toggle(item: ShoppingItem) {
        viewModelScope.launch { repository.toggleShoppingItem(item.id, !item.checked) }
    }

    fun remove(item: ShoppingItem) {
        viewModelScope.launch { repository.removeShoppingItem(item.id) }
    }

    fun clearDone() {
        viewModelScope.launch { repository.clearCheckedShopping() }
    }

    /** Adds a catalog ingredient to the list, or takes it off when it is already there. */
    fun toggleCatalogIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            val existing = uiState.value.items.firstOrNull { it.key == ingredient.key }
            if (existing != null) {
                repository.removeShoppingItem(existing.id)
            } else {
                repository.addShoppingItem(name = ingredient.name, ingredientKey = ingredient.key)
            }
        }
    }

    fun moveDoneToFridge() {
        viewModelScope.launch { repository.moveCheckedToFridge() }
    }
}
