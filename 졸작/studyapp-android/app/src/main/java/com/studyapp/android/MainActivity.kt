package com.studyapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.studyapp.android.navigation.AppNavigation
import com.studyapp.android.ui.theme.StudyappTheme
import androidx.core.view.WindowCompat
import com.studyapp.android.util.PreferenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //  로그인 상태 확인
        val isLoggedIn = PreferenceManager.isLoggedIn(this)
        println("🟢 MainActivity - isLoggedIn=$isLoggedIn, userId=${StudyApp.currentUserId}")

        setContent {
            StudyappTheme {
                AppNavigation()
            }
        }
    }
}

