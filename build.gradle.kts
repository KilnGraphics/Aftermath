@file:Suppress("INACCESSIBLE_TYPE")

import org.gradle.internal.os.OperatingSystem.*

buildscript {
	repositories {
		maven("https://plugins.gradle.org/m2/")
	}
	dependencies {
		classpath("org.kohsuke:github-api:1.114")
		classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
	}
}

plugins {
	`java-library`
	`maven-publish`
	id("org.cadixdev.licenser") version "0.5.0"
	signing
}
apply(null, "io.codearte.nexus-staging")

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

	implementation("org.jetbrains", "annotations", "20.1.0")
}

var changelog = ""

java {
	withSourcesJar()
}

tasks {
	withType<Sign> {
		onlyIf { project.hasProperty("sign") }
	}

	withType<Jar> {
		from("LICENSE") {
			rename { "${it}_${project.properties["archives_base_name"]!!}" }
		}
	}
}

if (project.hasProperty("sign")) {
	signing {
		useGpgCmd()
		sign(publishing.publications)
	}
}

license {
	header = rootProject.file("LICENSE")
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
				name.set("Multi Item Lib")
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
		val ossrhUsername = "OroArmor"
		val ossrhPassword = (if (project.hasProperty("ossrhPassword")) project.property("ossrhPassword") else System.getenv("OSSRH_PASSWORD")) as String?
		mavenLocal()
		maven {
			val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
			val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
			url = uri(if ((version as String).endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
			credentials {
				username = ossrhUsername
				password = ossrhPassword
			}
			name = "mavenCentral"
		}
	}
}


//nexusStaging {
//	username = "OroArmor"
//	password = (if(project.hasProperty("ossrhPassword")) project.property("ossrhPassword") else System.getenv("OSSRH_PASSWORD")) as String
//}

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
	val changelogs = changelogFile.readLines()
	changelog = changelogs.find { log -> log.contains(project.version as String) }!!
	println(changelog)
}