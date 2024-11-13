package com.example.presentationevaluator

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration

class BakingViewModel : ViewModel() {
    var uiState by mutableStateOf<UiState>(UiState.Initial)
    var apiResponse by mutableStateOf(GeminiResponse())
    var rawResponseString by mutableStateOf("")
    var collectingResources by mutableStateOf<String?>("Collecting resources...")
    var processingData by mutableStateOf<String?>(null)
    var generatingResponse by mutableStateOf<String?>(null)
    var collectedVideoUrl by mutableStateOf<Uri?>(null)
    var audioDuration : Long? = null
    var videoThumbNail by mutableStateOf<Bitmap?>(null)

    private var generativeModel:GenerativeModel? = null

    init {
        setupModel()
    }

    fun setupModel(apiKey: String = ""): Boolean{
        generativeModel = GenerativeModel(
            requestOptions = RequestOptions(
                timeout = Duration.INFINITE
            ),
            modelName = "gemini-1.5-pro-002",
            apiKey = apiKey.ifBlank { BuildConfig.apiKey },
            generationConfig = generationConfig {
                temperature = 1f
                topP = 0.95f
                topK = 40
                maxOutputTokens = 8192
            }
        )
        return true
    }

    private val promptText =
        "for this response you will be working as a presentation evaluator, about how was the presentation, take the audio and some frames of the video presentation and analyze everything, i want you to generate a json response based on this audio file, observe the person giving the presentation about a topic in the audio and generate strictly a json response with the given parameters, there must be nothing else in the response other than json, all the scores must be between 0.0 to 5.0. in remarks say what was good, what was bad, how to improve ect\n" +
                "`wpm`,\n" +
                "`wpm_remarks`,\n" +
                "`wpm_score`,\n" +
                "`number_of_stutters`, also notice clearance in voice here\n" +
                "`stutter_remarks`,\n" +
                "`stutter_score`,\n" +
                "`number_of_grammatical_errors`,\n" +
                "`grammatical_remarks`,\n" +
                "`grammatical_score`,\n" +
                "`list_of_topics_covered`,\n" +
                "`short_summary`,\n" +
                "`what_can_improve_in_content`, this can include having more topics, or even tips on how to distribute stuff\n" +
                "`list_of_possible_improvements_in_presentation`,\n" +
                "`content_score`,\n" +
                "`final_remarks`,\n" +
                "`overall_score`, give here your personal overall score, i would prefer a detailed response, do not assume anything about the ppt or slides as you can not see them."
    fun sendPrompt(
        audioFile: File,
        videoFrames: List<Bitmap>
    ) {
        uiState = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                generatingResponse = "Generating response..."
                val audioBytes = audioFile.readBytes()
                val response = generativeModel?.generateContent(
                    content {
                        image(videoFrames[0])
                        image(videoFrames[1])
                        image(videoFrames[2])
                        image(videoFrames[3])
                        image(videoFrames[4])
                        blob("audio/mp3", audioBytes)
                        text(promptText+if(audioDuration!=null)"the audio duration is $audioDuration seconds" else "")
                    }
                )
                response?.text?.let { outputContent ->
                    rawResponseString = outputContent
                    generatingResponse = "Response Generated"
                    delay(500)
                    collectingResources = "Collecting resources..."
                    processingData = null
                    generatingResponse = null
                    parseGeminiResponse(outputContent.substring(7, outputContent.length - 4))?.let {
                        apiResponse = it
                        uiState = UiState.Success(it)
                        Log.d("log1", "output string: $outputContent")
                        Log.d("log1", "output data: $uiState")
                    }
                }
            } catch (e: Exception) {
                uiState = UiState.Error(e.localizedMessage ?: "")
                e.printStackTrace()
            }
        }
    }
}
