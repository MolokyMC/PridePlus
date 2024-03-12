/*                        _ooOoo_
                         o8888888o
                         88" . "88
                         (| ^_^ |)
                         O\  =  /O
                      ____/`---'\____
                    .'  \\|     |//  `.
                   /  \\|||  :  |||//  \
                  /  _||||| -:- |||||-  \
                  |   | \\\  -  /// |   |
                  | \_|  ''\---/''  |   |
                  \  .-\__  `-`  ___/-. /
                ___`. .'  /--.--\  `. . ___
              ."" '<  `.___\_<|>_/___.'  >'"".
            | | :  `- \`.;`\ _ /`;.`/ - ` : | |
            \  \ `-.   \_ __\ /__ _/   .-` /  /
      ========`-.____`-.___\_____/___.-`____.-'========
                           `=---='
      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
           佛祖保佑       永无Exception     永不修改           */
package net.ccbluex.liquidbounce

import net.ccbluex.liquidbounce.cape.CapeAPI.registerCapeService
import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.special.AntiForge
import net.ccbluex.liquidbounce.features.special.BungeeCordSpoof
import net.ccbluex.liquidbounce.features.special.DonatorCape
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.script.ScriptManager
import net.ccbluex.liquidbounce.script.remapper.Remapper.loadSrg
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.HUD.Companion.createDefault
import net.ccbluex.liquidbounce.ui.cnfont.FontLoaders
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.minecraft.util.ResourceLocation
import op.wawa.VideoBackground
import op.wawa.manager.CombatManager
import op.wawa.sound.Sound
import op.wawa.utils.QQUtils
import op.wawa.utils.sound.TipSoundManager
import java.awt.SystemTray
import java.awt.TrayIcon
import java.util.*
import javax.imageio.ImageIO

object Pride {

    // Client information
    var CLIENT_NAME = "PridePlus"
    const val CLIENT_VERSION = "NextGen1.9"
    const val CLIENT_CREATOR = "WaWa"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"

    var isStarting = false

    var mainMenuPrep = false
    var darkMode: Boolean = false

    // Managers
    lateinit var moduleManager: ModuleManager
    lateinit var commandManager: CommandManager
    lateinit var eventManager: EventManager
    lateinit var fileManager: FileManager
    lateinit var scriptManager: ScriptManager
    lateinit var combatManager: CombatManager
    lateinit var tipSoundManager: TipSoundManager
    //lateinit var videoBackground: VideoBackground
    lateinit var userQQ: String

    // HUD & ClickGUI
    lateinit var hud: HUD

    lateinit var clickGui: ClickGui

    // Menu Background
    var background: ResourceLocation? = null

    fun displayTray(title: String, text: String, type: TrayIcon.MessageType?) {
        val tray = SystemTray.getSystemTray()
        val trayIcon = TrayIcon(ImageIO.read(Objects.requireNonNull(javaClass.getResourceAsStream("/assets/minecraft/pride/icon128.png"))))
        trayIcon.isImageAutoSize = true
        trayIcon.toolTip = "PridePlus NextGen"
        tray.add(trayIcon)
        trayIcon.displayMessage(title, text, type)
    }

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true

        ClientUtils.getLogger().info("Loading $CLIENT_NAME $CLIENT_VERSION")
        ClientUtils.getLogger().info("Initializing...")
        val startTime = System.currentTimeMillis()

        userQQ = QQUtils.getLoginQQNumber()
        ClientUtils.getLogger().info("PridePlus >> QQNumber has been read.")

        // Initialize managers
        fileManager = FileManager()
        eventManager = EventManager()

        combatManager = CombatManager()
        // Register listeners
        eventManager.registerListener(combatManager)
        eventManager.registerListener(RotationUtils())
        eventManager.registerListener(AntiForge())
        eventManager.registerListener(BungeeCordSpoof())
        eventManager.registerListener(DonatorCape())
        eventManager.registerListener(InventoryUtils())

        tipSoundManager = TipSoundManager()

        commandManager = CommandManager()

        // Load client fonts
        Fonts.loadFonts()
        FontLoaders.initFonts()
        ClientUtils.getLogger().info("PridePlus >> Fonts Loaded.")

        moduleManager = ModuleManager()

        // Setup modules
        moduleManager.registerModules()
        ClientUtils.getLogger().info("PridePlus >> Modules Loaded.")

        try {
            loadSrg()
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()

            ClientUtils.getLogger().info("PridePlus >> Scripts Loaded.")
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }

        // Register commands
        commandManager.registerCommands()
        ClientUtils.getLogger().info("PridePlus >> Commands Loaded.")

        // Load configs
        fileManager.loadConfigs(fileManager.modulesConfig, fileManager.valuesConfig, fileManager.accountsConfig,
            fileManager.friendsConfig, fileManager.xrayConfig, fileManager.shortcutsConfig)

        // ClickGUI
        clickGui = ClickGui()
        fileManager.loadConfig(fileManager.clickGuiConfig)

        fileManager.loadConfigs(
            fileManager.modulesConfig,
            fileManager.valuesConfig
        )

        // Register capes service
        try {
            registerCapeService()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to register cape service", throwable)
        }

        // Set HUD
        hud = createDefault()
        fileManager.loadConfig(fileManager.hudConfig)

        // video
        //videoBackground = VideoBackground()
        //videoBackground.load()

        // Load generators
        GuiAltManager.loadGenerators()

        // Set is starting status
        isStarting = false
        // Log success
        ClientUtils.getLogger().info("$CLIENT_NAME $CLIENT_VERSION loaded in ${(System.currentTimeMillis() - startTime)}ms!")
        // System Information
        displayTray("PridePlus 已加载完成", "使用即同意用户协议 \n链接: kdocs.cn/l/cmwaN2cwjvAl", TrayIcon.MessageType.INFO)
        ClientUtils.getLogger().info("使用PridePlus即代表你同意我们的用户协议及隐私政策.")
        //Sound
        Sound.INSTANCE.Spec()
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        eventManager.callEvent(ClientShutdownEvent())

        // Save all available configs
        fileManager.saveAllConfigs()
    }

}