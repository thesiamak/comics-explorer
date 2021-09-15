import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")

    kotlin("kapt")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    buildTypes {
        getByName("release"){
            // Enables code shrinking, obfuscation, and optimization for only
            // your project"s release build type.
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true

            // Includes the default ProGuard rules files that are packaged with
            // the Android Gradle plugin. To learn more, go to the section about
            // R8 configuration files.
            var appReleaseFileName :String by rootProject.extra
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")

            // Release build file auto rename
            applicationVariants.all {
                val variant = this
                variant.outputs
                    .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        output.outputFileName = "comics_explorer_${variant.buildType.name}_${defaultConfig.versionName}" +
                                "_${Date()}.apk"
                        appReleaseFileName = output.outputFileName
                    }
            }

        }

        getByName("debug"){
            isMinifyEnabled = false
        }
    }

    compileOptions{
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }

    val code = generateVersionCode()
    val appVersionName :String by rootProject.extra
    val name = "$appVersionName$code"

    defaultConfig {
        applicationId = "com.shortcut.explorer"
        versionName = name
        versionCode = 1
        minSdkVersion(23)
        targetSdkVersion(30)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    sourceSets.getByName("test") {
        java.srcDir("src/test/java")
        java.srcDir("src/test/kotlin")
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    val kotlin: String by rootProject.extra
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin")
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")


    //kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.32")


    // Dagger2
    val dagger: String by rootProject.extra
    implementation("com.google.dagger:dagger:$dagger")
    implementation("com.google.dagger:dagger-android-support:$dagger")
    kapt("com.google.dagger:dagger-compiler:$dagger")
    kapt("com.google.dagger:dagger-android-processor:$dagger")


    //===================== TEST DEPENDENCIES =============================//
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")

    // Clean reports tool
    testImplementation("org.amshove.kluent:kluent-android:1.68")

    // Mocking facilities
    testImplementation("org.mockito:mockito-core:3.11.2")
    testImplementation("io.mockk:mockk:1.12.0")




    //===================== ANDROID TEST DEPENDENCIES =============================//

    // Espresso
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // AndroidX test
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")

}


//Auto generate unique build codes for every minute
fun generateVersionCode() = (System.currentTimeMillis() / 60000).toInt()

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}