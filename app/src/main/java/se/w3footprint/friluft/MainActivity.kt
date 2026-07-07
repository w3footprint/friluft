package se.w3footprint.friluft

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import se.w3footprint.friluft.presentation.common.theme.FriLuftTheme
import se.w3footprint.friluft.presentation.navigation.FriLuftNavGraph

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FriLuftTheme {
                val navController = rememberNavController()
                FriLuftNavGraph(navController = navController)
            }
        }
    }
}
