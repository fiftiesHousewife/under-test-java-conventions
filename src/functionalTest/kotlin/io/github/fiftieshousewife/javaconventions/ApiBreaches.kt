package io.github.fiftieshousewife.javaconventions

/**
 * Main-code samples, each calling an API the forbidden-APIs check refuses, and compiling with no warning. Deprecated
 * and internal JDK APIs have none: compiling with every warning an error refuses them first.
 */
object ApiBreaches {

    val SAMPLES = mapOf(
        "com/example/Logging.java" to """
            package com.example;

            public class Logging {
                public java.util.logging.Logger logger() {
                    return java.util.logging.Logger.getLogger("x");
                }
            }
        """,
        "com/example/Today.java" to """
            package com.example;

            public class Today {
                public String now() {
                    return "" + java.time.LocalDate.now() + java.time.LocalDateTime.now() + java.time.Instant.now()
                        + java.time.ZonedDateTime.now();
                }
            }
        """,
        "com/example/Printer.java" to """
            package com.example;

            public class Printer {
                public void print(final String text) {
                    System.out.println(text);
                }
            }
        """,
        "com/example/Lower.java" to """
            package com.example;

            public class Lower {
                public String lower(final String text) {
                    return text.toLowerCase();
                }
            }
        """,
        "com/example/Opener.java" to """
            package com.example;

            import java.lang.reflect.Field;

            public class Opener {
                public void open(final Field field) {
                    field.setAccessible(true);
                }
            }
        """,
    )

    /** What each sample makes the forbidden-APIs check report. */
    val REPORTS = listOf(
        "java.util.logging.Logger", "java.time.LocalDate#now()", "java.time.LocalDateTime#now()",
        "java.time.Instant#now()", "java.time.ZonedDateTime#now()", "java.lang.System#out",
        "java.lang.String#toLowerCase()", "java.lang.reflect.AccessibleObject#setAccessible(boolean)",
    )
}
