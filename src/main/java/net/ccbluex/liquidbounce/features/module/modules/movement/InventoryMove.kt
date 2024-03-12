/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import net.minecraft.network.play.client.CPacketPlayer
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "InvMove", description = "Allows you to walk while an inventory is opened.", category = ModuleCategory.MOVEMENT)
class InventoryMove : Module() {
    private val noDetectableValue = BoolValue("NoDetectable", false)
    private val rotateValue = BoolValue("Rotate", true)
    val aacAdditionProValue = BoolValue("AACAdditionPro", false)
    private val noMoveClicksValue = BoolValue("NoMoveClicks", false)

    private val blinkPacketList = mutableListOf<CPacketPlayer>()
    private var lastInvOpen = false
    private var invOpen = false

    private fun updateKeyState() {
        if (mc.currentScreen != null && mc.currentScreen !is GuiChat && (!noDetectableValue.get() || mc.currentScreen !is GuiContainer)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, GameSettings.isKeyDown(mc.gameSettings.keyBindForward))
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.keyCode, GameSettings.isKeyDown(mc.gameSettings.keyBindRight))
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.keyCode, GameSettings.isKeyDown(mc.gameSettings.keyBindLeft))
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.keyCode, GameSettings.isKeyDown(mc.gameSettings.keyBindBack))
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, GameSettings.isKeyDown(mc.gameSettings.keyBindJump))
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, GameSettings.isKeyDown(mc.gameSettings.keyBindSprint))

            if (rotateValue.get()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    if (mc.player.rotationPitch > -90) {
                        mc.player.rotationPitch -= 5
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    if (mc.player.rotationPitch < 90) {
                        mc.player.rotationPitch += 5
                    }
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    mc.player.rotationYaw -= 5
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    mc.player.rotationYaw += 5
                }
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        updateKeyState()
    }

    @EventTarget
    fun onScreen(event: ScreenEvent) {
        updateKeyState()
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        if (noMoveClicksValue.get() && MovementUtils.isMoving) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        blinkPacketList.clear()
        invOpen = false
        lastInvOpen = false
    }

    override fun onDisable() {
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindForward) || mc.currentScreen != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.keyCode, false)
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindBack) || mc.currentScreen != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.keyCode, false)
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight) || mc.currentScreen != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.keyCode, false)
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft) || mc.currentScreen != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.keyCode, false)
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindJump) || mc.currentScreen != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.keyCode, false)
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSprint) || mc.currentScreen != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, false)
        }

    }

    override val tag: String?
        get() = if (aacAdditionProValue.get()) "AACAdditionPro" else "Bypass"
}
