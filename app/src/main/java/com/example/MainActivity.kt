package com.example

import android.content.Context
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

    // የመረጡትን ሰርቨር ሴቭ ማድረጊያ
    fun saveSelectedServer(context: Context, serverId: String) {
        val prefs = context.getSharedPreferences("AlphaVPN_Prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("SELECTED_SERVER_ID", serverId).apply()
    }

    // አፑ ሲከፈት የተመረጠውን ሰርቨር ማነቢያ
    fun getSavedServer(context: Context): String {
        val prefs = context.getSharedPreferences("AlphaVPN_Prefs", Context.MODE_PRIVATE)
        return prefs.getString("SELECTED_SERVER_ID", "AUTO_SELECT") ?: "AUTO_SELECT"
    }
}
