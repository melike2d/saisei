import org.gradle.api.Project
import java.io.ByteArrayOutputStream

// Courtesy of Kord: https://github.com/kordlib/kord/tree/main/buildSrc/src/main/kotlin

internal fun Project.git(vararg command: String): String {
    val output = ByteArrayOutputStream()
    exec {
        commandLine("git", *command)
        standardOutput = output
        errorOutput = output
        workingDir = rootDir
    }.rethrowFailure().assertNormalExitValue()
    return output.toString().trim()
}

private val Project.tag
    get() = git("tag", "--no-column", "--points-at", "HEAD")
        .takeIf { it.isNotBlank() }
        ?.lines()
        ?.single()

val Project.libraryVersion
    get() = tag ?: run {
        val snapshotPrefix = when (val branch = git("branch", "--show-current")) {
            "main" -> providers.gradleProperty("nextPlannedVersion").get()
            else -> branch.replace('/', '-')
        }
        "$snapshotPrefix-SNAPSHOT"
    }

val Project.commitHash get() = git("rev-parse", "--verify", "HEAD")
val Project.shortCommitHash get() = git("rev-parse", "--short", "HEAD")

val Project.isRelease get() = tag != null
