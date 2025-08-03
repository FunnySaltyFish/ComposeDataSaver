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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    namespace = "com.funny.data_saver_data_store"
}

tasks.register<Jar>("generateSourcesJar") {
    from(android.sourceSets["main"].java.srcDirs)
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)

    compileOnly(libs.datastore)
    compileOnly(libs.datastore.preferences)
    implementation(project(":data-saver-core"))

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

    // 设置所有的 publish 任务 需要在 sign 之后
    // 我也不知道为什么需要手动这么写，但是 Gradle 一直报错，只好按着报错一点点尝试
    // 最后写出了这一堆。。。
    val signTasks = tasks.filter { it.name.startsWith("sign") && it.name != "sign"}

    // project.logger.warn(signTasks.joinToString { it.name + ", " })
    tasks.configureEach {
        // project.logger.warn("task name: $name")
        if (!name.startsWith("publish")) return@configureEach
        if (name == "publish") return@configureEach

        signTasks.forEach { signTask ->
            this.dependsOn(signTask)
        }
    }
}