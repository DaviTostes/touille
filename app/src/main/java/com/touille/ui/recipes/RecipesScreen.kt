package com.touille.ui.recipes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.touille.domain.CookStatus
import com.touille.domain.Recipe
import com.touille.ui.EmptyState
import com.touille.ui.LocalStrings
import com.touille.ui.ScreenHeader
import com.touille.ui.Strings
import com.touille.ui.dotColor
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
    onOpenRecipe: (Long) -> Unit,
    onAddRecipe: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val keyboard = LocalSoftwareKeyboardController.current
    var query by rememberSaveable { mutableStateOf(state.filters.query) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var headerHeight by remember { mutableIntStateOf(0) }

    fun closeSearch() {
        keyboard?.hide()
        searchOpen = false
        query = ""
        viewModel.onQueryChange("")
    }

    BackHandler(enabled = searchOpen) { closeSearch() }

    val imeVisible = WindowInsets.isImeVisible
    var imeWasVisible by remember { mutableStateOf(false) }
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            imeWasVisible = true
        } else if (imeWasVisible && searchOpen) {
            // The user hid the keyboard: hide the whole search input with it.
            closeSearch()
        }
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
                    title = strings.recipesTitle,
                    subtitle = strings.recipesHeadline(state.readyCount, state.totalCount)
                )
            }

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 112.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp
            ) {
                item(span = StaggeredGridItemSpan.FullLine, key = "filters") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CookStatus.entries.forEach { status ->
                            TouilleFilterChip(
                                label = status.chipLabel(strings),
                                selected = state.filters.status == status,
                                onClick = { viewModel.onStatusSelected(status) }
                            )
                        }
                        TouilleFilterChip(
                            label = strings.filterQuick,
                            selected = state.filters.quickOnly,
                            onClick = viewModel::onQuickToggled
                        )
                        state.tags.forEach { tag ->
                            TouilleFilterChip(
                                label = tag,
                                selected = state.filters.tag == tag,
                                onClick = { viewModel.onTagSelected(tag) }
                            )
                        }
                        if (state.hasActiveFilters) {
                            ClearFilterChip(
                                label = strings.clearFilters,
                                onClick = viewModel::clearFilters
                            )
                        }
                    }
                }

                if (state.recipes.isEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine, key = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                emoji = if (state.totalCount == 0) "🍳" else "🔍",
                                title = if (state.totalCount == 0) strings.noRecipesTitle else strings.noMatchTitle,
                                subtitle = if (state.totalCount == 0) {
                                    strings.noRecipesSubtitle
                                } else {
                                    strings.noMatchRecipesSubtitle
                                }
                            )
                        }
                    }
                }

                items(state.recipes, key = { it.id }) { recipe ->
                    RecipePin(
                        recipe = recipe,
                        onClick = { onOpenRecipe(recipe.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !searchOpen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = { searchOpen = true },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = strings.searchRecipes)
                }
                ExtendedFloatingActionButton(
                    onClick = onAddRecipe,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.large,
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text(strings.tabAdd, style = MaterialTheme.typography.titleSmall) }
                )
            }
        }

        AnimatedVisibility(
            visible = searchOpen,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(160))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.22f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { closeSearch() }
                    )
            )
        }

        if (searchOpen) {
            val horizontalMargin = 20.dp
            val barHeight = 56.dp
            val contentWidth: androidx.compose.ui.unit.Dp = maxWidth
            val endWidth = contentWidth - horizontalMargin * 2
            val endX = with(density) { horizontalMargin.toPx() }
            val startX = with(density) { (contentWidth - horizontalMargin - barHeight).toPx() }
            val endY = headerHeight + with(density) { 6.dp.toPx() }
            val startY = screenHeightPx - with(density) { (horizontalMargin + barHeight).toPx() }

            val progress = remember { Animatable(1f) }
            LaunchedEffect(Unit) {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
                )
            }
            val expansion = 1f - progress.value

            SearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    viewModel.onQueryChange(it)
                },
                onClose = { closeSearch() },
                expansion = expansion,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = (startX + (endX - startX) * expansion).roundToInt(),
                            y = (startY + (endY - startY) * expansion).roundToInt()
                        )
                    }
                    .width(lerp(barHeight, endWidth, expansion))
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    expansion: Float,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    val fieldAlpha = ((expansion - 0.12f) / 0.4f).coerceIn(0f, 1f)
    val closeAlpha = ((expansion - 0.55f) / 0.35f).coerceIn(0f, 1f)

    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(lerp(28.dp, 18.dp, expansion)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .alpha(fieldAlpha)
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            strings.searchRecipes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            )
            if (closeAlpha > 0f) {
                IconButton(onClick = onClose, modifier = Modifier.alpha(closeAlpha)) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = strings.back,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Pinterest-style pin: a tall image (or a warm illustrated tile when there is no photo) with the
 * cook status on top and the title underneath.
 */
@Composable
private fun RecipePin(recipe: Recipe, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    val tint = recipe.status.dotColor

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(recipe.pinAspectRatio())
        ) {
            if (recipe.photoUri != null) {
                AsyncImage(
                    model = recipe.photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    tint.copy(alpha = 0.30f),
                                    tint.copy(alpha = 0.07f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        recipe.ingredients.firstOrNull()?.ingredient?.emoji ?: "🍲",
                        fontSize = 46.sp
                    )
                }
            }

            PinStatus(
                label = recipe.status.chipLabel(strings),
                tint = tint,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                recipe.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                recipe.pinMetaLine(strings),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PinStatus(label: String, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(tint)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1
        )
    }
}

@Composable
fun TouilleFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun ClearFilterChip(label: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        leadingIcon = {
            Icon(
                Icons.Outlined.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

private fun CookStatus.chipLabel(strings: Strings): String = when (this) {
    CookStatus.READY -> strings.filterReady
    CookStatus.ALMOST -> strings.filterAlmost
    CookStatus.MISSING -> strings.filterMissing
}

/** Varies with the recipe so the two columns breathe like a Pinterest board. */
private fun Recipe.pinAspectRatio(): Float {
    val variants = if (photoUri != null) {
        floatArrayOf(0.76f, 0.9f, 1.0f, 0.68f)
    } else {
        floatArrayOf(0.95f, 1.08f, 0.88f, 1.0f)
    }
    return variants[(id % variants.size).toInt()]
}

private fun Recipe.pinMetaLine(strings: Strings): String = buildList {
    if (prepMinutes > 0) add(strings.minutesShort(prepMinutes))
    add(strings.servingsCount(servings))
}.joinToString(" · ")
