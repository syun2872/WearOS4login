// プロジェクトレベルのbuild.gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.google.services)  // Firebaseプラグイン
        classpath(libs.gradle) // Android Gradle Plugin
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.3.14" apply false
}
