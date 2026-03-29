plugins {
    base
}

tasks.named("build") {
    dependsOn(":1_21:build", ":1_21_11:build")
}

tasks.register("publishPlatforms") {
    dependsOn(":1_21:publishPlatforms", ":1_21_11:publishPlatforms")
}