/*
 * PridePlus Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/MolokyMC/PridePlus/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.Pride
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import op.wawa.utils.animation.AnimationUtil
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO


class GuiMainMenu : GuiScreen(), GuiYesNoCallback {

    val bigLogo = ResourceLocation("pride/big.png")
    val darkIcon = ResourceLocation("pride/menu/dark.png")
    val lightIcon = ResourceLocation("pride/menu/light.png")

    var slideX : Float = 0F
    var fade : Float = 0F

    var sliderX : Float = 0F
    var sliderDarkX : Float = 0F

    var lastAnimTick: Long = 0L
    var alrUpdate = false

    var lastXPos = 0F

    var extendedModMode = false
    var extendedBackgroundMode = false

    companion object {
        var useParallax = true
    }

    override fun initGui() {
        slideX = 0F
        fade = 0F
        sliderX = 0F
        sliderDarkX = 0F
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!alrUpdate) {
            lastAnimTick = System.currentTimeMillis()
            alrUpdate = true
        }
        val creditInfo = "Copyright Mojang AB. Do not distribute!"

        drawBackground(0)



        GL11.glPushMatrix()
        renderSwitchButton()
        renderDarkModeButton()
        Fonts.font40.drawStringWithShadow("${Pride.CLIENT_NAME} ${Pride.CLIENT_VERSION} | zs.WaWa", 2F, height - 12F, -1)
        Fonts.font40.drawStringWithShadow(creditInfo, width - 3F - Fonts.font40.getStringWidth(creditInfo), height - 12F, -1)
        if (useParallax) moveMouseEffect(mouseX, mouseY, 10F)
        GlStateManager.disableAlpha()
        RenderUtils.drawImage(bigLogo, (width / 2F - 50F).toInt(), (height / 2F - 90F).toInt(), 100, 100)
        GlStateManager.enableAlpha()
        renderBar(mouseX, mouseY, partialTicks)
        GL11.glPopMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)

        if (!Pride.mainMenuPrep) {
            val animProgress = ((System.currentTimeMillis() - lastAnimTick).toFloat() / 1500F).coerceIn(0F, 1F)
            RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), Color(0F, 0F, 0F, 1F - animProgress))
            if (animProgress >= 1F)
                Pride.mainMenuPrep = true
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!Pride.mainMenuPrep || mouseButton != 0) return

        if (isMouseHover(2F, height - 26F, 28F, height - 16F, mouseX, mouseY))
            useParallax = !useParallax

        if (isMouseHover(2F, height - 38F, 28F, height - 28F, mouseX, mouseY))
            Pride.darkMode = !Pride.darkMode

        val staticX = width / 2F - 120F
        val staticY = height / 2F + 20F
        var index: Int = 0
        for (icon in if (extendedModMode) ExtendedImageButton.values() else ImageButton.values()) {
            if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY))
                when (index) {
                    0 -> if (extendedBackgroundMode) extendedBackgroundMode = false else if (extendedModMode) extendedModMode = false else mc.displayGuiScreen(GuiWorldSelection(this))
                    1 -> if (extendedBackgroundMode) GuiBackground.enabled = !GuiBackground.enabled else if (extendedModMode) mc.displayGuiScreen(GuiModList(this)) else mc.displayGuiScreen(GuiMultiplayer(this))
                    2 -> if (extendedBackgroundMode) GuiBackground.particles = !GuiBackground.particles else if (extendedModMode) mc.displayGuiScreen(GuiScripts(this)) else mc.displayGuiScreen(GuiAltManager(this))
                    3 -> if (extendedBackgroundMode) {
                        val file = MiscUtils.openFileChooser() ?: return
                        if (file.isDirectory) return

                        try {
                            Files.copy(file.toPath(), FileOutputStream(Pride.fileManager.backgroundFile))

                            val image = ImageIO.read(FileInputStream(Pride.fileManager.backgroundFile))
                            Pride.background = ResourceLocation("pride/background.png")
                            mc.textureManager.loadTexture(Pride.background, DynamicTexture(image))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            MiscUtils.showErrorPopup("Error", "Exception class: " + e.javaClass.name + "\nMessage: " + e.message)
                            Pride.fileManager.backgroundFile.delete()
                        }
                    } else mc.displayGuiScreen(GuiOptions(this, this.mc.gameSettings))
                    4 -> if (extendedBackgroundMode) {
                        //Pride.videoBackground.reload()
                    } else if (extendedModMode) extendedBackgroundMode = true else extendedModMode = true
                    5 -> mc.shutdown()
                }

            index++
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun moveMouseEffect(mouseX: Int, mouseY: Int, strength: Float) {
        val mX = mouseX - width / 2
        val mY = mouseY - height / 2
        val xDelta = mX.toFloat() / (width / 2).toFloat()
        val yDelta = mY.toFloat() / (height / 2).toFloat()

        GL11.glTranslatef(xDelta * strength, yDelta * strength, 0F)
    }

    fun renderSwitchButton() {
        sliderX = (sliderX + (if (useParallax) 2F else -2F)).coerceIn(0F, 12F)
        Fonts.font40.drawStringWithShadow("Parallax", 28F, height - 25F, -1)
        RenderUtils.drawRoundRect(4F, height - 24F, 22F, height - 18F, 3F, if (useParallax) Color(0, 111, 255, 255).rgb else (if (Pride.darkMode) Color(70, 70, 70, 255) else Color(140, 140, 140, 255)).rgb)
        RenderUtils.drawRoundRect(2F + sliderX, height - 26F, 12F + sliderX, height - 16F, 5F, Color.white.rgb)
    }

    fun renderDarkModeButton() {
        sliderDarkX = (sliderDarkX + (if (Pride.darkMode) 2F else -2F)).coerceIn(0F, 12F)
        GlStateManager.disableAlpha()
        RenderUtils.drawImage(darkIcon, 28F, height - 40F, 14, 14, 1F, 1F, 1F, sliderDarkX / 12F)
        RenderUtils.drawImage(lightIcon, 28F, height - 40F, 14, 14, 1F, 1F, 1F, 1F - (sliderDarkX / 12F))
        GlStateManager.enableAlpha()
        RenderUtils.drawRoundRect(4F, height - 36F, 22F, height - 30F, 3F, (if (Pride.darkMode) Color(70, 70, 70, 255) else Color(140, 140, 140, 255)).rgb)
        RenderUtils.drawRoundRect(2F + sliderDarkX, height - 38F, 12F + sliderDarkX, height - 28F, 5F, Color.white.rgb)
    }

    fun renderBar(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val staticX = width / 2F - 120F
        val staticY = height / 2F + 20F

        RenderUtils.drawRoundRect(staticX, staticY, staticX + 240F, staticY + 20F, 5F, (if (Pride.darkMode) Color(0, 0, 0, 100) else Color(255, 255, 255, 100)).rgb)

        var index: Int = 0
        var shouldAnimate = false
        var displayString: String? = null
        var moveX = 0F
        if (extendedModMode) {
            if (extendedBackgroundMode)
                for (icon in ExtendedBackgroundButton.values()) {
                    if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY)) {
                        shouldAnimate = true
                        displayString = if (icon == ExtendedBackgroundButton.Enabled)
                            "Custom background: ${if (GuiBackground.enabled) "§aON" else "§cOFF"}"
                        else if (icon == ExtendedBackgroundButton.Particles)
                            "${icon.buttonName}: ${if (GuiBackground.particles) "§aON" else "§cOFF"}"
                        else
                            icon.buttonName
                        moveX = staticX + 40F * index
                    }
                    index++
                }
            else
                for (icon in ExtendedImageButton.values()) {
                    if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY)) {
                        shouldAnimate = true
                        displayString = if (icon == ExtendedImageButton.DiscordRPC) "${icon.buttonName}: ${"§cOFF"}" else icon.buttonName
                        moveX = staticX + 40F * index
                    }
                    index++
                }
        } else
            for (icon in ImageButton.values()) {
                if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY)) {
                    shouldAnimate = true
                    displayString = icon.buttonName
                    moveX = staticX + 40F * index
                }
                index++
            }

        if (displayString != null)
            Fonts.font35.drawCenteredString(displayString!!, width / 2F, staticY + 30F, -1)
        else
            Fonts.font35.drawCenteredString("hi", width / 2F, staticY + 30F, -1)

        if (shouldAnimate) {
            if (fade == 0F)
                slideX = moveX
            else
                slideX = AnimationUtil.animate(moveX, slideX, 0.5F * (1F - partialTicks))

            lastXPos = moveX

            fade += 10F
            if (fade >= 100F) fade = 100F
        } else {
            fade -= 10F
            if (fade <= 0F) fade = 0F

            slideX = AnimationUtil.animate(lastXPos, slideX, 0.5F * (1F - partialTicks))
        }

        if (fade != 0F)
            RenderUtils.drawRoundRect(slideX, staticY, slideX + 40F, staticY + 20F, 5F, (if (Pride.darkMode) Color(0F, 0F, 0F, fade / 100F * 0.6F) else Color(1F, 1F, 1F, fade / 100F * 0.6F)).rgb)

        index = 0
        GlStateManager.disableAlpha()
        if (extendedModMode) {
            if (extendedBackgroundMode)
                for (i in ExtendedBackgroundButton.values()) {
                    if (Pride.darkMode)
                        RenderUtils.drawImage(i.texture, (staticX + 40F * index + 11F).toInt(), (staticY + 1F).toInt(), 18, 18)
                    else
                        RenderUtils.drawImage(i.texture, staticX + 40F * index + 11F, staticY + 1F, 18, 18, 0F, 0F, 0F, 1F)
                    index++
                }
            else
                for (i in ExtendedImageButton.values()) {
                    if (Pride.darkMode)
                        RenderUtils.drawImage(i.texture, (staticX + 40F * index + 11F).toInt(), (staticY + 1F).toInt(), 18, 18)
                    else
                        RenderUtils.drawImage(i.texture, staticX + 40F * index + 11F, staticY + 1F, 18, 18, 0F, 0F, 0F, 1F)
                    index++
                }
        } else
            for (i in ImageButton.values()) {
                if (Pride.darkMode)
                    RenderUtils.drawImage(i.texture, (staticX + 40F * index + 11F).toInt(), (staticY + 1F).toInt(), 18, 18)
                else
                    RenderUtils.drawImage(i.texture, staticX + 40F * index + 11F, staticY + 1F, 18, 18, 0F, 0F, 0F, 1F)
                index++
            }
        GlStateManager.enableAlpha()
    }

    fun isMouseHover(x: Float, y: Float, x2: Float, y2: Float, mouseX: Int, mouseY: Int): Boolean = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2

    enum class ImageButton(val buttonName: String, val texture: ResourceLocation) {
        Single("Singleplayer", ResourceLocation("pride/menu/singleplayer.png")),
        Multi("Multiplayer", ResourceLocation("pride/menu/multiplayer.png")),
        Alts("Alts", ResourceLocation("pride/menu/alt.png")),
        Settings("Settings", ResourceLocation("pride/menu/settings.png")),
        Mods("Mods/Customize", ResourceLocation("pride/menu/mods.png")),
        Exit("Exit", ResourceLocation("pride/menu/exit.png"))
    }

    enum class ExtendedImageButton(val buttonName: String, val texture: ResourceLocation) {
        Back("Back", ResourceLocation("pride/clickgui/back.png")),
        Mods("Mods", ResourceLocation("pride/menu/mods.png")),
        Scripts("Scripts", ResourceLocation("pride/clickgui/docs.png")),
        DiscordRPC("Discord RPC", ResourceLocation("pride/menu/discord.png")),
        Background("Background", ResourceLocation("pride/menu/wallpaper.png")),
        Exit("Exit", ResourceLocation("pride/menu/exit.png"))
    }

    enum class ExtendedBackgroundButton(val buttonName: String, val texture: ResourceLocation) {
        Back("Back", ResourceLocation("pride/clickgui/back.png")),
        Enabled("Enabled", ResourceLocation("pride/notification/new/checkmark.png")),
        Particles("Particles", ResourceLocation("pride/clickgui/brush.png")),
        Change("Change wallpaper", ResourceLocation("pride/clickgui/import.png")),
        Reset("Reload wallpaper", ResourceLocation("pride/clickgui/reload.png")),
        Exit("Exit", ResourceLocation("pride/menu/exit.png"))
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}