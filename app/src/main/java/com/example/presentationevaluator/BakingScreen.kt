package com.example.presentationevaluator

import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data object ProjectColors{
    val colorList = listOf(
        Color(203, 229, 62, 255),
        Color(0, 205, 172, 255),
        Color(255, 255, 255, 255),
        Color(165, 191, 254, 255),
        Color(193, 103, 255, 255),
        Color(247, 57, 143, 255),
        Color(247, 165, 28, 255),
    )

}

@Composable
fun BakingScreen(
    resultLauncher: ActivityResultLauncher<Intent>,
    bakingViewModel: BakingViewModel = viewModel()
) {
    var result by rememberSaveable { mutableStateOf("") }
    var showRaw by rememberSaveable { mutableStateOf(false) }
    var showInfoCard by rememberSaveable { mutableStateOf(false) }
    var hideButton by rememberSaveable { mutableStateOf(false) }
    var currentColor by rememberSaveable { mutableIntStateOf(0) }
    var duration by rememberSaveable { mutableIntStateOf(2000) }
    val targetColor by remember(currentColor){ mutableStateOf(ProjectColors.colorList[currentColor]) }
    val nextColor = remember {
        {
            Log.d("log1", "called")
            currentColor = (currentColor+1)%ProjectColors.colorList.size
        }
    }
    val primary by animateColorAsState(finishedListener = {
        if(duration!=2000){
            nextColor()
        }
    },targetValue = targetColor, label = "color", animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing))
    val context = LocalContext.current
    val limit = 1000f
    val interactionSource = remember { MutableInteractionSource() }


    val transition = rememberInfiniteTransition(label = "shimmer")
    val progressAnimated by transition.animateFloat(
        initialValue = -limit,
        targetValue = limit,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "wallpaper"
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            primary.copy(alpha = 0.3f)
        ),
        start = Offset(0f, progressAnimated),
        end = Offset(Float.POSITIVE_INFINITY, 0f),
    )
    Box(modifier = Modifier
        .fillMaxSize()
        .background(brush)
    ) {
        Column(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(text = "Pre-Eval", fontSize = 17.sp, color = Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier
                    .background(primary, RoundedCornerShape(0.dp, 30.dp, 30.dp, 0.dp))
                    .padding(20.dp, 5.dp)
                    .clickable(interactionSource, indication = null) {
                        nextColor()
                    })
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Outlined.Info, contentDescription = "info", modifier = Modifier
                    .padding(end = 20.dp)
                    .size(20.dp)
                    .clickable { showInfoCard = true }, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(bakingViewModel.videoThumbNail!=null) {
                AsyncImage(
                    model = bakingViewModel.videoThumbNail,
                    contentDescription = "image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .aspectRatio(16 / 9f)
                )
            }

            if (bakingViewModel.uiState is UiState.Loading) {
                if(bakingViewModel.collectingResources.equals("Collecting resources...")){
                    TextWithLoader(text = bakingViewModel.collectingResources ?: "", isLoading = true,primary= primary)
                } else {
                    hideButton = true
                    TextWithLoader(text = bakingViewModel.collectingResources ?: "", isLoading = false,primary= primary)
                    if(bakingViewModel.processingData!=null){
                        if(bakingViewModel.processingData.equals("Processing Data...")){
                            TextWithLoader(text = bakingViewModel.processingData ?: "", isLoading = true,primary= primary)
                        } else {
                            TextWithLoader(text = bakingViewModel.processingData ?: "", isLoading = false,primary= primary)
                            if(bakingViewModel.generatingResponse!=null){
                                if(bakingViewModel.generatingResponse.equals("Generating response...")){
                                    TextWithLoader(text = bakingViewModel.generatingResponse ?: "", isLoading = true,primary= primary)
                                } else {
                                    TextWithLoader(text = bakingViewModel.generatingResponse ?: "", isLoading = false, hideLine = true,primary= primary)
                                }
                            }
                        }
                    }
                }
            } else {
                hideButton = false
                var textColor = MaterialTheme.colorScheme.onSurface
                if (bakingViewModel.uiState is UiState.Error) {
                    textColor = MaterialTheme.colorScheme.error
                    result = (bakingViewModel.uiState as UiState.Error).errorMessage
                }

                if(result.isNotBlank()){
                    Text(
                        text = result,
                        textAlign = TextAlign.Start,
                        color = textColor,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                            .fillMaxSize()
                    )
                }

                if (bakingViewModel.uiState is UiState.Success){
                    ScoreCard(state = bakingViewModel.apiResponse,primary= primary)
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            if(bakingViewModel.rawResponseString.isNotBlank()){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.End)
                        .clickable { showRaw = true },
                ) {
                    Text(text = "click to see raw json response ", fontSize = 12.sp, color = Color.White)
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = "raw response", modifier = Modifier.size(15.dp), tint = Color.White)
                }
                Spacer(modifier = Modifier.height(100.dp))
            }

            if(showRaw){
                RawResponse(text = bakingViewModel.rawResponseString) {
                    showRaw = false
                }
            }
            if(showInfoCard){
                InfoCard(
                    onSetNewKey = {
                        bakingViewModel.setupModel(it).let {
                            Toast.makeText(context, "New key will now be used for response.", Toast.LENGTH_LONG).show()
                        }
                    },
                    primary=primary,
                    discoMode = {
                        duration = if(duration==2000) 500 else 2000
                        nextColor()
                    },
                    onDismiss = {
                        showInfoCard = false
                    }
                )
            }
        }

        if(!hideButton){
            Spacer(modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(200.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0, 0, 0, 255))
                    )
                ))
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = Color.Black),
                enabled = (bakingViewModel.uiState is UiState.Loading).not(),
                shape = RoundedCornerShape(10.dp),
                onClick = {
                    nextColor()
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).apply {
                        type = "video/*"
                    }
                    resultLauncher.launch(intent)
                    bakingViewModel.collectedVideoUrl = null
                    bakingViewModel.audioDuration = null
                    bakingViewModel.videoThumbNail = null
                    bakingViewModel.uiState = UiState.Loading
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(20.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Text(text = "Select Video Presentation", fontSize = 20.sp, modifier = Modifier.padding(10.dp))
            }
        }
        
        if(bakingViewModel.collectedVideoUrl==null){
            Column(
                modifier = Modifier
                    .padding(bottom = 150.dp)
                    .align(Alignment.BottomEnd)
                    .clickable(interactionSource, indication = null) {
                        nextColor()
                    }
            ) {
                ExtraLargeFontText(text = "Getting")
                ExtraLargeFontText(text = "Started")
                ExtraLargeFontText(text = "With")
                ExtraLargeFontText(text = "Evaluation")
            }
        }

    }

}

@Composable
fun ExtraLargeFontText(text: String){
    Text(
        text = text,
        fontSize = 70.sp,
        overflow = TextOverflow.Visible,
        maxLines = 1,
        fontWeight = FontWeight.ExtraBold,
        color = Color(255, 255, 255, 109),
        textAlign = TextAlign.Right,
        modifier = Modifier.fillMaxWidth()
    )

}


@Composable
fun InfoCard(
    primary: Color,
    discoMode: () -> Unit,
    onDismiss: () -> Unit,
    onSetNewKey: (String) -> Unit
){
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .background(Color(34, 34, 34, 255), RoundedCornerShape(20.dp))
                .padding(25.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "raw response", modifier = Modifier
                .size(25.dp)
                .align(Alignment.CenterHorizontally)
                .clickable { discoMode() },
                tint = primary)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "About Presentation Evaluator",
                fontSize = 18.sp,
                color = primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "1. Select a Video Presentation\n\n2. Let the Evaluator analyze the speech, content, fluency, grammar and more points about your presentation\n\n3. Get a score card based on how you did and Tips on how you can improve.",
                fontSize = 15.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))
            var key by remember{ mutableStateOf("") }
            OutlinedTextField(
                placeholder = { Text(text = "Enter your custom api key", color = primary) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onSetNewKey(key)
                    onDismiss()
                }),
                value = key,
                onValueChange = { key = it },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = primary.copy(alpha = 0.5f),
                    focusedBorderColor = primary
                )
            )
        }
    }
}



@Composable
fun TextWithLoader(text: String, isLoading: Boolean, hideLine: Boolean = false, primary: Color){


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = 20.dp),
    ) {
        if(isLoading){
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = primary,
                modifier = Modifier.size(15.dp),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = text,
                tint = primary,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = text, fontSize = 13.sp, color = primary)
    }
    if(!hideLine){
        var height by remember {
            mutableStateOf(0.dp)
        }
        val animatedHeight by animateDpAsState(targetValue = height, label = "bar")

        LaunchedEffect(isLoading) {
            if (!isLoading){
                height = 50.dp
            }
        }
        Spacer(modifier = Modifier
            .padding(start = 27.dp)
            .height(animatedHeight)
            .width(2.dp)
            .background(primary, RoundedCornerShape(50))
        )
    }

}



@Composable
fun Header(primary: Color){
    Text(
        text = "Evaluation Result:",
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        color = primary,
        modifier = Modifier.padding(vertical =  10.dp)
    )
}


@Composable
fun RawResponse(text: String, onDismiss: () -> Unit){
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .background(Color(34, 34, 34, 255), RoundedCornerShape(20.dp))
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}




