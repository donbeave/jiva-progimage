plugins {
    id("io.micronaut.library")
}

micronaut {
    version(Versions.micronaut)
}

dependencies {
    api("org.apache.tika:tika-core:${Versions.tika}")
}
