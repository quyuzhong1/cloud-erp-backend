package com.common.message.config;

import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Serializes deferred RocketMQ activation with the irreversible terminal drain.
 */
@Component
public class RocketMQConsumerLifecycleCoordinator {

    private boolean terminalDrainStarted;

    /**
     * Runs listener registration while holding the lifecycle lock.
     * Once terminal drain begins, activation is permanently rejected for this JVM.
     *
     * @param activation listener registration action
     */
    public synchronized void runActivation(Runnable activation) {
        if (terminalDrainStarted) {
            throw new TerminalDrainStartedException(
                    "RocketMQ consumers cannot be activated after terminal drain starts");
        }
        activation.run();
    }

    /**
     * Marks terminal drain before taking the listener snapshot, preventing a concurrent activation
     * from registering containers outside that snapshot.
     *
     * @param snapshotSupplier complete listener snapshot supplier
     * @param <T> snapshot type
     * @return drain permit containing the snapshot, or null when drain was already started
     */
    public synchronized <T> TerminalDrainPermit<T> beginTerminalDrain(Supplier<T> snapshotSupplier) {
        if (terminalDrainStarted) {
            return null;
        }
        terminalDrainStarted = true;
        return new TerminalDrainPermit<>(snapshotSupplier.get());
    }

    /**
     * Reports whether this JVM has entered the irreversible terminal drain phase.
     *
     * @return true after terminal drain starts
     */
    public synchronized boolean isTerminalDrainStarted() {
        return terminalDrainStarted;
    }

    /**
     * Snapshot captured atomically when terminal drain starts.
     *
     * @param <T> snapshot type
     */
    public static final class TerminalDrainPermit<T> {
        private final T snapshot;

        private TerminalDrainPermit(T snapshot) {
            this.snapshot = snapshot;
        }

        /**
         * Returns the snapshot captured under the lifecycle lock.
         *
         * @return terminal drain snapshot
         */
        public T getSnapshot() {
            return snapshot;
        }
    }

    /**
     * Raised when activation is attempted after terminal drain has started.
     */
    public static class TerminalDrainStartedException extends IllegalStateException {
        /**
         * Creates a terminal drain conflict.
         *
         * @param message conflict detail
         */
        public TerminalDrainStartedException(String message) {
            super(message);
        }
    }
}
