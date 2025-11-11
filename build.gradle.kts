plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

application {
    mainClass.set("aurora.engine.AuroraEngine")
}

tasks.named<Test>("test") {
    //useJUnitPlatform()
}

// use: ./gradlew run_file_test -Ptarget=path/to/source.aml
tasks.register<JavaExec>("run_file_test") {
    group = "Execution"
    description = "Compile and run FileTest with target source file."
    mainClass.set("aurora.engine.parser.FileTest")
    classpath = sourceSets["test"].runtimeClasspath
    workingDir = file("build/resources/test")
    
    // Pass the target file as a program argument
    args(project.findProperty("target") ?: "defaultTarget")

    // Enable debugging
    jvmArgs = listOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
}
