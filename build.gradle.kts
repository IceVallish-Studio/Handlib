plugins {
    base
}

tasks.named("build") {
    dependsOn(":1_21:build", ":1_21_11:build", ":26_1:build")
}