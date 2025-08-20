plugins {
    // No top-level plugins
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
