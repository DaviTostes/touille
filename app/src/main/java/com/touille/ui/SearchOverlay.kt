package com.touille.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import kotlin.math.roundToInt

/**
 * Search flow shared by the recipes, fridge and shopping screens: a floating button expands into
 * a bar just below the header while the content behind it dims. The query is live, so callers can
 * filter their lists as the user types.
 *
 * Must be called from a [BoxScope]; [contentWidth] is the width of the screen content (usually
 * `maxWidth` of the enclosing `BoxWithConstraints`).
 */
@Composable
fun BoxScope.SearchOverlay(
    expanded: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    placeholder: String,
    headerHeightPx: Int,
    screenHeightPx: Float,
    contentWidth: Dp,
    imeAction: ImeAction = ImeAction.Search,
    onSearchAction: () -> Unit = {},
    dimBackdrop: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val density = LocalDensity.current

    // While the search is open, Back closes it instead of leaving the tab.
    BackHandler(enabled = expanded, onBack = onClose)

    if (dimBackdrop) {
        AnimatedVisibility(
            visible = expanded,
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
                        onClick = onClose
                    )
            )
        }
    }

    if (!expanded) return

    // Drawn above the dimmed backdrop but below the bar (e.g. catalog results).
    content?.invoke()

    val horizontalMargin = 20.dp
    val barHeight = 56.dp
    val endWidth = contentWidth - horizontalMargin * 2
    val endX = with(density) { horizontalMargin.toPx() }
    val startX = with(density) { (contentWidth - horizontalMargin - barHeight).toPx() }
    val endY = headerHeightPx + with(density) { 6.dp.toPx() }
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
        onQueryChange = onQueryChange,
        onClose = onClose,
        expansion = expansion,
        placeholder = placeholder,
        imeAction = imeAction,
        onSearchAction = onSearchAction,
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    expansion: Float,
    placeholder: String,
    imeAction: ImeAction,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchAction()
                        keyboard?.hide()
                    },
                    onDone = {
                        onSearchAction()
                        keyboard?.hide()
                    }
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            placeholder,
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
