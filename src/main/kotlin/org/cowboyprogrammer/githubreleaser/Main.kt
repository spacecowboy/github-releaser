package org.cowboyprogrammer.githubreleaser

import net.sourceforge.argparse4j.ArgumentParsers.newArgumentParser
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import java.io.File
import kotlin.system.exitProcess




fun main(args: Array<String>) {
    try {
        val ns: Namespace
        try {
            ns = parseArgs(args)
        } catch (t: Throwable) {
            exitProcess(1)
        }

        // Set user input
        Credentials.user = ns.getMandatoryStringOrEnv("user", "GITHUB_USER")
        Credentials.repo = ns.getMandatoryStringOrEnv("repo", "GITHUB_REPO")
        Credentials.token = ns.getMandatoryStringOrEnv("token", "GITHUB_TOKEN")
        val tag = ns.getMandatoryStringOrEnv("tag", "GIT_TAG")

        val files = ns.get<ArrayList<File>>("files")

        files.forEach {
            if (!it.exists()) {
                throw RuntimeException("No such file: ${it.canonicalPath}")
            }
        }

        // Get a release object
        println("Getting or creating release for tag '$tag'...")
        val release = getOrCreateRelease(tag)

        println("Release: $release")

        // Upload file
        files.forEach {
            println("Uploading ${it.canonicalPath}...")
            val result = uploadReleaseFile(release.upload_url_sane, it)

            if (result.isSuccessful) {
                println("Upload successful: ${result.body()}")
            } else {
                println("Upload failed")
                println(result.errorBody().string())
                exitProcess(1)
            }
        }

    } catch (e: Throwable) {
        println("Error: ${e.message}")
        exitProcess(1)
    }
}

fun parseArgs(args: Array<String>): Namespace {
    val parser = newArgumentParser("github-releaser")
            .defaultHelp(true)
            .description("Upload artifacts to GitHub releases.")

    parser.addArgument("-u", "--user").help("GitHub user").default = ""
    parser.addArgument("-r", "--repo").help("GitHub repository name").default = ""
    parser.addArgument("-t", "--tag").help("Git tag for release").default = ""
    parser.addArgument("--token").help("GitHub account token").default = ""
    parser.addArgument("files")
            .metavar("FILE")
            .type(File::class.java)
            .nargs("+")
            .help("Files (one or more) two upload to the release")

    try {
        return parser.parseArgs(args)
    } catch (e: ArgumentParserException) {
        parser.handleError(e)
        throw e
    }
}


