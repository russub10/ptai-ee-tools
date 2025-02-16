package com.ptsecurity.appsec.ai.ee.utils.ci.integration.api.v41.tasks;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.api.AbstractApiClient;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.exceptions.GenericException;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.tasks.ServerVersionTasks;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.ptsecurity.appsec.ai.ee.utils.ci.integration.utils.CallHelper.call;

@Slf4j
public class ServerVersionTasksImpl extends AbstractTaskImpl implements ServerVersionTasks {
    public ServerVersionTasksImpl(@NonNull final AbstractApiClient client) {
        super(client);
    }

    @Override
    public Map<Component, String> current() throws GenericException {
        Map<Component, String> res = new HashMap<>();
        for (Component component : Component.values()) {
            log.debug("Getting current {} component version", component.getValue());
            String version = call(
                    () -> client.getVersionApi().apiVersionsProductCurrentGet(component.getValue()),
                    "PT AI server component API current version get failed");
            log.debug("Current version: {}", version);
            res.put(component, version);
        }
        return res;
    }

    @Override
    public Map<Component, String> latest() throws GenericException {
        Map<Component, String> res = new HashMap<>();
        for (Component component : Component.values()) {
            log.debug("Getting latest {} component version", component.getValue());
            String version = call(
                    () -> client.getVersionApi().apiVersionsProductLatestGet(component.getValue()),
                    "PT AI server component API current version get failed");
            log.debug("Latest version: {}", version);
            res.put(component, version);
        }
        return res;
    }
}
