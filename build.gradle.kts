plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
    `maven-publish`
}

kotlin {
    jvmToolchain(17) // 또는 11
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

group = "com.example.metrics"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
    // Spock Framework
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0")

    // Groovy
    testImplementation("org.codehaus.groovy:groovy:3.0.17")

    // Ktor Client Mock
    testImplementation("io.ktor:ktor-client-mock:2.3.4")

    // Kotlinx Coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Kotlinx Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.example.metrics"
            artifactId = "metrics-sdk-kotlin"
            version = "1.0.0"
            from(components["java"])
        }
    }
}