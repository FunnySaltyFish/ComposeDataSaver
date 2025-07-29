import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("com.android.application")
    // kotlinx-serilization
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.compose.compiler)
}

kotlin {
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.addAll("-Xexpect-actual-classes")
            }
        }
    }

    androidTarget { }
    
    jvm("desktop")

    // 添加 iOS 目标
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "DataSaverCoreDemo"
            isStatic = true
        }
    }

    // 添加 WASM 目标
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeDataSaverDemo")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeDataSaverDemo.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }


    sourceSets {
        val desktopMain by getting
        val wasmJsMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        
        androidMain.dependencies {
            implementation(project(":data-saver-mmkv"))
            implementation(project(":data-saver-data-store-preferences"))

            /**
             * implementation project(path: ":data_saver_mmkv")
             *     implementation 'com.tencent:mmkv:1.2.14'
             *
             *     // if you want to use DataStore
             *     implementation project(path: ':data_saver_data_store_preferences')
             *     def data_store_version = "1.0.0"
             *     implementation "androidx.datastore:datastore:$data_store_version"
             *     implementation "androidx.datastore:datastore-preferences:$data_store_version"
             */
            implementation(libs.mmkv)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(project(":data-saver-core"))

            implementation(compose.material)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.precompose)
            implementation(libs.precompose.viewmodel) // For ViewModel intergration
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        // 创建 iOS 公共源码集
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        // WASM 平台依赖
        wasmJsMain.dependencies {

        }
    }
}

android {
    namespace = "com.funny.data_saver"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.funny.data_saver"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.funny.data_saver"
            packageVersion = "1.0.0"
        }
    }
}
