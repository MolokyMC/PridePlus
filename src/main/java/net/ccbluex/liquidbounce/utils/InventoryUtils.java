/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.utils;

import net.ccbluex.liquidbounce.event.ClickWindowEvent;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.Listenable;
import net.ccbluex.liquidbounce.event.PacketEvent;
import net.ccbluex.liquidbounce.utils.timer.MSTimer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;

import java.util.Arrays;
import java.util.List;

public final class InventoryUtils extends MinecraftInstance implements Listenable {

    public static final MSTimer CLICK_TIMER = new MSTimer();
    public static final List<Block> BLOCK_BLACKLIST = Arrays.asList(
            Blocks.CHEST, Blocks.ENDER_CHEST, Blocks.TRAPPED_CHEST, Blocks.ANVIL, Blocks.SAND, Blocks.WEB, Blocks.TORCH,
            Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.WATERLILY, Blocks.DISPENSER, Blocks.STONE_PRESSURE_PLATE, Blocks.WOODEN_PRESSURE_PLATE,
            Blocks.NOTEBLOCK, Blocks.DROPPER, Blocks.TNT, Blocks.STANDING_BANNER, Blocks.WALL_BANNER, Blocks.REDSTONE_TORCH
    );
    public static int findItem2(final int startSlot, final int endSlot, final Item item) {
        for (int i = startSlot; i < endSlot; i++) {
            final ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();

            if (stack.getItem() == item)
                return i;
        }
        return -1;
    }
    public static int findItem(final int startSlot, final int endSlot, final Item item) {
        for (int i = startSlot; i < endSlot; i++) {
            final ItemStack stack = mc.player.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem().equals(item))
                return i;
        }

        return -1;
    }


    public static boolean hasSpaceHotbar() {
        for (int i = 36; i < 45; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == null)
                return true;
        }

        return false;
    }

    public static int findAutoBlockBlock() {
        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.player.inventoryContainer.getSlot(i).getStack();

            if ((itemStack.getItem() instanceof ItemBlock) && itemStack.stackSize > 0) {
                final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                final Block block = itemBlock.getBlock();

                if (block.isFullBlock(block.getDefaultState()) && !BLOCK_BLACKLIST.contains(block)
                        && !(block instanceof BlockBush))
                    return i;
            }
        }

        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.player.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null && (itemStack.getItem() instanceof ItemBlock) && itemStack.stackSize > 0) {
                final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                final Block block = itemBlock.getBlock();

                if (!BLOCK_BLACKLIST.contains(block) && !(block instanceof BlockBush))
                    return i;
            }
        }

        return -1;
    }

    @EventTarget
    public void onClick(final ClickWindowEvent event) {
        CLICK_TIMER.reset();
    }

    @EventTarget
    public void onPacket(final PacketEvent event) {
        final Packet packet = event.getPacket();

        if (packet instanceof CPacketPlayerTryUseItemOnBlock)
            CLICK_TIMER.reset();
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
