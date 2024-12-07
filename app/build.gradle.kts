plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fyp1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fyp1"
        minSdk = 26
        targetSdk = 33
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

    buildFeatures{
        viewBinding = true;

    }
}
configurations {
    all {
        exclude(group = "com.intellij", module = "annotations")
    }}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.1")

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("androidx.room:room-compiler:2.6.1")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("com.google.android.libraries.places:places:2.5.0")
    implementation ("org.jetbrains:annotations:23.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation ("com.google.ar:core:1.16.0")
    implementation("com.gorisse.thomas.sceneform:sceneform:1.20.1")
    implementation ("de.javagl:obj:0.4.0")
    implementation ("com.github.Spikeysanju:MotionToast:1.4")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")

    implementation ("androidx.camera:camera-core:1.1.0-beta01")
    implementation ("androidx.camera:camera-camera2:1.1.0-beta01")
    implementation ("androidx.camera:camera-lifecycle:1.1.0-beta01")
    implementation ("androidx.camera:camera-view:1.1.0-beta01")
    implementation ("com.github.f0ris.sweetalert:library:1.6.2")

    implementation ("com.google.guava:guava:31.1-android")

    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.android.volley:volley:1.2.1")





}

