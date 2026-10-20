package com.touille.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touille.data.TouilleRepository
import com.touille.di.LanguageStore
import com.touille.domain.CookStatus
import com.touille.ui.ConfirmDialog
import com.touille.ui.Language
import com.touille.ui.LocalStrings
import com.touille.ui.ScreenHeader
import com.touille.ui.StatTile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: TouilleRepository, languageStore: LanguageStore) {
    val ingredients by repository.ingredients.collectAsStateWithLifecycle(emptyList())
    val recipes by repository.recipes.collectAsStateWithLifecycle(emptyList())
    val shopping by repository.shopping.collectAsStateWithLifecycle(emptyList())
    val language by languageStore.language.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    var confirmEmpty by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            ScreenHeader(title = strings.kitchenTitle, subtitle = strings.kitchenTagline)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        value = "${ingredients.count { it.owned }}/${ingredients.size}",
                        label = strings.statInFridge,
                        emoji = "🧊",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "${recipes.size}",
                        label = strings.statRecipes,
                        emoji = "📖",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        value = "${recipes.count { it.status == CookStatus.READY }}",
                        label = strings.statReady,
                        emoji = "🍳",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = "${shopping.count { !it.checked }}",
                        label = strings.statToBuy,
                        emoji = "🛒",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item { LanguageCard(language = language, onSelect = languageStore::set, label = strings.languageLabel) }

        item {
            OutlinedButton(
                onClick = { confirmEmpty = true },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    strings.emptyFridgeButton,
                    modifier = Modifier.padding(start = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧑‍🍳", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        strings.kitchenBlurb,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (confirmEmpty) {
        ConfirmDialog(
            title = strings.emptyFridgeTitle,
            body = strings.emptyFridgeBody,
            confirmLabel = strings.emptyFridgeAction,
            dismissLabel = strings.deleteCancel,
            onConfirm = {
                confirmEmpty = false
                scope.launch { repository.clearFridge() }
            },
            onDismiss = { confirmEmpty = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageCard(language: Language, onSelect: (Language) -> Unit, label: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Language.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = language == option,
                        onClick = { onSelect(option) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = Language.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(option.label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
