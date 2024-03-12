/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.minecraft.network.play.client.CPacketPlayer

class HurtCommand : Command("hurt") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        var damage = 1

        if (args.size > 1) {
            try {
                damage = args[1].toInt()
            } catch (ignored: NumberFormatException) {
                chatSyntaxError()
                return
            }
        }

        // Latest NoCheatPlus damage exploit
        val thePlayer = mc.player ?: return

        val x = thePlayer.posX
        val y = thePlayer.posY
        val z = thePlayer.posZ

        for (i in 0 until 65 * damage) {
            mc.connection!!.sendPacket(CPacketPlayer.Position(x, y + 0.049, z, false))
            mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, false))
        }

        mc.connection!!.sendPacket(CPacketPlayer.Position(x, y, z, true))

        // Output message
        chat("You were damaged.")
    }
}