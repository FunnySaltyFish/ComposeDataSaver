plugins {
    id("com.android.library")
    kotlin("android")
    id("convention.publication")
    alias(libs.plugins.compose.compiler)
}

group = libs.versions.group.get()
version = libs.versions.project.get()

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    namespace = "com.funny.data_saver_data_store"
}

tasks.register<Jar>("generateSourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
//    archiveClassifier.set("sources")
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
//    compileOnly(libs.kotlin.stdlib)

    compileOnly(libs.datastore)
    compileOnly(libs.datastore.preferences)
    implementation(project(":data-saver-core"))
//    compileOnly(libs.kotlinx.coroutines.core)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

afterEvaluate {
    publishing {
        publications {
            // Creates a Maven publication called "release".
            create<MavenPublication>("release") {
                // Applies the component for the release build variant.
                from(components["release"])

                // You can then customize attributes of the publication as shown below.
                groupId = libs.versions.group.get()
                artifactId = "data-saver-data-store-preferences"
                version = libs.versions.project.get()
                artifact(tasks["generateSourcesJar"])
            }
        }
    }
}