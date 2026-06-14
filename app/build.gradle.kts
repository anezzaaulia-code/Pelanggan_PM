plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "anezza.aulia.pelanggan_pm"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "anezza.aulia.pelanggan_pm"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("io.getstream:photoview:1.0.3")
    implementation("com.guolindev.permissionx:permissionx:1.8.1")
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.android.material:material:1.12.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}