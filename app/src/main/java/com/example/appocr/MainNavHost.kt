package com.example.appocr

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appocr.viewmodel.CameraViewModel

enum class Navigation(var rute: String) {
    MainScreen("main_screen"),
    CameraScreen("camera_screen"),
    PreviewScreen("preview_screen")
}

@Composable
fun MainNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    cameraViewModel: CameraViewModel,
    startDesination: String = Navigation.MainScreen.rute
) {
    NavHost(
        navController = navController,
        startDestination = startDesination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Navigation.MainScreen.rute) {
            MainScreen(viewModel = cameraViewModel, navController)
        }
        composable(Navigation.CameraScreen.rute) {
            CameraScreen(cameraViewModel = cameraViewModel, navController)
        }
        composable(Navigation.PreviewScreen.rute){
            PreviewScreen(cameraViewModel = cameraViewModel, navController = navController)
        }
    }
}