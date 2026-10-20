package com.touille.ui.editor

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.touille.domain.Ingredient
import com.touille.ui.CatalogOpener
import com.touille.ui.CatalogPicker
import com.touille.ui.IngredientChip
import com.touille.ui.Language
import com.touille.ui.LocalStrings
import com.touille.ui.Strings
import com.touille.ui.displayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeEditorScreen(
    viewModel: RecipeEditorViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalStrings.current
    var pickerOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch { viewModel.onPhoto(copyToAppStorage(context, uri)) }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    if (state.isEditing) strings.editRecipe else strings.newRecipe,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = strings.back)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PhotoField(
                    photoUri = state.photoUri,
                    onPick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onClear = { viewModel.onPhoto(null) },
                    strings = strings
                )
            }

            item {
                EditorField(
                    value = state.name,
                    onValueChange = viewModel::onName,
                    label = strings.recipeName
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditorField(
                        value = state.prepMinutes,
                        onValueChange = viewModel::onPrepMinutes,
                        label = strings.prepField,
                        numeric = true,
                        modifier = Modifier.weight(1f)
                    )
                    EditorField(
                        value = state.servings,
                        onValueChange = viewModel::onServings,
                        label = strings.servingsField,
                        numeric = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                EditorField(
                    value = state.tags,
                    onValueChange = viewModel::onTags,
                    label = strings.tagsField
                )
            }

            item {
                Text(
                    strings.ingredientsWithCount(state.selected.size),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(state.selectedIngredients, key = { it.key }) { ingredient ->
                SelectedIngredientRow(
                    ingredient = ingredient,
                    amount = state.selected[ingredient.key].orEmpty(),
                    onAmount = { viewModel.onAmount(ingredient.key, it) },
                    onRemove = { viewModel.toggleIngredient(ingredient.key) },
                    strings = strings,
                    language = state.language
                )
            }

            item {
                CatalogOpener(label = strings.addFromCatalog, onClick = { pickerOpen = true })
            }

            item {
                EditorField(
                    value = state.steps,
                    onValueChange = viewModel::onSteps,
                    label = strings.methodHeader,
                    multiline = true
                )
            }
        }

        Button(
            onClick = viewModel::save,
            enabled = state.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(52.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                if (state.isEditing) strings.saveChanges else strings.addToNotebook,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }

    if (pickerOpen) {
        CatalogPicker(
            ingredients = state.catalog,
            selectedKeys = state.selected.keys,
            query = state.query,
            onQueryChange = viewModel::onQuery,
            onToggle = viewModel::toggleIngredient,
            onClose = {
                viewModel.onQuery("")
                pickerOpen = false
            },
            language = state.language,
            counter = strings.selectedCount(state.selected.size),
            doneLabel = strings.done,
            searchHint = strings.addFromCatalog
        )
    }
}

@Composable
private fun PhotoField(
    photoUri: String?,
    onPick: () -> Unit,
    onClear: () -> Unit,
    strings: Strings
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onPick),
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onClear,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Outlined.Close, contentDescription = strings.removePhoto)
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.AddAPhoto,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    strings.addPhoto,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectedIngredientRow(
    ingredient: Ingredient,
    amount: String,
    onAmount: (String) -> Unit,
    onRemove: () -> Unit,
    strings: Strings,
    language: Language
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(ingredient.emoji, style = MaterialTheme.typography.bodyLarge)
        Text(
            ingredient.displayName(language),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = amount,
            onValueChange = onAmount,
            placeholder = { Text(strings.quantity, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            modifier = Modifier.width(96.dp),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = strings.removeItem(ingredient.displayName(language)),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    numeric: Boolean = false,
    multiline: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = !multiline,
        modifier = if (multiline) {
            modifier.fillMaxWidth().heightIn(min = 140.dp)
        } else {
            modifier.fillMaxWidth()
        },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

private suspend fun copyToAppStorage(context: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val target = File(context.filesDir, "photos").apply { mkdirs() }
                .resolve("${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use(input::copyTo)
            } ?: return@runCatching null
            target.toURI().toString()
        }.getOrNull()
    }
