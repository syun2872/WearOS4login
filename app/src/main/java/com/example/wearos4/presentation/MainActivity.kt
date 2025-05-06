package com.example.wearos4.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserPreferences"
    private val PREF_NICKNAME = "nickname"
    private val PREF_PIN = "pin"

    private var startTime: Date = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setContent {
            var currentScreen by remember { mutableStateOf(Screen.Initial) }

            when (currentScreen) {
                Screen.Initial -> InitialScreen(onLoginClick = { currentScreen = Screen.Login }, onRegisterClick = { currentScreen = Screen.Register })
                Screen.Register -> RegisterScreen(onRegisterSuccess = { currentScreen = Screen.Main })
                Screen.Login -> LoginScreen(onLoginSuccess = { currentScreen = Screen.Main })
                Screen.Main -> MainScreen()
            }
        }
    }

    @Composable
    fun InitialScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("新規登録")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ログイン")
                }
            }
        }
    }

    @Composable
    fun RegisterScreen(onRegisterSuccess: () -> Unit) {
        var nickname by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("ニックネーム") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PINコード") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        // エラーチェック
                        errorMessage = when {
                            nickname.isEmpty() -> "ニックネームを入力してください"
                            pin.length != 4 -> "PINコードは4桁で入力してください"
                            else -> ""
                        }

                        if (errorMessage.isEmpty()) {
                            val savedNickname = sharedPreferences.getString(PREF_NICKNAME, null)
                            val savedPin = sharedPreferences.getString(PREF_PIN, null)

                            if (nickname == savedNickname && pin == savedPin) {
                                errorMessage = "新規登録できません。同じニックネームとPINコードが既に登録されています。"
                            } else {
                                sharedPreferences.edit {
                                    putString(PREF_NICKNAME, nickname)
                                    putString(PREF_PIN, pin)
                                }
                                Toast.makeText(this@MainActivity, "新規登録完了！", Toast.LENGTH_SHORT).show()
                                onRegisterSuccess()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("新規登録")
                }
            }
        }
    }

    @Composable
    fun LoginScreen(onLoginSuccess: () -> Unit) {
        var nickname by remember { mutableStateOf("") }
        var pin by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf("") }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("ニックネーム") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PINコード") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        val savedNickname = sharedPreferences.getString(PREF_NICKNAME, null)
                        val savedPin = sharedPreferences.getString(PREF_PIN, null)

                        if (nickname == savedNickname && pin == savedPin) {
                            Toast.makeText(this@MainActivity, "ログイン成功！", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            errorMessage = "ニックネームかPINコードが間違っています。"
                            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ログイン")
                }
            }
        }
    }

    @Composable
    fun MainScreen() {
        var idToken by remember { mutableStateOf("") }
        val auth = FirebaseAuth.getInstance()

        // FirebaseからIDトークンを取得
        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            idToken = result.token ?: "IDトークンの取得に失敗しました"
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("IDトークン: $idToken")

                Spacer(modifier = Modifier.height(16.dp))

                Text("この画面にIDトークンが表示されます")
            }
        }
    }

    enum class Screen {
        Initial,
        Register,
        Login,
        Main
    }
}
