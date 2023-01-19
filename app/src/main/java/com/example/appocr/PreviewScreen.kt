package com.example.appocr

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.util.Xml
import android.view.MotionEvent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.VectorProperty
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.appocr.util.drawClickableText
import com.example.appocr.viewmodel.CameraViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class)
@SuppressLint("MutableCollectionMutableState")
@Composable
fun PreviewScreen(cameraViewModel: CameraViewModel, navController: NavController) {
    val context = LocalContext.current
    val uri by remember { mutableStateOf(cameraViewModel.uri.value!!) }
    var listBoundingBox by remember { mutableStateOf<List<Text.TextBlock>?>(null) }
    val image = InputImage.fromFilePath(context, uri)
    val imageBitmap by remember { mutableStateOf(image.bitmapInternal?.asImageBitmap()) }
    val recognizer =
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val selectedLabel = remember { mutableStateListOf<String>() }
    val CONST_OFFSET = 1.22f
//    val CONST_OFFSET = 1f

    recognizer.process(image).addOnCompleteListener { listBoundingBox = it.result.textBlocks }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val (ivImage, boxContainer, canvas, bottomBar) = createRefs()
        var textMeasure = rememberTextMeasurer()

        Image(
            bitmap = imageBitmap!!,
            contentDescription = "Image",
            modifier = Modifier
                .constrainAs(ivImage) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(bottomBar.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                },
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        Canvas(modifier = Modifier
            .constrainAs(canvas) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(bottomBar.top)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    Log.d(TAG, "PreviewScreen: on Tap ${offset.x} ${offset.y}")
                    listBoundingBox?.forEachIndexed { index, it ->
//                        textBlock.lines.forEach {
//                            it.boundingBox?.apply {
//                                if (offset.x >= left && offset.x <= right &&
//                                    offset.y >= top.times(CONST_OFFSET) && offset.y <= bottom.times(CONST_OFFSET)
//                                ) {
//                                    if (!selectedLabel.contains(it.text)) {
//                                        selectedLabel.add(it.text)
//                                    } else {
//                                        selectedLabel.remove(it.text)
//                                    }
//                                }
//                            }
//                        }
                        it.boundingBox?.apply {
                            if (offset.x >= left && offset.x <= right &&
                                offset.y >= top.times(CONST_OFFSET) && offset.y <= bottom.times(CONST_OFFSET)
                            ) {
                                if (!selectedLabel.contains(it.text)) {
                                    selectedLabel.add(it.text)
                                } else {
                                    selectedLabel.remove(it.text)
                                }
                            }
                        }
                    }
                }
            }, onDraw = {
            listBoundingBox?.forEachIndexed { index, it ->
//                textBlock.lines.forEach {
//                    it.boundingBox?.apply {
//                        val xRect = it.boundingBox?.left?.toFloat() ?: 0f
//                        val yRect = it.boundingBox?.top?.toFloat()?.times(CONST_OFFSET) ?: 0f
////                        val yRect = it.boundingBox?.top?.toFloat()?.times(CONST_OFFSET) ?: 0f
//
//                        val offset = Offset(xRect, yRect)
////                        val offset = Offset(0f,0f)
//                        drawRect(
//                            Color.Green,
//                            offset,
//                            Size(width().times(CONST_OFFSET), height().times(CONST_OFFSET)),
////                            Size(width().toFloat(), height().toFloat()),
//                            1f,
//                            style = Stroke(2f)
//                        )
//
//                        val annotText = textMeasure.measure(
//                            AnnotatedString(
//                                it.text,
//                                SpanStyle(
//                                    fontSize = height().div(2.5).sp,
//                                    background = if (selectedLabel.contains(it.text)) Color.White.copy(
//                                        0.45f
//                                    ) else Color.Transparent
//                                )
//                            )
//                        )
//                        drawText(annotText, Color.Blue, offset)
//                    }
//                }
                it.boundingBox?.apply {
                    val xRect = it.boundingBox?.left?.toFloat() ?: 0f
                    val yRect = it.boundingBox?.top?.toFloat()?.times(CONST_OFFSET) ?: 0f
//                        val yRect = it.boundingBox?.top?.toFloat()?.times(CONST_OFFSET) ?: 0f

                    val offset = Offset(xRect, yRect)
//                        val offset = Offset(0f,0f)
                    drawRect(
                        Color.Green,
                        offset,
                        Size(width().times(CONST_OFFSET), height().times(CONST_OFFSET)),
//                            Size(width().toFloat(), height().toFloat()),
                        1f,
                        style = Stroke(2f)
                    )

                    val annotText = textMeasure.measure(
                        AnnotatedString(
                            it.text,
                            SpanStyle(
                                fontSize = height().div(2.5).sp,
                                background = if (selectedLabel.contains(it.text)) Color.White.copy(
                                    0.45f
                                ) else Color.Transparent
                            )
                        )
                    )
                    drawText(annotText, Color.Blue, offset)
                }
            }
        })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 68.dp)
                .background(Color.Black.copy(0.54f))
                .constrainAs(bottomBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                navController.navigate(Navigation.CameraScreen.rute)
            }) {
                Text("Cancel")
            }
            Button(onClick = {
                cameraViewModel.setSelectedLabel(selectedLabel.toList())
                navController.navigate(
                    route = Navigation.MainScreen.rute,
                    navOptions = NavOptions.Builder().setPopUpTo(Navigation.MainScreen.rute, true)
                        .build()
                )
            }) {
                Text("Save")
            }
        }
    }
}