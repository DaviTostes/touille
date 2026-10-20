package com.touille.ui.fridge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touille.domain.Ingredient
import com.touille.ui.ConfirmDialog
import com.touille.ui.CountPill
import com.touille.ui.EmptyState
import com.touille.ui.IngredientChip
import com.touille.ui.Language
import com.touille.ui.LocalStrings
import com.touille.ui.ProgressCard
import com.touille.ui.ScreenHeader
import com.touille.ui.SearchField
import com.touille.ui.displayName

@Composable
fun FridgeScreen(viewModel: FridgeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FridgeContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onToggleCategory = viewModel::toggleCategory,
        onToggleIngredient = viewModel::toggleIngredient,
        onClear = viewModel::clearFridge
    )
}

@Composable
private fun FridgeContent(
    state: FridgeUiState,
    onQueryChange: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onToggleIngredient: (Ingredient) -> Unit,
    onClear: () -> Unit
) {
    val strings = LocalStrings.current
    var query by rememberSaveable { mutableStateOf(state.query) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(
                title = strings.fridgeTitle,
                subtitle = strings.fridgeCount(state.ownedTotal)
            )

            if (state.catalogTotal > 0) {
                ProgressCard(
                    label = strings.fridgeProgress(state.ownedTotal, state.catalogTotal),
                    progress = state.ownedTotal / state.catalogTotal.toFloat(),
                    trailing = {
                        if (state.ownedTotal > 0) {
                            TextButton(onClick = { confirmClear = true }) {
                                Text(
                                    strings.emptyFridgeAction,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                )
            }

            SearchField(
                value = query,
                onValueChange = {
                    query = it
                    onQueryChange(it)
                },
                placeholder = strings.searchIngredients
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (state.categories.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            emoji = "🔍",
                            title = strings.noMatchTitle,
                            subtitle = strings.noMatchIngredients
                        )
                    }
                }
            }

            items(state.categories, key = { it.name }) { category ->
                CategorySection(
                    category = category,
                    language = state.language,
                    onHeaderClick = { onToggleCategory(category.name) },
                    onToggleIngredient = onToggleIngredient
                )
            }
        }
    }

    if (confirmClear) {
        ConfirmDialog(
            title = strings.emptyFridgeTitle,
            body = strings.emptyFridgeBody,
            confirmLabel = strings.emptyFridgeAction,
            dismissLabel = strings.deleteCancel,
            onConfirm = {
                confirmClear = false
                onClear()
            },
            onDismiss = { confirmClear = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    category: FridgeCategory,
    language: Language,
    onHeaderClick: () -> Unit,
    onToggleIngredient: (Ingredient) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (category.collapsed) -90f else 0f,
        label = "chevron"
    )
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onHeaderClick)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                LocalStrings.current.categoryLabel(category.name),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            CountPill(
                text = "${category.ownedCount}/${category.ingredients.size}",
                filled = category.ownedCount > 0
            )
        }
        AnimatedVisibility(
            visible = !category.collapsed,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    category.ingredients.forEach { ingredient ->
                        IngredientChip(
                            name = ingredient.displayName(language),
                            emoji = ingredient.emoji,
                            selected = ingredient.owned,
                            onClick = { onToggleIngredient(ingredient) }
                        )
                    }
                }
            }
        }
    }
}
