package com.ptsecurity.appsec.ai.ee.utils.ci.integration.jobs;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.AbstractTool;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.api.AbstractApiClient;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.api.Factory;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.domain.ConnectionSettings;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.domain.TokenCredentials;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.exceptions.AstPolicyViolationException;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.exceptions.GenericException;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.exceptions.MinorAstErrorsException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Getter
@Slf4j
@RequiredArgsConstructor
@SuperBuilder
public abstract class AbstractJob extends AbstractTool {
    public static final String DEFAULT_OUTPUT_FOLDER = ".ptai";
    public static final String DEFAULT_PTAI_URL = "https://ptai.domain.org:443";
    public static final boolean DEFAULT_INSECURE = true;
    public static final String DEFAULT_TOKEN = "";
    
    @NonNull
    @Builder.Default
    protected ConnectionSettings connectionSettings = ConnectionSettings.builder()
            .url(DEFAULT_PTAI_URL)
            .credentials(TokenCredentials.builder().token(DEFAULT_TOKEN).build())
            .insecure(DEFAULT_INSECURE)
            .build();

    public enum JobExecutionResult {
        FAILED, INTERRUPTED, SUCCESS
    }

    @Getter
    @Builder.Default
    protected AbstractApiClient client = null;

    public JobExecutionResult execute() {
        try {
            init();
            validate();
            client = Factory.client(this);

            unsafeExecute();
            return JobExecutionResult.SUCCESS;
        } catch (GenericException e) {
            return processException(e);
        }
    }

    public JobExecutionResult processException(@NonNull final GenericException e) {
        if (null != e.getCause()) {
            if (e.getCause() instanceof InterruptedException) {
                log.debug("Job execution interrupted");
                return JobExecutionResult.INTERRUPTED;
            } else if (e.getCause() instanceof AstPolicyViolationException || e.getCause() instanceof MinorAstErrorsException) {
                log.debug(e.getDetailedMessage(), e.getCause());
                return JobExecutionResult.FAILED;
            }
        }
        severe(e.getDetailedMessage());
        log.error(e.getDetailedMessage(), e.getCause());
        return JobExecutionResult.FAILED;
    }

    protected abstract void init() throws GenericException;

    protected void validate() throws GenericException {
        connectionSettings.validate();
    }

    protected abstract void unsafeExecute() throws GenericException;
    /**
     * Method replaces macro expressions like ${FOO} in the input text using dictionary. AstJob's
     * method doesn't do any replacements as those are to be implemented in its descendants.
     * For example, Jenkins plugin may override this implementation
     * with hudson.Util.replaceMacro call
     * @param value String with macro expressions to be replaced
     * @param replacements Dictionary with name / value pairs
     * @return String with macro substitutions complete
     */
    public String replaceMacro(final String value, final Map<String, String> replacements) {
        return value;
    };
}
