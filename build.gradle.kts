import com.squareup.kotlinpoet.*
import java.io.FileWriter

plugins {
    kotlin("jvm") version "1.3.72"
}

group = "top.ntutn"
version = "2.0"
//version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup:kotlinpoet:1.7.2")
    }
}

sourceSets{
    main{
        java{
            srcDirs("build/classes/kotlin/generated")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("commons-io:commons-io:20030203.000550")
    implementation("org.thymeleaf:thymeleaf:3.0.11.RELEASE")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
    implementation("com.vladsch.flexmark:flexmark:0.62.2")
    implementation("com.vladsch.flexmark:flexmark-util:0.62.2")
    implementation("com.vladsch.flexmark:flexmark-ext-tables:0.62.2")
    implementation("com.vladsch.flexmark:flexmark-ext-toc:0.62.2")
    implementation("com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.62.2")
    implementation("com.vladsch.flexmark:flexmark-ext-footnotes:0.62.2")
    implementation("com.vladsch.flexmark:flexmark-ext-gfm-tasklist:0.62.2")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
}

// https://stackoverflow.com/questions/48553029/how-do-i-overwrite-a-task-in-gradle-kotlin-dsl
// https://github.com/gradle/kotlin-dsl/issues/705
// https://github.com/gradle/kotlin-dsl/issues/716
val fatJar = task("fatJar", type = org.gradle.jvm.tasks.Jar::class) {
    System.out.println("fatJar打包")
    archiveFileName.set("${project.name}-fat.jar")
    manifest {
        attributes["Main-Class"] = "top.ntutn.AppKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec).exclude("META-INF/LICENSE.txt").exclude("META-INF/NOTICE.txt")
}

val generateBuildConfigClass = task("generateBuildConfigClass", type = org.gradle.jvm.tasks.Jar::class) {
    System.out.println("生成BuildConfig.kt")
    val content = FileSpec.builder("", "BuildConfig")
        .addType(
            TypeSpec.objectBuilder("BuildConfig")
                .addProperty(
                    PropertySpec.builder("versionName", String::class)
                        .initializer("%S", archiveVersion.get())
                        .build()
                )
                .build()
        )
        .build()
    val file = File("./build/classes/kotlin/generated")
    content.writeTo(file)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    "build" {
        dependsOn(fatJar)
    }
    fatJar

}