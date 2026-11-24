package com.example.videojuego.world

import com.example.videojuego.data.save.WorldSave
import kotlin.test.Test
import kotlin.test.assertEquals

class BagTest {
    private fun controller() = WorldController(
        WorldMap(List(12) { y -> if (y == 0 || y == 11) "~".repeat(12) else "~" + ".".repeat(10) + "~" }),
        emptyList(), 5.5f, 5.5f,
    )

    @Test
    fun fish_goToTheBag() {
        val c = controller()
        assertEquals(4, c.addToBag(ItemType.FISH, 4))
        assertEquals(4, c.state.value.count(ItemType.FISH))
        assertEquals(0, c.state.value.inChest(ItemType.FISH))
    }

    @Test
    fun fish_addUpWithWhatWasAlreadyThere() {
        val c = controller()
        c.addToBag(ItemType.FISH, 2)
        c.addToBag(ItemType.FISH, 3)
        assertEquals(5, c.state.value.count(ItemType.FISH))
    }

    @Test
    fun fish_overTheBagLimitGoToTheChest() {
        val c = controller()
        assertEquals(12, c.addToBag(ItemType.FISH, 12))
        assertEquals(MAX_PER_ITEM, c.state.value.count(ItemType.FISH))
        assertEquals(12 - MAX_PER_ITEM, c.state.value.inChest(ItemType.FISH))
    }

    @Test
    fun fish_areLostOnlyWhenBagAndChestAreFull() {
        val c = controller()
        c.restore(WorldSave(chest = mapOf("FISH" to WorldController.CHEST_MAX_PER_ITEM)))
        assertEquals(MAX_PER_ITEM, c.addToBag(ItemType.FISH, 15))
        assertEquals(MAX_PER_ITEM, c.state.value.count(ItemType.FISH))
        assertEquals(WorldController.CHEST_MAX_PER_ITEM, c.state.value.inChest(ItemType.FISH))
        assertEquals(0, c.addToBag(ItemType.FISH, 3))
    }

    @Test
    fun nothingToAdd_changesNothing() {
        val c = controller()
        assertEquals(0, c.addToBag(ItemType.FISH, 0))
        assertEquals(0, c.addToBag(ItemType.FISH, -3))
        assertEquals(0, c.state.value.count(ItemType.FISH))
    }

    @Test
    fun fish_areSavedAndRestored() {
        val c = controller()
        c.addToBag(ItemType.FISH, 4)
        val saved = c.toWorldSave()
        assertEquals(4, saved.bag["FISH"])
        val other = controller()
        other.restore(saved)
        assertEquals(4, other.state.value.count(ItemType.FISH))
    }

    @Test
    fun otherItemsAreNotTouched() {
        val c = controller()
        c.restore(WorldSave(bag = mapOf("COCONUT" to 2)))
        c.addToBag(ItemType.FISH, 3)
        assertEquals(2, c.state.value.count(ItemType.COCONUT))
    }
}
