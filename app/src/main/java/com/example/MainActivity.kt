package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SoundOperatorApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SoundOperatorViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initWebViewCacheDirectories()
    requestNotificationPermission()
    enableEdgeToEdge()
    setContent {
      val soundViewModel: SoundOperatorViewModel = viewModel()
      val activeThemeId by soundViewModel.currentThemeId.collectAsState()
      MyApplicationTheme(themeId = activeThemeId) {
        SoundOperatorApp(viewModel = soundViewModel)
      }
    }
  }

  private fun requestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
      }
    }
  }

  private fun initWebViewCacheDirectories() {
    try {
      val paths = listOf(
        "WebView",
        "WebView/Default",
        "WebView/Default/HTTP Cache",
        "WebView/Default/HTTP Cache/Code Cache",
        "WebView/Default/HTTP Cache/Code Cache/js",
        "WebView/Default/HTTP Cache/Code Cache/wasm"
      )
      paths.forEach { path ->
        val dir = java.io.File(cacheDir, path)
        if (!dir.exists()) {
          dir.mkdirs()
        }
      }
    } catch (_: Exception) {}
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Sound Operator $name", modifier = modifier)
}

