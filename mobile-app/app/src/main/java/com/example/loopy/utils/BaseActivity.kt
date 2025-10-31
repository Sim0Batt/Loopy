package com.example.loopy.core

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.loopy.login.LoginActivity
import com.example.loopy.utils.SessionManager

abstract class BaseActivity : AppCompatActivity() {

    private val TAG = "BaseActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Log.d(TAG, "onCreate: Checking session for ${javaClass.simpleName}")
        if (SessionManager.currentUserId == null) {
            Log.w(TAG, "Session check FAILED. User is null. Redirecting to Login.")
            goToLogin()
        } else {
            Log.i(TAG, "Session check OK. User is logged in (ID: ${SessionManager.currentUserId}). Proceeding.")
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}