plugins {
    kotlin("jvm") version "2.2.21"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.14.4")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-test:3.5.16")
    testImplementation("io.projectreactor.netty:reactor-netty-core:1.2.18")
    testImplementation("io.projectreactor.netty:reactor-netty-http:1.2.18")
}

tasks.test {
    useJUnitPlatform()
    // strict ordering, one JVM process for the whole run
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
}