package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/** How test runs end, the integration tests' own suite, and Mockito's agent. */
class TestRunsTest {

    private val failingTest = """
        package com.example;

        import static org.assertj.core.api.Assertions.assertThat;

        import org.junit.jupiter.api.Test;

        class GreetingRudeTest {
            @Test
            void greetsRudely() {
                assertThat(new Greeting().greet("Ada")).isEqualTo("Go away, Ada");
            }
        }
    """

    private val skippedIntegrationTest = """
        package com.example;

        import static org.junit.jupiter.api.Assumptions.assumeTrue;

        import org.junit.jupiter.api.Test;

        class ContainerTest {
            @Test
            void needsAContainerRuntime() {
                assumeTrue(false, "no container runtime");
            }
        }
    """

    private val agentTest = """
        package com.example;

        import static org.assertj.core.api.Assertions.assertThat;

        import java.lang.management.ManagementFactory;
        import org.junit.jupiter.api.Test;

        class AgentTest {
            @Test
            void startsWithMockitosAgent() {
                assertThat(ManagementFactory.getRuntimeMXBean().getInputArguments())
                    .anyMatch(argument -> argument.startsWith("-javaagent:") && argument.contains("mockito-core-"));
            }
        }
    """

    private fun withSkippedIntegrationTest(dir: File) = CleanSample.into(SampleProject(dir))
        .write("src/integrationTest/java/com/example/ContainerTest.java", skippedIntegrationTest)

    @Test
    fun `passing tests end in one line`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).build("test")
        assertThat(result.output).contains("1 test passed.")
    }

    @Test
    fun `failing tests fail a quiet build in one line, naming the report by a path that shows no home folder`(
        @TempDir dir: File,
    ) {
        val result = CleanSample.into(SampleProject(dir)).test("com/example/GreetingRudeTest.java", failingTest)
            .buildAndFail("test", "--quiet")
        assertThat(result.output).contains("1 of 2 tests failed. Report: build/reports/tests/test/index.html")
            .doesNotContain(dir.canonicalPath, dir.absolutePath)
    }

    @Test
    fun `with listEachTest, a quiet build shows each test's result`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).test("com/example/GreetingRudeTest.java", failingTest)
            .buildAndFail("test", "--quiet", "-PunderTest.listEachTest=true")
        assertThat(result.output)
            .contains("GreetingTest > greetsByName() PASSED", "GreetingRudeTest > greetsRudely() FAILED")
    }

    @Test
    fun `without listEachTest, a quiet build shows no passing test`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).build("test", "--quiet")
        assertThat(result.output).doesNotContain("PASSED")
    }

    @Test
    fun `check runs the integration tests`(@TempDir dir: File) {
        val result = CleanSample.into(SampleProject(dir)).build("check", "--dry-run")
        assertThat(result.output).contains(":integrationTest SKIPPED")
    }

    @Test
    fun `in CI, an integration test that didn't run fails the build`(@TempDir dir: File) {
        val result = withSkippedIntegrationTest(dir).withEnvironment(mapOf("CI" to "true"))
            .buildAndFail("integrationTest")
        assertThat(result.output).contains("1 of 1 tests didn't run: CI must run every integration test")
    }

    @Test
    fun `with integrationTestsMaySkip, an integration test that didn't run doesn't fail CI`(@TempDir dir: File) {
        val result = withSkippedIntegrationTest(dir).withEnvironment(mapOf("CI" to "true"))
            .build("integrationTest", "-PunderTest.integrationTestsMaySkip=true")
        assertThat(result.output).contains("No tests ran; 1 skipped.")
    }

    private fun withMockito(dir: File) = CleanSample.into(SampleProject(dir))
        .buildScript("dependencies { testImplementation(\"org.mockito:mockito-core:5.18.0\") }")
        .test("com/example/AgentTest.java", agentTest)

    @Test
    fun `a test JVM with Mockito on its class path starts with Mockito's agent`(@TempDir dir: File) {
        val result = withMockito(dir).build("test")
        assertThat(result.output).contains("All 2 tests passed.")
    }

    @Test
    fun `with mockitoAgent off, a test JVM starts without Mockito's agent`(@TempDir dir: File) {
        val result = withMockito(dir).buildAndFail("test", "-PunderTest.mockitoAgent=false")
        assertThat(result.output).contains("1 of 2 tests failed.")
    }
}
