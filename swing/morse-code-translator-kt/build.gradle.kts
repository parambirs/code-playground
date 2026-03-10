plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "io.github.parambirs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("io.github.parambirs.morse.MainApplicationKt")
}