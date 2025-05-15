package com.example.wearos4.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var onPermissionGranted: (() -> Unit) // 後から呼び出すコールバック

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()

    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        setContent {
            var sleepData by remember { mutableStateOf("睡眠データ未取得") }

            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        requestFitPermission {
                            fetchSleepData { data -> sleepData = data }
                        }
                    }) {
                        Text("睡眠データ取得")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(sleepData)
                }
            }
        }
    }

    // パーミッションをリクエストして、許可されていればコールバック実行
    private fun requestFitPermission(onGranted: () -> Unit) {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            onGranted()
        } else {
            onPermissionGranted = onGranted
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Google Fitアクセス許可済み", Toast.LENGTH_SHORT).show()
                onPermissionGranted() // アクセス許可後に続きの処理
            } else {
                Toast.makeText(this, "Google Fitアクセス拒否されました", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchSleepData(onDataReceived: (String) -> Unit) {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)

        val request = DataReadRequest.Builder()
            .read(DataType.TYPE_SLEEP_SEGMENT)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(this, account)
            .readData(request)
            .addOnSuccessListener { response ->
                val sleepData = response.getDataSet(DataType.TYPE_SLEEP_SEGMENT)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                var result = ""
                for (dp in sleepData.dataPoints) {
                    val start = Date(dp.getStartTime(TimeUnit.MILLISECONDS))
                    val end = Date(dp.getEndTime(TimeUnit.MILLISECONDS))
                    val stage = dp.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()
                    result += "開始: ${sdf.format(start)}\n終了: ${sdf.format(end)}\nステージ: $stage\n\n"
                }
                onDataReceived(result.ifEmpty { "睡眠データなし" })
            }
            .addOnFailureListener {
                onDataReceived("データ取得失敗: ${it.message}")
            }
    }
}
