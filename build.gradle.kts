plugins {
    java
    id("eclipse")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.harukiyu.claritypets"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.226-stable")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("net.kyori:adventure-nbt:5.2.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly(fileTree("libs") { include("*.jar") })

    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("com.mysql:mysql-connector-j:9.2.0")
}

eclipse {
    classpath {
        plusConfigurations.add(configurations.compileClasspath.get())
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("com.zaxxer.hikari", "com.harukiyu.claritypets.libs.hikari")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
