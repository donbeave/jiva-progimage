plugins {
    id("com.adarshr.test-logger")
    id("io.micronaut.application")
}

micronaut {
    version(Versions.micronaut)
    processing {
        incremental(true)
        module(project.name)
        group(project.group.toString())
        annotations("com.zhokhov.progimage.*")
    }
}

dependencies {
    // modules
    implementation(project(":progimage-shared"))
    implementation(project(":progimage-utils"))
    testImplementation(project(":progimage-client"))

    // Micronaut
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-management")
    testImplementation("io.micronaut.test:micronaut-test-junit5")

    // libraries
    implementation("ch.qos.logback:logback-classic")
    implementation("javax.annotation:javax.annotation-api")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClassName = "com.zhokhov.progimage.filters.FiltersApplication"
}
