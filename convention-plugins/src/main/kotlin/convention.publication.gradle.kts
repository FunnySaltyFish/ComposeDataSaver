import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.*

plugins {
    `maven-publish`
    signing
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
    // Configure maven central repository
    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        // Provide artifacts information requited by Maven Central
        pom {
            name.set("ComposeDataSaver")
            description.set("在 Compose Multiplatform 中优雅完成数据持久化 | An elegant way to do data persistence in Compose Multiplatform")
            url.set("https://github.com/FunnySaltyFish/ComposeDataSaver")

            licenses {
                license {
                    name.set("Apache License 2.0")
                    url.set("https://opensource.org/licenses/Apache-2.0")
                }
            }
            developers {
                developer {
                    id.set("FunnySaltyFish")
                    name.set("FunnySaltyFish")
                    email.set("funnysaltyfish@foxmail.com")
                }
            }
            scm {
                url.set("https://github.com/FunnySaltyFish/ComposeDataSaver")
            }
        }
    }
}

// Signing artifacts. Signing.* extra properties values will be used
signing {
    sign(publishing.publications)
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

    // Reason: Task ':data-saver-data-store-preferences:generateMetadataFileForReleasePublication' uses this output of task ':data-saver-data-store-preferences:generateSourcesJar' without declaring an explicit or implicit dependency. This can lead to incorrect results being produced, depending on what order the tasks are executed.
    val generateSourcesJar = tasks.findByName("generateSourcesJar") ?: return@afterEvaluate
    tasks.configureEach {
        if (!name.startsWith("generateMetadataFileFor")) return@configureEach

        this.dependsOn(generateSourcesJar)
    }
}