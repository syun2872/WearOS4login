package com.example.wearos4.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // ログイン成功 → MainActivity2へ遷移
                        val intent = Intent(this, MainActivity2::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // ログイン失敗
                        Toast.makeText(this, "ログインに失敗しました", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
