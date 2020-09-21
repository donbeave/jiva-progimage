plugins {
    id("io.micronaut.library")
}

micronaut {
    version(Versions.micronaut)
}

dependencies {
    // modules
    implementation(project(":progimage-shared"))

    // Micronaut
    api("io.micronaut:micronaut-http-client")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
