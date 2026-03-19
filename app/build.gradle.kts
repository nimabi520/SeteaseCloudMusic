plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.seteasecloudmusic"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.seteasecloudmusic"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Retrofit 核心库（它会自动引入其依赖的 OkHttp 和 Okio 库）
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    // Retrofit 的 GSON 转换器，用于 JSON 数据的序列化和反序列化
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    //实现协程相关依赖
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    //liquid glass实现相关库
    implementation("io.github.kyant0:backdrop:1.0.6")

}