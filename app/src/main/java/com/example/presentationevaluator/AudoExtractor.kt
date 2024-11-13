package com.example.presentationevaluator

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

suspend fun extractAudioFromVideo(context: Context, videoUri: Uri, onDurationCallBack: (Long) -> Unit): File? {
    val audioFile = File(context.getExternalFilesDir(null), "extracted_audio.mp3")
    val extractor = MediaExtractor()
    var audioDurationUs: Long? = null
    try {
        // Set data source for the video file
        extractor.setDataSource(context, videoUri, null)
        Log.d("log1", "1")
        // Find the audio track index
        var audioTrackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                audioTrackIndex = i
                audioDurationUs = format.getLong(MediaFormat.KEY_DURATION)
                break
            }
        }

        Log.d("log1", "2")
        if (audioTrackIndex == -1) {
            // No audio track found
            return null
        }

        extractor.selectTrack(audioTrackIndex)

        Log.d("log1", "3")
        // Create a MediaMuxer to write the extracted audio
        val format = extractor.getTrackFormat(audioTrackIndex)
        val muxer = MediaMuxer(audioFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val muxerAudioTrack = muxer.addTrack(format)
        muxer.start()

        Log.d("log1", "4")
        val buffer = ByteArray(1024 * 1024)
        val byteBuffer = java.nio.ByteBuffer.wrap(buffer)
        val bufferInfo = android.media.MediaCodec.BufferInfo()

        while (true) {
            bufferInfo.size = extractor.readSampleData(byteBuffer, 0)
            if (bufferInfo.size < 0) {
                break
            }
            bufferInfo.presentationTimeUs = extractor.sampleTime
            bufferInfo.flags = 0
            muxer.writeSampleData(muxerAudioTrack, byteBuffer, bufferInfo)
            extractor.advance()
        }

        Log.d("log1", "5")
        muxer.stop()
        muxer.release()
        extractor.release()

        audioDurationUs?.let {
            onDurationCallBack(it)
        }

        return audioFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        extractor.release()
    }
}
