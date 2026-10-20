package com.touille.domain

import com.touille.data.IngredientSeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CookMatcherTest {

    private fun ingredient(index: Int, owned: Boolean) = Ingredient(
        key = "ingredient$index",
        name = "ingredient$index",
        namePt = "ingrediente$index",
        category = "Pantry",
        emoji = "🥄",
        owned = owned
    )

    private fun recipe(name: String, vararg owned: Boolean) = Recipe(
        id = owned.size.toLong(),
        name = name,
        photoUri = null,
        steps = "",
        prepMinutes = 10,
        servings = 2,
        tags = emptyList(),
        ingredients = owned.mapIndexed { index, has ->
            RecipeIngredient(ingredient(index, has), "")
        }
    )

    @Test
    fun `catalog keys are unique and stable`() {
        val keys = IngredientSeed.all.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        assertEquals(keys, IngredientSeed.all.map { it.name })
    }

    @Test
    fun `no missing ingredients is ready`() {
        assertEquals(CookStatus.READY, recipe("omelette", true, true, true).status)
    }

    @Test
    fun `one missing ingredient is almost there`() {
        assertEquals(CookStatus.ALMOST, recipe("ratatouille", true, true, false).status)
    }

    @Test
    fun `two missing ingredients is almost there`() {
        assertEquals(CookStatus.ALMOST, recipe("cassoulet", true, false, false).status)
    }

    @Test
    fun `three missing ingredients is missing`() {
        assertEquals(CookStatus.MISSING, recipe("bouillabaisse", true, false, false, false).status)
    }

    @Test
    fun `recipe with no ingredients is ready`() {
        assertEquals(CookStatus.READY, recipe("water").status)
    }

    @Test
    fun `missing list names the missing ingredients only`() {
        val target = recipe("tarte", true, false, false)
        val missing = target.missing.map { it.ingredient.key }
        assertEquals(listOf("ingredient1", "ingredient2"), missing)
    }

    @Test
    fun `missingFor uses the owned id set`() {
        val target = recipe("soup", false, false, false)
        val owned = setOf("ingredient0", "ingredient1")
        assertEquals(listOf("ingredient2"), CookMatcher.missingFor(target, owned).map { it.key })
        assertEquals(CookStatus.ALMOST, CookMatcher.statusOf(target, owned))
    }

    @Test
    fun `search matches either language`() {
        val onion = Ingredient("Onion", "Onion", "Cebola", "Vegetables", "🧅", false)
        assertTrue(onion.matches("oni"))
        assertTrue(onion.matches("ceb"))
        assertTrue(onion.matches("CEBOLA"))
        assertFalse(onion.matches("garlic"))
    }

    @Test
    fun `search ignores missing accents in query and in name`() {
        val sugar = Ingredient("Sugar", "Sugar", "Açúcar", "Pantry", "🍬", false)
        assertTrue(sugar.matches("acucar"))
        assertTrue(sugar.matches("acuca"))
        assertTrue(sugar.matches("Açúcar"))
        assertTrue(sugar.matches("ACUCAR"))

        val cassava = Ingredient("Cassava", "Cassava", "Mandioca", "Vegetables", "🥔", false)
        assertFalse(cassava.matches("acucar"))

        val passion = Ingredient("Passion fruit", "Passion fruit", "Maracujá", "Fruit", "🍈", false)
        assertTrue(passion.matches("maracuja"))
        assertTrue(passion.matches("MARACUJÁ"))

        assertTrue("Pão de queijo".containsFolded("pao"))
        assertTrue("Feijao tropeiro".containsFolded("feijão"))
    }

    @Test
    fun `library sort surfaces ready then almost then missing`() {
        val ready = recipe("ready", true)
        val almost = recipe("almost", true, false)
        val missing = recipe("missing", false, false, false)
        val sorted = CookMatcher.sortForLibrary(listOf(missing, almost, ready))
        assertEquals(listOf("ready", "almost", "missing"), sorted.map { it.name })
    }

    @Test
    fun `sort breaks ties by fewest missing then name`() {
        val twoMissing = recipe("aaa", true, false, false)
        val oneMissing = recipe("zzz", true, false)
        val sorted = CookMatcher.sortForLibrary(listOf(twoMissing, oneMissing))
        assertEquals(listOf("zzz", "aaa"), sorted.map { it.name })
    }
}
