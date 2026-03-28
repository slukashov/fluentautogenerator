import org.jetbrains.intellij.platform.gradle.Constants

plugins {
    id("java")
    // Ensure these versions match your libs.versions.toml or are hardcoded (e.g., "2.0.0")
    alias(libs.plugins.kotlinJvm) 
    id("org.jetbrains.intellij.platform") version "2.10.4"
    id("org.jetbrains.changelog") version "2.2.0"
}

// Configuration Variables (Passed from gradle.properties)
val ProductVersion: String by project
val RiderPluginId: String by project

// 1. REPOSITORY FIX: Added mavenCentral() so the Kotlin compiler can find its build tools
repositories {
    mavenCentral() 
    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
    }
}

tasks.wrapper {
    gradleVersion = "8.8"
    distributionType = Wrapper.DistributionType.ALL
}

// 2. VERSIONING: Pulling from the 'extra' property set in your project
version = extra["PluginVersion"] as String

// 3. SOURCE SETS: Mapping to your specific Rider folder structure
sourceSets {
    main {
        java.srcDir("src/rider/main/java")
        kotlin.srcDir("src/rider/main/kotlin")
        resources.srcDir("src/rider/main/resources")
    }
}

tasks.compileKotlin {
    kotlinOptions { 
        jvmTarget = "17" 
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

dependencies {
    intellijPlatform {
        // Targets the specific Rider version for C# PSI access
        rider(ProductVersion, useInstaller = false)
        jetbrainsRuntime()
    }
}

tasks.runIde {
    // Standard memory allocation for a Rider sandbox
    maxHeapSize = "1500m"
}

// 4. CHANGELOG CONFIGURATION
changelog {
    // Support for 4-digit versions like 1.0.3.1
    headerParserRegex.set("""^\[?(\d+(?:\.\d+)+)\]?$""".toRegex())
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

// 5. PLUGIN XML PATCHING
tasks.patchPluginXml {
    changeNotes.set(provider {
        // Automatically pulls the latest notes from CHANGELOG.md
        changelog.renderItem(
            changelog.getOrNull(project.version.toString()) ?: changelog.getUnreleased(),
            org.jetbrains.changelog.Changelog.OutputType.HTML
        )
    })
    
    // Pulls from 'SinceBuild' in gradle.properties
    sinceBuild.set(providers.gradleProperty("SinceBuild"))
}

// 6. BUILD & PUBLISH: Pure JVM logic only
tasks.buildPlugin {
    doLast {
        // Ensure the output directory exists for your CI/CD to grab the ZIP
        copy {
            from("${layout.buildDirectory.get()}/distributions/${rootProject.name}-${version}.zip")
            into("${rootDir}/output")
        }
    }
}

tasks.publishPlugin {
}

// 7. SEARCHABLE OPTIONS FIX: 
// Disabling this prevents the exit code 255 error on macOS/Headless environments
tasks.buildSearchableOptions {
    enabled = false
}