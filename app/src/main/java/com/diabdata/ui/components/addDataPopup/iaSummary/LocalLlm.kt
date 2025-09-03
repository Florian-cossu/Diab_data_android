// LocalLlm.kt
package com.diabdata.ui.components.addDataPopup.iaSummary

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File
import java.io.FileOutputStream

fun copyAssetToFilesDir(context: Context, assetName: String): String {
    val outFile = File(context.filesDir, assetName)
    if (!outFile.exists()) {
        context.assets.open(assetName).use { input ->
            FileOutputStream(outFile).use { output ->
                input.copyTo(output)
            }
        }
    }
    return outFile.absolutePath
}

fun initializeGemma(context: Context): LlmInference {
    val taskPath = copyAssetToFilesDir(context, "gemma-3-270m-it-int8.task")
    val options = LlmInference.LlmInferenceOptions.builder()
        .setModelPath(taskPath)
        .setMaxTopK(64)
        .build()
    return LlmInference.createFromOptions(context, options)
}
