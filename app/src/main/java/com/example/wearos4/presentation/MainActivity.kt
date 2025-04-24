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

        val isFirstLogin = sharedPreferences.getString(PREF_NICKNAME, null) == null

        setContent {
            val focusManager = LocalFocusManager.current

            if (isFirstLogin) {
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
                                    focusManager.clearFocus()

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

                        // エラーメッセージの表示
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
            } else {
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
                                    focusManager.clearFocus()

                                    // エラーチェック
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
                                }
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // エラーメッセージの表示
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
            }
        }
    }

    private fun navigateToMainScreen() {
        setContent {
            var result by remember { mutableStateOf("") }
            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

            // 開始時間を記録
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

                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    private fun calculateTime(sdf: SimpleDateFormat, label: String): String {
        val endTime = Date()
        val durationMillis = endTime.time - startTime.time
        val durationSeconds = durationMillis / 1000.0

        val startFormatted = sdf.format(startTime)
        val endFormatted = sdf.format(endTime)

        return "ボタン「$label」\n開始: $startFormatted\n終了: $endFormatted\n経過: $durationSeconds 秒"
    }
}