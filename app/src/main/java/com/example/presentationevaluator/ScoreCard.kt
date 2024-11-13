package com.example.presentationevaluator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentationevaluator.ui.theme.PresentationEvaluatorTheme
import kotlinx.coroutines.delay


@Composable
fun ScoreCard(state: GeminiResponse, primary: Color){
    val fadeWhite = remember {
        Color(255, 255, 255, 32)
    }
    Column(modifier = Modifier
        .padding(horizontal = 20.dp)
        .fillMaxSize()
    ) {
        val keys = remember {
            mutableStateListOf(true, false, false, false, false, false, false, false, false)
        }
        LaunchedEffect(Unit) {
            keys.forEach {
                delay(300L)
                keys[keys.indexOf(it)] = true
            }
        }

        AnimatedVisibility(visible = keys[0]) {
            Column{
                Header(primary)
                val calculatedScore = remember {
                    (state.wpmScore + state.grammaticalScore + state.stutterScore + state.contentScore) / 4f
                }
                OverAllScore(
                    aiScore = state.overallScore.toString(),
                    calculatedScore = calculatedScore.toString(),
                    primary = primary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(visible = keys[1]) {
            Column(
                modifier = Modifier
                    .background(fadeWhite, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(255, 255, 255, 79), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                HeaderWithScore(header = "Words Per Minute:", score = state.wpmScore.toString(), value = state.wpm.toString(),primary= primary)
                Remarks(text = state.wpmRemarks.toString())
            }

        }

        Spacer(modifier = Modifier.height(20.dp))
        AnimatedVisibility(visible = keys[2]) {
            Column(
                modifier = Modifier
                    .background(fadeWhite, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(255, 255, 255, 79), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                HeaderWithScore(header = "Stutters/Redundancy:", score = state.stutterScore.toString(), value = state.numberOfStutters.toString(),primary= primary)
                Remarks(text = state.stutterRemarks.toString())
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedVisibility(visible = keys[3]) {
            Column(
                modifier = Modifier
                    .background(fadeWhite, RoundedCornerShape(10.dp))
                    .border(1.dp, Color(255, 255, 255, 79), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                HeaderWithScore(header = "Grammatical errors noticed:", score = state.grammaticalScore.toString(), value = state.numberOfGrammaticalErrors.toString(),primary= primary)
                Remarks(text = state.grammaticalRemarks.toString())
            }

        }
        Spacer(modifier = Modifier.height(20.dp))


        if(state.listOfTopicsCovered.isNotEmpty()){
            AnimatedVisibility(visible = keys[4]) {
                Column{
                    Text(
                        text = "Topics Covered",
                        fontSize = 20.sp,
                        color = primary,
                        fontWeight = FontWeight.Bold
                    )
                    state.listOfTopicsCovered.forEach {
                        Text(
                            text = "\u2022 $it",
                            fontSize = 15.sp,
                            color = Color(255, 255, 255, 201),
                        )
                    }
                }

            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        state.shortSummary?.let {
            AnimatedVisibility(visible = keys[5]) {
                Column{
                    Text(
                        text = "Summary",
                        fontSize = 20.sp,
                        color = primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = it, fontSize = 15.sp, color = Color(255, 255, 255, 201))
                }

            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if(state.listOfPossibleImprovementsInPresentation.isNotEmpty()){
            AnimatedVisibility(visible = keys[6]) {
                Column{
                    Text(
                        text = "What to focus on",
                        fontSize = 20.sp,
                        color = primary,
                        fontWeight = FontWeight.Bold
                    )
                    state.listOfPossibleImprovementsInPresentation.forEach {
                        Text(
                            text = "\u2022 $it",
                            fontSize = 15.sp,
                            color = Color(255, 255, 255, 201),
                        )
                    }
                }

            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        state.whatCanImproveInContent?.let {
            AnimatedVisibility(visible = keys[7]) {
                Column{
                    Text(
                        text = "How can we improve content",
                        fontSize = 20.sp,
                        color = primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = it, fontSize = 15.sp, color = Color(255, 255, 255, 201))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        state.finalRemarks?.let {
            AnimatedVisibility(visible = keys[8]) {
                Text(text = it, fontSize = 15.sp, color = Color(255, 255, 255, 201))
            }
        }


    }

}

@Composable
fun OverAllScore(aiScore: String, calculatedScore: String, primary: Color){
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "OverAll Score:", fontSize = 25.sp, color = primary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = "$calculatedScore/5.0", fontSize = 25.sp, color = primary, fontWeight = FontWeight.Bold)
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Score by AI:", fontSize = 12.sp, color = Color(255, 255, 255, 185), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = "$aiScore/5.0", fontSize = 12.sp, color = Color(255, 255, 255, 185), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Remarks(text: String){
    Text(
        text = text,
        fontSize = 15.sp,
        color = Color(255, 255, 255, 185),
        modifier = Modifier.padding(5.dp)
    )
}

@Composable
fun HeaderWithScore(header: String, score: String, value:String = "", primary: Color){
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$header $value",
            fontSize = 15.sp,
            color = Color(255, 255, 255, 185),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$score/5.0",
            fontSize = 15.sp,
            color = primary
        )
        Icon(imageVector = Icons.Filled.Star, contentDescription = header, modifier = Modifier.size(20.dp))
    }
}


@Preview(showBackground = false, showSystemUi = true)
@Composable
fun OverAllScorePreview(){
    val testGeminiResponse = GeminiResponse(
        wpm = 120,
        wpmRemarks = "Good speed but could be a bit slower.",
        wpmScore = 8.5f,

        numberOfStutters = 3,
        stutterRemarks = "Minimal stuttering; overall flow is smooth.",
        stutterScore = 7.8f,

        numberOfGrammaticalErrors = 2,
        grammaticalRemarks = "Minor grammatical errors.",
        grammaticalScore = 9.2f,

        listOfTopicsCovered = listOf("Introduction", "Main Topic", "Conclusion"),
        whatCanImproveInContent = "Provide more examples to clarify complex points.",
        listOfPossibleImprovementsInPresentation = listOf(
            "Maintain consistent eye contact",
            "Slow down at key points for emphasis"
        ),

        contentScore = 8.7f,
        finalRemarks = "Solid presentation with room for improvement in engagement.",
        overallScore = 8.3f
    )
    PresentationEvaluatorTheme {
        Surface(onClick = { /*TODO*/ }, color = Color.Black) {
            ScoreCard(state = testGeminiResponse, primary = Color.Yellow)
        }
    }
}