package io.github.fiftieshousewife.javaconventions

/** Main-code samples, each breaking one Checkstyle or PMD rule and compiling with no warning. */
object StyleBreaches {

    private val sixChoices = (1..6).joinToString("\n") {
        "        if (a > $it) {\n            total += $it;\n        }"
    }

    val SAMPLES = mapOf(
        "com/example/Stars.java" to
            "package com.example;\n\nimport java.util.*;\n\npublic class Stars {\n" +
            "    public List<String> none() {\n        return Collections.emptyList();\n    }\n}\n",
        "com/example/Unused.java" to
            "package com.example;\n\nimport java.util.Map;\n\npublic class Unused {\n}\n",
        "com/example/Redundant.java" to
            "package com.example;\n\nimport java.lang.String;\n\npublic class Redundant {\n" +
            "    public String name() {\n        return \"x\";\n    }\n}\n",
        "com/example/Braces.java" to
            "package com.example;\n\npublic class Braces {\n    public int sign(final int a) {\n" +
            "        if (a < 0) return -1;\n        return 1;\n    }\n}\n",
        "com/example/Empty.java" to
            "package com.example;\n\npublic class Empty {\n    public void nothing(final boolean a) {\n" +
            "        if (a) {\n        }\n    }\n}\n",
        "com/example/Curly.java" to
            "package com.example;\n\npublic class Curly\n{\n    public int pick(final boolean a) {\n" +
            "        final int b;\n        if (a) {\n            b = 1;\n        }\n" +
            "        else {\n            b = 2;\n" +
            "        }\n        return b;\n    }\n}\n",
        "com/example/Simplify.java" to
            "package com.example;\n\npublic class Simplify {\n    public boolean same(final boolean a) {\n" +
            "        return a == true;\n    }\n\n    public boolean either(final boolean a) {\n" +
            "        if (a) {\n            return true;\n        } else {\n" +
            "            return false;\n        }\n    }\n}\n",
        "com/example/NotFinal.java" to
            "package com.example;\n\npublic class NotFinal {\n    public int one() {\n        int x = 1;\n" +
            "        return x;\n    }\n}\n",
        "com/example/Paths.java" to
            "package com.example;\n\npublic class Paths {\n    public int total(final int a) {\n" +
            "        int total = 0;\n$sixChoices\n        return total;\n    }\n}\n",
        "com/example/Wide.java" to
            "package com.example;\n\npublic class Wide {\n    public String text() {\n        return \"" +
            "x".repeat(110) + "\";\n    }\n}\n",
    )

    /** What each sample makes Checkstyle or PMD report. */
    val REPORTS = listOf(
        "[AvoidStarImport]", "[UnusedImports]", "[RedundantImport]", "[NeedBraces]", "[EmptyBlock]", "[LeftCurly]",
        "[RightCurly]", "[SimplifyBooleanExpression]", "[SimplifyBooleanReturn]", "[FinalLocalVariable]",
        "[NPathComplexity]", "[LineLength]", "AvoidFieldNameMatchingMethodName",
    )
}
