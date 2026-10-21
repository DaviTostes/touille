package com.touille.ui.fridge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touille.domain.Ingredient
import com.touille.ui.ConfirmDialog
import com.touille.ui.CountPill
import com.touille.ui.EmptyState
import com.touille.ui.LocalStrings
import com.touille.ui.ProgressCard
import com.touille.ui.ScreenHeader
import com.touille.ui.SearchOverlay
import com.touille.ui.displayName

@Composable
fun FridgeScreen(viewModel: FridgeViewModel, scrollToTopSignal: Int = 0) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FridgeContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onToggleCategory = viewModel::toggleCategory,
        onToggleIngredient = viewModel::toggleIngredient,
        onClear = viewModel::clearFridge,
        scrollToTopSignal = scrollToTopSignal
    )
}

@Composable
private fun FridgeContent(
    state: FridgeUiState,
    onQueryChange: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onToggleIngredient: (Ingredient) -> Unit,
    onClear: () -> Unit,
    scrollToTopSignal: Int
) {
    val strings = LocalStrings.current
    val gridState = rememberLazyGridState()
    var query by rememberSaveable { mutableStateOf(state.query) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var ownedExpanded by rememberSaveable { mutableStateOf(true) }
    var headerHeight by remember { mutableIntStateOf(0) }

    fun closeSearch() {
        query = ""
        onQueryChange("")
        searchOpen = false
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) gridState.animateScrollToItem(0)
    }

    // Once the full list is back after closing the search, land on the top instead of mid-item.
    LaunchedEffect(searchOpen, state.query) {
        if (!searchOpen) gridState.scrollToItem(0)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val owned = state.categories
            .flatMap { it.ingredients }
            .filter { it.owned }
            .sortedBy { it.displayName(state.language) }
        // While searching, matches inside the fridge should stay visible even if the section is closed.
        val showOwned = owned.isNotEmpty() && (ownedExpanded || state.query.isNotBlank())

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .onSizeChanged { headerHeight = it.height }
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp),
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
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                state = gridState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    // Leave room for the floating button, and start below the search bar when open.
                    top = if (searchOpen) 70.dp else 12.dp,
                    bottom = if (searchOpen) 24.dp else 112.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (owned.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "owned-header") {
                        GroupHeader(
                            title = strings.fridgeOwnedTitle,
                            count = owned.size.toString(),
                            collapsed = !ownedExpanded,
                            filled = true,
                            onClick = { ownedExpanded = !ownedExpanded }
                        )
                    }
                    if (showOwned) {
                        items(owned, key = { "owned-${it.key}" }) { ingredient ->
                            IngredientTile(
                                name = ingredient.displayName(state.language),
                                emoji = ingredient.emoji,
                                selected = true,
                                onClick = { onToggleIngredient(ingredient) }
                            )
                        }
                    }
                }

                if (state.categories.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                        EmptyState(
                            emoji = "🔍",
                            title = strings.noMatchTitle,
                            subtitle = strings.noMatchIngredients
                        )
                    }
                }

                state.categories.forEach { category ->
                    item(span = { GridItemSpan(maxLineSpan) }, key = "header-${category.name}") {
                        GroupHeader(
                            title = strings.categoryLabel(category.name),
                            count = "${category.ownedCount}/${category.ingredients.size}",
                            collapsed = category.collapsed,
                            filled = category.ownedCount > 0,
                            onClick = { onToggleCategory(category.name) },
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    if (!category.collapsed) {
                        items(
                            items = category.ingredients,
                            key = { "cat-${category.name}-${it.key}" }
                        ) { ingredient ->
                            IngredientTile(
                                name = ingredient.displayName(state.language),
                                emoji = ingredient.emoji,
                                selected = ingredient.owned,
                                onClick = { onToggleIngredient(ingredient) }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !searchOpen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FloatingActionButton(
                onClick = { searchOpen = true },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Outlined.Search, contentDescription = strings.searchIngredients)
            }
        }

        SearchOverlay(
            expanded = searchOpen,
            query = query,
            onQueryChange = {
                query = it
                onQueryChange(it)
            },
            onClose = { closeSearch() },
            placeholder = strings.searchIngredients,
            headerHeightPx = headerHeight,
            screenHeightPx = screenHeightPx,
            contentWidth = maxWidth,
            // The list itself is the result: keep it readable instead of dimming it.
            dimBackdrop = false
        )
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

/** Collapsible section title: chevron, label and the owned/total counter. */
@Composable
private fun GroupHeader(
    title: String,
    count: String,
    collapsed: Boolean,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(if (collapsed) -90f else 0f, label = "groupChevron")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Outlined.ExpandMore,
            contentDescription = null,
            modifier = Modifier
                .rotate(rotation)
                .size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        CountPill(text = count, filled = filled)
    }
}

/** Rounded ingredient card used by the five-column grid. */
@Composable
private fun IngredientTile(
    name: String,
    emoji: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    val borderColor by animateColorAsState(
        if (selected) scheme.primary else scheme.outline,
        label = "ingredientTileBorder"
    )
    val content by animateColorAsState(
        if (selected) scheme.onSurface else scheme.onSurfaceVariant,
        label = "ingredientTileContent"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clip(shape)
            .background(scheme.surface)
            .border(if (selected) 2.dp else 1.dp, borderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp, lineHeight = 22.sp)
            Text(
                name,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                color = content,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (selected) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(14.dp)
            )
        }
    }
}
