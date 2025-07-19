plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.hitproduct"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.hitproduct"
        minSdk = 26
        targetSdk = 35
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

    buildFeatures {
        viewBinding = true
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


    //auto resize
    implementation ("com.github.JessYanCoding:AndroidAutoSize:v1.2.1")

    implementation ("com.github.bumptech.glide:glide:4.16.0")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("net.orandja.shadowlayout:shadowlayout:1.0.1")

    // Lifecycle ViewModel + LiveData
    val lifecycle_version = "2.8.7"
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    val navigation_version = "2.5.3"
    implementation ("androidx.navigation:navigation-fragment:${navigation_version}")
    implementation ("androidx.navigation:navigation-ui:${navigation_version}")
    implementation ("androidx.navigation:navigation-fragment-ktx:${navigation_version}")
    implementation ("androidx.navigation:navigation-ui-ktx:${navigation_version}")

    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.2")

    //lottie
    implementation ("com.airbnb.android:lottie:5.2.0")

    //socket io
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }
    implementation("org.json:json:20230227")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    implementation("com.google.android.material:material:1.12.0")
    implementation ("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("com.github.Kwasow:BottomNavigationCircles-Android:1.2")

    implementation ("nl.joery.animatedbottombar:library:1.1.0")

    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")

    implementation("com.github.kizitonwose:CalendarView:2.2.0")

//    implementation ("com.github.santaevpavel:OutlineSpan:0.1.1")
    //banh xe lich
//    implementation ("cn.aigestudio.wheelpicker:wheelpicker:2.1.4")

}