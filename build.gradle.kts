plugins {
    java
    `java-library`
}

group = "io.github.gaming32"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val nonTransitive: Configuration by configurations.creating {
    configurations.compileOnly.get().extendsFrom(this)
    configurations.runtimeOnly.get().extendsFrom(this)
}

dependencies {
    api("org.ow2.asm:asm:9.6")

    nonTransitive("net.sourceforge.argparse4j:argparse4j:0.9.0")

    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.ow2.asm:asm-tree:9.6")
}

tasks.test {
    useJUnitPlatform()
}
