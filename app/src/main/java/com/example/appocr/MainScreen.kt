package com.example.appocr

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.appocr.viewmodel.CameraViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@SuppressLint("PermissionLaunchedDuringComposition", "MutableCollectionMutableState")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: CameraViewModel, navController: NavController) {
    val cameraPermission = rememberPermissionState(
        Manifest.permission.CAMERA
    )
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(), verticalArrangement = Arrangement.Top
    ) {
        var ocrValue = viewModel.selectedLabel.value
        Log.d("TAG", "MainScreen: $ocrValue")
        Text(text = "OCR")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = ocrValue,
                onValueChange = { viewModel.selectedLabel.value = it },
                enabled = true,
                placeholder = { Text(text = "OCR text") })
            Button(onClick = {
                if (cameraPermission.status.isGranted) {
                    navController.navigate(Navigation.CameraScreen.rute)
                } else {
                    cameraPermission.launchPermissionRequest()
                }
            }, modifier = Modifier.height(TextFieldDefaults.MinHeight)) {
                Text(text = "Scan")
            }
        }
    }
}