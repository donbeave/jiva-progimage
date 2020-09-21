plugins {
    id("io.micronaut.library")
}

micronaut {
    version(Versions.micronaut)
}

dependencies {
    // Micronaut
    implementation("io.micronaut:micronaut-http-client")
}
