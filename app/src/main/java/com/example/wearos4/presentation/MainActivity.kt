package com.example.wearos4.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "UserPreferences"
    private val PREF_NICKNAME = "nickname"
    private val PREF_PIN = "pin"

    private lateinit var startTime: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setContent {
            var isFirstLogin by remember { mutableStateOf(true) }

            if (isFirstLogin) {
                // 新規登録かログイン選択画面
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                isFirstLogin = false
                                // 新規登録画面に移行
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("新規登録")
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                isFirstLogin = false
                                // ログイン画面に移行
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ログイン")
                        }
                    }
                }
            } else {
                val isExistingUser = sharedPreferences.getString(PREF_NICKNAME, null) != null
                if (isExistingUser) {
                    // ログイン画面
                    var nicknameInput by remember { mutableStateOf("") }
                    var pinInput by remember { mutableStateOf("") }
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
                                value = nicknameInput,
                                onValueChange = { nicknameInput = it },
                                label = { Text("ニックネーム") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            TextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("PINコード") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val savedNickname = sharedPreferences.getString(PREF_NICKNAME, null)
                                        val savedPin = sharedPreferences.getString(PREF_PIN, null)

                                        if (nicknameInput == savedNickname && pinInput == savedPin) {
                                            Toast.makeText(this@MainActivity, "ログイン成功！", Toast.LENGTH_SHORT).show()
                                            navigateToMainScreen()
                                        } else {
                                            Toast.makeText(this@MainActivity, "ニックネームまたはPINコードが間違っています", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
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
                                    errorMessage = when {
                                        nicknameInput.isEmpty() -> "ニックネームを入力してください"
                                        pinInput.length != 4 -> "PINコードは4桁で入力してください"
                                        else -> ""
                                    }

                                    if (errorMessage.isEmpty()) {
                                        val savedNickname = sharedPreferences.getString(PREF_NICKNAME, null)
                                        val savedPin = sharedPreferences.getString(PREF_PIN, null)

                                        if (nicknameInput == savedNickname && pinInput == savedPin) {
                                            Toast.makeText(this@MainActivity, "ログイン成功！", Toast.LENGTH_SHORT).show()
                                            navigateToMainScreen()
                                        } else {
                                            Toast.makeText(this@MainActivity, "ニックネームまたはPINコードが間違っています", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ログイン")
                            }
                        }
                    }
                } else {
                    // 新規登録画面
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
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            TextField(
                                value = pin,
                                onValueChange = { pin = it },
                                label = { Text("PINコード") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        // エラーチェック
                                        errorMessage = when {
                                            nickname.isEmpty() -> "ニックネームを入力してください"
                                            pin.length != 4 -> "PINコードは4桁で入力してください"
                                            else -> ""
                                        }

                                        if (errorMessage.isEmpty()) {
                                            sharedPreferences.edit {
                                                putString(PREF_NICKNAME, nickname)
                                                putString(PREF_PIN, pin)
                                            }
                                            Toast.makeText(this@MainActivity, "新規登録完了！", Toast.LENGTH_SHORT).show()
                                            navigateToMainScreen()
                                        } else {
                                            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
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
                                        sharedPreferences.edit {
                                            putString(PREF_NICKNAME, nickname)
                                            putString(PREF_PIN, pin)
                                        }
                                        Toast.makeText(this@MainActivity, "新規登録完了！", Toast.LENGTH_SHORT).show()
                                        navigateToMainScreen()
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
            }
        }
    }

    private fun navigateToMainScreen() {
        setContent {
            var result by remember { mutableStateOf("") }
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

            startTime = Date()

            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            result = calculateTime(sdf, "〇")
                        }) {
                            Text("〇")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            result = calculateTime(sdf, "？")
                        }) {
                            Text("？")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            result = calculateTime(sdf, "☓")
                        }) {
                            Text("☓")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("結果: $result")
                }
            }
        }
    }

    private fun calculateTime(sdf: SimpleDateFormat, button: String): String {
        val endTime = Date()
        val timeDiff = endTime.time - startTime.time
        val formattedTime = sdf.format(timeDiff)

        return "ボタン: $button, 時間: $formattedTime"
    }
}
