// Root build file for the Photography Assistant project

plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("kotlin-kapt") version "1.9.0" apply false
}

group = "com.example.photographyassistant"
version = "1.0.0"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

clean {
    delete(rootProject.buildDir)
}