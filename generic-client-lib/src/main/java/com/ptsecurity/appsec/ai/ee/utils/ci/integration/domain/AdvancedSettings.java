package com.ptsecurity.appsec.ai.ee.utils.ci.integration.domain;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.Resources;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.exceptions.GenericException;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.utils.CallHelper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class AdvancedSettings {
    protected static final String SYSTEM_PREFIX = "ptai.";
    private static AdvancedSettings DEFAULT;

    private final Map<SettingInfo, Object> settings = new HashMap<>();

    public enum SettingType {
        STRING, INTEGER
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum SettingInfo {
        /**
         * Maximum response body size to be output to log
         */
        LOGGING_HTTP_RESPONSE_MAX_BODY_SIZE("logging.http.response.max.body.size", SettingType.INTEGER, 102400, Resources::i18n_ast_settings_advanced_logging_http_response_max_body_size),
        LOGGING_HTTP_REQUEST_MAX_BODY_SIZE("logging.http.request.max.body.size", SettingType.INTEGER, 51200, Resources::i18n_ast_settings_advanced_logging_http_request_max_body_size),
        HTTP_REQUEST_READ_TIMEOUT("http.request.read.timeout", SettingType.INTEGER, 3600, Resources::i18n_ast_settings_advanced_http_request_read_timeout),
        HTTP_REQUEST_WRITE_TIMEOUT("http.request.write.timeout", SettingType.INTEGER, 3600, Resources::i18n_ast_settings_advanced_logging_http_response_max_body_size);

        private final String name;
        private final SettingType type;
        private final Object defaultValue;
        private final Supplier<String> descriptionFunction;
    }

    public static AdvancedSettings getDefault() {
        if (null == DEFAULT) DEFAULT = new AdvancedSettings();
        return DEFAULT;
    }

    public AdvancedSettings() {
        for (SettingInfo settingInfo : SettingInfo.values())
            settings.put(settingInfo, settingInfo.getDefaultValue());
        apply();
    }

    public void apply(@NonNull final Properties properties, final boolean systemProperties) {
        for (SettingInfo setting : SettingInfo.values()) {
            String settingName = setting.getName();
            if (systemProperties) settingName = SYSTEM_PREFIX + settingName;
            String stringValue = properties.getProperty(settingName);
            if (null == stringValue) continue;
            if (SettingType.STRING == setting.getType()) {
                log.trace("Set {} = {}", setting.getName(), stringValue);
                settings.put(setting, stringValue);
            } else if (SettingType.INTEGER == setting.getType()) {
                try {
                    int value = Integer.parseInt(stringValue);
                    log.trace("Set {} = {}", setting.getName(), stringValue);
                    settings.put(setting, value);
                } catch (NumberFormatException e) {
                    log.warn("Skip {} = {} as string to number conversion failed", setting.getName(), stringValue);
                }
            } else
                log.trace("Skip {} = {} as parameter of unknown type", setting.getName(), stringValue);
        }
    }

    public void apply() {
        log.trace("Set advanced settings values using system properties");
        apply(System.getProperties(), true);
    }

    @SneakyThrows
    public void apply(final String settings) {
        if (StringUtils.isEmpty(settings)) return;
        Properties properties = new Properties();
        ByteArrayInputStream bis = new ByteArrayInputStream(settings.getBytes(StandardCharsets.UTF_8));
        properties.load(bis);
        apply(properties, false);
    }

    public static void validate(final String settings) throws GenericException {
        ByteArrayInputStream bis = new ByteArrayInputStream(settings.getBytes(StandardCharsets.UTF_8));
        CallHelper.call(() -> new Properties().load(bis), "Properties load failed");
    }

    public int getInt(@NonNull final SettingInfo info) {
        if (SettingType.INTEGER != info.getType())
            throw GenericException.raise("Can't get advanced setting integer value", new ClassCastException());
        return (Integer) settings.get(info);
    }

    public String getString(@NonNull final SettingInfo info) {
        if (SettingType.STRING != info.getType())
            throw GenericException.raise("Can't get advanced setting integer value", new ClassCastException());
        return (String) settings.get(info);
    }

    @Override
    public String toString() {
        List<String> result = new ArrayList<>();
        for (SettingInfo setting : SettingInfo.values()) {
            result.add("# " + setting.descriptionFunction.get());
            result.add(setting.getName() + " = " + settings.get(setting));
        }
        return result.stream().collect(Collectors.joining(System.getProperty("line.separator")));
    }
}
