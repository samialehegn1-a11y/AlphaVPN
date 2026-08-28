package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AlphaVpnTheme
import com.example.vpn.ui.screens.MainScaffold
import com.example.vpn.viewmodel.VpnViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: VpnViewModel = viewModel()
            AlphaVpnTheme(darkTheme = true) {
                MainScaffold(viewModel = viewModel)
            }
        }
    }
}
