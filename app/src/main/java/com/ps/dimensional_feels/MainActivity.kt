package com.ps.dimensional_feels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.ps.dimensional_feels.data.database.ImageToUploadDao
import com.ps.dimensional_feels.navigation.Screen
import com.ps.dimensional_feels.navigation.SetupNavGraph
import com.ps.dimensional_feels.presentation.theme.DimensionalFeelsTheme
import com.ps.dimensional_feels.util.Constants.APP_ID
import com.ps.dimensional_feels.util.retryUploadingImageToFirebase
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var keepSplashOpened = true

    @Inject
    lateinit var imageToUploadDao: ImageToUploadDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { keepSplashOpened }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(applicationContext)
        setContent {
            DimensionalFeelsTheme {
                val navController = rememberNavController()
                SetupNavGraph(startDestinationRoute = getStartDestination(),
                    navController = navController,
                    onDataLoaded = { keepSplashOpened = false })
            }
        }
        cleanupCheck(scope = lifecycleScope, imageToUploadDao = imageToUploadDao)
    }
}


private fun cleanupCheck(
    scope: CoroutineScope, imageToUploadDao: ImageToUploadDao
) {
    scope.launch(Dispatchers.IO) {
        val result = imageToUploadDao.getAllImages()
        result.forEach { imageToUpload ->
            retryUploadingImageToFirebase(imageToUpload = imageToUpload, onSuccess = {
                scope.launch(Dispatchers.IO) {
                    imageToUploadDao.deleteImageToUpload(imageId = imageToUpload.id)
                }
            })
        }
    }
}

private fun getStartDestination(): String {
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route else Screen.Authentication.route
}