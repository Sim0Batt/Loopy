package com.example.loopy.chat

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loopy.R
import com.example.loopy.chat.scripts.ChatCaller
import kotlinx.coroutines.launch

class ChatActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_activity)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val chatContainer = findViewById<LinearLayout>(R.id.chatContainer)
        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        val messageInputText = findViewById<EditText>(R.id.inputMessage)

        submitButton.setOnClickListener {
            val testo = messageInputText.text.toString().trim()
            if (testo.isNotEmpty()) {
                addMessage(testo, true, chatContainer, scrollView)
                lifecycleScope.launch {
                    val agentResponse = ChatCaller().run(testo, "Simone")
                    addMessage(agentResponse, false, chatContainer, scrollView)
                }
                messageInputText.text?.clear()
            }

        }



    }

    private fun addMessage(testo: String, isSentByUser: Boolean, chatContainer: LinearLayout, scrollView: ScrollView) {
        // Crea una nuova TextView programmaticamente
        val textViewMessaggio = TextView(this)
        textViewMessaggio.text = testo

        // Imposta lo stile (padding, margini, ecc.)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            // Se il messaggio è inviato, allinealo a destra. Altrimenti a sinistra.
            gravity = if (isSentByUser) Gravity.END else Gravity.START
            bottomMargin = 16 // Un po' di spazio tra i messaggi
        }
        textViewMessaggio.layoutParams = layoutParams
        textViewMessaggio.setPadding(24, 16, 24, 16)

        // Imposta lo sfondo e il colore del testo
        if (isSentByUser) {
            textViewMessaggio.background = ContextCompat.getDrawable(this, R.drawable.user_message_text_background)
            textViewMessaggio.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } else {
            textViewMessaggio.background = ContextCompat.getDrawable(this, R.drawable.agent_message_text_background)
            textViewMessaggio.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }

        // Aggiungi la TextView alla LinearLayout
        chatContainer.addView(textViewMessaggio)

        // Scorri automaticamente verso il basso per mostrare l'ultimo messaggio
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
}