package com.example.wearos4.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var startTime: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // アプリ起動時の時間を記録
        startTime = Date()

        setContent {
            var result by remember { mutableStateOf("") }

            val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

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

        Log.d("TimeResult", "Start: $startFormatted, End: $endFormatted, Duration: $durationSeconds 秒")

        return "ボタン「$label」\n開始: $startFormatted\n終了: $endFormatted\n経過: $durationSeconds 秒"
    }
}