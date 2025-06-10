package com.example.wearos4.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        setContent {
            val focusManager = LocalFocusManager.current

            var step by remember { mutableStateOf(1) }

            var date by remember { mutableStateOf("") }

            var deepHour by remember { mutableStateOf("") }
            var deepMin by remember { mutableStateOf("") }

            var lightHour by remember { mutableStateOf("") }
            var lightMin by remember { mutableStateOf("") }

            var jsonOutput by remember { mutableStateOf("") }

            Surface(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Text(
                            text = when (step) {
                                1 -> "\u2460 日付を入力（例：20250610）"
                                2 -> "\u2461 深い睡眠時間を入力"
                                3 -> "\u2462 浅い睡眠時間を入力"
                                4 -> "\u2705 入力完了：確認してください"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 18.sp
                        )
                    }
                    item {
                        when (step) {
                            1 -> OutlinedTextField(
                                value = date,
                                onValueChange = { if (it.all { c -> c.isDigit() }) date = it },
                                label = { Text("日付（8桁）") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )

                            2 -> SleepTimeInput(
                                title = "深い睡眠",
                                hour = deepHour,
                                min = deepMin,
                                onHourChange = { if (it.all { c -> c.isDigit() }) deepHour = it },
                                onMinChange = { if (it.all { c -> c.isDigit() }) deepMin = it }
                            )

                            3 -> SleepTimeInput(
                                title = "浅い睡眠",
                                hour = lightHour,
                                min = lightMin,
                                onHourChange = { if (it.all { c -> c.isDigit() }) lightHour = it },
                                onMinChange = { if (it.all { c -> c.isDigit() }) lightMin = it }
                            )

                            4 -> {
                                val deepTotal = (deepHour.toIntOrNull() ?: 0) * 60 + (deepMin.toIntOrNull() ?: 0)
                                val lightTotal = (lightHour.toIntOrNull() ?: 0) * 60 + (lightMin.toIntOrNull() ?: 0)
                                val json = JSONObject().apply {
                                    put("date", date)
                                    put("deep_sleep_minutes", deepTotal)
                                    put("light_sleep_minutes", lightTotal)
                                }
                                jsonOutput = json.toString(2)

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("\uD83D\uDCC5 日付: $date")
                                    Text("\uD83C\uDF19 深い睡眠: $deepHour 時間 $deepMin 分（$deepTotal 分）")
                                    Text("\uD83D\uDCA4 浅い睡眠: $lightHour 時間 $lightMin 分（$lightTotal 分）")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("\uD83D\uDCE6 JSON:")
                                    Text(jsonOutput)
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (step > 1) {
                                Button(onClick = {
                                    focusManager.clearFocus()
                                    step--
                                }) {
                                    Text("\u2190 戻る")
                                }
                            }
                            Button(onClick = {
                                focusManager.clearFocus()
                                if (step < 4) step++ else {
                                    postJsonToApiGateway(jsonOutput) { success, message ->
                                        runOnUiThread {
                                            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                step = 1
                                                date = ""
                                                deepHour = ""
                                                deepMin = ""
                                                lightHour = ""
                                                lightMin = ""
                                                jsonOutput = ""
                                            }
                                        }
                                    }
                                }
                            }) {
                                Text(if (step < 4) "決定 \u2192" else "送信")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SleepTimeInput(
        title: String,
        hour: String,
        min: String,
        onHourChange: (String) -> Unit,
        onMinChange: (String) -> Unit
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("現在の入力: ${hour.ifEmpty { "0" }}時間 ${min.ifEmpty { "0" }}分")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = hour,
                    onValueChange = onHourChange,
                    label = { Text("時間") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(120.dp)
                )
                OutlinedTextField(
                    value = min,
                    onValueChange = onMinChange,
                    label = { Text("分") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }

    private fun postJsonToApiGateway(jsonBody: String, onResult: (Boolean, String) -> Unit) {
        val client = OkHttpClient()
        val url = "https://6y9xnelgzf.execute-api.ap-northeast-1.amazonaws.com/SLeep_API"

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "送信失敗: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    onResult(true, "送信成功: ${response.code}")
                } else {
                    onResult(false, "失敗コード: ${response.code}")
                }
            }
        })
    }
}