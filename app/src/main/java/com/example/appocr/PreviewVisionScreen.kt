package com.example.appocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.example.appocr.util.PackageManagerUtils
import com.example.appocr.viewmodel.CameraViewModel
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequest
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import com.google.api.services.vision.v1.model.AnnotateImageRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse
import com.google.api.services.vision.v1.model.EntityAnnotation
import com.google.api.services.vision.v1.model.Feature
import com.google.api.services.vision.v1.model.Image
import com.google.cloud.vision.v1.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


@RequiresApi(Build.VERSION_CODES.P)
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
//    detectDocumentText(path)
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source)
    val request = prepareAnnotationRequest(bitmap, context).execute()
    val result = convertResponseToString(request)
    Log.d("TAG", "PreviewVisionScreen: $result")

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

//@Throws(IOException::class)
//fun detectDocumentText(filePath: String?) {
//    val requests: MutableList<AnnotateImageRequest> = ArrayList()
//    val imgBytes: ByteString = ByteString.readFrom(FileInputStream(filePath))
//    val img: Image = Image.newBuilder().setContent(imgBytes).build()
//    val feat: Feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build()
//
//    val request: AnnotateImageRequest =
//        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build()
//    requests.add(request)
//
//
////    Log.d(TAG, "detectDocumentText: ${System.getenv("GOOGLE_APPLICATION_CREDENTIALS")}")
//    val credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
//    Log.d("TAG", "detectDocumentText: $credentialsPath")
//    val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsPath))
//    val settings = ImageAnnotatorSettings
//        .newBuilder()
//        .setCredentialsProvider { credentials }
//        .build()
//
//    val imageVision = ImageAnnotatorClient.create(settings)
////    val imageVision = ImageAnnotatorClient.create()
//    val response = imageVision.batchAnnotateImages(requests)
//    val responses = response.responsesList
//
//    for (res in responses) {
//        if (res.hasError()) {
//            System.out.format("Error: %s%n", res.getError().getMessage())
//            return
//        }
//
//        // For full list of available annotations, see http://g.co/cloud/vision/docs
//        val annotation: TextAnnotation = res.getFullTextAnnotation()
//        for (page in annotation.getPagesList()) {
//            var pageText = ""
//            for (block in page.getBlocksList()) {
//                var blockText = ""
//                for (para in block.getParagraphsList()) {
//                    var paraText = ""
//                    for (word in para.getWordsList()) {
//                        var wordText = ""
//                        for (symbol in word.getSymbolsList()) {
//                            wordText = wordText + symbol.getText()
//                            System.out.format(
//                                "Symbol text: %s (confidence: %f)%n",
//                                symbol.getText(), symbol.getConfidence()
//                            )
//                        }
//                        System.out.format(
//                            "Word text: %s (confidence: %f)%n%n", wordText, word.getConfidence()
//                        )
//                        paraText = String.format("%s %s", paraText, wordText)
//                    }
//                    // Output Example using Paragraph:
//                    println("%nParagraph: %n$paraText")
//                    System.out.format(
//                        "Paragraph Confidence: %f%n",
//                        para.getConfidence()
//                    )
//                    blockText = blockText + paraText
//                }
//                pageText = pageText + blockText
//            }
//        }
//        println("%nComplete annotation:")
//        System.out.println(annotation.getText())
//    }
//}

@Throws(IOException::class)
private fun prepareAnnotationRequest(bitmap: Bitmap, context: Context): Vision.Images.Annotate {
    val ANDROID_CERT_HEADER = "X-Android-Cert"
    val ANDROID_PACKAGE_HEADER = "X-Android-Package"
    val MAX_LABEL_RESULTS = 10
    val MAX_DIMENSION = 1200

    val transport = NetHttpTransport()
    val jsonFactory = GsonFactory.getDefaultInstance()
    val requestInitializer: VisionRequestInitializer =
        object : VisionRequestInitializer(BuildConfig.API_KEY) {
            /**
             * We override this so we can inject important identifying fields into the HTTP
             * headers. This enables use of a restricted cloud platform API key.
             */
            @Throws(IOException::class)
            override fun initializeVisionRequest(visionRequest: VisionRequest<*>) {
                super.initializeVisionRequest(visionRequest)
                val packageName: String = context.packageName
                visionRequest.requestHeaders[ANDROID_PACKAGE_HEADER] = packageName
                val sig: String? = PackageManagerUtils.getSignature(context.packageManager, packageName)
                visionRequest.requestHeaders[ANDROID_CERT_HEADER] = sig
            }
        }
    val visionBuilder = Vision.Builder(transport, jsonFactory, null)
    visionBuilder.setVisionRequestInitializer(requestInitializer)
    val vision = visionBuilder.build()
    val batchAnnotateImagesRequest = BatchAnnotateImagesRequest()
    batchAnnotateImagesRequest.setRequests(object : ArrayList<AnnotateImageRequest?>() {
        init {
            val annotateImageRequest = AnnotateImageRequest()

            // Add the image
            val base64EncodedImage = Image()
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            val byteArrayOutputStream = ByteArrayOutputStream()
            val imageBytes = byteArrayOutputStream.toByteArray()

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes)
            annotateImageRequest.setImage(base64EncodedImage)

            // add the features we want
            annotateImageRequest.setFeatures(object : ArrayList<Feature?>() {
                init {
                    val labelDetection = Feature()
                    labelDetection.setType("LABEL_DETECTION")
                    labelDetection.setMaxResults(MAX_LABEL_RESULTS)
                    add(labelDetection)
                }
            })

            // Add the list of one thing to the request
            add(annotateImageRequest)
        }
    })

    val annotateRequest = vision.images().annotate(batchAnnotateImagesRequest)
    // Due to a bug: requests to Vision API containing large images fail when GZipped.
    // Due to a bug: requests to Vision API containing large images fail when GZipped.
    annotateRequest.disableGZipContent = true
    Log.d("TAG", "created Cloud Vision request object, sending request")

    return annotateRequest
}

private fun convertResponseToString(response: BatchAnnotateImagesResponse): String? {
    val message = StringBuilder("I found these things:\n\n")
    val labels: List<EntityAnnotation> = response.getResponses().get(0).getLabelAnnotations()
    if (labels != null) {
        for (label in labels) {
            message.append(
                java.lang.String.format(
                    Locale.US,
                    "%.3f: %s",
                    label.getScore(),
                    label.getDescription()
                )
            )
            message.append("\n")
        }
    } else {
        message.append("nothing")
    }
    return message.toString()
}