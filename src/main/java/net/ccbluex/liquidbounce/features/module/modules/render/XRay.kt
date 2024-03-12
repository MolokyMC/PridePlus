/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.block.Block
import net.minecraft.init.Blocks

@ModuleInfo(name = "XRay", description = "Allows you to see ores through walls.", category = ModuleCategory.RENDER)
class XRay : Module() {

    val xrayBlocks = mutableListOf<Block>(
            Blocks.COAL_ORE,
            Blocks.IRON_ORE,
            Blocks.GOLD_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.LAPIS_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.QUARTZ_ORE,
            Blocks.CLAY,
            Blocks.GLOWSTONE,
            Blocks.CRAFTING_TABLE,
            Blocks.TORCH,
            Blocks.LADDER,
            Blocks.TNT,
            Blocks.COAL_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.DIAMOND_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.FIRE,
            Blocks.MOSSY_COBBLESTONE,
            Blocks.MOB_SPAWNER,
            Blocks.END_PORTAL_FRAME,
            Blocks.ENCHANTING_TABLE,
            Blocks.BOOKSHELF,
            Blocks.COMMAND_BLOCK,
            Blocks.LAVA,
            Blocks.FLOWING_LAVA,
            Blocks.WATER,
            Blocks.FLOWING_WATER,
            Blocks.FURNACE,
            Blocks.LIT_FURNACE
    )

    override fun onToggle(state: Boolean) {
        mc.renderGlobal.loadRenderers()
    }
}
