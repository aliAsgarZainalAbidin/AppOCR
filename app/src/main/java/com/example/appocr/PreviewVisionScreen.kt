package com.example.appocr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.appocr.viewmodel.CameraViewModel
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.protobuf.ByteString
import java.io.FileInputStream
import java.io.IOException


@OptIn(ExperimentalTextApi::class)
@Composable
fun PreviewVisionScreen(cameraViewModel: CameraViewModel, navController: NavController) {
    val context = LocalContext.current
    val uri by remember { mutableStateOf(cameraViewModel.uri.value!!) }
    val path by remember { mutableStateOf(cameraViewModel.photoPath.value!!) }
    var listBoundingBox by remember { mutableStateOf<List<Text.TextBlock>?>(null) }
    val image = InputImage.fromFilePath(context, uri)
    val imageBitmap by remember { mutableStateOf(image.bitmapInternal?.asImageBitmap()) }
    val selectedLabel = remember { mutableStateListOf<String>() }
    val CONST_OFFSET = 1.22f
//    val CONST_OFFSET = 1f
    detectDocumentText(path)

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

@Throws(IOException::class)
fun detectDocumentText(filePath: String?) {
    val requests: MutableList<AnnotateImageRequest> = ArrayList()
    val imgBytes: ByteString = ByteString.readFrom(FileInputStream(filePath))
    val img: Image = Image.newBuilder().setContent(imgBytes).build()
    val feat: Feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build()

    val request: AnnotateImageRequest =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
    requests.add(request)


//    Log.d(TAG, "detectDocumentText: ${System.getenv("GOOGLE_APPLICATION_CREDENTIALS")}")
//    val credentialsPath = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS")
//    val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsPath))
//    val settings = ImageAnnotatorSettings
//        .newBuilder()
//        .setCredentialsProvider { credentials }
//        .build()

//    val imageVision = ImageAnnotatorClient.create(settings)
    val imageVision = ImageAnnotatorClient.create()
    val response = imageVision.batchAnnotateImages(requests)
    val responses = response.responsesList

    for (res in responses) {
        if (res.hasError()) {
            System.out.format("Error: %s%n", res.getError().getMessage())
            return
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        val annotation: TextAnnotation = res.getFullTextAnnotation()
        for (page in annotation.getPagesList()) {
            var pageText = ""
            for (block in page.getBlocksList()) {
                var blockText = ""
                for (para in block.getParagraphsList()) {
                    var paraText = ""
                    for (word in para.getWordsList()) {
                        var wordText = ""
                        for (symbol in word.getSymbolsList()) {
                            wordText = wordText + symbol.getText()
                            System.out.format(
                                "Symbol text: %s (confidence: %f)%n",
                                symbol.getText(), symbol.getConfidence()
                            )
                        }
                        System.out.format(
                            "Word text: %s (confidence: %f)%n%n", wordText, word.getConfidence()
                        )
                        paraText = String.format("%s %s", paraText, wordText)
                    }
                    // Output Example using Paragraph:
                    println("%nParagraph: %n$paraText")
                    System.out.format(
                        "Paragraph Confidence: %f%n",
                        para.getConfidence()
                    )
                    blockText = blockText + paraText
                }
                pageText = pageText + blockText
            }
        }
        println("%nComplete annotation:")
        System.out.println(annotation.getText())
    }
}

@Throws(IOException::class)
fun detectDocumentTexts(filePath: String?) {
    val requests: MutableList<AnnotateImageRequest> = ArrayList()
    val imgBytes = ByteString.readFrom(FileInputStream(filePath))
    val img = Image.newBuilder().setContent(imgBytes).build()
    val feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build()
    val request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
    requests.add(request)
    ImageAnnotatorClient.create().use { client ->
        val response = client.batchAnnotateImages(requests)
        val responses = response.responsesList
        client.close()
        for (res in responses) {
            if (res.hasError()) {
                System.out.format("Error: %s%n", res.error.message)
                return
            }

            // For full list of available annotations, see http://g.co/cloud/vision/docs
            val annotation = res.fullTextAnnotation
            for (page in annotation.pagesList) {
                var pageText = ""
                for (block in page.blocksList) {
                    var blockText = ""
                    for (para in block.paragraphsList) {
                        var paraText = ""
                        for (word in para.wordsList) {
                            var wordText = ""
                            for (symbol in word.symbolsList) {
                                wordText = wordText + symbol.text
                                System.out.format(
                                    "Symbol text: %s (confidence: %f)%n",
                                    symbol.text, symbol.confidence
                                )
                            }
                            System.out.format(
                                "Word text: %s (confidence: %f)%n%n", wordText, word.confidence
                            )
                            paraText = String.format("%s %s", paraText, wordText)
                        }
                        // Output Example using Paragraph:
                        println("%nParagraph: %n$paraText")
                        System.out.format(
                            "Paragraph Confidence: %f%n",
                            para.confidence
                        )
                        blockText = blockText + paraText
                    }
                    pageText = pageText + blockText
                }
            }
            println("%nComplete annotation:")
            System.out.println(annotation.text)
        }
    }
}