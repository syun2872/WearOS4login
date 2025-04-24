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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // ここで activity_main.xml を使用

        // ログインボタンのクリックリスナー設定
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            // LoginActivity への遷移
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
class MainActivity2 : ComponentActivity() {

    private lateinit var startTime: Date
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ XML レイアウトは使わない！
        // setContentView(R.layout.activity_main) は削除！

        // ✅ Firebase 初期化
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // ✅ 匿名ログイン
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("FirebaseLogin", "ログイン成功: UID=${user?.uid}")
                } else {
                    Log.e("FirebaseLogin", "ログイン失敗", task.exception)
                }
            }

        // ✅ 起動時間記録
        startTime = Date()

        // ✅ Jetpack Compose のレイアウト
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

        Log.d(
            "TimeResult",
            "Start: $startFormatted, End: $endFormatted, Duration: $durationSeconds 秒"
        )

        return "ボタン「$label」\n開始: $startFormatted\n終了: $endFormatted\n経過: $durationSeconds 秒"
    }
}

