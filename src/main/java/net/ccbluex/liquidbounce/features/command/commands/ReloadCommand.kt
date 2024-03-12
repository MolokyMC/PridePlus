/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.font.Fonts

class ReloadCommand : Command("reload", "configreload") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        chat("Reloading...")
        chat("§c§lReloading commands...")
        Pride.commandManager = CommandManager()
        Pride.commandManager.registerCommands()
        Pride.isStarting = true
        Pride.scriptManager.disableScripts()
        Pride.scriptManager.unloadScripts()
        for(module in Pride.moduleManager.modules)
            Pride.moduleManager.generateCommand(module)
        chat("§c§lReloading scripts...")
        Pride.scriptManager.reloadScripts()
        chat("§c§lReloading fonts...")
        Fonts.loadFonts()
        chat("§c§lReloading modules...")
        Pride.fileManager.loadConfig(Pride.fileManager.modulesConfig)
        Pride.isStarting = false
        chat("§c§lReloading values...")
        Pride.fileManager.loadConfig(Pride.fileManager.valuesConfig)
        chat("§c§lReloading accounts...")
        Pride.fileManager.loadConfig(Pride.fileManager.accountsConfig)
        chat("§c§lReloading friends...")
        Pride.fileManager.loadConfig(Pride.fileManager.friendsConfig)
        chat("§c§lReloading xray...")
        Pride.fileManager.loadConfig(Pride.fileManager.xrayConfig)
        chat("§c§lReloading HUD...")
        Pride.fileManager.loadConfig(Pride.fileManager.hudConfig)
        chat("§c§lReloading ClickGUI...")
        Pride.clickGui = ClickGui()
        Pride.fileManager.loadConfig(Pride.fileManager.clickGuiConfig)
        Pride.isStarting = false
        chat("Reloaded.")
    }
}
