package com.touille.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.touille.domain.Ingredient
import com.touille.domain.ShoppingItem

enum class Language(val code: String, val label: String) {
    EN("en", "English"),
    PT("pt", "Português")
}

data class Strings(
    val tabFridge: String,
    val tabRecipes: String,
    val tabAdd: String,
    val tabKitchen: String,
    val tabShopping: String,
    val fridgeTitle: String,
    val fridgeOwnedTitle: String,
    val fridgeCount: (Int) -> String,
    val fridgeProgress: (Int, Int) -> String,
    val emptyFridgeAction: String,
    val emptyFridgeTitle: String,
    val emptyFridgeBody: String,
    val searchIngredients: String,
    val noMatchTitle: String,
    val noMatchIngredients: String,
    val recipesTitle: String,
    val recipesHeadline: (Int, Int) -> String,
    val searchRecipes: String,
    val filterReady: String,
    val filterAlmost: String,
    val filterMissing: String,
    val filterQuick: String,
    val clearFilters: String,
    val noRecipesTitle: String,
    val noRecipesSubtitle: String,
    val noMatchRecipesSubtitle: String,
    val statusReady: String,
    val statusAlmost: String,
    val statusMissing: String,
    val missingList: (String) -> String,
    val missingCount: (Int) -> String,
    val minutesShort: (Int) -> String,
    val servingsCount: (Int) -> String,
    val ingredientsCount: (Int) -> String,
    val back: String,
    val editRecipe: String,
    val deleteRecipe: String,
    val ingredientsHeader: String,
    val methodHeader: String,
    val readyBanner: String,
    val addMissingToList: (Int) -> String,
    val addedToList: (Int) -> String,
    val deleteTitle: (String) -> String,
    val deleteBody: String,
    val deleteConfirm: String,
    val deleteCancel: String,
    val available: String,
    val missing: String,
    val newRecipe: String,
    val addPhoto: String,
    val removePhoto: String,
    val recipeName: String,
    val prepField: String,
    val servingsField: String,
    val tagsField: String,
    val ingredientsWithCount: (Int) -> String,
    val addFromCatalog: String,
    val done: String,
    val selectedCount: (Int) -> String,
    val quantity: String,
    val removeItem: (String) -> String,
    val saveChanges: String,
    val addToNotebook: String,
    val kitchenTitle: String,
    val kitchenTagline: String,
    val statInFridge: String,
    val statRecipes: String,
    val statReady: String,
    val statAlmost: String,
    val statToBuy: String,
    val emptyFridgeButton: String,
    val kitchenBlurb: String,
    val languageLabel: String,
    val appearanceLabel: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val shoppingTitle: String,
    val shoppingHeadline: (Int, Int) -> String,
    val shoppingEmptyTitle: String,
    val shoppingEmptySubtitle: String,
    val addItemAction: String,
    val searchOrAddPlaceholder: String,
    val addCustomItem: (String) -> String,
    val sectionToBuy: String,
    val sectionDone: String,
    val bought: String,
    val clearDone: String,
    val moveToFridge: (Int) -> String,
    val movedToFridge: (Int) -> String,
    val shoppingMoveHint: String,
    val categoryLabel: (String) -> String
)

private val EnglishCategories = mapOf(
    "Proteins" to "Proteins",
    "Seafood" to "Seafood",
    "Vegetables" to "Vegetables",
    "Fruit" to "Fruit",
    "Dairy & Eggs" to "Dairy & Eggs",
    "Grains & Pasta" to "Grains & Pasta",
    "Pantry" to "Pantry",
    "Herbs & Spices" to "Herbs & Spices",
    "Condiments" to "Condiments",
    "Drinks" to "Drinks & Juices",
    "Baking & Sweets" to "Baking & Sweets"
)

private val PortugueseCategories = mapOf(
    "Proteins" to "Proteínas",
    "Seafood" to "Frutos do mar",
    "Vegetables" to "Legumes e verduras",
    "Fruit" to "Frutas",
    "Dairy & Eggs" to "Laticínios e ovos",
    "Grains & Pasta" to "Grãos e massas",
    "Pantry" to "Despensa",
    "Herbs & Spices" to "Ervas e temperos",
    "Condiments" to "Molhos e conservas",
    "Drinks" to "Bebidas e sucos",
    "Baking & Sweets" to "Doces e confeitaria"
)

val EnglishStrings = Strings(
    tabFridge = "Fridge",
    tabRecipes = "Recipes",
    tabAdd = "Add",
    tabKitchen = "Kitchen",
    tabShopping = "List",
    fridgeTitle = "My fridge",
    fridgeOwnedTitle = "In the fridge",
    fridgeCount = { count ->
        when (count) {
            0 -> "Your fridge is empty — tap what you have."
            1 -> "1 ingredient in your fridge."
            else -> "$count ingredients in your fridge."
        }
    },
    fridgeProgress = { owned, total -> "$owned of $total ingredients marked." },
    emptyFridgeAction = "Empty it",
    emptyFridgeTitle = "Empty the fridge?",
    emptyFridgeBody = "Every ingredient goes back to unmarked. Your recipes stay untouched.",
    searchIngredients = "Search ingredients",
    noMatchTitle = "Nothing matches",
    noMatchIngredients = "Try another ingredient name.",
    recipesTitle = "Recipes",
    recipesHeadline = { ready, total ->
        when {
            total == 0 -> "Nothing in the notebook yet."
            ready == 0 -> "Nothing cookable right now — check your fridge."
            else -> "$ready of $total ready to cook right now."
        }
    },
    searchRecipes = "Search recipes or ingredients",
    filterReady = "Ready",
    filterAlmost = "Almost",
    filterMissing = "Missing",
    filterQuick = "Under 30 min",
    clearFilters = "Clear",
    noRecipesTitle = "No recipes yet",
    noRecipesSubtitle = "Add your first recipe and Touille will tell you when you can cook it.",
    noMatchRecipesSubtitle = "Loosen a filter to see more.",
    statusReady = "Ready to cook",
    statusAlmost = "Almost there",
    statusMissing = "Missing ingredients",
    missingList = { names -> "Missing $names" },
    missingCount = { count -> "Missing $count ingredients" },
    minutesShort = { minutes -> "$minutes min" },
    servingsCount = { count -> if (count == 1) "1 serving" else "$count servings" },
    ingredientsCount = { count -> if (count == 1) "1 ingredient" else "$count ingredients" },
    back = "Back",
    editRecipe = "Edit recipe",
    deleteRecipe = "Delete recipe",
    ingredientsHeader = "Ingredients",
    methodHeader = "Method",
    readyBanner = "You have everything for this one.",
    addMissingToList = { count ->
        if (count == 1) "Add 1 missing to the list" else "Add $count missing to the list"
    },
    addedToList = { count ->
        if (count == 1) "1 ingredient is on your list." else "$count ingredients are on your list."
    },
    deleteTitle = { name -> "Delete $name?" },
    deleteBody = "This removes the recipe from your notebook.",
    deleteConfirm = "Delete",
    deleteCancel = "Keep",
    available = "Available",
    missing = "Missing",
    newRecipe = "New recipe",
    addPhoto = "Add a photo",
    removePhoto = "Remove photo",
    recipeName = "Recipe name",
    prepField = "Prep (min)",
    servingsField = "Servings",
    tagsField = "Tags (comma separated)",
    ingredientsWithCount = { count -> "Ingredients ($count)" },
    addFromCatalog = "Add from the catalog",
    done = "Done",
    selectedCount = { count -> if (count == 1) "1 selected" else "$count selected" },
    quantity = "qty",
    removeItem = { name -> "Remove $name" },
    saveChanges = "Save changes",
    addToNotebook = "Add to notebook",
    kitchenTitle = "Kitchen",
    kitchenTagline = "Touiller — to stir, to toss together.",
    statInFridge = "In your fridge",
    statRecipes = "Recipes",
    statReady = "Ready to cook",
    statAlmost = "Almost there",
    statToBuy = "To buy",
    emptyFridgeButton = "Empty the fridge",
    kitchenBlurb = "Ingredients come from a fixed catalog so every recipe matches exactly. " +
        "Mark what you have, and the notebook sorts itself.",
    languageLabel = "Language",
    appearanceLabel = "Appearance",
    themeSystem = "System",
    themeLight = "Light",
    themeDark = "Dark",
    shoppingTitle = "Shopping list",
    shoppingHeadline = { toBuy, total ->
        when {
            total == 0 -> "Nothing to buy yet — add what you're missing."
            toBuy == 0 -> "All $total done — time to cook."
            else -> "$toBuy of $total still to buy."
        }
    },
    shoppingEmptyTitle = "Your list is empty",
    shoppingEmptySubtitle = "Add an item by hand, or let a recipe send its missing ingredients here.",
    addItemAction = "Add an item",
    searchOrAddPlaceholder = "Search or add an item",
    addCustomItem = { name -> "Add \"$name\"" },
    sectionToBuy = "To buy",
    sectionDone = "In the cart",
    bought = "In the cart",
    clearDone = "Clear",
    moveToFridge = { count ->
        if (count == 1) "Move 1 into the fridge" else "Move $count into the fridge"
    },
    movedToFridge = { count ->
        if (count == 1) "1 ingredient is now in your fridge."
        else "$count ingredients are now in your fridge."
    },
    shoppingMoveHint = "Checked items go straight to your fridge.",
    categoryLabel = { key -> EnglishCategories[key] ?: key }
)

val PortugueseStrings = Strings(
    tabFridge = "Geladeira",
    tabRecipes = "Receitas",
    tabAdd = "Nova",
    tabKitchen = "Cozinha",
    tabShopping = "Lista",
    fridgeTitle = "Minha geladeira",
    fridgeOwnedTitle = "Na geladeira",
    fridgeCount = { count ->
        when (count) {
            0 -> "Sua geladeira está vazia — toque no que você tem."
            1 -> "1 ingrediente na sua geladeira."
            else -> "$count ingredientes na sua geladeira."
        }
    },
    fridgeProgress = { owned, total -> "$owned de $total ingredientes marcados." },
    emptyFridgeAction = "Esvaziar",
    emptyFridgeTitle = "Esvaziar a geladeira?",
    emptyFridgeBody = "Todos os ingredientes voltam a ficar desmarcados. Suas receitas continuam aí.",
    searchIngredients = "Buscar ingredientes",
    noMatchTitle = "Nada encontrado",
    noMatchIngredients = "Tente outro nome de ingrediente.",
    recipesTitle = "Receitas",
    recipesHeadline = { ready, total ->
        when {
            total == 0 -> "Nada no caderno ainda."
            ready == 0 -> "Nada pronto agora — confira sua geladeira."
            else -> "$ready de $total prontas para fazer agora."
        }
    },
    searchRecipes = "Buscar receitas ou ingredientes",
    filterReady = "Pronta",
    filterAlmost = "Quase lá",
    filterMissing = "Faltando",
    filterQuick = "Até 30 min",
    clearFilters = "Limpar",
    noRecipesTitle = "Nenhuma receita ainda",
    noRecipesSubtitle = "Cadastre sua primeira receita e o Touille avisa quando der para fazer.",
    noMatchRecipesSubtitle = "Solte um filtro para ver mais.",
    statusReady = "Pronta para fazer",
    statusAlmost = "Quase lá",
    statusMissing = "Faltam ingredientes",
    missingList = { names -> "Falta $names" },
    missingCount = { count -> "Faltam $count ingredientes" },
    minutesShort = { minutes -> "$minutes min" },
    servingsCount = { count -> if (count == 1) "1 porção" else "$count porções" },
    ingredientsCount = { count -> if (count == 1) "1 ingrediente" else "$count ingredientes" },
    back = "Voltar",
    editRecipe = "Editar receita",
    deleteRecipe = "Excluir receita",
    ingredientsHeader = "Ingredientes",
    methodHeader = "Modo de preparo",
    readyBanner = "Você tem tudo para fazer esta.",
    addMissingToList = { count ->
        if (count == 1) "Anotar 1 que falta" else "Anotar os $count que faltam"
    },
    addedToList = { count ->
        if (count == 1) "1 ingrediente foi para a lista." else "$count ingredientes foram para a lista."
    },
    deleteTitle = { name -> "Excluir $name?" },
    deleteBody = "A receita sai do seu caderno.",
    deleteConfirm = "Excluir",
    deleteCancel = "Manter",
    available = "Tem",
    missing = "Falta",
    newRecipe = "Nova receita",
    addPhoto = "Adicionar foto",
    removePhoto = "Remover foto",
    recipeName = "Nome da receita",
    prepField = "Preparo (min)",
    servingsField = "Porções",
    tagsField = "Tags (separadas por vírgula)",
    ingredientsWithCount = { count -> "Ingredientes ($count)" },
    addFromCatalog = "Adicionar do catálogo",
    done = "Pronto",
    selectedCount = { count -> if (count == 1) "1 selecionado" else "$count selecionados" },
    quantity = "qtd",
    removeItem = { name -> "Remover $name" },
    saveChanges = "Salvar alterações",
    addToNotebook = "Adicionar ao caderno",
    kitchenTitle = "Cozinha",
    kitchenTagline = "Touiller — mexer, misturar.",
    statInFridge = "Na geladeira",
    statRecipes = "Receitas",
    statReady = "Prontas para fazer",
    statAlmost = "Quase lá",
    statToBuy = "A comprar",
    emptyFridgeButton = "Esvaziar a geladeira",
    kitchenBlurb = "Os ingredientes vêm de um catálogo fixo para que cada receita bata exatamente. " +
        "Marque o que você tem e o caderno se organiza sozinho.",
    languageLabel = "Idioma",
    appearanceLabel = "Aparência",
    themeSystem = "Sistema",
    themeLight = "Claro",
    themeDark = "Escuro",
    shoppingTitle = "Lista de compras",
    shoppingHeadline = { toBuy, total ->
        when {
            total == 0 -> "Nada para comprar ainda — anote o que está faltando."
            toBuy == 0 -> "Tudo pronto: $total itens comprados."
            else -> "$toBuy de $total ainda para comprar."
        }
    },
    shoppingEmptyTitle = "Sua lista está vazia",
    shoppingEmptySubtitle = "Anote um item à mão ou deixe uma receita mandar para cá os ingredientes que faltam.",
    addItemAction = "Anotar item",
    searchOrAddPlaceholder = "Buscar ou anotar um item",
    addCustomItem = { name -> "Anotar \"$name\"" },
    sectionToBuy = "Para comprar",
    sectionDone = "Já no carrinho",
    bought = "No carrinho",
    clearDone = "Limpar",
    moveToFridge = { count ->
        if (count == 1) "Mandar 1 para a geladeira" else "Mandar $count para a geladeira"
    },
    movedToFridge = { count ->
        if (count == 1) "1 ingrediente foi para a sua geladeira."
        else "$count ingredientes foram para a sua geladeira."
    },
    shoppingMoveHint = "O que você marca vai direto para a geladeira.",
    categoryLabel = { key -> PortugueseCategories[key] ?: key }
)

fun stringsFor(language: Language): Strings =
    if (language == Language.PT) PortugueseStrings else EnglishStrings

val LocalStrings = staticCompositionLocalOf { EnglishStrings }
val LocalLanguage = staticCompositionLocalOf { Language.EN }

fun Ingredient.displayName(language: Language): String =
    if (language == Language.PT) namePt else name

fun ShoppingItem.displayName(language: Language): String =
    ingredient?.displayName(language) ?: customName
