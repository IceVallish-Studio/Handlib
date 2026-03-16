pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven { url = uri("https://maven.fabricmc.net/") }
		maven { url = uri("https://maven.neoforged.net/releases") }
	}

	plugins {
		id("fabric-loom") version "1.9-SNAPSHOT"
		id("net.neoforged.moddev") version "2.0.139"
		id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
	}
}

rootProject.name = "handlib"

include("1_21")
include("1_21:fabric")
include("1_21:neoforge")
include("1_21_11")
include("1_21_11:fabric")
include("1_21_11:neoforge")

project(":1_21").projectDir         = file("src/1_21")
project(":1_21:fabric").projectDir  = file("src/1_21/fabric")
project(":1_21:neoforge").projectDir = file("src/1_21/neoforge")
project(":1_21_11").projectDir         = file("src/1_21_11")
project(":1_21_11:fabric").projectDir  = file("src/1_21_11/fabric")
project(":1_21_11:neoforge").projectDir = file("src/1_21_11/neoforge")