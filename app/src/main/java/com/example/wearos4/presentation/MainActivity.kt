package com.example.wearos4.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase初期化
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // Googleログインオプション
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // google-services.jsonの値と一致
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { authResult ->
                    // サインイン成功・失敗をコールバックで判定
                }
            } catch (e: Exception) {
                // エラー処理
            }
        }

        setContent {
            var date by remember { mutableStateOf("") }
            var deepSleep by remember { mutableStateOf("") }
            var lightSleep by remember { mutableStateOf("") }
            var sendStatus by remember { mutableStateOf("") }
            var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
            var showJson by remember { mutableStateOf(false) }
            var jsonPreview by remember { mutableStateOf("") }
            var feedback by remember { mutableStateOf("") }
            val scrollState = rememberScrollState()

            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isLoggedIn) {
                        Button(onClick = {
                            val signInIntent = googleSignInClient.signInIntent
                            launcher.launch(signInIntent)
                        }) {
                            Text("Googleでログイン")
                        }
                    } else {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("日付 (yyyyMMdd)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deepSleep,
                            onValueChange = { deepSleep = it.filter { c -> c.isDigit() } },
                            label = { Text("深い睡眠時間 (分)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lightSleep,
                            onValueChange = { lightSleep = it.filter { c -> c.isDigit() } },
                            label = { Text("浅い睡眠時間 (分)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (date.isBlank() || deepSleep.isBlank() || lightSleep.isBlank()) {
                                sendStatus = "すべての項目を入力してください"
                            } else {
                                val previewJson = JSONObject().apply {
                                    put("Sleep_DynamoDB", date)
                                    put("deep_sleep", deepSleep.toInt())
                                    put("light_sleep", lightSleep.toInt())
                                }.toString(2)
                                jsonPreview = previewJson
                                showJson = true
                            }
                        }) {
                            Text("送信内容を確認")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (showJson) {
                            Text("送信予定のJSON:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                jsonPreview.trim(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                softWrap = true,
                                maxLines = Int.MAX_VALUE
                            )
                            Row {
                                Button(onClick = {
                                    sendStatus = "送信中..."
                                    showJson = false
                                    sendDataToAws(date, deepSleep.toInt(), lightSleep.toInt()) { success, message ->
                                        sendStatus = if (success) "送信成功" else "送信失敗: $message"
                                    }
                                }) {
                                    Text("この内容でAWSに送信")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { showJson = false }) {
                                    Text("キャンセル")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Button(
                            onClick = {
                                if (date.isBlank()) {
                                    feedback = "日付を入力してください"
                                } else {
                                    feedback = "取得中..."
                                    fetchSleepDataFromAws(date) { deep, light, error ->
                                        if (error != null) {
                                            feedback = "取得失敗: $error"
                                        } else if (deep != null && light != null) {
                                            feedback = createFeedback(deep, light)
                                        } else {
                                            feedback = "データが見つかりません"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("AWSから睡眠データ取得")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        if (feedback.isNotBlank()) {
                            Text(
                                feedback.trim(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                softWrap = true,
                                maxLines = Int.MAX_VALUE
                            )
                        }
                        if (sendStatus.isNotBlank()) {
                            Text(
                                sendStatus.trim(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                softWrap = true,
                                maxLines = Int.MAX_VALUE
                            )
                        }
                    }
                }
            }
        }
    }

    private fun sendDataToAws(date: String, deepSleep: Int, lightSleep: Int, callback: (Boolean, String) -> Unit) {
        val json = JSONObject()
        json.put("Sleep_DynamoDB", date) // プライマリキー
        json.put("deep_sleep", deepSleep)
        json.put("light_sleep", lightSleep)

        val jsonString = json.toString()
        val url = "https://6y9xnelgzf.execute-api.ap-northeast-1.amazonaws.com/SLeep_API/Date_Resource"

        val client = OkHttpClient()
        val body = RequestBody.create("application/json; charset=utf-8".toMediaType(), jsonString)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    callback(true, "")
                } else {
                    callback(false, "HTTPエラーコード: ${response.code}")
                }
            } catch (e: IOException) {
                callback(false, e.message ?: "通信エラー")
            }
        }
    }

    private fun fetchSleepDataFromAws(date: String, callback: (Int?, Int?, String?) -> Unit) {
        // URLをユーザー入力の日付で作成
        val url = "https://6y9xnelgzf.execute-api.ap-northeast-1.amazonaws.com/SLeep_API/Date_Resource?Sleep_DynamoDB=$date"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val notFound = json.optBoolean("not_found", false)
                    if (notFound) {
                        callback(null, null, null) // データが見つかりません
                    } else {
                        // deep_sleep, light_sleepがStringでもIntでもOKにする
                        val deep = try {
                            json.getInt("deep_sleep")
                        } catch (e: Exception) {
                            try { json.getString("deep_sleep").toInt() } catch (e: Exception) { -1 }
                        }
                        val light = try {
                            json.getInt("light_sleep")
                        } catch (e: Exception) {
                            try { json.getString("light_sleep").toInt() } catch (e: Exception) { -1 }
                        }
                        if (deep >= 0 && light >= 0) {
                            callback(deep, light, null)
                        } else {
                            callback(null, null, "データが不正です")
                        }
                    }
                } else if (response.code == 404) {
                    callback(null, null, null) // データが見つかりません
                } else {
                    callback(null, null, "HTTPエラーコード: ${response.code}")
                }
            } catch (e: IOException) {
                callback(null, null, e.message ?: "通信エラー")
            }
        }
    }

    private fun createFeedback(deep: Int, light: Int): String {
        return when {
            deep >= 120 -> "深い睡眠時間が十分です！よく休めています。"
            deep >= 60 -> "深い睡眠時間は平均的です。もう少し増やしてみましょう。"
            else -> "深い睡眠が不足気味です。寝る前のスマホやカフェインを控えてみましょう。"
        } + "（深い睡眠: ${deep}分, 浅い睡眠: ${light}分）"
    }
}