package io.github.fiftieshousewife.javaconventions

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Settings an organisation can change, in three layers:
 *  - environment (proxy, package repository): never set here; it comes from the organisation's Gradle init script;
 *  - code-quality standards: the toolkit's rules are defaults, replaceable or switchable off ([codeStyle]);
 *  - testing evidence (coverage, mutation sampling, ...): always on; only thresholds can change ([minLineCoverage]).
 *
 * Every value can also be set as a Gradle property (for example `underTest.codeStyle=NONE` in an init script or
 * gradle.properties), so an organisation can change all its builds without editing each repo.
 */
abstract class UnderTestExtension {

    /** Whose code-quality rules the style, complexity, copy-paste, bug and forbidden-API checks use. */
    abstract val codeStyle: Property<CodeStyleSource>

    /** With [CodeStyleSource.ORGANISATION]: the organisation's Checkstyle configuration file. */
    abstract val checkstyleConfig: RegularFileProperty

    /** With [CodeStyleSource.ORGANISATION]: the organisation's PMD rule set file. */
    abstract val pmdRuleset: RegularFileProperty

    /** With [CodeStyleSource.ORGANISATION]: extra forbidden-API signatures, in addition to the bundled JDK sets. */
    abstract val forbiddenApiSignatures: RegularFileProperty

    /**
     * Paths the code-quality checks leave out: code a repo keeps below the standards on purpose. Ant-style patterns
     * from the source folder ([SourcePatterns]), such as every `legacy` package or every class whose name ends in
     * `WeakTest`. Coverage and the compiler still see that code.
     */
    abstract val codeStyleExcludes: ListProperty<String>

    /** With `false`, neither the SLF4J API nor Log4j is added: for code that logs nothing, and publishes no logging. */
    abstract val logging: Property<Boolean>

    /** A SpotBugs filter file of findings to leave out, kept in the repo; with any `codeStyle` but NONE. */
    abstract val spotbugsExcludeFilter: RegularFileProperty

    /** The fewest copied tokens of Java that fail the copy-paste check (50 unless changed). */
    abstract val cpdMinimumTokens: Property<Int>

    /** Testing evidence: with it, pitest fails unless the tests catch at least this percentage of planted bugs. */
    abstract val minMutationScore: Property<Int>

    /** Testing evidence: with `true`, `check` runs pitest too, so a minimum mutation score is checked every build. */
    abstract val mutationTestingInCheck: Property<Boolean>

    /**
     * Testing evidence: with `true`, an integration test that didn't run doesn't fail a CI build. For CI machines that
     * can't give the tests what they need, such as a runner whose containers aren't the kind the tests start.
     */
    abstract val integrationTestsMaySkip: Property<Boolean>

    /** With `true`, each test's result shows even in a quiet build (`--quiet`), failures without stack traces. */
    abstract val listEachTest: Property<Boolean>

    /** With `false`, a test JVM doesn't start with Mockito's agent, and Mockito attaches it itself as it runs. */
    abstract val mockitoAgent: Property<Boolean>

    /** The most classes a package may hold, in any source set (20 unless changed). */
    abstract val maxClassesPerPackage: Property<Int>

    /** Testing evidence: smallest line coverage a project may have, as a fraction (0.80 means 80%). */
    abstract val minLineCoverage: Property<java.math.BigDecimal>
}

enum class CodeStyleSource {
    /** The rules inside this plugin. */
    TOOLKIT,

    /** The toolkit's tools, with the organisation's rule files. */
    ORGANISATION,

    /** No code-quality checks from the toolkit; the organisation's own plugin provides them. */
    NONE,
}
