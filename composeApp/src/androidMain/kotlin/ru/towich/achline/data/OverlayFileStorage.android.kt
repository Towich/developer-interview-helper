package ru.towich.achline.data

import android.content.Context
import java.io.File

actual fun createOverlayFileStorage(): OverlayFileStorage =
    AndroidOverlayFileStorage(AchlineAndroidContext.require())

private class AndroidOverlayFileStorage(
    private val context: Context,
) : OverlayFileStorage {

    override fun readTextOrNull(): String? {
        val f = overlayFile()
        if (!f.exists()) return null
        return f.readText()
    }

    override fun writeAtomic(text: String) {
        val dir = File(context.filesDir, "achline").apply { mkdirs() }
        val tmp = File(dir, "overlay.json.tmp")
        val final = File(dir, "overlay.json")
        tmp.writeText(text)
        if (!tmp.renameTo(final)) {
            tmp.copyTo(final, overwrite = true)
            tmp.delete()
        }
    }

    private fun overlayFile(): File = File(File(context.filesDir, "achline"), "overlay.json")
}
