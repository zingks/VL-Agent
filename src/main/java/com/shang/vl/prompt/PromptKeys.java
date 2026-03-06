package com.shang.vl.prompt;

/**
 * Prompt 模板标识常量。
 * Created by shangwei2009@hotmail.com on 2026/3/6 22:11
 */
public final class PromptKeys {

    public static final String IMAGE_TO_DEMAND = "imageToDemand";
    public static final String REQUIREMENT_TO_TEST_CASES = "requirementToTestCases";
    public static final String TEST_CASES_REVIEW = "testCasesReview";

    private PromptKeys() {
        throw new IllegalStateException("Utility class");
    }
}
