plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
//    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.dapittriandidev.patani"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dapittriandidev.patani"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation (platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.0")
    implementation("com.google.android.gms:play-services-auth:19.0.0")
//    implementation "com.google.firebase:firebase-analytics:21.3.0"
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("de.hdodenhof:circleimageview:3.1.0")
//    implementation("com.google.firebase:firebase-crashlytics:")
    implementation("androidx.core:core:1.10.1")
//    implementation ("com.google.firebase:firebase-storage:20.2.3")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
