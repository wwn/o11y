package ch.nickl.o11y.dresden;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test log handler that keeps every {@link ExtLogRecord} it sees, together with a frozen
 * snapshot of the MDC as it was on the logging thread. That snapshot is what lets a test
 * assert, after the request has completed, which MDC keys a given log record carried.
 */
public final class RecordingHandler extends ExtHandler {

    private final List<ExtLogRecord> records = new CopyOnWriteArrayList<>();

    @Override
    protected void doPublish(ExtLogRecord record) {
        record.copyAll(); // snapshot MDC/NDC while still on the logging thread
        records.add(record);
    }

    /** The most recent record whose formatted message starts with {@code messagePrefix}. */
    public Optional<ExtLogRecord> lastRecordStartingWith(String messagePrefix) {
        ExtLogRecord match = null;
        for (ExtLogRecord record : records) {
            String message = record.getFormattedMessage();
            if (message != null && message.startsWith(messagePrefix)) {
                match = record;
            }
        }
        return Optional.ofNullable(match);
    }

    public List<String> messages() {
        return records.stream().map(ExtLogRecord::getFormattedMessage).toList();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
