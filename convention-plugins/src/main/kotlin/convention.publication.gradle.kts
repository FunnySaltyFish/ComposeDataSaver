import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import java.util.Properties

plugins {
    id("com.vanniktech.maven.publish")
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["signing.key"] = null
ext["mavenCentralUsername"] = null
ext["mavenCentralPassword"] = null

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
    ext["signing.key"] = System.getenv("GPG_KEY_CONTENTS")
    ext["mavenCentralUsername"] = System.getenv("MAVEN_CENTRAL_USERNAME")
    ext["mavenCentralPassword"] = System.getenv("MAVEN_CENTRAL_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

mavenPublishing {
    // Configure publishing to Maven Central
    publishToMavenCentral()

    // Configure signing
    signAllPublications()

    // Configure what to publish - for Kotlin Multiplatform projects
    // 如果依赖了 kotlin multiplatform，则需要配置
    if (project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        configure(
            KotlinMultiplatform(
                javadocJar = JavadocJar.Empty(),
                sourcesJar = true,
                androidVariantsToPublish = listOf("debug", "release")
            )
        )
    }

    // Configure publication coordinates and metadata
    val group = libs.findVersionAsString("group")
    val version = libs.findVersionAsString("project")
    println("group: $group, version: $version")
    coordinates(
        groupId = group,
        artifactId = project.name,
        version = version
    )

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

// 输出调试信息
val mavenCentralUsername = getExtraString("mavenCentralUsername")
val mavenCentralPassword = getExtraString("mavenCentralPassword")
val signingKeyId = getExtraString("signing.keyId")
val signingPassword = getExtraString("signing.password")
val signingKey = getExtraString("signing.key")

println("Signing Key ID: $signingKeyId, Signing Password: ${signingPassword?.length}, Signing Key: ${signingKey?.length}")
println("mavenCentral Username: $mavenCentralUsername, mavenCentral Password: ${mavenCentralPassword?.length}")

val Project.libs
    get(): VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun VersionCatalog.findVersionAsString(alias: String) = findVersion(alias).get().toString()
