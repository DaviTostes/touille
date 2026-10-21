package com.touille.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.touille.data.TouilleRepository
import com.touille.di.LanguageStore
import com.touille.di.ThemeStore
import com.touille.ui.editor.RecipeEditorScreen
import com.touille.ui.editor.RecipeEditorViewModel
import com.touille.ui.fridge.FridgeScreen
import com.touille.ui.fridge.FridgeViewModel
import com.touille.ui.recipes.RecipeDetailScreen
import com.touille.ui.recipes.RecipeDetailViewModel
import com.touille.ui.recipes.RecipesScreen
import com.touille.ui.recipes.RecipesViewModel
import com.touille.ui.settings.SettingsScreen
import com.touille.ui.shopping.ShoppingScreen
import com.touille.ui.shopping.ShoppingViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val ROUTE_HOME = "home"
private const val ROUTE_EDITOR = "editor/{recipeId}"
private const val ROUTE_DETAIL = "recipe/{recipeId}"

// The bottom-bar tabs are pages of the home pager, in bar order.
private const val TAB_RECIPES = 0
private const val TAB_FRIDGE = 1
private const val TAB_SHOPPING = 2
private const val TAB_SETTINGS = 3

private data class Tab(val icon: ImageVector, val label: (Strings) -> String)

private val tabs = listOf(
    Tab(Icons.AutoMirrored.Outlined.MenuBook) { it.tabRecipes },
    Tab(Icons.Outlined.Kitchen) { it.tabFridge },
    Tab(Icons.Outlined.ShoppingCart) { it.tabShopping },
    Tab(Icons.Outlined.Tune) { it.tabKitchen }
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TouilleApp(repository: TouilleRepository, languageStore: LanguageStore, themeStore: ThemeStore) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val strings = LocalStrings.current
    val onHome = backStackEntry?.destination?.route == ROUTE_HOME
    val imeVisible = WindowInsets.isImeVisible
    val shopping by repository.shopping.collectAsStateWithLifecycle(emptyList())
    val toBuyCount = shopping.count { !it.checked }
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    // Page requested by a screen outside home (e.g. "add to shopping list" in a recipe).
    var pendingPage by rememberSaveable { mutableIntStateOf(-1) }
    // Bumped when the already selected tab is tapped; each page scrolls itself to the top.
    val scrollToTopRequests = remember { mutableStateListOf(*Array(tabs.size) { 0 }) }

    val selectTab: (Int) -> Unit = { index ->
        if (index != pagerState.currentPage) {
            if (onHome) {
                scope.launch { pagerState.animateScrollToPage(index) }
            } else {
                pendingPage = index
                navController.popBackStack(ROUTE_HOME, inclusive = false)
            }
        } else if (onHome) {
            scrollToTopRequests[index] = scrollToTopRequests[index] + 1
        }
    }

    // Back from another tab returns to the first tab, like the old tab back stack did.
    BackHandler(enabled = onHome && pagerState.currentPage != TAB_RECIPES) {
        scope.launch { pagerState.animateScrollToPage(TAB_RECIPES) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = MaterialTheme.shapes.small,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
        bottomBar = {
            // With the keyboard up the bar would sit behind it and only add empty padding.
            if (onHome && !imeVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = { selectTab(index) },
                            icon = {
                                val badge = if (index == TAB_SHOPPING) toBuyCount else 0
                                BadgedBox(
                                    badge = {
                                        if (badge > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ) { Text(badge.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.label(strings))
                                }
                            },
                            label = {
                                Text(tab.label(strings), style = MaterialTheme.typography.labelSmall)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        WithSnackbar(snackbarHostState) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                enterTransition = { fadeIn(tween(180)) },
                exitTransition = { fadeOut(tween(180)) },
                popEnterTransition = { fadeIn(tween(180)) },
                popExitTransition = { fadeOut(tween(180)) }
            ) {
                composable(ROUTE_HOME) {
                    LaunchedEffect(pendingPage) {
                        if (pendingPage in tabs.indices) {
                            // The pager may not be measured yet right after returning from a
                            // detail screen; wait for it so the scroll lands on the right page.
                            snapshotFlow { pagerState.layoutInfo.pageSize }.first { it > 0 }
                            pagerState.scrollToPage(pendingPage)
                            pendingPage = -1
                        }
                    }
                    HomePager(
                        pagerState = pagerState,
                        repository = repository,
                        languageStore = languageStore,
                        themeStore = themeStore,
                        scrollToTopRequests = scrollToTopRequests,
                        onOpenRecipe = { id -> navController.navigate("recipe/$id") },
                        onAddRecipe = { navController.navigate("editor/0") }
                    )
                }
                composable(
                    route = ROUTE_EDITOR,
                    arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
                ) { entry ->
                    val recipeId = entry.arguments?.getLong("recipeId") ?: 0L
                    RecipeEditorScreen(
                        viewModel = viewModel { RecipeEditorViewModel(repository, languageStore, recipeId) },
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }
                composable(
                    route = ROUTE_DETAIL,
                    arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
                ) { entry ->
                    val recipeId = entry.arguments?.getLong("recipeId") ?: 0L
                    RecipeDetailScreen(
                        viewModel = viewModel { RecipeDetailViewModel(repository, recipeId) },
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate("editor/$id") },
                        onOpenShopping = { selectTab(TAB_SHOPPING) }
                    )
                }
            }
        }
    }
}

/**
 * The four main screens side by side. The pager follows the finger while dragging and keeps
 * each page's scroll state, so switching tabs feels continuous.
 */
@Composable
private fun HomePager(
    pagerState: PagerState,
    repository: TouilleRepository,
    languageStore: LanguageStore,
    themeStore: ThemeStore,
    scrollToTopRequests: List<Int>,
    onOpenRecipe: (Long) -> Unit,
    onAddRecipe: () -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1
    ) { page ->
        val scrollToTopSignal = scrollToTopRequests.getOrElse(page) { 0 }
        when (page) {
            TAB_RECIPES -> RecipesScreen(
                viewModel = viewModel { RecipesViewModel(repository) },
                onOpenRecipe = onOpenRecipe,
                onAddRecipe = onAddRecipe,
                scrollToTopSignal = scrollToTopSignal
            )
            TAB_FRIDGE -> FridgeScreen(
                viewModel = viewModel { FridgeViewModel(repository, languageStore) },
                scrollToTopSignal = scrollToTopSignal
            )
            TAB_SHOPPING -> ShoppingScreen(
                viewModel = viewModel { ShoppingViewModel(repository, languageStore) },
                scrollToTopSignal = scrollToTopSignal
            )
            TAB_SETTINGS -> SettingsScreen(
                repository = repository,
                languageStore = languageStore,
                themeStore = themeStore,
                scrollToTopSignal = scrollToTopSignal
            )
        }
    }
}
