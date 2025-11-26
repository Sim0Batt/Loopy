package com.example.loopy.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.loopy.R
import com.example.loopy.core.BaseActivity
import com.example.loopy.utils.SessionManager

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val userId = SessionManager.currentUserId!!
    }
}
