package BMAA;

import com.google.common.base.Stopwatch;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class Algorithm implements Callable<Result> {

    @Override
    public Result call() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        execute();
        stopwatch.stop();

        return generateResults();
    }

    abstract void execute();

    abstract Result generateResults();
}
