package com.common.message.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Resolves the RocketMQ consumer startup switch consistently for all lifecycle components.
 */
final class RocketMQConsumerEnabledResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQConsumerEnabledResolver.class);

    private RocketMQConsumerEnabledResolver() {
    }

    /**
     * Resolves the consumer switch into a state that preserves the difference between an intentional
     * blue-green deferral and an invalid release placeholder.
     *
     * @param environment Spring environment, may be null during very early bootstrap
     * @return resolved consumer switch state
     */
    static ConsumerSwitchState resolve(Environment environment) {
        if (environment == null) {
            return ConsumerSwitchState.ENABLED;
        }

        String configuredValue = environment.getProperty(
                RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY);
        if (configuredValue == null || configuredValue.trim().isEmpty()) {
            return ConsumerSwitchState.ENABLED;
        }
        if ("true".equalsIgnoreCase(configuredValue.trim())) {
            return ConsumerSwitchState.ENABLED;
        }
        if ("false".equalsIgnoreCase(configuredValue.trim())) {
            return ConsumerSwitchState.DEFERRED;
        }

        LOGGER.error("Invalid {} value '{}'; RocketMQ consumers will remain disabled",
                RocketMQConsumerBootstrapPostProcessor.CONSUMER_ENABLED_PROPERTY, configuredValue);
        return ConsumerSwitchState.INVALID;
    }

    /**
     * Resolves the raw consumer switch without using Spring's strict Boolean converter.
     * Missing values preserve the historical enabled behavior; malformed release values fail closed.
     *
     * @param environment Spring environment, may be null during very early bootstrap
     * @return true when listeners may be registered at startup
     */
    static boolean isEnabled(Environment environment) {
        return resolve(environment) == ConsumerSwitchState.ENABLED;
    }

    enum ConsumerSwitchState {
        ENABLED,
        DEFERRED,
        INVALID
    }
}
