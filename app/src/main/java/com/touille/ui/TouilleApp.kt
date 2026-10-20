package com.touille.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.touille.data.TouilleRepository
import com.touille.di.LanguageStore
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

private const val ROUTE_FRIDGE = "fridge"
private const val ROUTE_RECIPES = "recipes"
private const val ROUTE_SHOPPING = "shopping"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_EDITOR = "editor/{recipeId}"
private const val ROUTE_DETAIL = "recipe/{recipeId}"

private data class Tab(val route: String, val icon: ImageVector, val label: (Strings) -> String)

private val tabs = listOf(
    Tab(ROUTE_RECIPES, Icons.AutoMirrored.Outlined.MenuBook) { it.tabRecipes },
    Tab(ROUTE_FRIDGE, Icons.Outlined.Kitchen) { it.tabFridge },
    Tab(ROUTE_SHOPPING, Icons.Outlined.ShoppingCart) { it.tabShopping },
    Tab(ROUTE_SETTINGS, Icons.Outlined.Tune) { it.tabKitchen }
)

@Composable
fun TouilleApp(repository: TouilleRepository, languageStore: LanguageStore) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val strings = LocalStrings.current
    val showBar = backStackEntry?.destination?.route in tabs.map { it.route }
    val shopping by repository.shopping.collectAsStateWithLifecycle(emptyList())
    val toBuyCount = shopping.count { !it.checked }
    val snackbarHostState = remember { SnackbarHostState() }
    val openTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val currentTabIndex = backStackEntry?.destination?.route
        ?.let { route -> tabs.indexOfFirst { it.route == route } }
        ?: -1

    // Horizontal swipe anywhere on a tab switches to the previous/next one.
    val swipeModifier = if (currentTabIndex >= 0) {
        Modifier.pointerInput(currentTabIndex) {
            val threshold = 150.dp.toPx()
            var totalDrag = 0f
            detectHorizontalDragGestures(
                onDragStart = { totalDrag = 0f },
                onHorizontalDrag = { change, dragAmount ->
                    totalDrag += dragAmount
                    change.consume()
                },
                onDragEnd = {
                    when {
                        totalDrag <= -threshold && currentTabIndex < tabs.lastIndex ->
                            openTab(tabs[currentTabIndex + 1].route)
                        totalDrag >= threshold && currentTabIndex > 0 ->
                            openTab(tabs[currentTabIndex - 1].route)
                    }
                }
            )
        }
    } else {
        Modifier
    }

    val tabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        val from = tabs.indexOfFirst { it.route == initialState.destination.route }
        val to = tabs.indexOfFirst { it.route == targetState.destination.route }
        if (from >= 0 && to >= 0 && from != to) {
            slideIntoContainer(
                towards = if (to > from) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                },
                animationSpec = tween(320)
            )
        } else {
            fadeIn(tween(180))
        }
    }
    val tabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        val from = tabs.indexOfFirst { it.route == initialState.destination.route }
        val to = tabs.indexOfFirst { it.route == targetState.destination.route }
        if (from >= 0 && to >= 0 && from != to) {
            slideOutOfContainer(
                towards = if (to > from) {
                    AnimatedContentTransitionScope.SlideDirection.Left
                } else {
                    AnimatedContentTransitionScope.SlideDirection.Right
                },
                animationSpec = tween(320)
            )
        } else {
            fadeOut(tween(180))
        }
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
            if (showBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEach { tab ->
                        val selected =
                            backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { openTab(tab.route) },
                            icon = {
                                val badge = if (tab.route == ROUTE_SHOPPING) toBuyCount else 0
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
                startDestination = ROUTE_RECIPES,
                modifier = Modifier
                    .padding(padding)
                    .imePadding()
                    .then(swipeModifier),
                enterTransition = tabEnter,
                exitTransition = tabExit,
                popEnterTransition = tabEnter,
                popExitTransition = tabExit
            ) {
                composable(ROUTE_FRIDGE) {
                    FridgeScreen(viewModel { FridgeViewModel(repository, languageStore) })
                }
                composable(ROUTE_RECIPES) {
                    RecipesScreen(
                        viewModel = viewModel { RecipesViewModel(repository) },
                        onOpenRecipe = { id -> navController.navigate("recipe/$id") },
                        onAddRecipe = { navController.navigate("editor/0") }
                    )
                }
                composable(ROUTE_SHOPPING) {
                    ShoppingScreen(viewModel { ShoppingViewModel(repository, languageStore) })
                }
                composable(ROUTE_SETTINGS) {
                    SettingsScreen(repository, languageStore)
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
                        onOpenShopping = { openTab(ROUTE_SHOPPING) }
                    )
                }
            }
        }
    }
}
