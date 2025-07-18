plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "de.jochen_manns.buyitv0"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.jochen_manns.buyitv0"
        minSdk = 26
        targetSdk = 36
        versionCode = 22
        versionName = "2.13"
    }

    buildTypes {
        release {
            // isMinifyEnabled = true
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
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                output.outputFileName = "buyit.apk"
            }
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
}