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
                            label = { Text("日付 (yyyy-MM-dd)") },
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
                                // 送信内容プレビュー用JSON生成
                                val previewJson = JSONObject().apply {
                                    put("date", date)
                                    put("deepSleep", deepSleep.toInt())
                                    put("lightSleep", lightSleep.toInt())
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
                            // プレビュー用JSON表示
                            Text(jsonPreview, modifier = Modifier.padding(8.dp))
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
                        Text(sendStatus)
                    }
                }
            }
        }
    }

    private fun sendDataToAws(date: String, deepSleep: Int, lightSleep: Int, callback: (Boolean, String) -> Unit) {
        val json = JSONObject()
        json.put("date", date)
        json.put("deep_sleep_minutes", deepSleep)
        json.put("light_sleep_minutes", lightSleep)


        val jsonString = json.toString()
        val url = "https://6y9xnelgzf.execute-api.ap-northeast-1.amazonaws.com/SLeep_API/Date_Resource" // ←あなたのAWSエンドポイント

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
}