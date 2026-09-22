package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.SoundOperatorApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initWebViewCacheDirectories()
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        SoundOperatorApp()
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

