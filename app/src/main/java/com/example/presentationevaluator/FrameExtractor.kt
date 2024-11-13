package com.example.presentationevaluator

import android.content.Context
import android.media.MediaMetadataRetriever
import android.graphics.Bitmap
import android.net.Uri

fun extractEquallyDistributedFrames(context: Context, videoUri: Uri, frameCount: Int = 5): List<Bitmap> {
    val retriever = MediaMetadataRetriever()

    try {
        // Set data source from URI using ContentResolver
        retriever.setDataSource(context, videoUri)

        // Get video duration in milliseconds
        val videoDurationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

        // Calculate time interval between frames
        val interval = videoDurationMs / (frameCount + 1)
        val frames = mutableListOf<Bitmap>()

        // Extract frames at equally distributed timestamps
        for (i in 1..frameCount) {
            val timeMs = i * interval
            val bitmap = retriever.getFrameAtTime(timeMs * 1000) // Convert to microseconds
            bitmap?.let { frames.add(it) }
        }

        return frames
    } catch (e: Exception) {
        e.printStackTrace()
        return emptyList()
    } finally {
        retriever.release()
    }
}
