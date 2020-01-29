package BMAA;

import Benchmark.Result;
import com.google.common.base.Stopwatch;

import java.time.Duration;
import java.util.concurrent.Callable;

public abstract class Algorithm implements Callable<Result> {

    @Override
    public Result call() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        execute();
        stopwatch.stop();

        return generateResults(stopwatch.elapsed());
    }

    public abstract void execute();

    public abstract Result generateResults(Duration runtime);
}
