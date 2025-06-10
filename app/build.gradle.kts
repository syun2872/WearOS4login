plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.wearos4.presentation"
    compileSdk = 35 // ここで直接指定

    defaultConfig {
        applicationId = "com.example.wearos4.presentation"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Firebaseの依存関係
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Composeの依存関係
    implementation(platform(libs.compose.bom))  // BOMを使用してComposeのバージョン管理
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.material3.android)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.navigation.runtime.android)
    implementation(libs.firebase.database.ktx)
    implementation(libs.play.services.fitness)

    // テスト関連
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)

    // デバッグ用
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

        implementation("androidx.compose.ui:ui:1.5.0")  // 基本的なUIコンポーネント
        implementation("androidx.compose.material:material:1.5.0")  // Material Designコンポーネント
        implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")  // プレビューサポート
        implementation("androidx.compose:compose-bom:2023.10.01")  // BOMでComposeの依存関係を統一
        implementation("androidx.compose.ui:ui-text:1.5.0")  // ui-text（KeyboardOptions, KeyboardTypeなど）
        implementation ("com.google.firebase:firebase-auth:21.0.1")

// Google Fit API（Google Play Services Fitness）
    implementation("com.google.android.gms:play-services-fitness:21.0.0")

    // Google Sign-In（Google アカウント認証）
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Firebase Authentication（既に追加済みなら不要）
    implementation("com.google.firebase:firebase-auth:22.1.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}