plugins {
    kotlin("jvm") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    //XML
    implementation("org.simpleframework:simple-xml:2.7.1")

    // Koog
    implementation("ai.koog:koog-agents:0.4.2")
    testImplementation("ai.koog:agents-test:0.4.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}