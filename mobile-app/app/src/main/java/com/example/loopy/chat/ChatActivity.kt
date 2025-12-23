package com.example.loopy.chat

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.loopy.MainActivity
import com.example.loopy.R
import com.example.loopy.chat.scripts.ChatCaller
import com.example.loopy.data.DataActivity
import com.example.loopy.devicemanager.DeviceManagerActivity
import com.example.loopy.profile.ProfileActivity
import com.example.loopy.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class ChatActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val chatContainer = findViewById<LinearLayout>(R.id.chatContainer)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val messageInputText = findViewById<EditText>(R.id.inputMessage)

        val userId = SessionManager.currentUserId!!

        submitButton.setOnClickListener {
            val testo = messageInputText.text.toString().trim()
            if (testo.isNotEmpty()) {
                addMessage(testo, true, chatContainer, scrollView)
                lifecycleScope.launch {
                    val agentResponse = ChatCaller.run(testo, userId)
                    addMessage(agentResponse, false, chatContainer, scrollView)
                }
                messageInputText.text?.clear()
            }

        }

        /*------------------TASTI NAVBAR--------------------*/
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        bottomNavBar.selectedItemId = R.id.nav_chatbot

        bottomNavBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chatbot -> {
                    true
                }

                R.id.nav_data -> {
                    val intent = Intent(this@ChatActivity, DataActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_home -> {
                    val intent = Intent(this@ChatActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_dm -> {
                    val intent = Intent(this@ChatActivity, DeviceManagerActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_profile -> {
                    val intent = Intent(this@ChatActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        bottomNavBar.selectedItemId = R.id.nav_chatbot
    }

    private fun addMessage(testo: String, isSentByUser: Boolean, chatContainer: LinearLayout, scrollView: ScrollView) {
        val textViewMessaggio = TextView(this)
        textViewMessaggio.text = testo

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (isSentByUser) Gravity.END else Gravity.START
            bottomMargin = 16
        }
        textViewMessaggio.layoutParams = layoutParams
        textViewMessaggio.setPadding(24, 16, 24, 16)

        if (isSentByUser) {
            textViewMessaggio.background = ContextCompat.getDrawable(this, R.drawable.user_message_text_background)
            textViewMessaggio.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        } else {
            textViewMessaggio.background = ContextCompat.getDrawable(this, R.drawable.agent_message_text_background)
            textViewMessaggio.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        }

        chatContainer.addView(textViewMessaggio)

        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
}