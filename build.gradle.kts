// Top-level build file where you can add configuration options common to all sub-projects/modules.
// add ksp https://developer.android.com/build/migrate-to-ksp#add-ksp
// https://developer.android.com/training/data-storage/room#kotlin -- opis primary components in room
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}