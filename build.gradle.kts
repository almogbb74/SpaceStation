plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeCompiler) apply false
}

// ponytail: local EDR agent (SentinelOne) locks newly-written build jars under Documents/
// indefinitely, breaking builds. Building outside that tree sidesteps it. Remove once the
// project folder gets a scan exclusion.
subprojects {
    layout.buildDirectory.set(File("C:/gradle-builds/moonboard_app/${project.name}"))
}
