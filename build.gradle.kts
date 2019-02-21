group = "hp"
version = "0.1"

plugins {
    kotlin("jvm") version "1.3.21"
    java
    application
}

application {
    mainClassName = "hp.compiler.CompilerKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
}

tasks {
    withType<JavaExec> {
        setArgsString("src/main/hp/Test.hp src/main/hp/Test2.hp")
    }
}