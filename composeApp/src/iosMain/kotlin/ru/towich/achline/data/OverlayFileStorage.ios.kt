package ru.towich.achline.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite
import platform.posix.remove
import platform.posix.rename

@OptIn(ExperimentalForeignApi::class)
actual fun createOverlayFileStorage(): OverlayFileStorage = IosOverlayFileStorage()

@OptIn(ExperimentalForeignApi::class)
private class IosOverlayFileStorage : OverlayFileStorage {

    private val overlayPath: String by lazy {
        val fm = NSFileManager.defaultManager
        val base = fm.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ) ?: error("Application Support URL")
        val dir = base.URLByAppendingPathComponent(pathComponent = "AchLine", isDirectory = true)
            ?: error("AchLine dir URL")
        fm.createDirectoryAtURL(
            url = dir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        val file = dir.URLByAppendingPathComponent(pathComponent = "overlay.json", isDirectory = false)
            ?: error("overlay URL")
        requireNotNull(file.path)
    }

    private val tmpPath: String get() = "$overlayPath.tmp"

    override fun readTextOrNull(): String? {
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(overlayPath)) return null
        val file = fopen(overlayPath, "rb") ?: return null
        try {
            fseek(file, 0, SEEK_END)
            val len = ftell(file)
            if (len < 0L) return null
            fseek(file, 0, SEEK_SET)
            if (len == 0L) return ""
            val size = len.toInt()
            val buf = ByteArray(size)
            val read = buf.usePinned { pinned ->
                fread(pinned.addressOf(0), 1UL, size.toULong(), file)
            }
            if (read != size.toULong()) return null
            return buf.decodeToString()
        } finally {
            fclose(file)
        }
    }

    override fun writeAtomic(text: String) {
        val fm = NSFileManager.defaultManager
        val bytes = text.encodeToByteArray()
        val file = fopen(tmpPath, "wb") ?: error("Не удалось открыть временный файл overlay")
        try {
            if (bytes.isNotEmpty()) {
                val written = bytes.usePinned { pinned ->
                    fwrite(pinned.addressOf(0), 1UL, bytes.size.toULong(), file)
                }
                check(written == bytes.size.toULong()) { "Не удалось записать overlay" }
            }
        } finally {
            fclose(file)
        }
        if (fm.fileExistsAtPath(overlayPath)) {
            remove(overlayPath)
        }
        if (rename(tmpPath, overlayPath) != 0) {
            error("Не удалось переименовать overlay")
        }
    }
}
