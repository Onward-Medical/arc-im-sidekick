package com.onwd.arc.im.sidekick.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class JsonFileStore(context: Context, private val prefix: String) {
    private val dataFlow: MutableSharedFlow<JsonElement> = MutableSharedFlow(0, 100)
    private val fileHandle = File(context.getExternalFilesDir(null), "$prefix.jsonl")
    private val outputWriter = PrintWriter(FileOutputStream(fileHandle, true), true)

    init {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                dataFlow.collect {
                    synchronized(fileHandle) {
                        outputWriter.println(Json.encodeToString(it))
                    }
                }
            }
        }
    }

    suspend fun write(data: JsonElement) {
        dataFlow.emit(data)
        if (Log.isLoggable(javaClass.simpleName, Log.DEBUG)) {
            Log.d(javaClass.simpleName, "Writing data: ${Json.encodeToString(data)}")
        }
    }

    suspend fun export(): File {
        return withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile(prefix, ".jsonl")
            synchronized(fileHandle) {
                fileHandle.copyTo(tempFile, overwrite = true)
                fileHandle.writeBytes(byteArrayOf())
            }
            tempFile
        }
    }
}
