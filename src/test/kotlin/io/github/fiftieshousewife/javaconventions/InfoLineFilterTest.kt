package io.github.fiftieshousewife.javaconventions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class InfoLineFilterTest {

    @Test
    fun `PIT's INFO lines are dropped and every other line passes through`() {
        val target = ByteArrayOutputStream()
        InfoLineFilter(PrintStream(target, true, Charsets.UTF_8)).use {
            it.write("first\n12:00:00 PIT >> INFO : noise\n12:00:01 PIT >> WARNING : kept\nlast".toByteArray())
        }
        assertThat(target.toString(Charsets.UTF_8)).isEqualTo("first\n12:00:01 PIT >> WARNING : kept\nlast")
    }
}
