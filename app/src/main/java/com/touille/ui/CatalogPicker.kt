package com.touille.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.touille.domain.Ingredient

/**
 * Full screen ingredient catalog picker. Shared by the recipe editor (pick the ingredients of a
 * recipe) and the shopping list (pick what to buy, so it can later jump into the fridge).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogPicker(
    ingredients: List<Ingredient>,
    selectedKeys: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    onClose: () -> Unit,
    language: Language,
    counter: String,
    doneLabel: String,
    searchHint: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val term = query.trim()
    val results = if (term.isEmpty()) ingredients else ingredients.filter { it.matches(term) }
    val grouped = results.groupBy { it.category }
        .entries
        .sortedBy { strings.categoryLabel(it.key) }

    BackHandler(onBack = onClose)
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                }
                SearchField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = searchHint,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    counter,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onClose) { Text(doneLabel) }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                grouped.forEach { (category, items) ->
                    item(key = "header-$category") {
                        Text(
                            strings.categoryLabel(category),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 16.dp,
                                bottom = 4.dp
                            )
                        )
                    }
                    items(items, key = { it.key }) { ingredient ->
                        PickerRow(
                            ingredient = ingredient,
                            language = language,
                            selected = ingredient.key in selectedKeys,
                            onClick = { onToggle(ingredient.key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerRow(
    ingredient: Ingredient,
    language: Language,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
        if (selected) {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
