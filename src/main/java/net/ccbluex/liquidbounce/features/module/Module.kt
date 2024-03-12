/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.Translate
import net.ccbluex.liquidbounce.features.value.Value
import org.lwjgl.input.Keyboard

open class Module : MinecraftInstance(), Listenable {
    // Module information
    // TODO: Remove ModuleInfo and change to constructor (#Kotlin)
    var animation = 0F
    val tab = Translate(0f , 0f)
    var name: String
    var description: String
    var category: ModuleCategory
    var keyBind = Keyboard.CHAR_NONE
        set(keyBind) {
            field = keyBind

            if (!Pride.isStarting)
                Pride.fileManager.saveConfig(Pride.fileManager.modulesConfig)
        }
    var array = true
        set(array) {
            field = array

            if (!Pride.isStarting)
                Pride.fileManager.saveConfig(Pride.fileManager.modulesConfig)
        }
    private val canEnable: Boolean

    var slideStep = 0F

    init {
        val moduleInfo = javaClass.getAnnotation(ModuleInfo::class.java)!!

        name = moduleInfo.name
        description = moduleInfo.description
        category = moduleInfo.category
        keyBind = moduleInfo.keyBind
        array = moduleInfo.array
        canEnable = moduleInfo.canEnable
    }

    // Current state of module
    var state = false
        set(value) {
            if (field == value)
                return

            // Call toggle
            onToggle(value)

            // Play sound and add notification
            if (!Pride.isStarting) {
                //mc.soundHandler.playSound("random.click", 1F)
                Pride.hud.addNotification(Notification("Module","${if (value) "Enabled " else "Disabled "}$name", if(value) NotifyType.SUCCESS else NotifyType.ERROR))
            }

            // Call on enabled or disabled
            if (value) {
                // Enable Sound
                Pride.tipSoundManager.enableSound.asyncPlay()

                onEnable()

                if (canEnable)
                    field = true
            } else {
                // Disable Sound
                Pride.tipSoundManager.disableSound.asyncPlay()

                onDisable()
                field = false
            }

            // Save module state
            Pride.fileManager.saveConfig(Pride.fileManager.modulesConfig)
        }


    // HUD
    val hue = Math.random().toFloat()
    var slide = 0F
    var higt = 0F

    // Tag
    open val tag: String?
        get() = null

    /**
     * Toggle module
     */
    fun toggle() {
        state = !state
    }

    /**
     * Called when module toggled
     */
    open fun onToggle(state: Boolean) {}

    /**
     * Called when module enabled
     */
    open fun onEnable() {}

    /**
     * Called when module disabled
     */
    open fun onDisable() {}

    /**
     * Get module by [valueName]
     */
    open fun getValue(valueName: String) = values.find { it.name.equals(valueName, ignoreCase = true) }

    /**
     * Get all values of module
     */
    open val values: List<Value<*>>
        get() = ClassUtils.getValues(this.javaClass, this)

    /**
     * Events should be handled when module is enabled
     */
    override fun handleEvents() = state
}