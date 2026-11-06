
import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Background updater using Swing Timer for polling database statistics
 * every 1-3 seconds without freezing the GUI or leaking threads.
 */
public class LiveDashboardUpdater {

    private final Timer timer;
    private final Runnable pollTask;
    private final java.util.concurrent.atomic.AtomicBoolean isPolling = new java.util.concurrent.atomic.AtomicBoolean(false);
    private boolean isRunning = false;

    /**
     * Initializes the LiveDashboardUpdater.
     * @param intervalMs Milliseconds between polls (e.g. 1500ms).
     * @param pollTask The task to run on the background or dispatch to EDT.
     */
    public LiveDashboardUpdater(int intervalMs, Runnable pollTask) {
        this.pollTask = pollTask;
        this.timer = new Timer(intervalMs, e -> {
            // Guard against worker pileup if database query takes longer than interval
            if (isPolling.compareAndSet(false, true)) {
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try {
                            pollTask.run();
                        } catch (Exception ex) {
                            System.err.println("[LiveDashboardUpdater] Error during poll: " + ex.getMessage());
                        } finally {
                            isPolling.set(false);
                        }
                        return null;
                    }
                };
                worker.execute();
            }
        });
        this.timer.setRepeats(true);
    }

    /**
     * Starts live polling.
     */
    public synchronized void start() {
        if (!isRunning) {
            isRunning = true;
            timer.start();
            // Trigger immediately once
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        pollTask.run();
                    } catch (Exception ex) {
                        System.err.println("[LiveDashboardUpdater] Initial poll error: " + ex.getMessage());
                    }
                    return null;
                }
            };
            worker.execute();
        }
    }

    /**
     * Stops live polling to release resources when screen closes or changes.
     */
    public synchronized void stop() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
