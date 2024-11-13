package com.example.presentationevaluator

import android.util.Log
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.json.JSONException

@Serializable
data class GeminiResponse(
    @SerialName("wpm") val wpm: Int? = null,
    @SerialName("wpm_remarks") val wpmRemarks: String? = null,
    @SerialName("wpm_score") val wpmScore: Float = 0f,

    @SerialName("number_of_stutters") val numberOfStutters: Int? = null,
    @SerialName("stutter_remarks") val stutterRemarks: String? = null,
    @SerialName("stutter_score") val stutterScore: Float = 0f,

    @SerialName("number_of_grammatical_errors") val numberOfGrammaticalErrors: Int? = null,
    @SerialName("grammatical_remarks") val grammaticalRemarks: String? = null,
    @SerialName("grammatical_score") val grammaticalScore: Float = 0f,

    @SerialName("list_of_topics_covered") val listOfTopicsCovered: List<String> = emptyList(),
    @SerialName("short_summary") val shortSummary: String? = null,
    @SerialName("what_can_improve_in_content") val whatCanImproveInContent: String? = null,
    @SerialName("list_of_possible_improvements_in_presentation") val listOfPossibleImprovementsInPresentation: List<String> = emptyList(),

    @SerialName("content_score") val contentScore: Float = 0f,
    @SerialName("final_remarks") val finalRemarks: String? = null,
    @SerialName("overall_score") val overallScore: Float = 0f
)


fun parseGeminiResponse(jsonString: String): GeminiResponse? {
    return try {
        // Initialize the JSON parser
        val json = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
        // Parse the string into the GeminiResponse data class
        json.decodeFromString<GeminiResponse>(jsonString)
    } catch (e: JSONException) {
        // Handle JSON parsing errors
        Log.e("log1","Error parsing JSON: ${e.message}")
        null
    } catch (e: Exception) {
        // Handle other exceptions (e.g., network issues, invalid response structure)
        Log.e("log1","An error occurred: ${e.message}")
        null
    }
}
