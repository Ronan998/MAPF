package BMAA;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

public class Time {

    private Stopwatch stopwatch = Stopwatch.createUnstarted();

    private int time = 0;

    public void incrementTimeStep() {
        this.time += 1;
    };

    public int getTimeSteps() {
        return this.time;
    }

    public void startStopWatch() {
        if (stopwatch.isRunning()) {
            throw new RuntimeException("Tried to start stopwatch when it was already running");
        }
        stopwatch.start();
    }

    public void stopStopWatch() {
        if (!stopwatch.isRunning()) {
            throw new RuntimeException("Tried to stop stopwatch when it was already stopped");
        }
        stopwatch.stop();
    }

    public long secondsElapsed() {
        return stopwatch.elapsed(TimeUnit.SECONDS);
    }

    public long milliSecondsElapsed() {
        return stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }
}
