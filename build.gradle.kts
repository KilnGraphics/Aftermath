@file:Suppress("INACCESSIBLE_TYPE")

import org.gradle.internal.os.OperatingSystem.*

buildscript {
	repositories {
		maven("https://plugins.gradle.org/m2/")
	}
	dependencies {
		classpath("org.kohsuke:github-api:1.114")
	}
}

plugins {
	`java-library`
	`maven-publish`
	id("org.cadixdev.licenser") version "0.6.1"
}

group = properties["maven_group"]!!
version = properties["version"]!!

val lwjglVersion = "3.3.0-SNAPSHOT"
val lwjglNatives = when (current()) {
	LINUX -> System.getProperty("os.arch").let {
		if (it.startsWith("arm") || it.startsWith("aarch64")) {
			val arch = if (it.contains("64") || it.startsWith("armv8")) {
				"arm64"
			} else {
				"arm32"
			}

			"natives-linux-$arch"
		} else {
			"natives-linux"
		}
	}
	MAC_OS -> if (System.getProperty("os.arch")
					.startsWith("aarch64")
	) "natives-macos-arm64" else "natives-macos"
	WINDOWS -> "natives-windows"
	else -> error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}

repositories {
	mavenCentral()
	maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
	implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
	implementation("org.lwjgl", "lwjgl")
	runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)

	api("org.jetbrains", "annotations", "20.1.0")
}

var changelog = ""

java {
	withSourcesJar()
}

tasks {
	withType<Jar> {
		from("LICENSE") {
			rename { "${it}_${project.properties["archives_base_name"]!!}" }
		}
	}
}

license {
	setHeader(rootProject.file("LICENSE"))
}

tasks.create<org.gradle.jvm.tasks.Jar>("javadocJar") {
	archiveClassifier.set("javadoc")
	from((tasks.findByName("javadoc") as Javadoc).destinationDir)
	dependsOn(tasks.findByName("javadoc"))
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifact(tasks.jar)
			artifact(tasks.findByName("sourcesJar"))
			artifact(tasks.findByName("javadocJar"))

			pom {
				name.set("Aftermath")
				packaging = "jar"
				// optionally artifactId can be defined here
				description.set("A JNI Library for the Nvidia Aftermath debugger")
				url.set("https://github.com/Blaze4D-MC/Aftermath")

				scm {
					connection.set("scm:git:git://github.com/Blaze4D-MC/Aftermath.git")
					developerConnection.set("scm:git:ssh://github.com/Blaze4D-MC/Aftermath.git")
					url.set("http://github.com/Blaze4D-MC/Aftermath")
				}

				licenses {
					license {
						name.set("MIT")
						url.set("https://mit-license.org/")
					}
				}

				developers {
					developer {
						id.set("OroArmor")
						name.set("Eli Orona")
						email.set("eliorona@live.com")
						url.set("oroarmor.com")
					}
				}
			}
		}
	}

	repositories {
		mavenLocal()
		if (System.getenv("MAVEN_URL") != null) {
			maven {
				setUrl(System.getenv("MAVEN_URL"))
				credentials {
					username = System.getenv("MAVEN_USERNAME")
					password = System.getenv("MAVEN_PASSWORD")
				}
				name = "OroArmorMaven"
			}
		}
	}
}


tasks.create("github") {
	onlyIf {
		System.getenv()["GITHUB_TOKEN"] != null
	}

	doLast {
		val github = org.kohsuke.github.GitHub.connectUsingOAuth(System.getenv()["GITHUB_TOKEN"])
		val repository = github.getRepository("Blaze4D-MC/Aftermath")

		val releaseBuilder = org.kohsuke.github.GHReleaseBuilder(repository, project.version as String?)
		releaseBuilder.name("Aftermath ${project.version}")
		releaseBuilder.body(changelog)
		releaseBuilder.commitish("master")

		val ghRelease = releaseBuilder.create()
		ghRelease.uploadAsset(file("${project.rootDir}/build/libs/${project.properties["archives_base_name"]!!}-${version}.jar"), "application/java-archive")
	}
}

tasks.create("generateChangelog") {
	val changelogFile = file("CHANGELOG.md")
	val changelogs = changelogFile.readText().split("----")
	changelog = changelogs.find { log -> log.contains(project.version as String) }!!
	println(changelog)
}