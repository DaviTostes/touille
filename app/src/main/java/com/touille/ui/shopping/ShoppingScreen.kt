package com.touille.ui.shopping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touille.domain.Ingredient
import com.touille.domain.ShoppingItem
import com.touille.ui.CheckCircle
import com.touille.ui.EmptyState
import com.touille.ui.Language
import com.touille.ui.LocalSnackbar
import com.touille.ui.LocalStrings
import com.touille.ui.ScreenHeader
import com.touille.ui.SearchOverlay
import com.touille.ui.SectionHeader
import com.touille.ui.Strings
import com.touille.ui.displayName
import kotlinx.coroutines.launch

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel, scrollToTopSignal: Int = 0) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var headerHeight by remember { mutableIntStateOf(0) }

    fun closeSearch() {
        viewModel.resetQuery()
        searchOpen = false
    }

    LaunchedEffect(scrollToTopSignal) {
        if (scrollToTopSignal > 0) listState.animateScrollToItem(0)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = with(density) { maxHeight.toPx() }

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .onSizeChanged { headerHeight = it.height }
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScreenHeader(
                    title = strings.shoppingTitle,
                    subtitle = strings.shoppingHeadline(state.toBuy.size, state.items.size)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.items.isEmpty()) {
                        item(key = "empty") {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(
                                    emoji = "🛒",
                                    title = strings.shoppingEmptyTitle,
                                    subtitle = strings.shoppingEmptySubtitle,
                                    action = {
                                        OutlinedButton(
                                            onClick = { searchOpen = true },
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text(strings.addItemAction)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (state.toBuy.isNotEmpty()) {
                        item(key = "header-tobuy") {
                            SectionHeader(
                                title = strings.sectionToBuy,
                                count = state.toBuy.size,
                                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                            )
                        }
                        items(state.toBuy, key = { it.id }) { item ->
                            ShoppingRow(
                                item = item,
                                language = state.language,
                                strings = strings,
                                onToggle = { viewModel.toggle(item) },
                                onRemove = { viewModel.remove(item) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    if (state.done.isNotEmpty()) {
                        item(key = "header-done") {
                            SectionHeader(
                                title = strings.sectionDone,
                                count = state.done.size,
                                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                                trailing = {
                                    TextButton(onClick = viewModel::clearDone) {
                                        Text(strings.clearDone, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            )
                        }
                        items(state.done, key = { it.id }) { item ->
                            ShoppingRow(
                                item = item,
                                language = state.language,
                                strings = strings,
                                onToggle = { viewModel.toggle(item) },
                                onRemove = { viewModel.remove(item) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
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
                        Icon(Icons.Outlined.Search, contentDescription = strings.searchOrAddPlaceholder)
                    }
                }
            }

            AnimatedVisibility(visible = state.moveableCount > 0) {
                MoveToFridgeBar(
                    label = strings.moveToFridge(state.moveableCount),
                    hint = strings.shoppingMoveHint,
                    onClick = {
                        val moved = state.moveableCount
                        viewModel.moveDoneToFridge()
                        scope.launch { snackbar?.showSnackbar(strings.movedToFridge(moved)) }
                    }
                )
            }
        }

        SearchOverlay(
            expanded = searchOpen,
            query = state.query,
            onQueryChange = viewModel::onQueryChange,
            onClose = { closeSearch() },
            placeholder = strings.searchOrAddPlaceholder,
            headerHeightPx = headerHeight,
            screenHeightPx = screenHeightPx,
            contentWidth = maxWidth,
            imeAction = ImeAction.Done,
            onSearchAction = viewModel::submitSearch
        ) {
            CatalogResults(
                query = state.query,
                catalog = state.catalog,
                selectedKeys = state.catalogKeys,
                language = state.language,
                strings = strings,
                topPadding = with(density) { headerHeight.toDp() } + 72.dp,
                onToggle = viewModel::toggleCatalogIngredient,
                onAddCustom = viewModel::submitSearch
            )
        }
    }
}

/**
 * Catalog results shown while the search flow is open: type to filter, tap an ingredient to put it
 * on the list (or take it off), and add the typed text as a free-form item when nothing matches.
 */
@Composable
private fun CatalogResults(
    query: String,
    catalog: List<Ingredient>,
    selectedKeys: Set<String>,
    language: Language,
    strings: Strings,
    topPadding: Dp,
    onToggle: (Ingredient) -> Unit,
    onAddCustom: () -> Unit
) {
    val term = query.trim()
    val results = remember(catalog, term) {
        if (term.isEmpty()) catalog else catalog.filter { it.matches(term) }
    }
    val exactMatch = remember(catalog, term) {
        if (term.isEmpty()) null else catalog.firstOrNull { it.matchesExactly(term) }
    }
    val grouped = results.groupBy { it.category }
        .entries
        .sortedBy { strings.categoryLabel(it.key) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp)
        ) {
            if (term.isNotEmpty() && exactMatch == null) {
                item(key = "custom") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onAddCustom)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            strings.addCustomItem(term),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (results.isEmpty()) {
                item(key = "no-match") {
                    Text(
                        strings.noMatchIngredients,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }

            grouped.forEach { (category, ingredients) ->
                item(key = "header-$category") {
                    Text(
                        strings.categoryLabel(category),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                items(ingredients, key = { it.key }) { ingredient ->
                    CatalogResultRow(
                        ingredient = ingredient,
                        language = language,
                        selected = ingredient.key in selectedKeys,
                        onClick = { onToggle(ingredient) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogResultRow(
    ingredient: Ingredient,
    language: Language,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(ingredient.emoji, style = MaterialTheme.typography.bodyLarge)
        Text(
            ingredient.displayName(language),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        CheckCircle(
            checked = selected,
            onClick = onClick,
            contentDescription = null,
            activeColor = MaterialTheme.colorScheme.secondary,
            inactiveColor = MaterialTheme.colorScheme.outline,
            uncheckedIcon = Icons.Outlined.Add
        )
    }
}

@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    language: Language,
    strings: Strings,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = item.displayName(language)
    val contentAlpha by animateFloatAsState(if (item.checked) 0.6f else 1f, label = "shoppingRowAlpha")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onToggle)
            .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CheckCircle(
            checked = item.checked,
            onClick = onToggle,
            contentDescription = if (item.checked) strings.available else strings.missing,
            activeColor = MaterialTheme.colorScheme.secondary,
            inactiveColor = MaterialTheme.colorScheme.outline,
            uncheckedIcon = Icons.Outlined.Add
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .alpha(contentAlpha),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, style = MaterialTheme.typography.bodyMedium)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.amount.isNotBlank()) {
                Text(
                    item.amount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else if (item.fromCatalog) {
                Text(
                    strings.categoryLabel(item.ingredient?.category.orEmpty()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Outlined.DeleteOutline,
                contentDescription = strings.removeItem(label),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MoveToFridgeBar(label: String, hint: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Outlined.Kitchen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(label, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
