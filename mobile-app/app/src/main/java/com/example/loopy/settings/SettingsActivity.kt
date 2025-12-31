package com.example.loopy.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.loopy.R
import com.example.loopy.core.BaseActivity
import com.example.loopy.login.LoginActivity
import com.example.loopy.login.RegisterActivity
import com.example.loopy.profile.ProfileActivity
import com.example.loopy.utils.SessionManager
import androidx.core.net.toUri

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val userId = SessionManager.currentUserId!!
        val editAccountButton = findViewById<Button>(R.id.EditAccountButton)
        val logoutButton = findViewById<Button>(R.id.LogoutButton)
        val functionalSheetButton = findViewById<Button>(R.id.FunctionalSheet)
        val technicalSheetButton = findViewById<Button>(R.id.TechnicalSheet)
        val githubButton = findViewById<Button>(R.id.GithubButton)




        editAccountButton.setOnClickListener {
            val intent = Intent(this@SettingsActivity, EditAccountActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        functionalSheetButton.setOnClickListener {
            val url = "https://github.com/Sim0Batt/Loopy/blob/main/documentation/Functional_Sheet_Loopy.pdf"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = url.toUri()

            startActivity(intent)
        }

        technicalSheetButton.setOnClickListener {
            val url = "https://github.com/Sim0Batt/Loopy/blob/main/documentation/Technical_Sheet_Loopy.pdf"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = url.toUri()

            startActivity(intent)
        }

        githubButton.setOnClickListener {
            val url = "https://github.com/Sim0Batt/Loopy"

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = url.toUri()

            startActivity(intent)
        }


    }
}
