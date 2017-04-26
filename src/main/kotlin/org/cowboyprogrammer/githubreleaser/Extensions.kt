package org.cowboyprogrammer.githubreleaser

import net.sourceforge.argparse4j.inf.Namespace
import java.io.File
import java.nio.file.Files.probeContentType

val File.contentType: String
    get() {
        return probeContentType(toPath())
    }

fun Namespace.getMandatoryStringOrEnv(arg: String, envVar: String): String {
    val result = getStringOrEnv(arg, envVar)

    if (result.isNotEmpty()) {
        return result
    }

    throw IllegalArgumentException("Option '$arg' and environment variable '$envVar' were both empty; one must be specified.")
}

fun net.sourceforge.argparse4j.inf.Namespace.getStringOrEnv(arg: String, envVar: String): String {
    return getStringOr(arg, System.getenv(envVar) ?: "")
}

fun net.sourceforge.argparse4j.inf.Namespace.getStringOr(arg: String, value: String): String {
    val argVal = getString(arg)
    return if (argVal.isNotEmpty()) argVal else value
}
