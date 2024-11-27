package com.onwd.arc.im.sidekick.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.util.zip.GZIPOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private const val MAX_FILE_SIZE = 1024 * 1024 * 100 // 100MB

class JsonFileStore(private val context: Context, private val prefix: String) {
    private val dataFlow: MutableSharedFlow<JsonElement> = MutableSharedFlow(0, 100)
    private val fileHandle = File(context.getExternalFilesDir(null), "$prefix.jsonl")
    private val outputWriter = PrintWriter(FileOutputStream(fileHandle, true), true)
    val exportFolder by lazy {
        File(context.getExternalFilesDir(null), "export").apply {
            mkdirs()
        }
    }

    init {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                dataFlow.collect {
                    if (fileHandle.length() > MAX_FILE_SIZE) {
                        export()
                    }
                    synchronized(fileHandle) {
                        outputWriter.println(Json.encodeToString(it))
                    }
                }
            }
        }
    }

    /**
     * Write data to the file.
     */
    suspend fun write(data: JsonElement) {
        dataFlow.emit(data)
        if (Log.isLoggable(javaClass.simpleName, Log.DEBUG)) {
            Log.d(javaClass.simpleName, "Writing data: ${Json.encodeToString(data)}")
        }
    }

    /**
     * Compress and export the current data to a new file and clear the current file.
     */
    suspend fun export() = withContext(Dispatchers.IO) {
        if (fileHandle.length() == 0L) {
            return@withContext
        }
        val tempFile = File.createTempFile(
            "$prefix-",
            ".jsonl.gz",
            exportFolder
        )
        synchronized(fileHandle) {
            outputWriter.flush()
            GZIPOutputStream(tempFile.outputStream(), true).use { gzip ->
                fileHandle.inputStream().copyTo(gzip)
            }
            fileHandle.writeBytes(byteArrayOf())
        }
    }
}
