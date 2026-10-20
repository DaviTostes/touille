package com.touille.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touille.data.TouilleRepository
import com.touille.di.LanguageStore
import com.touille.domain.Ingredient
import com.touille.ui.Language
import com.touille.ui.displayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EditorUiState(
    val recipeId: Long = 0,
    val name: String = "",
    val photoUri: String? = null,
    val steps: String = "",
    val prepMinutes: String = "",
    val servings: String = "2",
    val tags: String = "",
    val selected: Map<String, String> = emptyMap(),
    val catalog: List<Ingredient> = emptyList(),
    val query: String = "",
    val language: Language = Language.EN,
    val saved: Boolean = false
) {
    val isEditing: Boolean get() = recipeId != 0L
    val canSave: Boolean get() = name.isNotBlank() && selected.isNotEmpty()
    val selectedIngredients: List<Ingredient>
        get() = catalog.filter { it.key in selected }.sortedBy { it.displayName(language) }
    val results: List<Ingredient>
        get() = query.trim()
            .let { term -> if (term.isEmpty()) catalog else catalog.filter { it.matches(term) } }
            .sortedBy { it.displayName(language) }
}

class RecipeEditorViewModel(
    private val repository: TouilleRepository,
    languageStore: LanguageStore,
    recipeId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState(recipeId = recipeId))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val catalog = repository.ingredients.first()
            _uiState.value = _uiState.value.copy(catalog = catalog)
        }
        viewModelScope.launch {
            languageStore.language.collect { language -> update { it.copy(language = language) } }
        }
        if (recipeId != 0L) {
            viewModelScope.launch {
                val recipe = repository.recipe(recipeId).first() ?: return@launch
                _uiState.value = _uiState.value.copy(
                    name = recipe.name,
                    photoUri = recipe.photoUri,
                    steps = recipe.steps,
                    prepMinutes = recipe.prepMinutes.takeIf { it > 0 }?.toString().orEmpty(),
                    servings = recipe.servings.toString(),
                    tags = recipe.tags.joinToString(", "),
                    selected = recipe.ingredients.associate { it.ingredient.key to it.amount }
                )
            }
        }
    }

    fun onName(value: String) = update { it.copy(name = value) }
    fun onPhoto(value: String?) = update { it.copy(photoUri = value) }
    fun onSteps(value: String) = update { it.copy(steps = value) }
    fun onPrepMinutes(value: String) = update { it.copy(prepMinutes = value.filter(Char::isDigit)) }
    fun onServings(value: String) = update { it.copy(servings = value.filter(Char::isDigit)) }
    fun onTags(value: String) = update { it.copy(tags = value) }
    fun onQuery(value: String) = update { it.copy(query = value) }

    fun toggleIngredient(key: String) = update { state ->
        state.copy(
            selected = if (key in state.selected) {
                state.selected - key
            } else {
                state.selected + (key to "")
            }
        )
    }

    fun onAmount(key: String, amount: String) = update { state ->
        if (key in state.selected) state.copy(selected = state.selected + (key to amount)) else state
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            repository.saveRecipe(
                id = state.recipeId,
                name = state.name,
                photoUri = state.photoUri,
                steps = state.steps.trim(),
                prepMinutes = state.prepMinutes.toIntOrNull() ?: 0,
                servings = state.servings.toIntOrNull() ?: 1,
                tags = state.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                items = state.selected.toList()
            )
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    private inline fun update(block: (EditorUiState) -> EditorUiState) {
        _uiState.value = block(_uiState.value)
    }
}
