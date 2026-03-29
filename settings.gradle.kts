pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		maven { url = uri("https://maven.fabricmc.net/") }
		maven { url = uri("https://maven.neoforged.net/releases") }
	}

	plugins {
		id("fabric-loom") version "1.15-SNAPSHOT"
		id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
		id("net.neoforged.moddev") version "2.0.139"
		id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
		id("com.modrinth.minotaur") version "2.+"
		id("net.darkhax.curseforgegradle") version "1.1.25"
	}
}

rootProject.name = "handlib"



include("26_1")
include("26_1:fabric")
include("26_1:neoforge")

project(":26_1").projectDir         = file("src/26_1")
project(":26_1:fabric").projectDir  = file("src/26_1/fabric")
project(":26_1:neoforge").projectDir = file("src/26_1/neoforge")