package com.clawdroid.android.data.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class EmbeddingService @Inject constructor() {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private val embeddingDim = 384

    suspend fun initialize(modelBytes: ByteArray? = null) = withContext(Dispatchers.IO) {
        try {
            env = OrtEnvironment.getEnvironment()
            if (modelBytes != null) {
                session = env!!.createSession(modelBytes)
            }
        } catch (e: Exception) {
            // ONNX model not available — fallback to keyword search
            env = null
            session = null
        }
    }

    suspend fun embed(text: String): FloatArray? = withContext(Dispatchers.IO) {
        val currentSession = session ?: return@withContext null
        try {
            val inputTensor = OnnxTensor.createTensor(env!!, arrayOf(text))
            val results = currentSession.run(mapOf("input" to inputTensor))
            val output = results[0].value as Array<FloatArray>
            output[0]
        } catch (e: Exception) {
            null
        }
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        var dot = 0f; var normA = 0f; var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return if (normA > 0f && normB > 0f) dot / (sqrt(normA) * sqrt(normB)) else 0f
    }

    fun close() {
        session?.close()
        env?.close()
    }
}
