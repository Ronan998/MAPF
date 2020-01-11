package Benchmark;

import BMAA.BMAA;
import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import java.time.Duration;
import java.util.concurrent.*;

public class Benchmark {

    private Map map;
    private Scenario scenario;

    private int timeLimit;

    public Benchmark(String mapFile, String scenarioFile, int timeLimit) {
        this.map = new Map(mapFile);
        this.scenario = new Scenario(scenarioFile, map.getGraph());

        this.timeLimit = timeLimit;
    }

    // -----------------------------------------------------------------------------

    public void setAgentLimit(int limit) {
        this.scenario.setAgentLimit(limit);
    }

    // -----------------------------------------------------------------------------

    public void runBmaa(int expansions, int vision, int moves, boolean push, boolean flow) {

        BMAA algorithm = new BMAA(map.getGraph(),
                scenario.getS(),
                scenario.getT(),
                expansions,
                vision,
                moves,
                push,
                flow);

        algorithm.execute();
    }

    public static void main(String[] args) {
        Benchmark simpleGrid = new Benchmark("Berlin_1_256.map", "Berlin_1_256-even-2.scen", 30);
        simpleGrid.setAgentLimit(300);


        TimeLimiter timeLimiter = SimpleTimeLimiter.create(Executors.newCachedThreadPool());

        Long runningTime = null;
        try {
            timeLimiter.runWithTimeout(() -> {
                try {
                    Stopwatch stopwatch = Stopwatch.createStarted();
                    simpleGrid.runBmaa(32, 32, 32, true, false);
                    stopwatch.stop();
                }
                catch (Exception e) {
                    System.exit(0);
                }
            }, Duration.ofSeconds(3));
//            runningTime = timeLimiter.callWithTimeout(() -> {
//                Stopwatch stopwatch = Stopwatch.createStarted();
//                simpleGrid.runBmaa(32, 32, 32, true, false);
//                stopwatch.stop();
//                return stopwatch.elapsed(TimeUnit.SECONDS);
//            }, Duration.ofSeconds(3));
        }
        catch (InterruptedException e) {
            System.out.println("Thread was interrupted, never should happen");
        }
        catch (TimeoutException e) {
            System.out.println("Execution timed out");
        }

        System.out.println("Running time of program was " + runningTime);
    }
}
