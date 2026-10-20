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
    val draft: String = "",
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

    private val draft = MutableStateFlow("")
    private val query = MutableStateFlow("")

    val uiState: StateFlow<ShoppingUiState> = combine(
        repository.shopping,
        repository.ingredients,
        languageStore.language,
        draft,
        query
    ) { shopping, catalog, language, draftText, search ->
        ShoppingUiState(
            items = shopping,
            catalog = catalog,
            draft = draftText,
            query = search,
            language = language
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingUiState())

    fun onDraftChange(value: String) {
        draft.value = value
    }

    /** Adds whatever is typed in the quick add field, or brings an already listed item back. */
    fun submitDraft() {
        val value = draft.value
        if (value.isBlank()) return
        draft.value = ""
        viewModelScope.launch { repository.addShoppingItem(name = value) }
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

    fun toggleCatalogIngredient(ingredient: Ingredient) {
        viewModelScope.launch { repository.addShoppingItem(name = ingredient.name, ingredientKey = ingredient.key) }
    }

    fun moveDoneToFridge() {
        viewModelScope.launch { repository.moveCheckedToFridge() }
    }
}
