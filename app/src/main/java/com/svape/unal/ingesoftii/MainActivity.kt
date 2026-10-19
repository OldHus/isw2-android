package com.svape.unal.ingesoftii

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.svape.unal.ingesoftii.di.AppContainer
import com.svape.unal.ingesoftii.navigation.DeepLinkTarget
import com.svape.unal.ingesoftii.navigation.IngesoftNavHost
import com.svape.unal.ingesoftii.navigation.Routes
import com.svape.unal.ingesoftii.ui.theme.IngesoftiiApp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private var keepSplashOnScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        splashScreen.setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .setDuration(SPLASH_EXIT_DURATION_MS)
                .withEndAction { provider.remove() }
                .start()
        }

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        val appContainer = (application as AppApplication).appContainer
        handleNotificationIntent(intent, appContainer)

        setContent {
            IngesoftiiApp {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val route = backStackEntry?.destination?.route

                LaunchedEffect(route) {
                    if (route != null && route != Routes.Splash.route) keepSplashOnScreen = false
                }
                LaunchedEffect(Unit) {
                    delay(SPLASH_MAX_DURATION_MS)
                    keepSplashOnScreen = false
                }

                IngesoftNavHost(appContainer = appContainer, navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent, (application as AppApplication).appContainer)
    }

    override fun onResume() {
        super.onResume()
        getSystemService(NotificationManager::class.java)?.cancelAll()
    }

    private fun handleNotificationIntent(intent: Intent?, appContainer: AppContainer) {
        val type = intent?.getStringExtra("type") ?: return
        appContainer.pendingDeepLink.value = DeepLinkTarget.fromTypeKey(type)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private companion object {
        const val SPLASH_EXIT_DURATION_MS = 300L
        const val SPLASH_MAX_DURATION_MS = 4000L
    }
}