package com.common.message.config;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.autoconfigure.ListenerContainerConfiguration;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

public class RocketMQConsumerBootstrapIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class, RocketMQAutoConfiguration.class)
            .withPropertyValues("erp.mq.consumer.enabled=false");

    @Test
    public void disabledConsumerKeepsProducerTemplateAndBusinessListener() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(DefaultMQProducer.class);
            assertThat(context).hasSingleBean(RocketMQTemplate.class);
            assertThat(context).hasSingleBean(TestListener.class);
            assertThat(context).doesNotHaveBean(ListenerContainerConfiguration.class);
            assertThat(context).doesNotHaveBean(DefaultRocketMQListenerContainer.class);
            assertThat(context.getBean(RocketMQConsumerActivationManager.class).getActivationState())
                    .isEqualTo("DEFERRED");
        });
    }

    @Test
    public void malformedConsumerSwitchDoesNotFailApplicationStartup() {
        contextRunner.withPropertyValues("erp.mq.consumer.enabled=mqEnabled").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(ListenerContainerConfiguration.class);
            assertThat(context).doesNotHaveBean(DefaultRocketMQListenerContainer.class);
            assertThat(context.getBean(RocketMQConsumerActivationManager.class).getActivationState())
                    .isEqualTo("DEFERRED");
        });
    }

    @Configuration
    @ComponentScan(
            basePackageClasses = RocketMQConsumerBootstrapPostProcessor.class,
            useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = RocketMQConsumerBootstrapPostProcessor.class))
    static class TestConfiguration {

        @Bean(name = RocketMQAutoConfiguration.PRODUCER_BEAN_NAME)
        public DefaultMQProducer defaultMQProducer() {
            return new NoOpDefaultMQProducer();
        }

        @Bean
        public TestListener testListener() {
            return new TestListener();
        }

        @Bean
        public RocketMQConsumerActivationManager activationManager(ApplicationContext applicationContext,
                                                                   Environment environment) {
            return new RocketMQConsumerActivationManager(applicationContext, environment, () -> {
                // Registration is intentionally deferred in this bootstrap-only test.
            });
        }
    }

    @RocketMQMessageListener(consumerGroup = "consumer-bootstrap-test", topic = "consumer-bootstrap-test")
    static class TestListener implements RocketMQListener<String> {

        @Override
        public void onMessage(String message) {
        }
    }

    static class NoOpDefaultMQProducer extends DefaultMQProducer {

        @Override
        public void start() throws MQClientException {
        }

        @Override
        public void shutdown() {
        }
    }
}
