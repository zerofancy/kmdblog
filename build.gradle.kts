plugins {
    kotlin("jvm") version "1.3.72"
}

group = "top.ntutn"
version = "1.0"
//version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("commons-io:commons-io:20030203.000550")
    implementation("org.thymeleaf:thymeleaf:3.0.11.RELEASE")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation ("com.vladsch.flexmark:flexmark:0.62.2")
    implementation ("com.vladsch.flexmark:flexmark-util:0.62.2")
    implementation ("com.vladsch.flexmark:flexmark-ext-tables:0.62.2")
    implementation ("com.vladsch.flexmark:flexmark-ext-toc:0.62.2")
    implementation ("com.vladsch.flexmark:flexmark-ext-gfm-strikethrough:0.62.2")
    implementation ("com.vladsch.flexmark:flexmark-ext-footnotes:0.62.2")
    implementation ("com.vladsch.flexmark:flexmark-ext-gfm-tasklist:0.62.2")
}

// https://stackoverflow.com/questions/48553029/how-do-i-overwrite-a-task-in-gradle-kotlin-dsl
// https://github.com/gradle/kotlin-dsl/issues/705
// https://github.com/gradle/kotlin-dsl/issues/716
val fatJar = task("fatJar", type = org.gradle.jvm.tasks.Jar::class) {
    archiveFileName.set("${project.name}-fat.jar")
    manifest {
        attributes["Main-Class"] = "top.ntutn.AppKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec).exclude("META-INF/LICENSE.txt").exclude("META-INF/NOTICE.txt")
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