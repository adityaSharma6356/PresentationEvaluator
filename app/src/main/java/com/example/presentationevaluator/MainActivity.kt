package com.example.presentationevaluator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.presentationevaluator.ui.theme.PresentationEvaluatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private val viewModel: BakingViewModel by viewModels()

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                lifecycleScope.launch {
                    delay(1000)
                    viewModel.collectingResources = "Resources Collected"
                    viewModel.collectedVideoUrl = uri
                    Log.d("log1", "Received video file:$uri")
                    viewModel.processingData = "Processing Data..."
                    val audioFile = extractAudioFromVideo(this@MainActivity, uri, onDurationCallBack = {viewModel.audioDuration = it})
                    val videoFrames = uri.path?.let { extractEquallyDistributedFrames(this@MainActivity, uri, 5) } ?: emptyList()
                    delay(2000)
                    Log.d("log1", "Extracted audio file on path:${audioFile?.path}")
                    if (audioFile != null && videoFrames.isNotEmpty()) {
                        viewModel.videoThumbNail = videoFrames.first()
                        viewModel.processingData = "Video and Audio file extracted"
                        Log.d("log1", "Audio extension: ${audioFile.extension}")
                        viewModel.sendPrompt(audioFile, videoFrames)
                    }

                }
            }
        } else {
            viewModel.uiState = UiState.Initial
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PresentationEvaluatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BakingScreen(resultLauncher, viewModel)
                }
            }
        }
    }


}