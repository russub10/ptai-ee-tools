package com.ptsecurity.appsec.ai.ee.scan.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ptsecurity.appsec.ai.ee.scan.reports.Reports;
import com.ptsecurity.appsec.ai.ee.scan.result.issue.types.BaseIssue;
import com.ptsecurity.appsec.ai.ee.scan.settings.Policy;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@SuperBuilder
@NoArgsConstructor
public class ScanResult extends ScanBrief {
    /**
     * As AST result issues list may be big (for example, OWASP Benchmark
     * issues JSON is 75 megabytes and during its parsing JVM consumes
     * additional 2 GB RAM), PT AI server response parse may throw
     * OutOfMemoryException. This field equals true if parse successfully
     * finished
     */
    @Getter
    @Setter
    @Builder.Default
    protected boolean issuesParseOk = false;

    @Getter
    protected final List<BaseIssue> issues = new ArrayList<>();

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Strings {
        protected String title;
        protected String description;
    }

    @NonNull
    @Getter
    @Builder.Default
    @JsonProperty("i18n")
    protected Map<String, Map<Reports.Locale, Strings>> i18n = new HashMap<>();
}