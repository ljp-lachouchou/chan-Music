import org.jetbrains.kotlin.ir.backend.js.compile
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.defineProperty

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "com.software.mymusicplayer"
    compileSdk = 35


    defaultConfig {
        multiDexEnabled=true
        applicationId = "com.software.mymusicplayer"
        minSdk=29
        //noinspection EditedTargetSdkVersion,ExpiredTargetSdkVersion
        targetSdk=35
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
    kotlinOptions {
        jvmTarget = "11"
    }
}
dependencies {

    implementation("io.github.youth5201314:banner:2.2.3")

    //ROOM
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.paging:paging-runtime:3.2.1")
    implementation("androidx.room:room-paging:2.5.0")
    //圆形头像
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("androidx.camera:camera-video:1.3.0")
    //权限请求框架
//    implementation("com.tbruyelle.rxpermissions2:rxpermissions:0.9.4@aar")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("androidx.media3:media3-exoplayer:1.5.0")
    implementation("androidx.media3:media3-ui:1.5.0")
    implementation("androidx.media3:media3-session:1.5.0")
    implementation("androidx.media3:media3-datasource-okhttp:1.5.0")
    implementation("androidx.core:core-splashscreen:1.0.0-alpha02")
    //kt反射支持
//    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    // Kotlin 协程核心库
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // Android 平台上的协程扩展库，提供了适合 Android 的协程上下文等功能
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    //rxjava在retrofit的使用
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.11.0")//适配器
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("com.facebook.shimmer:shimmer:0.5.0")//闪烁效果

    //retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")//转换器
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    //okp
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    implementation(libs.animation.core.android)
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("io.github.scwang90:refresh-layout-kernel:3.0.0-alpha")    //核心必须依赖
    implementation ("io.github.scwang90:refresh-header-classics:3.0.0-alpha")    //经典刷新头
    implementation  ("io.github.scwang90:refresh-header-radar:3.0.0-alpha")       //雷达刷新头
    implementation  ("io.github.scwang90:refresh-header-falsify:3.0.0-alpha")     //虚拟刷新头
    implementation  ("io.github.scwang90:refresh-header-material:3.0.0-alpha")    //谷歌刷新头
    implementation  ("io.github.scwang90:refresh-header-two-level:3.0.0-alpha")   //二级刷新头
    implementation  ("io.github.scwang90:refresh-footer-ball:3.0.0-alpha")        //球脉冲加载
    implementation  ("io.github.scwang90:refresh-footer-classics:3.0.0-alpha")    //经典加载
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}