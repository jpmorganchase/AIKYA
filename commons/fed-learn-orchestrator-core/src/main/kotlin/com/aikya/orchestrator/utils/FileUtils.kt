package com.aikya.orchestrator.utils

import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object FileUtils {

    fun readFileAsString(inputStream: InputStream): String {
        return inputStream.bufferedReader(Charset.defaultCharset()).use { it.readText() }
    }
    @Throws(IOException::class)
    fun readFromFilePath(path: String): String {
        var trimmedPath = path.trim()
        if (!Files.exists(Paths.get(trimmedPath))) {
            trimmedPath = getAbsolutePath(trimmedPath)
        }
        return String(Files.readAllBytes(Paths.get(trimmedPath)))
    }

    @Throws(IOException::class)
    fun getAbsolutePath(classPathResource: String): String {
        val resource = ClassPathResource(classPathResource)
        return resource.file.absolutePath
    }
}