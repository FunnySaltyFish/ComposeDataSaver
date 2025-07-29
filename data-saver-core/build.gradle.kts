@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig


plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("convention.publication")
    alias(libs.plugins.compose.compiler)
}

group = libs.versions.group.get()
version = libs.versions.project.get()

kotlin {
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.addAll("-Xexpect-actual-classes")
            }
        }
    }

    androidTarget {
        publishAllLibraryVariants()
    }

    jvm("desktop")
    
    // 添加 iOS 目标
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "DataSaverCore"
            isStatic = true
        }
        iosTarget.setUpiOSObserver()
    }

    // 添加 WASM 目标
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeDataSaver")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeDataSaverCore.js"
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

        }
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.kotlin.reflection)
            implementation(libs.kotlinx.coroutines.core)
        }
        desktopMain.dependencies {

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

        val commonTest by getting
        // 创建 iOS 测试源码集
        val iosTest by creating {
            dependsOn(commonTest)
        }
        val iosX64Test by getting {
            dependsOn(iosTest)
        }
        val iosArm64Test by getting {
            dependsOn(iosTest)
        }
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
        }
        
        // 测试依赖配置
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        
        // iOS 测试依赖
        iosTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        
        // Android 测试依赖
        val androidInstrumentedTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.espresso.core)
            }
        }

        // Desktop 测试依赖
        val desktopTest by getting {
            dependsOn(commonTest)
        }

        // WASM 测试依赖
        val wasmJsTest by getting {
            dependsOn(commonTest)
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
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

afterEvaluate {
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

// https://github.com/KevinnZou/compose-webview-multiplatform/blob/b876c0934c0bf24b30789e151f5acae923f22465/webview/build.gradle.kts
fun org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget.setUpiOSObserver() {
    // https://juejin.cn/post/7292298037203484726
    // 可通过 ./gradlew :data-saver-core:compileKotlinIosX64 生成产物，生成的产物可用如下命令查找
    // find data-saver-core/build/classes/kotlin/iosX64/main/cinterop/data-saver-core-cinterop-observer/default -type f
    compilations.getByName("main") {
        cinterops {
            val observer by creating {
                defFile(project.file("src/nativeInterop/cinterop/objectObserver.def"))
                compilerOpts("-Isrc/nativeInterop/cinterop")
                includeDirs {
                    allHeaders("src/nativeInterop/cinterop")
                }
                packageName = "com.funny.data_saver.core"
            }
        }
    }


//    compilations.getByName("main") {
//        cinterops.create("observer") {
//            compilerOpts("-F $path")
//        }
//    }
}