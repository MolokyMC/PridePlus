
package op.wawa.utils.sound

import me.utils.FileUtils
import net.ccbluex.liquidbounce.Pride
import java.io.File

class TipSoundManager {
    var enableSound : TipSoundPlayer
    var disableSound : TipSoundPlayer

    init {
        val enableSoundFile = File(Pride.fileManager.soundsDir,"enable.wav")
        val disableSoundFile = File(Pride.fileManager.soundsDir,"disable.wav")

        if(!enableSoundFile.exists())
            FileUtils.unpackFile(enableSoundFile,"assets/minecraft/pride/sound/enable.wav")

        if(!disableSoundFile.exists())
            FileUtils.unpackFile(disableSoundFile,"assets/minecraft/pride/sound/disable.wav")

        enableSound = TipSoundPlayer(enableSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
    }
}