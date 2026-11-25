package io.github.fiftieshousewife.javaconventions

import java.io.File

/**
 * Ant-style path patterns, matched against a file's path from the source or class folder it is in: `**` is any number
 * of folders, `*` any characters in one name, `?` one character. A pattern ending in `/` matches everything under it.
 */
object SourcePatterns {

    /** Whether the path, from its folder and with `/` between names, matches any of the patterns. */
    fun excluded(relativePath: String, patterns: Collection<String>): Boolean =
        patterns.any { regex(it).matches(relativePath) }

    /** The files with this extension under the folders, leaving out those the patterns match, in a stable order. */
    fun files(roots: Collection<File>, extension: String, patterns: Collection<String>): List<File> =
        roots.filter(File::isDirectory).flatMap { root ->
            root.walkTopDown().filter { it.isFile && it.extension == extension }
                .filterNot { excluded(it.relativeTo(root).invariantSeparatorsPath, patterns) }.toList()
        }.sorted()

    /** The same patterns for compiled classes: `Foo.java` becomes `Foo.class` and its nested classes, `Foo$*.class`. */
    fun forClasses(patterns: Collection<String>): List<String> = patterns.flatMap { pattern ->
        if (pattern.endsWith(".java")) {
            pattern.removeSuffix(".java").let { listOf("$it.class", "$it\$*.class") }
        } else {
            listOf(pattern)
        }
    }

    private fun regex(pattern: String): Regex {
        val names = (if (pattern.endsWith("/")) "$pattern**" else pattern).trimStart('/').split('/')
        return Regex(
            names.withIndex().joinToString("") { (index, name) ->
                val last = index == names.lastIndex
                when {
                    name == "**" && last -> ".*"
                    name == "**" -> "(?:[^/]*/)*"
                    last -> nameRegex(name)
                    else -> nameRegex(name) + "/"
                }
            },
        )
    }

    private fun nameRegex(name: String): String = name.map {
        when (it) {
            '*' -> "[^/]*"
            '?' -> "[^/]"
            else -> Regex.escape(it.toString())
        }
    }.joinToString("")
}
