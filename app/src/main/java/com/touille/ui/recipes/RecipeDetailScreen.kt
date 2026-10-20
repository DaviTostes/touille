package com.touille.ui.recipes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.touille.domain.CookStatus
import com.touille.domain.Recipe
import com.touille.domain.RecipeIngredient
import com.touille.ui.CheckCircle
import com.touille.ui.ConfirmDialog
import com.touille.ui.CountPill
import com.touille.ui.Language
import com.touille.ui.LocalLanguage
import com.touille.ui.LocalSnackbar
import com.touille.ui.LocalStrings
import com.touille.ui.SectionHeader
import com.touille.ui.StatusBadge
import com.touille.ui.Strings
import com.touille.ui.displayName
import com.touille.ui.dotColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onOpenShopping: () -> Unit
) {
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val language = LocalLanguage.current
    val snackbar = LocalSnackbar.current
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }
    val current = recipe

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                }
            },
            actions = {
                if (current != null) {
                    IconButton(onClick = { onEdit(current.id) }) {
                        Icon(Icons.Outlined.Edit, contentDescription = strings.editRecipe)
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = strings.deleteRecipe)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (current == null) return@Column

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                if (current.photoUri != null) {
                    AsyncImage(
                        model = current.photoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(current.name, style = MaterialTheme.typography.headlineMedium)
                    StatusBadge(status = current.status)
                    FactsRow(current, strings)
                }
            }

            item {
                AnimatedVisibility(
                    visible = current.status == CookStatus.READY,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                ) {
                    ReadyBanner(strings.readyBanner)
                }
            }

            item {
                SectionHeader(
                    title = strings.ingredientsHeader,
                    count = current.ingredients.size,
                    trailing = {
                        CountPill(
                            text = "${current.available.size}/${current.ingredients.size}",
                            filled = current.missing.isEmpty()
                        )
                    }
                )
            }

            items(current.ingredients, key = { it.ingredient.key }) { line ->
                IngredientRow(
                    line = line,
                    strings = strings,
                    language = language,
                    onToggleOwned = { viewModel.toggleOwned(line.ingredient) }
                )
            }

            val steps = current.steps.trim().lines().map(String::trim).filter(String::isNotEmpty)
            if (steps.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(strings.methodHeader, style = MaterialTheme.typography.titleMedium)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            steps.forEachIndexed { index, step ->
                                StepRow(
                                    text = step,
                                    number = if (steps.size > 1) index + 1 else null
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = current.missing.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = {
                        val count = current.missing.size
                        viewModel.addMissingToList(current)
                        scope.launch {
                            val result = snackbar?.showSnackbar(
                                message = strings.addedToList(count),
                                actionLabel = strings.tabShopping,
                                withDismissAction = false,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) onOpenShopping()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        strings.addMissingToList(current.missing.size),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }

    if (confirmDelete && current != null) {
        ConfirmDialog(
            title = strings.deleteTitle(current.name),
            body = strings.deleteBody,
            confirmLabel = strings.deleteConfirm,
            dismissLabel = strings.deleteCancel,
            onConfirm = {
                confirmDelete = false
                viewModel.delete()
                onBack()
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun FactsRow(recipe: Recipe, strings: Strings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Fact(
                icon = Icons.Outlined.Schedule,
                value = if (recipe.prepMinutes > 0) strings.minutesShort(recipe.prepMinutes) else "—",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(34.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Fact(
                icon = Icons.Outlined.Restaurant,
                value = strings.servingsCount(recipe.servings),
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(34.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Fact(
                icon = Icons.Outlined.ShoppingCart,
                value = strings.ingredientsCount(recipe.ingredients.size),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Fact(icon: ImageVector, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun StepRow(text: String, number: Int?) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (number != null) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    number.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReadyBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Outlined.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            message,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun IngredientRow(
    line: RecipeIngredient,
    strings: Strings,
    language: Language,
    onToggleOwned: () -> Unit
) {
    val owned = line.ingredient.owned
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CheckCircle(
            checked = owned,
            onClick = onToggleOwned,
            contentDescription = if (owned) strings.available else strings.missing,
            activeColor = CookStatus.READY.dotColor,
            inactiveColor = MaterialTheme.colorScheme.error,
            uncheckedIcon = Icons.Outlined.Add
        )
        Text(line.ingredient.emoji, style = MaterialTheme.typography.bodyMedium)
        Text(
            line.ingredient.displayName(language),
            style = MaterialTheme.typography.bodyLarge,
            color = if (owned) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f)
        )
        if (line.amount.isNotBlank()) {
            CountPill(text = line.amount, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}
