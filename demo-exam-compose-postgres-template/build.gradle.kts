import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.21"
    id("com.google.devtools.ksp") version "2.2.21-2.0.4"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21"
    id("org.jetbrains.compose") version "1.10.3"
}

group = "ru.demoexam"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.apache.poi:poi-ooxml:5.4.1")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.sqlite:sqlite-bundled:2.6.2")
    implementation("ch.qos.logback:logback-classic:1.5.21")
    ksp("androidx.room:room-compiler:2.8.4")

    testImplementation(kotlin("test"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("importXlsx") {
    group = "application"
    description = "Imports demo exam XLSX files into local Room/SQLite database"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ru.demoexam.template.importer.XlsxImportMainKt")

    if (project.hasProperty("importDir")) {
        args(project.property("importDir").toString())
    }
}

compose.desktop {
    application {
        mainClass = "ru.demoexam.template.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "DemoExamTemplate"
            packageVersion = "1.0.0"
            vendor = "Demo Exam Template"
            description = "Template for demo exam desktop applications on Compose Desktop and Room"
        }
    }
}
