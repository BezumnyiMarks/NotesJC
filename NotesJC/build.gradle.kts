buildscript {
    val agp_version by extra("8.7.0")
    val agp_version1 by extra("8.8.0")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id ("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
    id ("com.android.library") version "8.0.2" apply false
    id ("org.jetbrains.kotlin.plugin.serialization") version "1.7.10" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}