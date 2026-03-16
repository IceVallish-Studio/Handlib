plugins {
    base
}

tasks.named("build") {
    dependsOn(":26_1:build")
}